package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Bill
import com.example.data.model.BillFrequency
import com.example.data.model.BillStatus
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.WealthItem
import com.example.data.model.WealthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(private val database: AppDatabase) {
    private val accountDao = database.accountDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val billDao = database.billDao()
    private val budgetDao = database.budgetDao()
    private val wealthDao = database.wealthDao()

    // Accounts
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun insertAccount(account: Account): Long = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccountSafely(accountId: Long) = withContext(Dispatchers.IO) {
        // Soft delete: keep transaction records intact
        accountDao.archiveAccount(accountId)
    }

    // Categories
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> = categoryDao.getCategoriesByType(type)

    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        // Reassign affected transactions to "Uncategorized"
        transactionDao.unassignCategory(categoryId)
        categoryDao.deleteCategory(categoryId)
    }

    // Transactions
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> = transactionDao.getRecentTransactions(limit)
    fun getTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsInRange(startMillis, endMillis)

    suspend fun insertTransaction(transaction: Transaction): Long = withContext(Dispatchers.IO) {
        val id = transactionDao.insertTransaction(transaction)
        // Adjust account balances
        applyTransactionBalanceImpact(transaction, reverse = false)
        id
    }

    suspend fun updateTransaction(oldTransaction: Transaction, updatedTransaction: Transaction) = withContext(Dispatchers.IO) {
        // Revert old transaction's impact
        applyTransactionBalanceImpact(oldTransaction, reverse = true)
        // Apply updated transaction's impact
        applyTransactionBalanceImpact(updatedTransaction, reverse = false)
        transactionDao.updateTransaction(updatedTransaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        applyTransactionBalanceImpact(transaction, reverse = true)
        transactionDao.deleteTransaction(transaction.id)
    }

    suspend fun toggleRefund(transaction: Transaction) = withContext(Dispatchers.IO) {
        val newRefundState = !transaction.isRefunded
        // If it was not refunded and is now refunded, reverse balance deduction
        if (transaction.type == TransactionType.EXPENSE) {
            val balanceDelta = if (newRefundState) transaction.amount else -transaction.amount
            accountDao.adjustBalance(transaction.accountId, balanceDelta)
        }
        transactionDao.setRefundStatus(transaction.id, newRefundState)
    }

    private suspend fun applyTransactionBalanceImpact(transaction: Transaction, reverse: Boolean) {
        val multiplier = if (reverse) -1.0 else 1.0
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                // If it's already refunded, no active deduction on account balance
                if (!transaction.isRefunded) {
                    accountDao.adjustBalance(transaction.accountId, -transaction.amount * multiplier)
                }
            }
            TransactionType.INCOME -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount * multiplier)
            }
            TransactionType.TRANSFER -> {
                // Deduct from source account
                accountDao.adjustBalance(transaction.accountId, -transaction.amount * multiplier)
                // Add to destination account
                transaction.toAccountId?.let { toId ->
                    accountDao.adjustBalance(toId, transaction.amount * multiplier)
                }
            }
        }
    }

    // Bills
    val allBills: Flow<List<Bill>> = billDao.getAllBills()
    fun getUpcomingBills(limit: Int = 5): Flow<List<Bill>> = billDao.getUpcomingBills(limit)

    suspend fun insertBill(bill: Bill): Long = withContext(Dispatchers.IO) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) = withContext(Dispatchers.IO) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(billId: Long) = withContext(Dispatchers.IO) {
        billDao.deleteBill(billId)
    }

    suspend fun markBillPaid(bill: Bill, createTransaction: Boolean = true) = withContext(Dispatchers.IO) {
        billDao.updateStatus(bill.id, BillStatus.PAID)
        if (createTransaction && bill.accountId != null) {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val tx = Transaction(
                amount = bill.amount,
                type = TransactionType.EXPENSE,
                accountId = bill.accountId,
                accountName = bill.accountName,
                categoryName = bill.categoryName,
                categoryIcon = "receipt_long",
                categoryColor = "#F43F5E",
                dateMillis = System.currentTimeMillis(),
                timeString = timeFmt,
                note = "Bill Payment: ${bill.name}"
            )
            insertTransaction(tx)
        }
    }

    // Budgets
    fun getBudgetsForMonth(monthKey: String): Flow<List<Budget>> = budgetDao.getBudgetsForMonth(monthKey)
    fun getOverallBudgetForMonth(monthKey: String): Flow<Budget?> = budgetDao.getOverallBudgetForMonth(monthKey)

    suspend fun insertOrUpdateBudget(budget: Budget): Long = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budgetId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budgetId)
    }

    // Wealth Items
    val allWealthItems: Flow<List<WealthItem>> = wealthDao.getAllWealthItems()
    fun getWealthItemsByType(type: WealthType): Flow<List<WealthItem>> = wealthDao.getWealthItemsByType(type)

    suspend fun insertWealthItem(item: WealthItem): Long = withContext(Dispatchers.IO) {
        wealthDao.insertWealthItem(item)
    }

    suspend fun updateWealthItem(item: WealthItem) = withContext(Dispatchers.IO) {
        wealthDao.updateWealthItem(item)
    }

    suspend fun deleteWealthItem(itemId: Long) = withContext(Dispatchers.IO) {
        wealthDao.deleteWealthItem(itemId)
    }

    // Seed defaults if database is freshly initialized
    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        if (categoryDao.getCategoryCount() == 0) {
            val expenseCategories = listOf(
                Category(name = "Food & Dining", type = CategoryType.EXPENSE, iconName = "restaurant", colorHex = "#F59E0B", orderIndex = 1, isDefault = true),
                Category(name = "Groceries", type = CategoryType.EXPENSE, iconName = "shopping_cart", colorHex = "#10B981", orderIndex = 2, isDefault = true),
                Category(name = "Transport", type = CategoryType.EXPENSE, iconName = "directions_bus", colorHex = "#3B82F6", orderIndex = 3, isDefault = true),
                Category(name = "Fuel", type = CategoryType.EXPENSE, iconName = "local_gas_station", colorHex = "#F97316", orderIndex = 4, isDefault = true),
                Category(name = "Shopping", type = CategoryType.EXPENSE, iconName = "shopping_bag", colorHex = "#EC4899", orderIndex = 5, isDefault = true),
                Category(name = "Entertainment", type = CategoryType.EXPENSE, iconName = "movie", colorHex = "#8B5CF6", orderIndex = 6, isDefault = true),
                Category(name = "Healthcare", type = CategoryType.EXPENSE, iconName = "favorite", colorHex = "#EF4444", orderIndex = 7, isDefault = true),
                Category(name = "Education", type = CategoryType.EXPENSE, iconName = "school", colorHex = "#6366F1", orderIndex = 8, isDefault = true),
                Category(name = "Travel", type = CategoryType.EXPENSE, iconName = "flight", colorHex = "#06B6D4", orderIndex = 9, isDefault = true),
                Category(name = "Bills & Utilities", type = CategoryType.EXPENSE, iconName = "receipt_long", colorHex = "#F43F5E", orderIndex = 10, isDefault = true),
                Category(name = "Subscriptions", type = CategoryType.EXPENSE, iconName = "subscriptions", colorHex = "#A855F7", orderIndex = 11, isDefault = true),
                Category(name = "Home", type = CategoryType.EXPENSE, iconName = "home", colorHex = "#14B8A6", orderIndex = 12, isDefault = true),
                Category(name = "Other Expense", type = CategoryType.EXPENSE, iconName = "more_horiz", colorHex = "#64748B", orderIndex = 13, isDefault = true)
            )

            val incomeCategories = listOf(
                Category(name = "Salary", type = CategoryType.INCOME, iconName = "payments", colorHex = "#10B981", orderIndex = 1, isDefault = true),
                Category(name = "Freelance", type = CategoryType.INCOME, iconName = "laptop_mac", colorHex = "#06B6D4", orderIndex = 2, isDefault = true),
                Category(name = "Business", type = CategoryType.INCOME, iconName = "storefront", colorHex = "#3B82F6", orderIndex = 3, isDefault = true),
                Category(name = "Interest", type = CategoryType.INCOME, iconName = "account_balance", colorHex = "#8B5CF6", orderIndex = 4, isDefault = true),
                Category(name = "Investment", type = CategoryType.INCOME, iconName = "trending_up", colorHex = "#10B981", orderIndex = 5, isDefault = true),
                Category(name = "Gift", type = CategoryType.INCOME, iconName = "redeem", colorHex = "#EC4899", orderIndex = 6, isDefault = true),
                Category(name = "Refund", type = CategoryType.INCOME, iconName = "replay", colorHex = "#F59E0B", orderIndex = 7, isDefault = true),
                Category(name = "Other Income", type = CategoryType.INCOME, iconName = "attach_money", colorHex = "#64748B", orderIndex = 8, isDefault = true)
            )

            categoryDao.insertAll(expenseCategories + incomeCategories)
        }

        if (accountDao.getAccountCount() == 0) {
            val mainBankId = accountDao.insertAccount(
                Account(
                    name = "Primary Checking",
                    providerName = "Main Bank",
                    last4Digits = "4012",
                    type = AccountType.BANK,
                    balance = 3450.00,
                    colorHex = "#3B82F6",
                    iconName = "account_balance"
                )
            )
            val cashId = accountDao.insertAccount(
                Account(
                    name = "Wallet Cash",
                    providerName = "Cash",
                    last4Digits = "",
                    type = AccountType.CASH,
                    balance = 280.00,
                    colorHex = "#10B981",
                    iconName = "payments"
                )
            )
            val creditCardId = accountDao.insertAccount(
                Account(
                    name = "Sapphire Credit",
                    providerName = "Credit Network",
                    last4Digits = "8821",
                    type = AccountType.CREDIT_CARD,
                    creditLimit = 5000.00,
                    balance = 420.00, // outstanding balance
                    billingDate = 5,
                    paymentDueDate = 25,
                    colorHex = "#8B5CF6",
                    iconName = "credit_card"
                )
            )

            // Seed initial sample transactions for immediate friendly visualization
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val sampleTransactions = listOf(
                Transaction(
                    amount = 3200.00,
                    type = TransactionType.INCOME,
                    accountId = mainBankId,
                    accountName = "Primary Checking",
                    categoryName = "Salary",
                    categoryIcon = "payments",
                    categoryColor = "#10B981",
                    dateMillis = now - dayMillis * 12,
                    timeString = "09:00",
                    note = "Monthly salary deposit"
                ),
                Transaction(
                    amount = 84.50,
                    type = TransactionType.EXPENSE,
                    accountId = mainBankId,
                    accountName = "Primary Checking",
                    categoryName = "Groceries",
                    categoryIcon = "shopping_cart",
                    categoryColor = "#10B981",
                    dateMillis = now - dayMillis * 2,
                    timeString = "14:20",
                    locationName = "Central Market",
                    note = "Weekly fresh produce and pantry"
                ),
                Transaction(
                    amount = 26.00,
                    type = TransactionType.EXPENSE,
                    accountId = cashId,
                    accountName = "Wallet Cash",
                    categoryName = "Food & Dining",
                    categoryIcon = "restaurant",
                    categoryColor = "#F59E0B",
                    dateMillis = now - dayMillis * 1,
                    timeString = "12:45",
                    locationName = "Green Leaf Cafe",
                    note = "Lunch with colleague"
                ),
                Transaction(
                    amount = 14.99,
                    type = TransactionType.EXPENSE,
                    accountId = creditCardId,
                    accountName = "Sapphire Credit",
                    categoryName = "Subscriptions",
                    categoryIcon = "subscriptions",
                    categoryColor = "#A855F7",
                    dateMillis = now - dayMillis * 4,
                    timeString = "08:15",
                    note = "Music streaming subscription"
                ),
                Transaction(
                    amount = 200.00,
                    type = TransactionType.TRANSFER,
                    accountId = mainBankId,
                    accountName = "Primary Checking",
                    toAccountId = cashId,
                    toAccountName = "Wallet Cash",
                    categoryName = "Transfer",
                    categoryIcon = "swap_horiz",
                    categoryColor = "#06B6D4",
                    dateMillis = now - dayMillis * 5,
                    timeString = "11:00",
                    note = "ATM cash withdrawal"
                )
            )
            transactionDao.insertAll(sampleTransactions)

            // Seed sample bills
            billDao.insertBill(
                Bill(
                    name = "High-speed Internet",
                    amount = 59.99,
                    dueDateMillis = now + dayMillis * 5,
                    accountId = mainBankId,
                    accountName = "Primary Checking",
                    frequency = BillFrequency.MONTHLY,
                    categoryName = "Bills & Utilities",
                    note = "Fiber broadband"
                )
            )
            billDao.insertBill(
                Bill(
                    name = "Apartment Rent",
                    amount = 1150.00,
                    dueDateMillis = now + dayMillis * 8,
                    accountId = mainBankId,
                    accountName = "Primary Checking",
                    frequency = BillFrequency.MONTHLY,
                    categoryName = "Bills & Utilities",
                    note = "Monthly lease"
                )
            )

            // Seed overall monthly budget
            val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(now))
            budgetDao.insertBudget(
                Budget(
                    monthKey = monthKey,
                    categoryId = null,
                    categoryName = "Overall Monthly Budget",
                    limitAmount = 2400.00,
                    colorHex = "#10B981"
                )
            )

            // Seed a sample savings goal & investment
            wealthDao.insertWealthItem(
                WealthItem(
                    type = WealthType.SAVINGS,
                    name = "Emergency Reserve Fund",
                    targetAmount = 10000.00,
                    currentAmount = 4500.00,
                    contributionOrPaymentAmount = 300.00,
                    frequency = "Monthly",
                    notes = "Target 6 months of living expenses"
                )
            )
            wealthDao.insertWealthItem(
                WealthItem(
                    type = WealthType.INVESTMENT,
                    name = "Global Index ETF",
                    subType = "Index Funds",
                    principalOrInitialAmount = 5000.00,
                    currentAmount = 5620.00,
                    contributionOrPaymentAmount = 250.00,
                    frequency = "Monthly",
                    notes = "Long-term low-cost broad index portfolio"
                )
            )
        }
    }

    // CSV Export
    suspend fun exportTransactionsToCsv(): String = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllTransactionsSnapshot()
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Amount,Category,Account,ToAccount,Location,Note,IsRefunded,Splits\n")
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (tx in transactions) {
            val dateStr = dateFmt.format(Date(tx.dateMillis))
            val safeNote = tx.note.replace(",", " ").replace("\n", " ")
            val safeLocation = (tx.locationName ?: "").replace(",", " ")
            val splits = (tx.splitsSummary ?: "").replace(",", ";")
            sb.append("${tx.id},$dateStr,${tx.timeString},${tx.type.name},${tx.amount},\"${tx.categoryName}\",\"${tx.accountName}\",\"${tx.toAccountName ?: ""}\",\"$safeLocation\",\"$safeNote\",${tx.isRefunded},\"$splits\"\n")
        }
        sb.toString()
    }

    // JSON Backup Export with Granular Scope Support (FULL, PROFILE, TRANSACTIONS)
    suspend fun exportBackupJson(scope: BackupScope, userPreferences: UserPreferences): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "FinTrack")
        root.put("version", 2)
        root.put("backupType", scope.name)
        root.put("exportedAt", System.currentTimeMillis())

        if (scope == BackupScope.FULL || scope == BackupScope.PROFILE) {
            val profObj = JSONObject()
            profObj.put("userName", userPreferences.userName)
            profObj.put("currencyCode", userPreferences.currencyCode)
            profObj.put("currencySymbol", userPreferences.currencySymbol)
            profObj.put("dateFormatPattern", userPreferences.dateFormatPattern)
            profObj.put("is24HourTime", userPreferences.is24HourTime)
            profObj.put("themeMode", userPreferences.themeMode)
            profObj.put("themeStyle", userPreferences.themeStyle)
            profObj.put("hideBalances", userPreferences.hideBalances)
            root.put("userProfile", profObj)

            val accArray = JSONArray()
            for (acc in accountDao.getAllAccountsSnapshot()) {
                val obj = JSONObject()
                obj.put("id", acc.id)
                obj.put("name", acc.name)
                obj.put("providerName", acc.providerName)
                obj.put("last4Digits", acc.last4Digits)
                obj.put("type", acc.type.name)
                obj.put("creditLimit", acc.creditLimit)
                obj.put("colorHex", acc.colorHex)
                obj.put("iconName", acc.iconName)
                obj.put("balance", acc.balance)
                obj.put("billingDate", acc.billingDate)
                obj.put("paymentDueDate", acc.paymentDueDate)
                obj.put("isArchived", acc.isArchived)
                obj.put("createdAt", acc.createdAt)
                accArray.put(obj)
            }
            root.put("accounts", accArray)
        }

        if (scope == BackupScope.FULL) {
            val billArray = JSONArray()
            for (bill in billDao.getAllBillsSnapshot()) {
                val obj = JSONObject()
                obj.put("id", bill.id)
                obj.put("name", bill.name)
                obj.put("amount", bill.amount)
                obj.put("dueDateMillis", bill.dueDateMillis)
                obj.put("accountId", bill.accountId ?: JSONObject.NULL)
                obj.put("accountName", bill.accountName)
                obj.put("frequency", bill.frequency.name)
                obj.put("reminderEnabled", bill.reminderEnabled)
                obj.put("status", bill.status.name)
                obj.put("categoryName", bill.categoryName)
                obj.put("note", bill.note)
                billArray.put(obj)
            }
            root.put("bills", billArray)

            val budgetArray = JSONArray()
            for (b in budgetDao.getAllBudgetsSnapshot()) {
                val obj = JSONObject()
                obj.put("id", b.id)
                obj.put("monthKey", b.monthKey)
                obj.put("categoryId", b.categoryId ?: JSONObject.NULL)
                obj.put("categoryName", b.categoryName)
                obj.put("limitAmount", b.limitAmount)
                obj.put("colorHex", b.colorHex)
                budgetArray.put(obj)
            }
            root.put("budgets", budgetArray)

            val wealthArray = JSONArray()
            for (w in wealthDao.getAllWealthItemsSnapshot()) {
                val obj = JSONObject()
                obj.put("id", w.id)
                obj.put("type", w.type.name)
                obj.put("name", w.name)
                obj.put("principalOrInitialAmount", w.principalOrInitialAmount)
                obj.put("currentAmount", w.currentAmount)
                obj.put("targetAmount", w.targetAmount)
                obj.put("outstandingAmount", w.outstandingAmount)
                obj.put("contributionOrPaymentAmount", w.contributionOrPaymentAmount)
                obj.put("interestRate", w.interestRate)
                obj.put("durationMonths", w.durationMonths)
                obj.put("frequency", w.frequency)
                obj.put("startDateMillis", w.startDateMillis)
                obj.put("targetOrRenewalDateMillis", w.targetOrRenewalDateMillis)
                obj.put("subType", w.subType)
                obj.put("referenceOrPolicyNumber", w.referenceOrPolicyNumber)
                obj.put("notes", w.notes)
                wealthArray.put(obj)
            }
            root.put("wealthItems", wealthArray)

            val catArray = JSONArray()
            for (c in categoryDao.getAllCategoriesSnapshot()) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("name", c.name)
                obj.put("type", c.type.name)
                obj.put("iconName", c.iconName)
                obj.put("colorHex", c.colorHex)
                obj.put("orderIndex", c.orderIndex)
                obj.put("isDefault", c.isDefault)
                catArray.put(obj)
            }
            root.put("categories", catArray)
        }

        if (scope == BackupScope.FULL || scope == BackupScope.TRANSACTIONS) {
            val txArray = JSONArray()
            for (tx in transactionDao.getAllTransactionsSnapshot()) {
                val obj = JSONObject()
                obj.put("id", tx.id)
                obj.put("amount", tx.amount)
                obj.put("type", tx.type.name)
                obj.put("accountId", tx.accountId)
                obj.put("accountName", tx.accountName)
                obj.put("toAccountId", tx.toAccountId ?: JSONObject.NULL)
                obj.put("toAccountName", tx.toAccountName ?: JSONObject.NULL)
                obj.put("categoryId", tx.categoryId ?: JSONObject.NULL)
                obj.put("categoryName", tx.categoryName)
                obj.put("categoryIcon", tx.categoryIcon)
                obj.put("categoryColor", tx.categoryColor)
                obj.put("dateMillis", tx.dateMillis)
                obj.put("timeString", tx.timeString)
                obj.put("locationName", tx.locationName ?: JSONObject.NULL)
                obj.put("latitude", tx.latitude ?: JSONObject.NULL)
                obj.put("longitude", tx.longitude ?: JSONObject.NULL)
                obj.put("note", tx.note)
                obj.put("isRefunded", tx.isRefunded)
                obj.put("splitsSummary", tx.splitsSummary ?: JSONObject.NULL)
                txArray.put(obj)
            }
            root.put("transactions", txArray)
        }

        root.toString(2)
    }

    // Inspect JSON backup content before restoring
    fun inspectBackup(jsonString: String): BackupInspection {
        return try {
            val root = JSONObject(jsonString)
            val hasUser = root.has("userProfile")
            val userObj = if (hasUser) root.optJSONObject("userProfile") else null
            val txCount = root.optJSONArray("transactions")?.length() ?: 0
            val accCount = root.optJSONArray("accounts")?.length() ?: 0
            val billsCount = root.optJSONArray("bills")?.length() ?: 0
            val budgetsCount = root.optJSONArray("budgets")?.length() ?: 0
            val wealthCount = root.optJSONArray("wealthItems")?.length() ?: 0
            val catCount = root.optJSONArray("categories")?.length() ?: 0

            BackupInspection(
                isValid = true,
                backupType = root.optString("backupType", if (hasUser && txCount > 0) "FULL" else if (hasUser) "PROFILE" else "TRANSACTIONS"),
                hasUserProfile = hasUser,
                userName = userObj?.optString("userName")?.takeIf { it.isNotBlank() },
                themeMode = userObj?.optString("themeMode"),
                themeStyle = userObj?.optString("themeStyle"),
                accountsCount = accCount,
                billsCount = billsCount,
                budgetsCount = budgetsCount,
                wealthCount = wealthCount,
                categoriesCount = catCount,
                transactionsCount = txCount
            )
        } catch (e: Exception) {
            BackupInspection(
                isValid = false,
                backupType = "UNKNOWN",
                hasUserProfile = false,
                userName = null,
                themeMode = null,
                themeStyle = null,
                accountsCount = 0,
                billsCount = 0,
                budgetsCount = 0,
                wealthCount = 0,
                categoriesCount = 0,
                transactionsCount = 0,
                errorMessage = e.localizedMessage ?: "Invalid JSON payload"
            )
        }
    }

    // Restore backup according to selected scope
    suspend fun importBackupJson(jsonString: String, scope: BackupScope): ImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var restoredPrefs: UserPreferences? = null
            var txRestored = 0
            var accRestored = 0
            var billsRestored = 0

            if (scope == BackupScope.FULL || scope == BackupScope.PROFILE) {
                if (root.has("userProfile")) {
                    val p = root.getJSONObject("userProfile")
                    restoredPrefs = UserPreferences(
                        userName = p.optString("userName", ""),
                        currencyCode = p.optString("currencyCode", "USD"),
                        currencySymbol = p.optString("currencySymbol", "$"),
                        dateFormatPattern = p.optString("dateFormatPattern", "MMM dd, yyyy"),
                        is24HourTime = p.optBoolean("is24HourTime", false),
                        themeMode = p.optString("themeMode", "SYSTEM"),
                        themeStyle = p.optString("themeStyle", "PLAIN"),
                        hideBalances = p.optBoolean("hideBalances", false),
                        onboardingCompleted = true
                    )
                }

                if (root.has("accounts")) {
                    val arr = root.getJSONArray("accounts")
                    val accountsList = mutableListOf<Account>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        accountsList.add(
                            Account(
                                id = o.optLong("id", 0L),
                                name = o.optString("name", "Account"),
                                providerName = o.optString("providerName", ""),
                                last4Digits = o.optString("last4Digits", ""),
                                type = try { AccountType.valueOf(o.optString("type")) } catch (_: Exception) { AccountType.BANK },
                                creditLimit = o.optDouble("creditLimit", 0.0),
                                colorHex = o.optString("colorHex", "#10B981"),
                                iconName = o.optString("iconName", "account_balance"),
                                balance = o.optDouble("balance", 0.0),
                                billingDate = o.optInt("billingDate", 1),
                                paymentDueDate = o.optInt("paymentDueDate", 15),
                                isArchived = o.optBoolean("isArchived", false),
                                createdAt = o.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (accountsList.isNotEmpty()) {
                        accountDao.insertAccounts(accountsList)
                        accRestored = accountsList.size
                    }
                }
            }

            if (scope == BackupScope.FULL) {
                if (root.has("bills")) {
                    val arr = root.getJSONArray("bills")
                    val billsList = mutableListOf<Bill>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        billsList.add(
                            Bill(
                                id = o.optLong("id", 0L),
                                name = o.optString("name", "Bill"),
                                amount = o.optDouble("amount", 0.0),
                                dueDateMillis = o.optLong("dueDateMillis", System.currentTimeMillis()),
                                accountId = if (o.isNull("accountId")) null else o.optLong("accountId"),
                                accountName = o.optString("accountName", ""),
                                frequency = try { BillFrequency.valueOf(o.optString("frequency")) } catch (_: Exception) { BillFrequency.MONTHLY },
                                reminderEnabled = o.optBoolean("reminderEnabled", true),
                                status = try { BillStatus.valueOf(o.optString("status")) } catch (_: Exception) { BillStatus.PENDING },
                                categoryName = o.optString("categoryName", "Bills"),
                                note = o.optString("note", "")
                            )
                        )
                    }
                    if (billsList.isNotEmpty()) {
                        billDao.insertBills(billsList)
                        billsRestored = billsList.size
                    }
                }

                if (root.has("budgets")) {
                    val arr = root.getJSONArray("budgets")
                    val budgetList = mutableListOf<Budget>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        budgetList.add(
                            Budget(
                                id = o.optLong("id", 0L),
                                monthKey = o.optString("monthKey", "OVERALL"),
                                categoryId = if (o.isNull("categoryId")) null else o.optLong("categoryId"),
                                categoryName = o.optString("categoryName", "Overall Spending"),
                                limitAmount = o.optDouble("limitAmount", 0.0),
                                colorHex = o.optString("colorHex", "#3B82F6")
                            )
                        )
                    }
                    if (budgetList.isNotEmpty()) {
                        budgetDao.insertBudgets(budgetList)
                    }
                }

                if (root.has("wealthItems")) {
                    val arr = root.getJSONArray("wealthItems")
                    val wealthList = mutableListOf<WealthItem>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        wealthList.add(
                            WealthItem(
                                id = o.optLong("id", 0L),
                                type = try { WealthType.valueOf(o.optString("type")) } catch (_: Exception) { WealthType.SAVINGS },
                                name = o.optString("name", "Asset"),
                                principalOrInitialAmount = o.optDouble("principalOrInitialAmount", 0.0),
                                currentAmount = o.optDouble("currentAmount", 0.0),
                                targetAmount = o.optDouble("targetAmount", 0.0),
                                outstandingAmount = o.optDouble("outstandingAmount", 0.0),
                                contributionOrPaymentAmount = o.optDouble("contributionOrPaymentAmount", 0.0),
                                interestRate = o.optDouble("interestRate", 0.0),
                                durationMonths = o.optInt("durationMonths", 12),
                                frequency = o.optString("frequency", "Monthly"),
                                startDateMillis = o.optLong("startDateMillis", System.currentTimeMillis()),
                                targetOrRenewalDateMillis = o.optLong("targetOrRenewalDateMillis", System.currentTimeMillis() + 365L * 24 * 3600 * 1000),
                                subType = o.optString("subType", ""),
                                referenceOrPolicyNumber = o.optString("referenceOrPolicyNumber", ""),
                                notes = o.optString("notes", "")
                            )
                        )
                    }
                    if (wealthList.isNotEmpty()) {
                        wealthDao.insertWealthItems(wealthList)
                    }
                }

                if (root.has("categories")) {
                    val arr = root.getJSONArray("categories")
                    val catList = mutableListOf<Category>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        catList.add(
                            Category(
                                id = o.optLong("id", 0L),
                                name = o.optString("name", "Category"),
                                type = try { CategoryType.valueOf(o.optString("type")) } catch (_: Exception) { CategoryType.EXPENSE },
                                iconName = o.optString("iconName", "receipt"),
                                colorHex = o.optString("colorHex", "#64748B"),
                                orderIndex = o.optInt("orderIndex", 0),
                                isDefault = o.optBoolean("isDefault", false)
                            )
                        )
                    }
                    if (catList.isNotEmpty()) {
                        categoryDao.insertAll(catList)
                    }
                }
            }

            if (scope == BackupScope.FULL || scope == BackupScope.TRANSACTIONS) {
                if (root.has("transactions")) {
                    val arr = root.getJSONArray("transactions")
                    val txList = mutableListOf<Transaction>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        txList.add(
                            Transaction(
                                id = o.optLong("id", 0L),
                                amount = o.optDouble("amount", 0.0),
                                type = try { TransactionType.valueOf(o.optString("type")) } catch (_: Exception) { TransactionType.EXPENSE },
                                accountId = o.optLong("accountId", 1L),
                                accountName = o.optString("accountName", "Account"),
                                toAccountId = if (o.isNull("toAccountId")) null else o.optLong("toAccountId"),
                                toAccountName = if (o.isNull("toAccountName")) null else o.optString("toAccountName"),
                                categoryId = if (o.isNull("categoryId")) null else o.optLong("categoryId"),
                                categoryName = o.optString("categoryName", "General"),
                                categoryIcon = o.optString("categoryIcon", "receipt"),
                                categoryColor = o.optString("categoryColor", "#64748B"),
                                dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                                timeString = o.optString("timeString", "12:00 PM"),
                                locationName = if (o.isNull("locationName")) null else o.optString("locationName"),
                                latitude = if (o.isNull("latitude")) null else o.optDouble("latitude"),
                                longitude = if (o.isNull("longitude")) null else o.optDouble("longitude"),
                                note = o.optString("note", ""),
                                isRefunded = o.optBoolean("isRefunded", false),
                                splitsSummary = if (o.isNull("splitsSummary")) null else o.optString("splitsSummary")
                            )
                        )
                    }
                    if (txList.isNotEmpty()) {
                        transactionDao.insertAll(txList)
                        txRestored = txList.size
                    }
                }
            }

            val summaryParts = mutableListOf<String>()
            if (txRestored > 0) summaryParts.add("$txRestored transactions")
            if (accRestored > 0) summaryParts.add("$accRestored accounts/cards")
            if (billsRestored > 0) summaryParts.add("$billsRestored bills")
            if (restoredPrefs != null) summaryParts.add("user profile & preferences")

            val msg = if (summaryParts.isNotEmpty()) {
                "Successfully restored ${summaryParts.joinToString(", ")}!"
            } else {
                "Restore completed."
            }

            ImportResult(success = true, message = msg, restoredUserProfile = restoredPrefs)
        } catch (e: Exception) {
            ImportResult(success = false, message = "Restore failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun resetAllUserData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        accountDao.deleteAllAccounts()
        billDao.deleteAllBills()
        budgetDao.deleteAllBudgets()
        wealthDao.deleteAllWealthItems()
    }
}

enum class BackupScope {
    FULL,
    PROFILE,
    TRANSACTIONS
}

data class BackupInspection(
    val isValid: Boolean,
    val backupType: String,
    val hasUserProfile: Boolean,
    val userName: String?,
    val themeMode: String?,
    val themeStyle: String?,
    val accountsCount: Int,
    val billsCount: Int,
    val budgetsCount: Int,
    val wealthCount: Int,
    val categoriesCount: Int,
    val transactionsCount: Int,
    val errorMessage: String? = null
)

data class ImportResult(
    val success: Boolean,
    val message: String,
    val restoredUserProfile: UserPreferences? = null
)

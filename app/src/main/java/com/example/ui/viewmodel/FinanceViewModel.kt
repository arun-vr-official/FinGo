package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.WealthItem
import com.example.data.model.WealthType
import com.example.data.repository.FinanceRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateRangeFilter {
    THIS_MONTH,
    LAST_MONTH,
    ALL
}

enum class SortOption {
    NEWEST,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

data class TransactionFilterState(
    val query: String = "",
    val type: TransactionType? = null,
    val accountId: Long? = null,
    val categoryName: String? = null,
    val dateRange: DateRangeFilter = DateRangeFilter.THIS_MONTH,
    val sortOption: SortOption = SortOption.NEWEST
)

data class MonthSummary(
    val monthName: String,
    val monthKey: String,
    val totalIncome: Double,
    val totalExpenses: Double,
    val availableBalance: Double,
    val budgetLimit: Double,
    val budgetRemaining: Double,
    val budgetPercent: Float
)

data class CategorySpend(
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val amount: Double,
    val percentage: Float
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(database)
    private val prefsRepo = PreferencesRepository(application)

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferences

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> = repository.getRecentTransactions(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<Bill>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingBills: StateFlow<List<Bill>> = repository.getUpcomingBills(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wealthItems: StateFlow<List<WealthItem>> = repository.allWealthItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    val currentMonthBudgets: StateFlow<List<Budget>> = repository.getBudgetsForMonth(currentMonthKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter & Search state for transactions
    private val _filterState = MutableStateFlow(TransactionFilterState())
    val filterState: StateFlow<TransactionFilterState> = _filterState.asStateFlow()

    // PIN lock session state
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Last used account ID for prefill
    var lastUsedAccountId: Long? = null
        private set

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            // If app is not pin protected, unlock immediately
            if (!prefsRepo.preferences.value.isAppLocked) {
                _isAppUnlocked.value = true
            }
        }
    }

    // App Lock management
    fun unlockWithPin(enteredPin: String): Boolean {
        val targetPin = prefsRepo.preferences.value.pinCode
        return if (targetPin == enteredPin || targetPin.isEmpty()) {
            _isAppUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (prefsRepo.preferences.value.isAppLocked) {
            _isAppUnlocked.value = false
        }
    }

    // Filter updates
    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun updateTypeFilter(type: TransactionType?) {
        _filterState.value = _filterState.value.copy(type = type)
    }

    fun updateAccountFilter(accountId: Long?) {
        _filterState.value = _filterState.value.copy(accountId = accountId)
    }

    fun updateCategoryFilter(categoryName: String?) {
        _filterState.value = _filterState.value.copy(categoryName = categoryName)
    }

    fun updateDateRange(range: DateRangeFilter) {
        _filterState.value = _filterState.value.copy(dateRange = range)
    }

    fun updateSortOption(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort)
    }

    fun resetFilters() {
        _filterState.value = TransactionFilterState()
    }

    // Computed Filtered Transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        filterState
    ) { txList, filter ->
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        txList.filter { tx ->
            // Query filter
            val matchesQuery = filter.query.isBlank() ||
                    tx.categoryName.contains(filter.query, ignoreCase = true) ||
                    tx.note.contains(filter.query, ignoreCase = true) ||
                    tx.accountName.contains(filter.query, ignoreCase = true) ||
                    (tx.locationName?.contains(filter.query, ignoreCase = true) == true) ||
                    tx.amount.toString().contains(filter.query)

            // Type filter
            val matchesType = filter.type == null || tx.type == filter.type

            // Account filter
            val matchesAccount = filter.accountId == null ||
                    tx.accountId == filter.accountId ||
                    tx.toAccountId == filter.accountId

            // Category filter
            val matchesCategory = filter.categoryName == null ||
                    tx.categoryName.equals(filter.categoryName, ignoreCase = true)

            // Date Range
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val txYear = txCal.get(Calendar.YEAR)
            val txMonth = txCal.get(Calendar.MONTH)

            val matchesDate = when (filter.dateRange) {
                DateRangeFilter.THIS_MONTH -> txYear == currentYear && txMonth == currentMonth
                DateRangeFilter.LAST_MONTH -> {
                    val prevMonth = if (currentMonth == 0) 11 else currentMonth - 1
                    val prevYear = if (currentMonth == 0) currentYear - 1 else currentYear
                    txYear == prevYear && txMonth == prevMonth
                }
                DateRangeFilter.ALL -> true
            }

            matchesQuery && matchesType && matchesAccount && matchesCategory && matchesDate
        }.sortedWith { a, b ->
            when (filter.sortOption) {
                SortOption.NEWEST -> b.dateMillis.compareTo(a.dateMillis)
                SortOption.OLDEST -> a.dateMillis.compareTo(b.dateMillis)
                SortOption.HIGHEST_AMOUNT -> b.amount.compareTo(a.amount)
                SortOption.LOWEST_AMOUNT -> a.amount.compareTo(b.amount)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month summary (for Home and Reports)
    val currentMonthSummary: StateFlow<MonthSummary> = combine(
        allTransactions,
        currentMonthBudgets
    ) { txList, budgets ->
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        val monthDisplay = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)

        var incomeTotal = 0.0
        var expenseTotal = 0.0

        for (tx in txList) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                if (tx.type == TransactionType.INCOME) {
                    incomeTotal += tx.amount
                } else if (tx.type == TransactionType.EXPENSE && !tx.isRefunded) {
                    expenseTotal += tx.amount
                }
            }
        }

        val available = incomeTotal - expenseTotal
        val overallBudget = budgets.find { it.categoryId == null }?.limitAmount ?: 0.0
        val remaining = if (overallBudget > 0) overallBudget - expenseTotal else 0.0
        val percent = if (overallBudget > 0) (expenseTotal / overallBudget).toFloat().coerceIn(0f, 1f) else 0f

        MonthSummary(
            monthName = monthDisplay,
            monthKey = monthKey,
            totalIncome = incomeTotal,
            totalExpenses = expenseTotal,
            availableBalance = available,
            budgetLimit = overallBudget,
            budgetRemaining = remaining,
            budgetPercent = percent
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthSummary(
            monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
            monthKey = currentMonthKey,
            totalIncome = 0.0,
            totalExpenses = 0.0,
            availableBalance = 0.0,
            budgetLimit = 0.0,
            budgetRemaining = 0.0,
            budgetPercent = 0f
        )
    )

    // Category spending for Current Month (for Reports and visual breakdown)
    val categorySpendings: StateFlow<List<CategorySpend>> = allTransactions.combine(currentMonthSummary) { txList, summary ->
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        val map = mutableMapOf<String, Triple<Double, String, String>>() // name -> (amount, color, icon)

        for (tx in txList) {
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            if (txCal.get(Calendar.YEAR) == currentYear && txCal.get(Calendar.MONTH) == currentMonth) {
                if (tx.type == TransactionType.EXPENSE && !tx.isRefunded) {
                    val current = map[tx.categoryName]
                    val newAmount = (current?.first ?: 0.0) + tx.amount
                    map[tx.categoryName] = Triple(newAmount, tx.categoryColor, tx.categoryIcon)
                }
            }
        }

        val totalExp = if (summary.totalExpenses > 0) summary.totalExpenses else 1.0
        map.entries.map { (catName, triple) ->
            CategorySpend(
                categoryName = catName,
                categoryColor = triple.second,
                categoryIcon = triple.third,
                amount = triple.first,
                percentage = ((triple.first / totalExp) * 100).toFloat()
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Formatter helpers
    fun formatCurrency(amount: Double): String {
        if (preferences.value.hideBalances) return "••••"
        val df = DecimalFormat("#,##0.00")
        val symbol = preferences.value.currencySymbol
        val formattedNumber = df.format(amount)
        return "$symbol$formattedNumber"
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat(preferences.value.dateFormatPattern, Locale.getDefault()).format(Date(millis))
    }

    // CRUD operations
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            lastUsedAccountId = transaction.accountId
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(oldTransaction: Transaction, updated: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(oldTransaction, updated)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val timeFmt = if (preferences.value.is24HourTime) "HH:mm" else "hh:mm a"
            val timeStr = SimpleDateFormat(timeFmt, Locale.getDefault()).format(Date(now))
            val copy = transaction.copy(
                id = 0,
                dateMillis = now,
                timeString = timeStr,
                isRefunded = false
            )
            repository.insertTransaction(copy)
        }
    }

    fun toggleRefund(transaction: Transaction) {
        viewModelScope.launch {
            repository.toggleRefund(transaction)
        }
    }

    // Accounts
    fun addAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccountSafely(accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccountSafely(accountId)
        }
    }

    // Bills
    fun addBill(bill: Bill) {
        viewModelScope.launch {
            repository.insertBill(bill)
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill)
        }
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            repository.deleteBill(billId)
        }
    }

    fun markBillPaid(bill: Bill, createTx: Boolean = true) {
        viewModelScope.launch {
            repository.markBillPaid(bill, createTx)
        }
    }

    // Budgets
    fun setOverallMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            val existing = currentMonthBudgets.value.find { it.categoryId == null }
            val budget = existing?.copy(limitAmount = amount) ?: Budget(
                monthKey = currentMonthKey,
                categoryId = null,
                categoryName = "Overall Monthly Budget",
                limitAmount = amount,
                colorHex = "#10B981"
            )
            repository.insertOrUpdateBudget(budget)
        }
    }

    fun setCategoryBudget(categoryId: Long, categoryName: String, amount: Double, color: String) {
        viewModelScope.launch {
            val existing = currentMonthBudgets.value.find { it.categoryId == categoryId }
            val budget = existing?.copy(limitAmount = amount) ?: Budget(
                monthKey = currentMonthKey,
                categoryId = categoryId,
                categoryName = categoryName,
                limitAmount = amount,
                colorHex = color
            )
            repository.insertOrUpdateBudget(budget)
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
        }
    }

    // Wealth items
    fun addWealthItem(item: WealthItem) {
        viewModelScope.launch {
            repository.insertWealthItem(item)
        }
    }

    fun updateWealthItem(item: WealthItem) {
        viewModelScope.launch {
            repository.updateWealthItem(item)
        }
    }

    fun deleteWealthItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteWealthItem(itemId)
        }
    }

    // Categories
    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun setUserName(name: String) {
        prefsRepo.setUserName(name)
    }

    fun setThemeStyle(style: String) {
        prefsRepo.setThemeStyle(style)
    }

    fun setCurrency(code: String, symbol: String) {
        prefsRepo.setCurrency(code, symbol)
    }

    fun setDateFormat(pattern: String) {
        prefsRepo.setDateFormat(pattern)
    }

    fun setTimeFormat24h(is24h: Boolean) {
        prefsRepo.setTimeFormat24h(is24h)
    }

    fun setThemeMode(mode: String) {
        prefsRepo.setThemeMode(mode)
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        val nextMode = if (isCurrentlyDark) "LIGHT" else "DARK"
        prefsRepo.setThemeMode(nextMode)
    }

    fun resetAllUserData() {
        viewModelScope.launch {
            repository.resetAllUserData()
        }
    }

    fun toggleHideBalances() {
        prefsRepo.setHideBalances(!preferences.value.hideBalances)
    }

    fun setAppLocked(locked: Boolean, pin: String = "") {
        prefsRepo.setAppLocked(locked, pin)
        if (!locked) {
            _isAppUnlocked.value = true
        }
    }

    fun completeOnboarding() {
        prefsRepo.setOnboardingCompleted(true)
    }

    // Export & Backup helpers
    suspend fun exportCsv(): String = repository.exportTransactionsToCsv()
    suspend fun exportBackupJson(scope: com.example.data.repository.BackupScope = com.example.data.repository.BackupScope.FULL): String =
        repository.exportBackupJson(scope, preferences.value)

    fun inspectBackup(jsonString: String): com.example.data.repository.BackupInspection =
        repository.inspectBackup(jsonString)

    suspend fun importBackupJson(jsonString: String, scope: com.example.data.repository.BackupScope): com.example.data.repository.ImportResult {
        val res = repository.importBackupJson(jsonString, scope)
        if (res.success && res.restoredUserProfile != null) {
            val prof = res.restoredUserProfile
            if (prof.userName.isNotBlank()) prefsRepo.setUserName(prof.userName)
            if (prof.currencyCode.isNotBlank()) prefsRepo.setCurrency(prof.currencyCode, prof.currencySymbol)
            if (prof.dateFormatPattern.isNotBlank()) prefsRepo.setDateFormat(prof.dateFormatPattern)
            prefsRepo.setTimeFormat24h(prof.is24HourTime)
            if (prof.themeMode.isNotBlank()) prefsRepo.setThemeMode(prof.themeMode)
            if (prof.themeStyle.isNotBlank()) prefsRepo.setThemeStyle(prof.themeStyle)
        }
        return res
    }
}

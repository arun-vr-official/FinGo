package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CompactBudgetProgress
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FinTrackHeader
import com.example.ui.components.openGoogleMaps
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBills: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenEditTransaction: (Transaction) -> Unit,
    onOpenEditBudget: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsState()
    val monthSummary by viewModel.currentMonthSummary.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val upcomingBills by viewModel.upcomingBills.collectAsState()
    val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with User Name & Profile Icon
            item {
                FinTrackHeader(
                    title = if (prefs.userName.isNotBlank()) "Hi, ${prefs.userName}" else monthSummary.monthName,
                    subtitle = if (prefs.userName.isNotBlank()) "${monthSummary.monthName} • Snapshot" else "Monthly Financial Snapshot",
                    hideBalances = prefs.hideBalances,
                    onToggleHideBalances = { viewModel.toggleHideBalances() },
                    isDarkTheme = isDark,
                    onToggleTheme = { viewModel.toggleTheme(isDark) },
                    onOpenProfile = onOpenProfile,
                    userName = prefs.userName,
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 6.dp)
                )
            }

            // Summary Card (Income, Expenses, Available)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Available Balance
                        Text(
                            text = "Available Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatCurrency(monthSummary.availableBalance),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            ),
                            color = if (monthSummary.availableBalance >= 0) MaterialTheme.colorScheme.onSurface else FinRose
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Income & Expense Sub-metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Income
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FinEmerald.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = FinEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Income",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(monthSummary.totalIncome),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = FinEmerald
                                    )
                                }
                            }

                            // Expense
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(FinRose.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = FinRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Expenses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(monthSummary.totalExpenses),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = FinRose
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Budget Progress Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_summary_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Budget",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = onOpenEditBudget,
                                modifier = Modifier.testTag("edit_budget_btn")
                            ) {
                                Text(
                                    text = if (monthSummary.budgetLimit > 0) "Adjust" else "+ Set Limit",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (monthSummary.budgetLimit > 0) {
                            CompactBudgetProgress(
                                spent = monthSummary.totalExpenses,
                                budget = monthSummary.budgetLimit,
                                currencySymbol = prefs.currencySymbol,
                                hideBalances = prefs.hideBalances
                            )
                        } else {
                            Text(
                                text = "No spending target set for this month yet. Tap '+ Set Limit' to keep your spending in check.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Upcoming Bills Section
            if (upcomingBills.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Obligations",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = onNavigateToBills) {
                            Text("See all", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            upcomingBills.take(3).forEachIndexed { index, bill ->
                                HomeBillRow(
                                    bill = bill,
                                    currencySymbol = prefs.currencySymbol,
                                    hideBalances = prefs.hideBalances,
                                    onMarkPaid = { viewModel.markBillPaid(bill) }
                                )
                                if (index < minOf(2, upcomingBills.size - 1)) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.testTag("view_all_transactions_button")
                    ) {
                        Text("View all", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (recentTxs.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No transactions yet",
                        message = "Add your first expense or income to start tracking.",
                        actionLabel = "Add transaction",
                        onAction = onOpenAddTransaction
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            recentTxs.take(5).forEachIndexed { index, tx ->
                                HomeTransactionRow(
                                    transaction = tx,
                                    formattedAmount = viewModel.formatCurrency(tx.amount),
                                    formattedDate = viewModel.formatDate(tx.dateMillis),
                                    onClick = { onOpenEditTransaction(tx) }
                                )
                                if (index < minOf(4, recentTxs.size - 1)) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Extra space for FAB & bottom nav
            }
        }

        // Floating Action Button for rapid transaction creation
        FloatingActionButton(
            onClick = onOpenAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("home_add_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun HomeBillRow(
    bill: Bill,
    currencySymbol: String,
    hideBalances: Boolean,
    onMarkPaid: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            CategoryIconBadge(
                iconName = "receipt_long",
                colorHex = "#F43F5E",
                size = 36.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${bill.frequency.getDisplayName()} • ${bill.accountName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (hideBalances) "••••" else "$currencySymbol${String.format(java.util.Locale.US, "%.2f", bill.amount)}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onMarkPaid,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Mark Paid",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HomeTransactionRow(
    transaction: Transaction,
    formattedAmount: String,
    formattedDate: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val titleText = if (transaction.note.isNotBlank()) transaction.note else transaction.categoryName

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Row 1: Note as title, amount at end
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(
                    iconName = if (transaction.type == TransactionType.TRANSFER) "swap_horiz" else transaction.categoryIcon,
                    colorHex = if (transaction.type == TransactionType.TRANSFER) "#06B6D4" else transaction.categoryColor,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.isRefunded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Refunded",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinEmerald,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FinEmerald.copy(alpha = 0.1f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            val prefix = when (transaction.type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                TransactionType.TRANSFER -> ""
            }
            val amountColor = when {
                transaction.isRefunded -> MaterialTheme.colorScheme.onSurfaceVariant
                transaction.type == TransactionType.INCOME -> FinEmerald
                transaction.type == TransactionType.EXPENSE -> FinRose
                else -> MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = if (formattedAmount == "••••") "••••" else "$prefix$formattedAmount",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Row 2: Bank details as now
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (transaction.type == TransactionType.TRANSFER) {
                    "${transaction.accountName} → ${transaction.toAccountName ?: "Account"}"
                } else {
                    transaction.accountName
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (transaction.categoryName.isNotBlank() && transaction.categoryName != titleText) {
                Text(
                    text = " • ${transaction.categoryName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 3: Location - Type - Date & time at end
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (!transaction.locationName.isNullOrBlank() || transaction.latitude != null) {
                    val locLabel = transaction.locationName ?: "Location"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable {
                                openGoogleMaps(context, transaction.latitude, transaction.longitude, transaction.locationName)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = locLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                }

                val typeLabel = when (transaction.type) {
                    TransactionType.EXPENSE -> "Expense"
                    TransactionType.INCOME -> "Income"
                    TransactionType.TRANSFER -> "Transfer"
                }
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = when (transaction.type) {
                        TransactionType.INCOME -> FinEmerald
                        TransactionType.EXPENSE -> FinRose
                        TransactionType.TRANSFER -> Color(0xFF06B6D4)
                    }
                )
            }

            Text(
                text = "$formattedDate ${transaction.timeString}".trim(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

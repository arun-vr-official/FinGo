package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FinTrackHeader
import com.example.ui.components.openGoogleMaps
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.DateRangeFilter
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onOpenAddTransaction: () -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val prefs by viewModel.preferences.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

    var showSortDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            FinTrackHeader(
                title = "Transactions",
                subtitle = "${filteredTransactions.size} records",
                hideBalances = prefs.hideBalances,
                onToggleHideBalances = { viewModel.toggleHideBalances() },
                isDarkTheme = isDark,
                onToggleTheme = { viewModel.toggleTheme(isDark) },
                actionIcon = Icons.AutoMirrored.Filled.Sort,
                onActionClick = { showSortDialog = true },
                onOpenProfile = onOpenProfile,
                userName = prefs.userName,
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search transactions, notes, locations...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (filterState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter: All
                FilterChip(
                    selected = filterState.type == null,
                    onClick = { viewModel.updateTypeFilter(null) },
                    label = { Text("All Types") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Expense
                FilterChip(
                    selected = filterState.type == TransactionType.EXPENSE,
                    onClick = {
                        viewModel.updateTypeFilter(
                            if (filterState.type == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                        )
                    },
                    label = { Text("Expenses") }
                )

                // Income
                FilterChip(
                    selected = filterState.type == TransactionType.INCOME,
                    onClick = {
                        viewModel.updateTypeFilter(
                            if (filterState.type == TransactionType.INCOME) null else TransactionType.INCOME
                        )
                    },
                    label = { Text("Income") }
                )

                // Transfer
                FilterChip(
                    selected = filterState.type == TransactionType.TRANSFER,
                    onClick = {
                        viewModel.updateTypeFilter(
                            if (filterState.type == TransactionType.TRANSFER) null else TransactionType.TRANSFER
                        )
                    },
                    label = { Text("Transfers") }
                )

                // Date Range Filter
                FilterChip(
                    selected = filterState.dateRange == DateRangeFilter.THIS_MONTH,
                    onClick = { viewModel.updateDateRange(DateRangeFilter.THIS_MONTH) },
                    label = { Text("This Month") }
                )

                FilterChip(
                    selected = filterState.dateRange == DateRangeFilter.LAST_MONTH,
                    onClick = { viewModel.updateDateRange(DateRangeFilter.LAST_MONTH) },
                    label = { Text("Last Month") }
                )

                FilterChip(
                    selected = filterState.dateRange == DateRangeFilter.ALL,
                    onClick = { viewModel.updateDateRange(DateRangeFilter.ALL) },
                    label = { Text("All Time") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                EmptyStateView(
                    title = "No transactions found",
                    message = if (filterState.query.isNotBlank() || filterState.type != null) {
                        "Try clearing your search or active filters to see all transactions."
                    } else {
                        "Start logging your financial journey by adding your first transaction."
                    },
                    actionLabel = if (filterState.query.isNotBlank() || filterState.type != null) {
                        "Reset Filters"
                    } else {
                        "Add transaction"
                    },
                    onAction = {
                        if (filterState.query.isNotBlank() || filterState.type != null) {
                            viewModel.resetFilters()
                        } else {
                            onOpenAddTransaction()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionCardItem(
                            transaction = tx,
                            formattedAmount = viewModel.formatCurrency(tx.amount),
                            formattedDate = viewModel.formatDate(tx.dateMillis),
                            onClick = { onSelectTransaction(tx) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // FAB to add transaction
        FloatingActionButton(
            onClick = onOpenAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("transactions_add_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
        }
    }

    // Sort Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort Transactions") },
            text = {
                Column {
                    listOf(
                        SortOption.NEWEST to "Newest date first",
                        SortOption.OLDEST to "Oldest date first",
                        SortOption.HIGHEST_AMOUNT to "Highest amount",
                        SortOption.LOWEST_AMOUNT to "Lowest amount"
                    ).forEach { (opt, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSortOption(opt)
                                    showSortDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = filterState.sortOption == opt,
                                onClick = {
                                    viewModel.updateSortOption(opt)
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun TransactionCardItem(
    transaction: Transaction,
    formattedAmount: String,
    formattedDate: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val titleText = if (transaction.note.isNotBlank()) transaction.note else transaction.categoryName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Note as title, Amount at end
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
                        size = 38.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Bank details as now
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transaction.type == TransactionType.TRANSFER) {
                        "${transaction.accountName} → ${transaction.toAccountName ?: "Account"}"
                    } else {
                        transaction.accountName
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.categoryName.isNotBlank() && transaction.categoryName != titleText) {
                    Text(
                        text = " • ${transaction.categoryName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Location - Type - Date & time at end
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Location with free Google Maps open
                    if (!transaction.locationName.isNullOrBlank() || transaction.latitude != null) {
                        val locLabel = transaction.locationName ?: "Location"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable {
                                    openGoogleMaps(
                                        context,
                                        transaction.latitude,
                                        transaction.longitude,
                                        transaction.locationName
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View location on Google Maps",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
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

                    // Type
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

                // Date & time at end
                Text(
                    text = "$formattedDate ${transaction.timeString}".trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

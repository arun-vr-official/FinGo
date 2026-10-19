package com.example.ui.screens.bills

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FinTrackHeader
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun BillsScreen(
    viewModel: FinanceViewModel,
    onOpenAddBill: () -> Unit,
    onEditBill: (Bill) -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val prefs by viewModel.preferences.collectAsState()
    val allBills by viewModel.bills.collectAsState()

    var statusFilter by remember { mutableStateOf<BillStatus?>(null) } // null = All active

    val filteredBills = allBills.filter { bill ->
        when (statusFilter) {
            null -> true
            else -> bill.status == statusFilter
        }
    }

    val totalPending = allBills.filter { it.status != BillStatus.PAID }.sumOf { it.amount }
    val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            FinTrackHeader(
                title = "Bills & Commitments",
                subtitle = "${allBills.size} bills scheduled",
                hideBalances = prefs.hideBalances,
                onToggleHideBalances = { viewModel.toggleHideBalances() },
                isDarkTheme = isDark,
                onToggleTheme = { viewModel.toggleTheme(isDark) },
                onOpenProfile = onOpenProfile,
                userName = prefs.userName,
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
            )

            // Total Upcoming Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Active Bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatCurrency(totalPending),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(FinRose.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = FinRose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All Bills") }
                )
                FilterChip(
                    selected = statusFilter == BillStatus.PENDING,
                    onClick = { statusFilter = if (statusFilter == BillStatus.PENDING) null else BillStatus.PENDING },
                    label = { Text("Due Soon") }
                )
                FilterChip(
                    selected = statusFilter == BillStatus.OVERDUE,
                    onClick = { statusFilter = if (statusFilter == BillStatus.OVERDUE) null else BillStatus.OVERDUE },
                    label = { Text("Overdue") }
                )
                FilterChip(
                    selected = statusFilter == BillStatus.PAID,
                    onClick = { statusFilter = if (statusFilter == BillStatus.PAID) null else BillStatus.PAID },
                    label = { Text("Paid / History") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bills List
            if (filteredBills.isEmpty()) {
                EmptyStateView(
                    title = "No bills found",
                    message = "Add recurring obligations like rent, internet, subscriptions or utilities.",
                    actionLabel = "Add bill",
                    onAction = onOpenAddBill,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBills, key = { it.id }) { bill ->
                        BillItemCard(
                            bill = bill,
                            formattedAmount = viewModel.formatCurrency(bill.amount),
                            formattedDueDate = viewModel.formatDate(bill.dueDateMillis),
                            onMarkPaid = { viewModel.markBillPaid(bill) },
                            onClick = { onEditBill(bill) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // FAB to add bill
        FloatingActionButton(
            onClick = onOpenAddBill,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("bills_add_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Bill", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun BillItemCard(
    bill: Bill,
    formattedAmount: String,
    formattedDueDate: String,
    onMarkPaid: () -> Unit,
    onClick: () -> Unit
) {
    val isPaid = bill.status == BillStatus.PAID
    val isOverdue = bill.status == BillStatus.OVERDUE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                CategoryIconBadge(
                    iconName = "receipt_long",
                    colorHex = if (isPaid) "#10B981" else "#F43F5E",
                    size = 40.dp,
                    iconSize = 22.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bill.status.getDisplayName(),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (bill.status) {
                                BillStatus.PAID -> FinEmerald
                                BillStatus.OVERDUE -> FinRose
                                else -> Color(0xFFF59E0B)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (bill.status) {
                                        BillStatus.PAID -> FinEmerald.copy(alpha = 0.1f)
                                        BillStatus.OVERDUE -> FinRose.copy(alpha = 0.1f)
                                        else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                    }
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "Due $formattedDueDate • ${bill.frequency.getDisplayName()}${if (bill.accountName.isNotBlank()) " • " + bill.accountName else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = onMarkPaid,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Mark as Paid",
                        tint = if (isPaid) FinEmerald else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

package com.example.ui.screens.wealth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WealthItem
import com.example.data.model.WealthType
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.FinBlue
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinPurple
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WealthScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onOpenAddWealth: () -> Unit,
    onEditWealth: (WealthItem) -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val wealthItems by viewModel.wealthItems.collectAsState()
    var selectedFilter by remember { mutableStateOf<WealthType?>(null) }

    val filteredItems = wealthItems.filter {
        selectedFilter == null || it.type == selectedFilter
    }

    // Totals
    val totalSavings = wealthItems.filter { it.type == WealthType.SAVINGS }.sumOf { it.currentAmount }
    val totalInvestments = wealthItems.filter { it.type == WealthType.INVESTMENT }.sumOf { it.currentAmount }
    val totalLoansOutstanding = wealthItems.filter { it.type == WealthType.LOAN }.sumOf { it.outstandingAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wealth & Commitments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onOpenProfile != null) {
                        IconButton(onClick = onOpenProfile) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddWealth,
                modifier = Modifier.testTag("wealth_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Wealth Item")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Overview Wealth Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Total Net Assets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = viewModel.formatCurrency(totalSavings + totalInvestments - totalLoansOutstanding),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatCurrency(totalSavings), fontWeight = FontWeight.Bold, color = FinEmerald)
                        }
                        Column {
                            Text("Investments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatCurrency(totalInvestments), fontWeight = FontWeight.Bold, color = FinBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Loans Left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatCurrency(totalLoansOutstanding), fontWeight = FontWeight.Bold, color = if (totalLoansOutstanding > 0) FinRose else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${wealthItems.size})") }
                )
                WealthType.values().forEach { type ->
                    val count = wealthItems.count { it.type == type }
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = if (selectedFilter == type) null else type },
                        label = { Text("${type.getDisplayName()} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredItems.isEmpty()) {
                EmptyStateView(
                    title = "No wealth items",
                    message = "Track savings goals, investment portfolios, insurance policies, and loan amortization in one calm dashboard.",
                    actionLabel = "Add Item",
                    onAction = onOpenAddWealth,
                    icon = Icons.Default.Savings,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        WealthCard(item = item, viewModel = viewModel, onClick = { onEditWealth(item) })
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WealthCard(
    item: WealthItem,
    viewModel: FinanceViewModel,
    onClick: () -> Unit
) {
    val (typeColor, typeIcon) = when (item.type) {
        WealthType.SAVINGS -> FinEmerald to Icons.Default.Savings
        WealthType.INVESTMENT -> FinBlue to Icons.Default.TrendingUp
        WealthType.LOAN -> FinRose to Icons.Default.CreditScore
        WealthType.INSURANCE -> FinPurple to Icons.Default.HealthAndSafety
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("wealth_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = "${item.type.getDisplayName()}${if (item.subType.isNotBlank()) " • " + item.subType else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    when (item.type) {
                        WealthType.SAVINGS -> {
                            Text(viewModel.formatCurrency(item.currentAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinEmerald)
                            Text("Goal: ${viewModel.formatCurrency(item.targetAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        WealthType.INVESTMENT -> {
                            Text(viewModel.formatCurrency(item.currentAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinBlue)
                            Text("Cost: ${viewModel.formatCurrency(item.principalOrInitialAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        WealthType.LOAN -> {
                            Text(viewModel.formatCurrency(item.outstandingAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinRose)
                            Text("Principal: ${viewModel.formatCurrency(item.principalOrInitialAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        WealthType.INSURANCE -> {
                            Text(viewModel.formatCurrency(item.contributionOrPaymentAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinPurple)
                            Text("${item.frequency} premium", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Progress bar for savings goals or loan repayment
            if (item.type == WealthType.SAVINGS && item.targetAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                val progress = (item.currentAmount / item.targetAmount).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = FinEmerald,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}% completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

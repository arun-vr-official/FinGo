package com.example.ui.screens.budgets

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Budget
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CompactBudgetProgress
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val prefs by viewModel.preferences.collectAsState()
    val monthSummary by viewModel.currentMonthSummary.collectAsState()
    val budgets by viewModel.currentMonthBudgets.collectAsState()
    val categorySpendings by viewModel.categorySpendings.collectAsState()

    var showEditOverallBudget by remember { mutableStateOf(false) }
    var overallLimitInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budgets", fontWeight = FontWeight.Bold) },
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
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Month Budget Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("overall_budget_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Overall Spending Limit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(monthSummary.monthName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = {
                                    overallLimitInput = if (monthSummary.budgetLimit > 0) String.format(Locale.US, "%.0f", monthSummary.budgetLimit) else ""
                                    showEditOverallBudget = true
                                },
                                modifier = Modifier.testTag("edit_overall_limit_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Limit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (monthSummary.budgetLimit > 0) {
                            CompactBudgetProgress(
                                spent = monthSummary.totalExpenses,
                                budget = monthSummary.budgetLimit,
                                currencySymbol = prefs.currencySymbol,
                                hideBalances = prefs.hideBalances
                            )

                            // Status badge
                            Spacer(modifier = Modifier.height(10.dp))
                            val pct = (monthSummary.totalExpenses / monthSummary.budgetLimit * 100).toInt()
                            val isExceeded = monthSummary.totalExpenses > monthSummary.budgetLimit
                            val isApproaching = pct >= 80 && !isExceeded

                            if (isExceeded || isApproaching) {
                                Text(
                                    text = if (isExceeded) "⚠️ Budget exceeded by ${prefs.currencySymbol}${String.format(Locale.US, "%.2f", monthSummary.totalExpenses - monthSummary.budgetLimit)}" else "⚡ Approaching limit ($pct% spent)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isExceeded) FinRose else Color(0xFFF59E0B)
                                )
                            }
                        } else {
                            Text("No overall budget limit set yet. Set a limit to monitor total monthly expenses comfortably.")
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    overallLimitInput = "2000"
                                    showEditOverallBudget = true
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Set Overall Budget Limit")
                            }
                        }
                    }
                }
            }

            // Spending by Category Breakdown
            item {
                Text(
                    text = "Category Spending (${monthSummary.monthName})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (categorySpendings.isEmpty()) {
                item {
                    Text(
                        text = "No category spending recorded for this month yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(categorySpendings) { spend ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconBadge(iconName = spend.categoryIcon, colorHex = spend.categoryColor, size = 32.dp, iconSize = 18.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(spend.categoryName, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(viewModel.formatCurrency(spend.amount), fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.US, "%.1f", spend.percentage)}% of expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (spend.percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Edit Overall Budget Dialog
    if (showEditOverallBudget) {
        AlertDialog(
            onDismissRequest = { showEditOverallBudget = false },
            title = { Text("Set Monthly Budget") },
            text = {
                Column {
                    Text("Enter maximum desired spending target for ${monthSummary.monthName}:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = overallLimitInput,
                        onValueChange = { overallLimitInput = it },
                        label = { Text("Limit Amount (${prefs.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("budget_limit_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = overallLimitInput.toDoubleOrNull() ?: 0.0
                        if (limit > 0) {
                            viewModel.setOverallMonthlyBudget(limit)
                        }
                        showEditOverallBudget = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOverallBudget = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package com.example.ui.screens.accounts

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FinTrackHeader
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun AccountsScreen(
    viewModel: FinanceViewModel,
    onOpenAddAccount: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val prefs by viewModel.preferences.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Calculate Net Positions
    val cashAndBankTotal = accounts
        .filter { it.type != AccountType.CREDIT_CARD }
        .sumOf { it.balance }

    val creditCardOutstanding = accounts
        .filter { it.type == AccountType.CREDIT_CARD }
        .sumOf { it.balance }

    val netPosition = cashAndBankTotal - creditCardOutstanding
    val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FinTrackHeader(
                    title = "Accounts",
                    subtitle = "Financial Sources & Balances",
                    hideBalances = prefs.hideBalances,
                    onToggleHideBalances = { viewModel.toggleHideBalances() },
                    isDarkTheme = isDark,
                    onToggleTheme = { viewModel.toggleTheme(isDark) },
                    onOpenProfile = onOpenProfile,
                    userName = prefs.userName,
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
                )
            }

            // Net Financial Position Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("net_position_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Net Liquid Position",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatCurrency(netPosition),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            ),
                            color = if (netPosition >= 0) MaterialTheme.colorScheme.onSurface else FinRose
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Bank & Cash Balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatCurrency(cashAndBankTotal),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = FinEmerald
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Credit Card Debt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatCurrency(creditCardOutstanding),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (creditCardOutstanding > 0) FinRose else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Accounts List Header
            item {
                Text(
                    text = "All Accounts (${accounts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (accounts.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No accounts yet",
                        message = "Add your bank accounts, cash wallets, or credit cards to see real-time balances.",
                        actionLabel = "Add account",
                        onAction = onOpenAddAccount
                    )
                }
            } else {
                items(accounts, key = { it.id }) { acc ->
                    AccountRowCard(
                        account = acc,
                        formattedBalance = viewModel.formatCurrency(acc.balance),
                        formattedCreditLimit = viewModel.formatCurrency(acc.creditLimit),
                        formattedAvailableCredit = viewModel.formatCurrency((acc.creditLimit - acc.balance).coerceAtLeast(0.0)),
                        onClick = { onEditAccount(acc) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to add account
        FloatingActionButton(
            onClick = onOpenAddAccount,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("accounts_add_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Account", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun AccountRowCard(
    account: Account,
    formattedBalance: String,
    formattedCreditLimit: String,
    formattedAvailableCredit: String,
    onClick: () -> Unit
) {
    val accColor = parseColorSafe(account.colorHex)
    val isCreditCard = account.type == AccountType.CREDIT_CARD

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("account_card_${account.id}"),
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (account.type) {
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.CASH -> Icons.Default.Payments
                                AccountType.WALLET -> Icons.Default.Wallet
                                else -> Icons.Default.CreditCard
                            },
                            contentDescription = null,
                            tint = accColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = buildString {
                                append(account.type.getDisplayName())
                                if (account.providerName.isNotBlank()) append(" • ${account.providerName}")
                                if (account.last4Digits.isNotBlank()) append(" (••${account.last4Digits})")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCreditCard) "Outstanding" else "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedBalance,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isCreditCard && account.balance > 0) FinRose else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Extra metrics for Credit Card
            if (isCreditCard) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                if (account.creditLimit > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available Credit: $formattedAvailableCredit",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinEmerald
                        )
                        Text(
                            text = "Limit: $formattedCreditLimit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Billing Date: Day ${account.billingDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Due Date: Day ${account.paymentDueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

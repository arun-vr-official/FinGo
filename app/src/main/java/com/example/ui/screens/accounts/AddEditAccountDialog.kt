package com.example.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

val AccountAccentColors = listOf(
    // Row 1: Blues, Cyans, Teals, Greens (10 items)
    "#2563EB", "#3B82F6", "#60A5FA", "#06B6D4", "#0891B2",
    "#0D9488", "#14B8A6", "#10B981", "#059669", "#16A34A",
    // Row 2: Limes, Yellows, Ambers, Oranges, Reds (10 items)
    "#65A30D", "#84CC16", "#EAB308", "#F59E0B", "#D97706",
    "#F97316", "#EA580C", "#EF4444", "#DC2626", "#F43F5E",
    // Row 3: Pinks, Magentas, Purples, Indigos, Slates (10 items)
    "#E11D48", "#EC4899", "#DB2777", "#C026D3", "#A855F7",
    "#8B5CF6", "#6366F1", "#4F46E5", "#78716C", "#475569"
)

@Composable
fun AddEditAccountDialog(
    viewModel: FinanceViewModel,
    accountToEdit: Account? = null,
    onDismiss: () -> Unit
) {
    val isEditing = accountToEdit != null

    var name by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var providerName by remember { mutableStateOf(accountToEdit?.providerName ?: "") }
    var last4Digits by remember { mutableStateOf(accountToEdit?.last4Digits ?: "") }
    var selectedType by remember { mutableStateOf(accountToEdit?.type ?: AccountType.BANK) }
    var balanceInput by remember {
        mutableStateOf(if (accountToEdit != null) String.format(Locale.US, "%.2f", accountToEdit.balance) else "")
    }
    var creditLimitInput by remember {
        mutableStateOf(if (accountToEdit != null && accountToEdit.creditLimit > 0) String.format(Locale.US, "%.2f", accountToEdit.creditLimit) else "")
    }
    var billingDateInput by remember {
        mutableStateOf(accountToEdit?.billingDate?.toString() ?: "1")
    }
    var paymentDueDateInput by remember {
        mutableStateOf(accountToEdit?.paymentDueDate?.toString() ?: "15")
    }
    var selectedColor by remember { mutableStateOf(accountToEdit?.colorHex ?: "#3B82F6") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Account" else "Add New Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name *") },
                    placeholder = { Text("e.g. Chase Freedom, Cash Wallet") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_account_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Grid
                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(AccountType.BANK, AccountType.CREDIT_CARD, AccountType.CASH).forEach { type ->
                        val isSelected = selectedType == type
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder(enabled = isSelected)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.getDisplayName().replace(" Account", ""),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(AccountType.DEBIT_CARD, AccountType.WALLET, AccountType.OTHER).forEach { type ->
                        val isSelected = selectedType == type
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder(enabled = isSelected)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.getDisplayName(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text(if (selectedType == AccountType.CREDIT_CARD) "Current Outstanding Balance" else "Current Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_account_balance"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // If Credit Card, show Credit Limit and Billing Cycle Date
                if (selectedType == AccountType.CREDIT_CARD) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = creditLimitInput,
                        onValueChange = { creditLimitInput = it },
                        label = { Text("Credit Limit") },
                        placeholder = { Text("e.g. 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("credit_limit_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = billingDateInput,
                            onValueChange = { input ->
                                if (input.isEmpty()) {
                                    billingDateInput = ""
                                } else if (input.all { it.isDigit() }) {
                                    val num = input.toIntOrNull()
                                    if (num != null) {
                                        billingDateInput = if (num > 31) "31" else input
                                    }
                                }
                            },
                            label = { Text("Billing Date *") },
                            placeholder = { Text("1 to 31") },
                            supportingText = {
                                val num = billingDateInput.toIntOrNull()
                                if (num != null && (num < 1 || num > 31)) {
                                    Text("Day must be 1 to 31", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Day 1–31 (max 31)")
                                }
                            },
                            isError = billingDateInput.isNotEmpty() && (billingDateInput.toIntOrNull() == null || (billingDateInput.toIntOrNull() ?: 0) !in 1..31),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("billing_date_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = paymentDueDateInput,
                            onValueChange = { input ->
                                if (input.isEmpty()) {
                                    paymentDueDateInput = ""
                                } else if (input.all { it.isDigit() }) {
                                    val num = input.toIntOrNull()
                                    if (num != null) {
                                        paymentDueDateInput = if (num > 31) "31" else input
                                    }
                                }
                            },
                            label = { Text("Payment Due Date") },
                            placeholder = { Text("1 to 31") },
                            supportingText = {
                                val num = paymentDueDateInput.toIntOrNull()
                                if (num != null && (num < 1 || num > 31)) {
                                    Text("Day must be 1 to 31", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Day 1–31 (max 31)")
                                }
                            },
                            isError = paymentDueDateInput.isNotEmpty() && (paymentDueDateInput.toIntOrNull() == null || (paymentDueDateInput.toIntOrNull() ?: 0) !in 1..31),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("payment_due_date_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Institution / Provider & Last 4 digits
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = providerName,
                        onValueChange = { providerName = it },
                        label = { Text("Institution / Provider") },
                        placeholder = { Text("Bank name") },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = last4Digits,
                        onValueChange = { if (it.length <= 4) last4Digits = it },
                        label = { Text("Last 4 digits") },
                        placeholder = { Text("1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accent Color (30 colors organized in 10 items x 3 rows)
                Text(
                    text = "Theme Accent (30 Colours)",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountAccentColors.chunked(10).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowColors.forEach { hex ->
                                val col = parseColorSafe(hex)
                                val isSelected = selectedColor.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .clickable { selectedColor = hex }
                                        .then(
                                            if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val balanceVal = balanceInput.toDoubleOrNull() ?: 0.0
                        val limitVal = creditLimitInput.toDoubleOrNull() ?: 0.0
                        val billingDay = billingDateInput.toIntOrNull()?.coerceIn(1, 31) ?: 1
                        val dueDay = paymentDueDateInput.toIntOrNull()?.coerceIn(1, 31) ?: 15

                        val acc = Account(
                            id = accountToEdit?.id ?: 0,
                            name = name.trim(),
                            providerName = providerName.trim(),
                            last4Digits = last4Digits.trim(),
                            type = selectedType,
                            balance = balanceVal,
                            creditLimit = limitVal,
                            billingDate = billingDay,
                            paymentDueDate = dueDay,
                            colorHex = selectedColor
                        )

                        if (isEditing) {
                            viewModel.updateAccount(acc)
                        } else {
                            viewModel.addAccount(acc)
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_account_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEditing) "Save Changes" else "Create Account", fontWeight = FontWeight.SemiBold)
                }

                // Delete button with safe explanation
                if (isEditing && accountToEdit != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Account")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && accountToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${accountToEdit.name}?") },
            text = {
                Text("Past transactions linked to this account will remain safely preserved in your transaction history.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccountSafely(accountToEdit.id)
                        showDeleteConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = FinRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

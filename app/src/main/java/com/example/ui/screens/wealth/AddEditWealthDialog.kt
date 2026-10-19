package com.example.ui.screens.wealth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WealthItem
import com.example.data.model.WealthType
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun AddEditWealthDialog(
    viewModel: FinanceViewModel,
    itemToEdit: WealthItem? = null,
    onDismiss: () -> Unit
) {
    val isEditing = itemToEdit != null
    var selectedType by remember { mutableStateOf(itemToEdit?.type ?: WealthType.SAVINGS) }
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var subType by remember { mutableStateOf(itemToEdit?.subType ?: "") }

    var primaryAmountInput by remember {
        mutableStateOf(
            if (itemToEdit != null) {
                when (itemToEdit.type) {
                    WealthType.SAVINGS -> String.format(Locale.US, "%.2f", itemToEdit.targetAmount)
                    WealthType.LOAN -> String.format(Locale.US, "%.2f", itemToEdit.principalOrInitialAmount)
                    WealthType.INVESTMENT -> String.format(Locale.US, "%.2f", itemToEdit.principalOrInitialAmount)
                    WealthType.INSURANCE -> String.format(Locale.US, "%.2f", itemToEdit.contributionOrPaymentAmount)
                }
            } else ""
        )
    }

    var secondaryAmountInput by remember {
        mutableStateOf(
            if (itemToEdit != null) {
                when (itemToEdit.type) {
                    WealthType.SAVINGS -> String.format(Locale.US, "%.2f", itemToEdit.currentAmount)
                    WealthType.LOAN -> String.format(Locale.US, "%.2f", itemToEdit.outstandingAmount)
                    WealthType.INVESTMENT -> String.format(Locale.US, "%.2f", itemToEdit.currentAmount)
                    WealthType.INSURANCE -> ""
                }
            } else ""
        )
    }

    var contributionInput by remember {
        mutableStateOf(if (itemToEdit != null && itemToEdit.contributionOrPaymentAmount > 0) String.format(Locale.US, "%.2f", itemToEdit.contributionOrPaymentAmount) else "")
    }

    var interestRateInput by remember {
        mutableStateOf(if (itemToEdit != null && itemToEdit.interestRate > 0) String.format(Locale.US, "%.2f", itemToEdit.interestRate) else "")
    }

    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit ${selectedType.getDisplayName()}" else "Add Wealth Item",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Type Segment
                if (!isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WealthType.values().forEach { type ->
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
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.getDisplayName().replace(" Goal", "").replace(" Policy", ""),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name / Title *") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                WealthType.SAVINGS -> "e.g. Emergency Fund, New Car"
                                WealthType.LOAN -> "e.g. Mortgage, Student Loan"
                                WealthType.INVESTMENT -> "e.g. Index Fund, Tech Portfolio"
                                WealthType.INSURANCE -> "e.g. Health Insurance, Life Term"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("wealth_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amounts according to type
                when (selectedType) {
                    WealthType.SAVINGS -> {
                        OutlinedTextField(
                            value = primaryAmountInput,
                            onValueChange = { primaryAmountInput = it },
                            label = { Text("Target Goal Amount *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = secondaryAmountInput,
                            onValueChange = { secondaryAmountInput = it },
                            label = { Text("Current Saved Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = contributionInput,
                            onValueChange = { contributionInput = it },
                            label = { Text("Monthly Contribution (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    WealthType.LOAN -> {
                        OutlinedTextField(
                            value = primaryAmountInput,
                            onValueChange = { primaryAmountInput = it },
                            label = { Text("Original Principal Amount *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = secondaryAmountInput,
                            onValueChange = { secondaryAmountInput = it },
                            label = { Text("Outstanding Balance Remaining") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = interestRateInput,
                                onValueChange = { interestRateInput = it },
                                label = { Text("Interest %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = contributionInput,
                                onValueChange = { contributionInput = it },
                                label = { Text("Monthly EMI") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                    WealthType.INVESTMENT -> {
                        OutlinedTextField(
                            value = primaryAmountInput,
                            onValueChange = { primaryAmountInput = it },
                            label = { Text("Invested Principal / Cost Basis *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = secondaryAmountInput,
                            onValueChange = { secondaryAmountInput = it },
                            label = { Text("Current Market Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    WealthType.INSURANCE -> {
                        OutlinedTextField(
                            value = primaryAmountInput,
                            onValueChange = { primaryAmountInput = it },
                            label = { Text("Premium Amount *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = subType,
                            onValueChange = { subType = it },
                            label = { Text("Policy Type (e.g. Health, Life, Auto)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val pAmount = primaryAmountInput.toDoubleOrNull() ?: 0.0
                        val sAmount = secondaryAmountInput.toDoubleOrNull() ?: 0.0
                        val contrib = contributionInput.toDoubleOrNull() ?: 0.0
                        val interest = interestRateInput.toDoubleOrNull() ?: 0.0

                        val item = WealthItem(
                            id = itemToEdit?.id ?: 0,
                            type = selectedType,
                            name = name.trim(),
                            subType = subType.trim(),
                            principalOrInitialAmount = if (selectedType == WealthType.SAVINGS) 0.0 else pAmount,
                            targetAmount = if (selectedType == WealthType.SAVINGS) pAmount else 0.0,
                            currentAmount = sAmount,
                            outstandingAmount = if (selectedType == WealthType.LOAN) sAmount else 0.0,
                            contributionOrPaymentAmount = if (selectedType == WealthType.INSURANCE) pAmount else contrib,
                            interestRate = interest,
                            notes = notes.trim()
                        )

                        if (isEditing) {
                            viewModel.updateWealthItem(item)
                        } else {
                            viewModel.addWealthItem(item)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_wealth_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEditing) "Save Changes" else "Save Wealth Item", fontWeight = FontWeight.SemiBold)
                }

                if (isEditing && itemToEdit != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteWealthItem(itemToEdit.id)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Item")
                    }
                }
            }
        }
    }
}

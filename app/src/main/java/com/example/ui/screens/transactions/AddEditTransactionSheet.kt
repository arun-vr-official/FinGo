package com.example.ui.screens.transactions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.openGoogleMaps
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Built-in categories for Transfers to ensure rich categorized tracking
val DefaultTransferCategories = listOf(
    Category(name = "Account Transfer", type = CategoryType.EXPENSE, iconName = "swap_horiz", colorHex = "#06B6D4"),
    Category(name = "Savings Deposit", type = CategoryType.EXPENSE, iconName = "savings", colorHex = "#10B981"),
    Category(name = "Card Payment", type = CategoryType.EXPENSE, iconName = "credit_card", colorHex = "#8B5CF6"),
    Category(name = "Cash Withdrawal", type = CategoryType.EXPENSE, iconName = "payments", colorHex = "#F59E0B"),
    Category(name = "Loan Payment", type = CategoryType.EXPENSE, iconName = "receipt_long", colorHex = "#EF4444"),
    Category(name = "Investment Transfer", type = CategoryType.EXPENSE, iconName = "trending_up", colorHex = "#3B82F6"),
    Category(name = "Other Transfer", type = CategoryType.EXPENSE, iconName = "more_horiz", colorHex = "#64748B")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    viewModel: FinanceViewModel,
    transactionToEdit: Transaction? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs by viewModel.preferences.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val allCategories by viewModel.categories.collectAsState()

    val isEditing = transactionToEdit != null
    var selectedType by remember {
        mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE)
    }

    var amountInput by remember {
        mutableStateOf(if (transactionToEdit != null) String.format(Locale.US, "%.2f", transactionToEdit.amount) else "")
    }

    // Title field (placed directly below amount)
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }

    // Account filtering: If Income, exclude credit cards (income always comes to deposit/bank/cash account)
    val eligibleAccounts = remember(selectedType, accounts) {
        if (selectedType == TransactionType.INCOME) {
            accounts.filter { it.type != AccountType.CREDIT_CARD }
        } else {
            accounts
        }
    }

    val defaultAccountId = transactionToEdit?.accountId
        ?: viewModel.lastUsedAccountId
        ?: eligibleAccounts.firstOrNull()?.id
        ?: accounts.firstOrNull()?.id
        ?: 1L

    var selectedAccountId by remember { mutableStateOf(defaultAccountId) }
    var selectedToAccountId by remember {
        mutableStateOf(transactionToEdit?.toAccountId ?: accounts.getOrNull(1)?.id ?: defaultAccountId)
    }

    // Ensure selected account is valid for income
    LaunchedEffect(selectedType, eligibleAccounts) {
        if (selectedType == TransactionType.INCOME) {
            val currentAcc = accounts.find { it.id == selectedAccountId }
            if (currentAcc == null || currentAcc.type == AccountType.CREDIT_CARD) {
                eligibleAccounts.firstOrNull()?.id?.let {
                    selectedAccountId = it
                }
            }
        }
    }

    // Categories by transaction type
    val relevantCategories = remember(selectedType, allCategories) {
        when (selectedType) {
            TransactionType.EXPENSE -> allCategories.filter { it.type == CategoryType.EXPENSE }
            TransactionType.INCOME -> allCategories.filter { it.type == CategoryType.INCOME }
            TransactionType.TRANSFER -> DefaultTransferCategories
        }
    }

    var selectedCategory by remember {
        mutableStateOf<Category?>(
            if (transactionToEdit != null) {
                relevantCategories.find { it.name == transactionToEdit.categoryName }
                    ?: allCategories.find { it.name == transactionToEdit.categoryName }
                    ?: relevantCategories.firstOrNull()
            } else {
                relevantCategories.firstOrNull()
            }
        )
    }

    // When type changes, select the first relevant category
    LaunchedEffect(selectedType) {
        if (!isEditing || selectedCategory?.type?.name != selectedType.name) {
            selectedCategory = relevantCategories.firstOrNull()
        }
    }

    // Category accordion state (collapsed shows 2 lines initially)
    var isCategoryAccordionExpanded by remember { mutableStateOf(false) }

    // Date & Time
    val now = remember { System.currentTimeMillis() }
    val initialDate = transactionToEdit?.dateMillis ?: now
    val defaultTimeStr = SimpleDateFormat(if (prefs.is24HourTime) "HH:mm" else "hh:mm a", Locale.getDefault()).format(Date(initialDate))
    var dateMillis by remember { mutableStateOf(initialDate) }
    var timeString by remember { mutableStateOf(transactionToEdit?.timeString ?: defaultTimeStr) }

    // Location & Coordinates
    var locationName by remember { mutableStateOf(transactionToEdit?.locationName ?: "") }
    var latitude by remember { mutableStateOf<Double?>(transactionToEdit?.latitude) }
    var longitude by remember { mutableStateOf<Double?>(transactionToEdit?.longitude) }
    var isLocating by remember { mutableStateOf(false) }

    var isRefunded by remember { mutableStateOf(transactionToEdit?.isRefunded ?: false) }

    // Helper to fetch exact current location from GPS hardware with coordinates & reverse geocode
    fun detectCurrentLocation() {
        isLocating = true
        try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locManager != null) {
                    val isGpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    val isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    fun applyExactLocation(loc: Location) {
                        latitude = loc.latitude
                        longitude = loc.longitude

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    val street = listOfNotNull(addr.subThoroughfare, addr.thoroughfare).joinToString(" ").trim()
                                    val locality = addr.locality ?: addr.subLocality ?: addr.adminArea
                                    val feature = addr.featureName?.takeIf { it != addr.subThoroughfare && it != addr.thoroughfare }

                                    val place = when {
                                        !feature.isNullOrBlank() && street.isNotBlank() -> "$feature, $street"
                                        street.isNotBlank() && !locality.isNullOrBlank() -> "$street, $locality"
                                        !feature.isNullOrBlank() -> feature
                                        !locality.isNullOrBlank() -> locality
                                        else -> "${String.format(Locale.US, "%.5f", loc.latitude)}, ${String.format(Locale.US, "%.5f", loc.longitude)}"
                                    }
                                    withContext(Dispatchers.Main) {
                                        if (locationName.isBlank() || locationName == "Detecting location...") {
                                            locationName = place
                                        }
                                        isLocating = false
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        if (locationName.isBlank() || locationName == "Detecting location...") {
                                            locationName = "${String.format(Locale.US, "%.5f", loc.latitude)}, ${String.format(Locale.US, "%.5f", loc.longitude)}"
                                        }
                                        isLocating = false
                                    }
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) {
                                    if (locationName.isBlank() || locationName == "Detecting location...") {
                                        locationName = "${String.format(Locale.US, "%.5f", loc.latitude)}, ${String.format(Locale.US, "%.5f", loc.longitude)}"
                                    }
                                    isLocating = false
                                }
                            }
                        }
                    }

                    // 1. Immediately apply last known GPS/Network location as instantaneous preview
                    val cachedLoc = (if (isGpsEnabled) locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null)
                        ?: (if (isNetworkEnabled) locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null)
                    if (cachedLoc != null && latitude == null) {
                        applyExactLocation(cachedLoc)
                    }

                    // 2. Request fresh active GPS fix from hardware
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                        locManager.getCurrentLocation(
                            provider,
                            CancellationSignal(),
                            context.mainExecutor
                        ) { freshLocation ->
                            if (freshLocation != null) {
                                applyExactLocation(freshLocation)
                            } else {
                                isLocating = false
                            }
                        }
                    } else {
                        val listener = object : LocationListener {
                            override fun onLocationChanged(freshLocation: Location) {
                                applyExactLocation(freshLocation)
                                try { locManager.removeUpdates(this) } catch (_: Exception) {}
                            }
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        }
                        val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                        locManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    }
                } else {
                    isLocating = false
                }
            } else {
                isLocating = false
            }
        } catch (_: Exception) {
            isLocating = false
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            detectCurrentLocation()
        }
    }

    // Automatically detect current location when adding a new transaction
    LaunchedEffect(isEditing) {
        if (!isEditing && locationName.isBlank() && latitude == null) {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                detectCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header with Type Segmented Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Transaction Type Selector (Expense / Income / Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.INCOME to "Income",
                    TransactionType.TRANSFER to "Transfer"
                ).forEach { (type, label) ->
                    val isSelected = selectedType == type
                    val activeBg = when (type) {
                        TransactionType.EXPENSE -> FinRose
                        TransactionType.INCOME -> FinEmerald
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable {
                                selectedType = type
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prefs.currencySymbol,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        placeholder = { Text("0.00", fontSize = 28.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_amount_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Title field directly below amount (stores note / title)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Title *") },
                placeholder = {
                    Text(
                        when (selectedType) {
                            TransactionType.INCOME -> "e.g. Salary, Client project, Dividend"
                            TransactionType.TRANSFER -> "e.g. Card payment, Savings transfer"
                            TransactionType.EXPENSE -> "e.g. Grocery shopping, Coffee, Dinner"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Categories Section: 2 Lines initial, with Show More accordion button
            val itemsPerRow = 4
            val categoryRows = remember(relevantCategories) {
                relevantCategories.chunked(itemsPerRow)
            }
            val displayedRows = if (isCategoryAccordionExpanded) categoryRows else categoryRows.take(2)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedType) {
                        TransactionType.TRANSFER -> "Transfer Category"
                        TransactionType.INCOME -> "Income Category"
                        TransactionType.EXPENSE -> "Expense Category"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                selectedCategory?.let {
                    Text(
                        text = "Selected: ${it.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { cat ->
                            val isSelected = selectedCategory?.name == cat.name
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = cat },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = CardDefaults.outlinedCardBorder(enabled = isSelected)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CategoryIconBadge(
                                        iconName = cat.iconName,
                                        colorHex = cat.colorHex,
                                        size = 32.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        // Fill empty cells if last row has fewer items
                        repeat(itemsPerRow - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Show More / Show Less Accordion Button
            if (categoryRows.size > 2) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { isCategoryAccordionExpanded = !isCategoryAccordionExpanded },
                        modifier = Modifier.testTag("category_accordion_toggle")
                    ) {
                        Icon(
                            imageVector = if (isCategoryAccordionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCategoryAccordionExpanded) "Show Less" else "Show More (${relevantCategories.size - (2 * itemsPerRow)} more)",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Account Selector (If income: hide credit cards, only deposit accounts!)
            val accountTitle = when (selectedType) {
                TransactionType.INCOME -> "Deposit Account"
                TransactionType.TRANSFER -> "From Account"
                TransactionType.EXPENSE -> "Account"
            }

            Text(
                text = accountTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedType == TransactionType.INCOME && eligibleAccounts.isEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No deposit accounts available. Please add a bank or cash account to receive income.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eligibleAccounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Card(
                            modifier = Modifier.clickable { selectedAccountId = acc.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder(enabled = isSelected)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Transfer Destination Account
            if (selectedType == TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "To Destination Account",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts.filter { it.id != selectedAccountId }) { acc ->
                        val isSelected = acc.id == selectedToAccountId
                        Card(
                            modifier = Modifier.clickable { selectedToAccountId = acc.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder(enabled = isSelected)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Location with Coordinates & Free Google Maps integration
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location (Coordinates & Map)") },
                placeholder = { Text("Store, cafe, address or locality...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (latitude != null) FinEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (locationName.isNotBlank() || latitude != null) {
                            IconButton(onClick = {
                                locationName = ""
                                latitude = null
                                longitude = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear location", modifier = Modifier.size(18.dp))
                            }
                        }
                        // GPS detect/refresh button
                        IconButton(onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasFine || hasCoarse) {
                                detectCurrentLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect GPS Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Open in Google Maps button (free native Android Intent)
                        IconButton(onClick = {
                            openGoogleMaps(context, latitude, longitude, locationName.ifBlank { "Current Location" })
                        }) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Open in Google Maps (Free)",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_location_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Coordinates Badge & Google Maps Free Viewer
            if (latitude != null && longitude != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinEmerald.copy(alpha = 0.1f))
                            .clickable {
                                openGoogleMaps(context, latitude, longitude, locationName)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = FinEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS: ${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)} • Open Map (Free)",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Refund / Reversal Toggle (if editing expense)
            if (isEditing && selectedType == TransactionType.EXPENSE) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mark as Refunded", fontWeight = FontWeight.Medium)
                        Text("Keeps transaction history but offsets expense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isRefunded,
                        onCheckedChange = { isRefunded = it },
                        modifier = Modifier.testTag("toggle_refund_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Action Buttons
            Button(
                onClick = {
                    val amountVal = amountInput.toDoubleOrNull() ?: 0.0
                    if (amountVal <= 0) return@Button

                    val accountObj = accounts.find { it.id == selectedAccountId }
                    val toAccountObj = accounts.find { it.id == selectedToAccountId }

                    val categoryNameFinal = selectedCategory?.name
                        ?: if (selectedType == TransactionType.TRANSFER) "Transfer" else "Uncategorized"
                    val categoryIconFinal = selectedCategory?.iconName
                        ?: if (selectedType == TransactionType.TRANSFER) "swap_horiz" else "category"
                    val categoryColorFinal = selectedCategory?.colorHex
                        ?: if (selectedType == TransactionType.TRANSFER) "#06B6D4" else "#64748B"

                    val tx = Transaction(
                        id = transactionToEdit?.id ?: 0,
                        amount = amountVal,
                        type = selectedType,
                        accountId = selectedAccountId,
                        accountName = accountObj?.name ?: "Account",
                        toAccountId = if (selectedType == TransactionType.TRANSFER) selectedToAccountId else null,
                        toAccountName = if (selectedType == TransactionType.TRANSFER) toAccountObj?.name else null,
                        categoryId = selectedCategory?.id,
                        categoryName = categoryNameFinal,
                        categoryIcon = categoryIconFinal,
                        categoryColor = categoryColorFinal,
                        dateMillis = dateMillis,
                        timeString = timeString,
                        locationName = if (locationName.isBlank()) null else locationName.trim(),
                        latitude = latitude,
                        longitude = longitude,
                        note = note.trim(),
                        isRefunded = isRefunded,
                        splitsSummary = null
                    )

                    if (isEditing && transactionToEdit != null) {
                        viewModel.updateTransaction(transactionToEdit, tx)
                    } else {
                        viewModel.addTransaction(tx)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Save Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Edit Actions: Duplicate & Delete
            if (isEditing && transactionToEdit != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.duplicateTransaction(transactionToEdit)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Duplicate")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTransaction(transactionToEdit)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

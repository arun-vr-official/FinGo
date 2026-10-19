package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FinEmerald
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Transaction
import com.example.data.model.WealthItem
import com.example.ui.components.PinLockOverlay
import com.example.ui.screens.accounts.AccountsScreen
import com.example.ui.screens.accounts.AddEditAccountDialog
import com.example.ui.screens.bills.AddEditBillDialog
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.BackupRestoreScreen
import com.example.ui.screens.settings.SecurityScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.AddEditTransactionSheet
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.screens.wealth.AddEditWealthDialog
import com.example.ui.screens.wealth.WealthScreen
import com.example.ui.theme.FinTrackTheme
import com.example.ui.theme.themePattern
import com.example.ui.viewmodel.FinanceViewModel

enum class MainTab {
    HOME,
    TRANSACTIONS,
    ACCOUNTS,
    BILLS
}

enum class SubScreen {
    PROFILE,
    WEALTH,
    BUDGETS,
    REPORTS,
    CATEGORIES,
    SECURITY,
    BACKUP,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by viewModel.preferences.collectAsState()
            val isUnlocked by viewModel.isAppUnlocked.collectAsState()

            val darkTheme = when (prefs.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            FinTrackTheme(darkTheme = darkTheme) {
                if (!prefs.onboardingCompleted) {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onFinish = { viewModel.completeOnboarding() }
                    )
                } else if (prefs.isAppLocked && !isUnlocked) {
                    PinLockOverlay(
                        onUnlockSuccess = { /* handled reactively */ },
                        onVerifyPin = { viewModel.unlockWithPin(it) }
                    )
                } else {
                    FinTrackAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinTrackAppContent(viewModel: FinanceViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var currentSubScreen by remember { mutableStateOf<SubScreen?>(null) }

    // Transaction sheet state
    var showTxSheet by remember { mutableStateOf(false) }
    var txToEdit by remember { mutableStateOf<Transaction?>(null) }
    val txSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Account dialog state
    var showAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    // Bill dialog state
    var showBillDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<Bill?>(null) }

    // Wealth dialog state
    var showWealthDialog by remember { mutableStateOf(false) }
    var wealthToEdit by remember { mutableStateOf<WealthItem?>(null) }

    // Handle back press on sub-screens
    if (currentSubScreen != null) {
        BackHandler {
            currentSubScreen = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentSubScreen == null) {
                val prefs by viewModel.preferences.collectAsState()
                val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .themePattern(
                            style = prefs.themeStyle,
                            isDark = isDark,
                            backgroundColor = MaterialTheme.colorScheme.background
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MainTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                val (iconSelected, iconUnselected, label, testTag) = when (tab) {
                                    MainTab.HOME -> listOf(Icons.Filled.Home, Icons.Outlined.Home, "Home", "nav_home")
                                    MainTab.TRANSACTIONS -> listOf(Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz, "Transactions", "nav_transactions")
                                    MainTab.ACCOUNTS -> listOf(Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "Accounts", "nav_accounts")
                                    MainTab.BILLS -> listOf(Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "Bills", "nav_bills")
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { currentTab = tab }
                                        .padding(vertical = 2.dp)
                                        .testTag(testTag as String),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Black overlay for footer logos/icons background
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = if (isSelected) 0.85f else 0.65f))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) FinEmerald else Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = (if (isSelected) iconSelected else iconUnselected) as androidx.compose.ui.graphics.vector.ImageVector,
                                            contentDescription = label as String,
                                            tint = if (isSelected) FinEmerald else Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = label as String,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val prefs by viewModel.preferences.collectAsState()
        val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .themePattern(
                    style = prefs.themeStyle,
                    isDark = isDark,
                    backgroundColor = MaterialTheme.colorScheme.background
                )
        ) {
            when (currentSubScreen) {
                SubScreen.PROFILE -> {
                    MoreScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onNavigateToWealth = { currentSubScreen = SubScreen.WEALTH },
                        onNavigateToBudgets = { currentSubScreen = SubScreen.BUDGETS },
                        onNavigateToReports = { currentSubScreen = SubScreen.REPORTS },
                        onNavigateToCategories = { currentSubScreen = SubScreen.CATEGORIES },
                        onNavigateToSecurity = { currentSubScreen = SubScreen.SECURITY },
                        onNavigateToBackup = { currentSubScreen = SubScreen.BACKUP },
                        onNavigateToSettings = { currentSubScreen = SubScreen.SETTINGS }
                    )
                }
                SubScreen.WEALTH -> {
                    WealthScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenAddWealth = {
                            wealthToEdit = null
                            showWealthDialog = true
                        },
                        onEditWealth = { item ->
                            wealthToEdit = item
                            showWealthDialog = true
                        },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.BUDGETS -> {
                    BudgetsScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.CATEGORIES -> {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.SECURITY -> {
                    SecurityScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.BACKUP -> {
                    BackupRestoreScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                SubScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = null },
                        onNavigateToSecurity = { currentSubScreen = SubScreen.SECURITY },
                        onNavigateToCategories = { currentSubScreen = SubScreen.CATEGORIES },
                        onNavigateToBackup = { currentSubScreen = SubScreen.BACKUP },
                        onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                    )
                }
                null -> {
                    when (currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTransactions = { currentTab = MainTab.TRANSACTIONS },
                                onNavigateToBills = { currentTab = MainTab.BILLS },
                                onOpenAddTransaction = {
                                    txToEdit = null
                                    showTxSheet = true
                                },
                                onOpenEditTransaction = { tx ->
                                    txToEdit = tx
                                    showTxSheet = true
                                },
                                onOpenEditBudget = { currentSubScreen = SubScreen.BUDGETS },
                                onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                            )
                        }
                        MainTab.TRANSACTIONS -> {
                            TransactionsScreen(
                                viewModel = viewModel,
                                onOpenAddTransaction = {
                                    txToEdit = null
                                    showTxSheet = true
                                },
                                onSelectTransaction = { tx ->
                                    txToEdit = tx
                                    showTxSheet = true
                                },
                                onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                            )
                        }
                        MainTab.ACCOUNTS -> {
                            AccountsScreen(
                                viewModel = viewModel,
                                onOpenAddAccount = {
                                    accountToEdit = null
                                    showAccountDialog = true
                                },
                                onEditAccount = { acc ->
                                    accountToEdit = acc
                                    showAccountDialog = true
                                },
                                onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                            )
                        }
                        MainTab.BILLS -> {
                            BillsScreen(
                                viewModel = viewModel,
                                onOpenAddBill = {
                                    billToEdit = null
                                    showBillDialog = true
                                },
                                onEditBill = { bill ->
                                    billToEdit = bill
                                    showBillDialog = true
                                },
                                onOpenProfile = { currentSubScreen = SubScreen.PROFILE }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit Transaction
    if (showTxSheet) {
        AddEditTransactionSheet(
            viewModel = viewModel,
            transactionToEdit = txToEdit,
            sheetState = txSheetState,
            onDismiss = { showTxSheet = false }
        )
    }

    // Dialog for Add/Edit Account
    if (showAccountDialog) {
        AddEditAccountDialog(
            viewModel = viewModel,
            accountToEdit = accountToEdit,
            onDismiss = { showAccountDialog = false }
        )
    }

    // Dialog for Add/Edit Bill
    if (showBillDialog) {
        AddEditBillDialog(
            viewModel = viewModel,
            billToEdit = billToEdit,
            onDismiss = { showBillDialog = false }
        )
    }

    // Dialog for Add/Edit Wealth Item
    if (showWealthDialog) {
        AddEditWealthDialog(
            viewModel = viewModel,
            itemToEdit = wealthToEdit,
            onDismiss = { showWealthDialog = false }
        )
    }
}

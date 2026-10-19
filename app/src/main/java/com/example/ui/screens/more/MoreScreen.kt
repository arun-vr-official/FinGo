package com.example.ui.screens.more

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.FinBlue
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinPurple
import com.example.ui.theme.FinRose
import com.example.ui.theme.themePattern
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: FinanceViewModel,
    onBack: (() -> Unit)? = null,
    onNavigateToWealth: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsState()
    val isDark = prefs.themeMode == "DARK" || (prefs.themeMode == "SYSTEM" && isSystemInDarkTheme())
    var showResetDialog by remember { mutableStateOf(false) }

    var userNameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }

    val activeStyle = AppThemeStyle.fromCode(prefs.themeStyle)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themePattern(
                style = prefs.themeStyle,
                isDark = isDark,
                backgroundColor = MaterialTheme.colorScheme.background
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            Box(modifier = Modifier.padding(start = 12.dp)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = FinEmerald)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
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
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // 1. User Profile & Name Field
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(FinEmerald.copy(alpha = 0.15f))
                                        .border(2.dp, FinEmerald, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (userNameInput.isNotBlank()) userNameInput.take(1).uppercase() else "U",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (userNameInput.isNotBlank()) userNameInput else "User Profile",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Your financial identity (stored safely on device)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = userNameInput,
                                onValueChange = {
                                    userNameInput = it
                                    viewModel.setUserName(it)
                                },
                                label = { Text("Your Name / Display Name") },
                                placeholder = { Text("e.g. Alex Morgan") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                supportingText = {
                                    Text("This name will appear on all screen headers and greetings.")
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("user_name_input")
                            )
                        }
                    }
                }

                // 2. Themes & Background Patterns
                item {
                    Text("Theme & Background Patterns", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            // Dark/Light Mode switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val currentThemeLabel = when (activeStyle) {
                                        AppThemeStyle.PLAIN -> if (isDark) "Plain Dark" else "Plain Light (Silver)"
                                        else -> "${activeStyle.displayName} (${if (isDark) "Dark Mode" else "Light Mode (Silver)"})"
                                    }
                                    Text(currentThemeLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        if (isDark) "Comfortable dark obsidian palette" else "Soft silver metallic background (non-glaring)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.toggleTheme(isDark)
                                        Toast.makeText(context, "Switched to ${if (!isDark) "Dark Mode" else "Light Mode"}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle theme mode",
                                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF8B5CF6),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Active Theme Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .themePattern(
                                        style = activeStyle.code,
                                        isDark = isDark,
                                        backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE5E9EE)
                                    )
                                    .border(1.5.dp, activeStyle.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(activeStyle.primaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(activeStyle.secondaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "ACTIVE: ${activeStyle.displayName.uppercase()}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                        }
                                        Text(
                                            text = if (isDark) "Dark mode active" else "Silver light mode active",
                                            fontSize = 10.sp,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "LIVE PREVIEW",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Select Theme Pattern:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Tap any theme below to preview and apply it immediately across all pages.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Theme Pattern selector cards
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(AppThemeStyle.values()) { style ->
                                    val isSelected = activeStyle == style
                                    Card(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clickable {
                                                viewModel.setThemeStyle(style.code)
                                                Toast.makeText(context, "Applied: ${style.displayName}", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder(enabled = false)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            // Mini live pattern preview box
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .themePattern(
                                                        style = style.code,
                                                        isDark = isDark,
                                                        backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE5E9EE)
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                                        color = if (isSelected) FinEmerald else MaterialTheme.colorScheme.outlineVariant,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.TopEnd
                                            ) {
                                                // Dual color accents badge
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(5.dp)
                                                ) {
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(style.primaryColor))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(style.secondaryColor))
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(FinEmerald),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val titleText = if (style == AppThemeStyle.PLAIN) {
                                                if (isDark) "Plain Dark" else "Plain Light"
                                            } else {
                                                style.displayName
                                            }

                                            Text(
                                                text = titleText,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = style.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp,
                                                lineHeight = 12.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // 3. Financial Planning Hub
            item {
                Text("Financial Planning", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MoreMenuRow(
                            title = "Wealth & Commitments",
                            subtitle = "Loans, savings goals, investments, insurance",
                            icon = Icons.Default.Savings,
                            iconTint = FinEmerald,
                            onClick = onNavigateToWealth,
                            testTag = "more_menu_wealth"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        MoreMenuRow(
                            title = "Monthly Budgets",
                            subtitle = "Overall spending limits & category targets",
                            icon = Icons.Default.PieChart,
                            iconTint = FinBlue,
                            onClick = onNavigateToBudgets,
                            testTag = "more_menu_budgets"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        MoreMenuRow(
                            title = "Reports & Insights",
                            subtitle = "Cash flow, category breakdown & savings rate",
                            icon = Icons.Default.Insights,
                            iconTint = FinPurple,
                            onClick = onNavigateToReports,
                            testTag = "more_menu_reports"
                        )
                    }
                }
            }

            // 4. Organization & Categories
            item {
                Text("Organization", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MoreMenuRow(
                            title = "Categories",
                            subtitle = "Customize expense & income category labels",
                            icon = Icons.Default.Category,
                            iconTint = Color(0xFFF59E0B),
                            onClick = onNavigateToCategories,
                            testTag = "more_menu_categories"
                        )
                    }
                }
            }

            // 5. System, Security & Data Management
            item {
                Text("Security & Data Management", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MoreMenuRow(
                            title = "Security & PIN Lock",
                            subtitle = "PIN protection, balance masking & privacy",
                            icon = Icons.Default.Security,
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToSecurity,
                            testTag = "more_menu_security"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        MoreMenuRow(
                            title = "Backup, Restore & Export",
                            subtitle = "Full app, profile & transactions backups with file restore",
                            icon = Icons.Default.Storage,
                            iconTint = Color(0xFF06B6D4),
                            onClick = onNavigateToBackup,
                            testTag = "more_menu_backup"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        MoreMenuRow(
                            title = "General Preferences",
                            subtitle = "Base currency, date format & time style",
                            icon = Icons.Default.Settings,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onNavigateToSettings,
                            testTag = "more_menu_settings"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        MoreMenuRow(
                            title = "Reset All Recorded Data",
                            subtitle = "Remove dummy contents and start fresh",
                            icon = Icons.Default.RestartAlt,
                            iconTint = FinRose,
                            onClick = { showResetDialog = true },
                            testTag = "more_menu_reset"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Recorded Data?") },
            text = {
                Text(
                    "This will delete all transactions, cards, accounts, bills, budgets, and wealth items to remove all dummy/sample contents and give you a fresh, clean slate.\n\nYour categories and base preferences will be preserved."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllUserData()
                        showResetDialog = false
                        Toast.makeText(context, "All dummy/sample data reset successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinRose),
                    modifier = Modifier.testTag("confirm_reset_data_btn")
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MoreMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

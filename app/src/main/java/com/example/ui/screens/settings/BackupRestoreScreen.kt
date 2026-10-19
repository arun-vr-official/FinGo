package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BackupInspection
import com.example.data.repository.BackupScope
import com.example.ui.theme.FinBlue
import com.example.ui.theme.FinEmerald
import com.example.ui.theme.FinRose
import com.example.ui.viewmodel.FinanceViewModel
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Export state
    var selectedExportScope by remember { mutableStateOf(BackupScope.FULL) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var pendingFileContent by remember { mutableStateOf("") }
    var pendingFileName by remember { mutableStateOf("fintrack_backup.json") }
    var pendingMimeType by remember { mutableStateOf("application/json") }

    // Document creation launcher (saves directly to Google Drive, SD Card, or Device Storage via SAF)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingFileContent.isNotEmpty()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(pendingFileContent.toByteArray(Charsets.UTF_8))
                    os.flush()
                }
                Toast.makeText(context, "Saved to Google Drive / Storage successfully!", Toast.LENGTH_LONG).show()
                exportStatus = "File '$pendingFileName' successfully saved to Google Drive / Storage."
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper to attach or share backup file to Email or Google Drive via FileProvider
    fun shareOrAttachFile(
        content: String,
        fileName: String,
        mimeType: String,
        isEmailTarget: Boolean = false
    ) {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(content, Charsets.UTF_8)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "FinTrack Backup - $fileName")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is your FinTrack backup file ($fileName).\n\n" +
                    "• Format: ${if (fileName.endsWith(".json")) "JSON (Error-proof database snapshot)" else "CSV (Spreadsheet records)"}\n" +
                    "• Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n" +
                    "To restore this backup: Open FinTrack -> Profile & Settings -> Backup, Restore & Reset -> Restore / Import Backup."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val title = if (isEmailTarget) "Attach Backup to Email (Gmail, Outlook, etc.)" else "Attach to Email, Google Drive, or Cloud Apps"
            val chooser = Intent.createChooser(intent, title)
            context.startActivity(chooser)
            exportStatus = "Backup file attached and ready to send via Email or Google Drive."
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to prepare file attachment: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Import state
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var pendingInspection by remember { mutableStateOf<BackupInspection?>(null) }
    var selectedRestoreScope by remember { mutableStateOf(BackupScope.FULL) }
    var showRestoreScopeDialog by remember { mutableStateOf(false) }

    // Reset state
    var showResetDialog by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                reader.close()
                inputStream?.close()

                val inspection = viewModel.inspectBackup(jsonString)
                if (inspection.isValid) {
                    pendingImportJson = jsonString
                    pendingInspection = inspection
                    selectedRestoreScope = when (inspection.backupType) {
                        "PROFILE" -> BackupScope.PROFILE
                        "TRANSACTIONS" -> BackupScope.TRANSACTIONS
                        else -> BackupScope.FULL
                    }
                    showRestoreScopeDialog = true
                } else {
                    Toast.makeText(context, "Invalid backup file: ${inspection.errorMessage ?: "Malformed JSON"}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup, Restore & Reset", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // EDUCATIONAL CARD: JSON vs CSV Error-Proof Guide
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FinEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = FinEmerald, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Which is Error-Proof: JSON vs CSV?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // JSON explanation
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FinEmerald,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "JSON (Recommended & 100% Error-Proof)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FinEmerald
                            )
                            Text(
                                "Preserves your complete database (Accounts, Balances, Cards, Transactions, Budgets, Recurring Bills, Wealth Commitments, Categories & Profile settings) with exact data types, precision numbers, and timestamps. It never breaks or misaligns columns even if your notes or payees contain commas (,), quotes (\"), newlines, or emojis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // CSV explanation
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = null,
                            tint = FinBlue,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "CSV (Spreadsheet Analysis Only)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FinBlue
                            )
                            Text(
                                "Best for opening transaction logs in Microsoft Excel or Google Sheets. It exports a flat list of transaction records, but cannot restore accounts, bills, budgets, or app settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Status Banner if an action just occurred
            if (exportStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FinEmerald.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FinEmerald.copy(alpha = 0.5f)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FinEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            exportStatus ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 1. BACKUP & EXPORT SECTION (JSON ERROR-PROOF)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FinBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = FinBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Backup / Export Data (JSON)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Error-proof full snapshot for Email, Google Drive & Devices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select what to backup:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 1: Full App
                    BackupScopeOptionRow(
                        title = "Fully (Complete App)",
                        subtitle = "User details, selected theme, cards, bills, wealth, budgets & all transactions",
                        selected = selectedExportScope == BackupScope.FULL,
                        onClick = { selectedExportScope = BackupScope.FULL }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 2: Profile & Cards
                    BackupScopeOptionRow(
                        title = "Profile & Cards Only",
                        subtitle = "User name, theme, currency & accounts / credit cards",
                        selected = selectedExportScope == BackupScope.PROFILE,
                        onClick = { selectedExportScope = BackupScope.PROFILE }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 3: Transactions
                    BackupScopeOptionRow(
                        title = "Transactions Only",
                        subtitle = "All historical income, expense and transfer transaction logs",
                        selected = selectedExportScope == BackupScope.TRANSACTIONS,
                        onClick = { selectedExportScope = BackupScope.TRANSACTIONS }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Choose how to save or attach backup:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Action 1: Attach to Email
                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportBackupJson(selectedExportScope)
                                val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                val fileName = "FinTrack_Backup_${selectedExportScope.name}_$dateTag.json"
                                shareOrAttachFile(json, fileName, "application/json", isEmailTarget = true)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("email_backup_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = FinEmerald)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Backup to Email", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Action 2 & 3: Save to Drive/Files & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.exportBackupJson(selectedExportScope)
                                    val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "FinTrack_Backup_${selectedExportScope.name}_$dateTag.json"
                                    pendingFileContent = json
                                    pendingFileName = fileName
                                    pendingMimeType = "application/json"
                                    createDocumentLauncher.launch(fileName)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("save_drive_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = FinBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Drive", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.exportBackupJson(selectedExportScope)
                                    val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "FinTrack_Backup_${selectedExportScope.name}_$dateTag.json"
                                    shareOrAttachFile(json, fileName, "application/json", isEmailTarget = false)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("share_backup_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share File", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.exportBackupJson(selectedExportScope)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FinTrack Backup JSON", json)
                                    clipboard.setPrimaryClip(clip)
                                    exportStatus = "Backup JSON (${selectedExportScope.name}) copied to clipboard!"
                                    Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("copy_json_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", fontSize = 12.sp)
                        }
                    }
                }
            }

            // SPREADSHEET EXPORT SECTION (CSV)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Spreadsheet Export (CSV)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Open transaction logs in Excel or Google Sheets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val csv = viewModel.exportCsv()
                                    val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "FinTrack_Transactions_$dateTag.csv"
                                    shareOrAttachFile(csv, fileName, "text/csv", isEmailTarget = true)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("email_csv_btn")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val csv = viewModel.exportCsv()
                                    val dateTag = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                    val fileName = "FinTrack_Transactions_$dateTag.csv"
                                    pendingFileContent = csv
                                    pendingFileName = fileName
                                    pendingMimeType = "text/csv"
                                    createDocumentLauncher.launch(fileName)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("drive_csv_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val csv = viewModel.exportCsv()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FinTrack CSV Export", csv)
                                    clipboard.setPrimaryClip(clip)
                                    exportStatus = "CSV spreadsheet data copied to clipboard!"
                                    Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("copy_csv_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. IMPORT & RESTORE SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FinEmerald.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = FinEmerald, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Restore / Import Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Restore from file or clipboard into this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Upload a backup file from another device. You can choose whether to restore fully, profile & cards only, or transactions only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("upload_backup_file_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = FinEmerald)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload File", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    val inspection = viewModel.inspectBackup(clip)
                                    if (inspection.isValid) {
                                        pendingImportJson = clip
                                        pendingInspection = inspection
                                        selectedRestoreScope = when (inspection.backupType) {
                                            "PROFILE" -> BackupScope.PROFILE
                                            "TRANSACTIONS" -> BackupScope.TRANSACTIONS
                                            else -> BackupScope.FULL
                                        }
                                        showRestoreScopeDialog = true
                                    } else {
                                        Toast.makeText(context, "Clipboard does not contain a valid backup JSON.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("paste_backup_json_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste JSON", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Status message card if any
            if (exportStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exportStatus ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 3. RESET & CLEAR DUMMY DATA SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FinRose.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = FinRose, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reset Data (Clear Sample Data)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Fresh start: remove sample & dummy contents", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Remove all recorded dummy transactions, cards, bills, and wealth items. Useful after first installing to remove demo data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinRose),
                        modifier = Modifier.fillMaxWidth().testTag("backup_screen_reset_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Recorded Data", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive Scope Selection Dialog on Import
    if (showRestoreScopeDialog && pendingInspection != null && pendingImportJson != null) {
        val insp = pendingInspection!!
        AlertDialog(
            onDismissRequest = { showRestoreScopeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = FinEmerald, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Restore Scope")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "A backup file was detected. What would you like to restore?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inspection summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("File Contents:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            val details = mutableListOf<String>()
                            if (insp.hasUserProfile) details.add("Profile (${insp.userName ?: "User"})")
                            if (insp.accountsCount > 0) details.add("${insp.accountsCount} Cards/Accounts")
                            if (insp.transactionsCount > 0) details.add("${insp.transactionsCount} Transactions")
                            if (insp.billsCount > 0) details.add("${insp.billsCount} Bills")
                            Text(
                                text = if (details.isNotEmpty()) details.joinToString(" • ") else "Custom snapshot",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Fully
                    RestoreScopeOption(
                        title = "Fully (Entire App)",
                        subtitle = "User details, selected theme, cards, bills and entire things",
                        selected = selectedRestoreScope == BackupScope.FULL,
                        onClick = { selectedRestoreScope = BackupScope.FULL }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Profile
                    RestoreScopeOption(
                        title = "Profile Only",
                        subtitle = "User details and cards/accounts",
                        selected = selectedRestoreScope == BackupScope.PROFILE,
                        onClick = { selectedRestoreScope = BackupScope.PROFILE }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Transactions
                    RestoreScopeOption(
                        title = "Transactions Only",
                        subtitle = "Historical transaction records only",
                        selected = selectedRestoreScope == BackupScope.TRANSACTIONS,
                        onClick = { selectedRestoreScope = BackupScope.TRANSACTIONS }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.importBackupJson(pendingImportJson!!, selectedRestoreScope)
                            showRestoreScopeDialog = false
                            if (res.success) {
                                exportStatus = res.message
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinEmerald),
                    modifier = Modifier.testTag("confirm_restore_btn")
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreScopeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
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
                        exportStatus = "All recorded sample data has been reset."
                        Toast.makeText(context, "All data reset successfully!", Toast.LENGTH_SHORT).show()
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
private fun BackupScopeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = CardDefaults.outlinedCardBorder(enabled = selected)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun RestoreScopeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) FinEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(enabled = selected)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) FinEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

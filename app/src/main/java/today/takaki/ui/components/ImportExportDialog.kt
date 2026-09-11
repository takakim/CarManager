package today.takaki.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import today.takaki.data.model.FuelLog
import today.takaki.data.model.VehicleProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ImportExportTab {
  EXPORT,
  IMPORT
}

enum class ExportFormat {
  CSV,
  JSON
}

@Composable
fun ImportExportDialog(
  vehicle: VehicleProfile,
  logs: List<FuelLog>,
  onDismiss: () -> Unit,
  onGenerateCsv: () -> String,
  onGenerateJson: () -> String,
  onGenerateAllJson: () -> String,
  onImportCsv: (csvText: String, replace: Boolean) -> Pair<Boolean, String>,
  onImportJson: (jsonText: String, replace: Boolean) -> Pair<Boolean, String>
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var selectedTab by remember { mutableStateOf(ImportExportTab.EXPORT) }

  // Export State
  var exportFormat by remember { mutableStateOf(ExportFormat.CSV) }
  var exportAllGarage by remember { mutableStateOf(false) }
  var exportPreviewText by remember { mutableStateOf("") }
  var isCopied by remember { mutableStateOf(false) }

  // Refresh export preview when settings change
  LaunchedEffect(exportFormat, exportAllGarage, vehicle, logs) {
    exportPreviewText = when {
      exportFormat == ExportFormat.CSV -> onGenerateCsv()
      exportAllGarage -> onGenerateAllJson()
      else -> onGenerateJson()
    }
    isCopied = false
  }

  // Import State
  var importText by remember { mutableStateOf("") }
  var replaceExisting by remember { mutableStateOf(false) }
  var importResultMsg by remember { mutableStateOf<String?>(null) }
  var isImportSuccess by remember { mutableStateOf(false) }

  // File picker launcher for import
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch(Dispatchers.IO) {
        try {
          context.contentResolver.openInputStream(uri)?.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream))
            val content = reader.readText()
            withContext(Dispatchers.Main) {
              importText = content
              importResultMsg = "File loaded into preview (${content.length} characters). Ready to import."
              isImportSuccess = true
            }
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            importResultMsg = "Failed to read file: ${e.localizedMessage}"
            isImportSuccess = false
          }
        }
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("import_export_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (selectedTab == ImportExportTab.EXPORT) Icons.Default.FileDownload else Icons.Default.FileUpload,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (selectedTab == ImportExportTab.EXPORT) "Export Refuel Data" else "Import Refuel Data",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Tab Navigation
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent
          ) {
            Tab(
              selected = selectedTab == ImportExportTab.EXPORT,
              onClick = { selectedTab = ImportExportTab.EXPORT },
              text = { Text("Export Data", fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = selectedTab == ImportExportTab.IMPORT,
              onClick = { selectedTab = ImportExportTab.IMPORT },
              text = { Text("Import Data", fontWeight = FontWeight.Bold) }
            )
          }
        }

        if (selectedTab == ImportExportTab.EXPORT) {
          // EXPORT VIEW
          Text(
            text = "Export Format & Scope",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = exportFormat == ExportFormat.CSV,
              onClick = { exportFormat = ExportFormat.CSV },
              label = { Text("CSV (Spreadsheet)") }
            )
            FilterChip(
              selected = exportFormat == ExportFormat.JSON,
              onClick = { exportFormat = ExportFormat.JSON },
              label = { Text("JSON Backup") }
            )
          }

          if (exportFormat == ExportFormat.JSON) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = !exportAllGarage,
                onClick = { exportAllGarage = false },
                label = { Text("Current Car Only") }
              )
              FilterChip(
                selected = exportAllGarage,
                onClick = { exportAllGarage = true },
                label = { Text("All Garage Cars") }
              )
            }
          }

          // Summary Info Card
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (exportFormat == ExportFormat.CSV) {
                  "Exports ${logs.size} refuel records for ${vehicle.name} formatted with headers for Excel, Numbers, and Google Sheets."
                } else {
                  if (exportAllGarage) "Full backup including all vehicle profiles and refuel histories in JSON."
                  else "Backup of ${vehicle.name} profile and its ${logs.size} refuel records."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Preview Area
          Text(text = "Data Preview", style = MaterialTheme.typography.labelMedium)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 140.dp)
          ) {
            Text(
              text = exportPreviewText.take(600) + if (exportPreviewText.length > 600) "\n... [${exportPreviewText.length} bytes total]" else "",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              modifier = Modifier.padding(10.dp)
            )
          }

          // Export Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, exportPreviewText)
                  putExtra(
                    Intent.EXTRA_SUBJECT,
                    if (exportFormat == ExportFormat.CSV) "FuelTracker_${vehicle.name.replace(" ", "_")}.csv"
                    else "FuelTracker_Backup.json"
                  )
                  type = if (exportFormat == ExportFormat.CSV) "text/csv" else "application/json"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Fuel Data")
                context.startActivity(shareIntent)
              },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share / Save File")
            }

            OutlinedButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("FuelTracker Export", exportPreviewText)
                clipboard.setPrimaryClip(clip)
                isCopied = true
                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.weight(0.9f)
            ) {
              Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isCopied) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isCopied) "Copied!" else "Copy Text")
            }
          }

        } else {
          // IMPORT VIEW
          Text(
            text = "Select Import Source",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                filePickerLauncher.launch(
                  arrayOf(
                    "text/*",
                    "application/json",
                    "text/csv",
                    "text/comma-separated-values",
                    "*/*"
                  )
                )
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pick File (.csv, .json)")
            }

            OutlinedButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                  val text = clip.getItemAt(0).text?.toString() ?: ""
                  if (text.isNotBlank()) {
                    importText = text
                    importResultMsg = "Pasted ${text.length} characters from clipboard."
                    isImportSuccess = true
                  } else {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Paste Clipboard")
            }
          }

          // Import Content Box
          OutlinedTextField(
            value = importText,
            onValueChange = {
              importText = it
              importResultMsg = null
            },
            label = { Text("CSV rows or JSON content") },
            placeholder = { Text("Paste CSV or JSON here, or pick a file above...") },
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 110.dp, max = 160.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          )

          // Replace Switch
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Replace Existing Records", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = if (replaceExisting) "All existing records will be cleared" else "Merge with existing records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = replaceExisting,
                onCheckedChange = { replaceExisting = it }
              )
            }
          }

          // Feedback Message
          if (importResultMsg != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isImportSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = importResultMsg!!,
                style = MaterialTheme.typography.bodySmall,
                color = if (isImportSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          // Process Import Button
          Button(
            onClick = {
              if (importText.isBlank()) {
                importResultMsg = "Please paste text or open a file first."
                isImportSuccess = false
                return@Button
              }

              val trimmed = importText.trim()
              if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                // Parse JSON
                val (success, msg) = onImportJson(trimmed, replaceExisting)
                isImportSuccess = success
                importResultMsg = msg
              } else {
                // Parse CSV
                val (success, msg) = onImportCsv(trimmed, replaceExisting)
                isImportSuccess = success
                importResultMsg = msg
              }
            },
            enabled = importText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Records Now")
          }
        }
      }
    },
    confirmButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}

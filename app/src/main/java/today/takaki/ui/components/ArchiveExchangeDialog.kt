package today.takaki.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import today.takaki.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import today.takaki.data.model.DistanceUnit
import today.takaki.data.model.EconomyUnit
import today.takaki.data.model.FuelType
import today.takaki.data.model.VehicleProfile
import today.takaki.data.model.VolumeUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ArchiveExchangeMode {
  EXCHANGE,
  ARCHIVE_ONLY
}

enum class ArchiveReasonPreset(val labelRes: Int) {
  TRADED_IN(R.string.reason_traded_in),
  SOLD_PRIVATE(R.string.reason_sold_private),
  LEASE_ENDED(R.string.reason_lease_ended),
  FLEET_CHANGE(R.string.reason_fleet_change),
  SCRAPPED(R.string.reason_scrapped),
  OTHER(R.string.reason_other)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveExchangeDialog(
  currentVehicle: VehicleProfile,
  latestOdometer: Double,
  onDismiss: () -> Unit,
  onConfirm: (
    archiveDateMillis: Long,
    archiveOdometer: Double,
    reason: String,
    createReplacement: Boolean,
    replacementVehicle: VehicleProfile?
  ) -> Unit
) {
  val context = LocalContext.current
  var mode by remember { mutableStateOf(ArchiveExchangeMode.EXCHANGE) }

  // Archive Fields
  var archiveDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
  var archiveOdoStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%.0f", latestOdometer)) }
  var selectedReason by remember { mutableStateOf(ArchiveReasonPreset.TRADED_IN) }
  var reasonExpanded by remember { mutableStateOf(false) }

  // Replacement Vehicle Fields (for Exchange mode)
  var newName by remember { mutableStateOf("Next Car") }
  var newMakeModel by remember { mutableStateOf("") }
  var newYearStr by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
  var newFuelType by remember { mutableStateOf(FuelType.GASOLINE) }
  var newFuelTypeExpanded by remember { mutableStateOf(false) }
  var newInitialOdoStr by remember { mutableStateOf("0") }
  var newTankCapacityStr by remember { mutableStateOf("50") }

  val calendar = Calendar.getInstance().apply { timeInMillis = archiveDateMillis }
  val datePickerDialog = DatePickerDialog(
    context,
    { _, y, m, d ->
      calendar.set(y, m, d)
      archiveDateMillis = calendar.timeInMillis
    },
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH),
    calendar.get(Calendar.DAY_OF_MONTH)
  )

  val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("archive_exchange_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (mode == ArchiveExchangeMode.EXCHANGE) Icons.Default.SwapHoriz else Icons.Default.Archive,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (mode == ArchiveExchangeMode.EXCHANGE) stringResource(R.string.exchange_vehicle_btn) else stringResource(R.string.archive_exchange_title),
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
        // Mode Selector TabRow
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          TabRow(
            selectedTabIndex = mode.ordinal,
            containerColor = Color.Transparent
          ) {
            Tab(
              selected = mode == ArchiveExchangeMode.EXCHANGE,
              onClick = {
                mode = ArchiveExchangeMode.EXCHANGE
                selectedReason = ArchiveReasonPreset.TRADED_IN
              },
              text = { Text(stringResource(R.string.archive_exchange_new_tab), fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = mode == ArchiveExchangeMode.ARCHIVE_ONLY,
              onClick = {
                mode = ArchiveExchangeMode.ARCHIVE_ONLY
                selectedReason = ArchiveReasonPreset.SOLD_PRIVATE
              },
              text = { Text(stringResource(R.string.archive_only_tab), fontWeight = FontWeight.Bold) }
            )
          }
        }

        // Current Car Info Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "${currentVehicle.name} (${currentVehicle.makeModel})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
              Text(
                text = stringResource(
                  R.string.archive_starting_odo_note,
                  String.format(Locale.getDefault(), "%,.0f", currentVehicle.initialOdometer),
                  currentVehicle.distanceUnit.symbol
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Section: Archive Details
        Text(
          text = stringResource(R.string.ownership_end_details),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Date Picker
        Surface(
          onClick = { datePickerDialog.show() },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(text = stringResource(R.string.exchange_archive_date), style = MaterialTheme.typography.labelMedium)
              Text(text = dateFormat.format(Date(archiveDateMillis)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
          }
        }

        // Final Odometer
        OutlinedTextField(
          value = archiveOdoStr,
          onValueChange = { archiveOdoStr = it },
          label = { Text("${stringResource(R.string.final_odometer)} (${currentVehicle.distanceUnit.symbol})") },
          placeholder = { Text("e.g. 65000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Reason Dropdown
        ExposedDropdownMenuBox(
          expanded = reasonExpanded,
          onExpandedChange = { reasonExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = stringResource(selectedReason.labelRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.reason_for_archive)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = reasonExpanded,
            onDismissRequest = { reasonExpanded = false }
          ) {
            ArchiveReasonPreset.values().forEach { reason ->
              DropdownMenuItem(
                text = { Text(stringResource(reason.labelRes)) },
                onClick = {
                  selectedReason = reason
                  reasonExpanded = false
                }
              )
            }
          }
        }

        // If Exchange Mode: Section for New Replacement Car
        if (mode == ArchiveExchangeMode.EXCHANGE) {
          HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

          Text(
            text = stringResource(R.string.new_replacement_car_details),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text(stringResource(R.string.car_nickname)) },
            placeholder = { Text("e.g. Daily Driver") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newMakeModel,
            onValueChange = { newMakeModel = it },
            label = { Text(stringResource(R.string.make_and_model)) },
            placeholder = { Text("e.g. Toyota RAV4 or Tesla Model 3") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = newYearStr,
              onValueChange = { newYearStr = it },
              label = { Text(stringResource(R.string.vehicle_year)) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            // Fuel Type Dropdown
            ExposedDropdownMenuBox(
              expanded = newFuelTypeExpanded,
              onExpandedChange = { newFuelTypeExpanded = it },
              modifier = Modifier.weight(1.3f)
            ) {
              OutlinedTextField(
                value = stringResource(newFuelType.nameRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.fuel_type_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = newFuelTypeExpanded) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )
              ExposedDropdownMenu(
                expanded = newFuelTypeExpanded,
                onDismissRequest = { newFuelTypeExpanded = false }
              ) {
                FuelType.values().forEach { ft ->
                  DropdownMenuItem(
                    text = { Text(stringResource(ft.nameRes)) },
                    onClick = {
                      newFuelType = ft
                      newFuelTypeExpanded = false
                    }
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = newInitialOdoStr,
              onValueChange = { newInitialOdoStr = it },
              label = { Text("${stringResource(R.string.initial_odometer_label)} (${currentVehicle.distanceUnit.symbol})") },
              placeholder = { Text("0") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = newTankCapacityStr,
              onValueChange = { newTankCapacityStr = it },
              label = {
                Text(
                  if (newFuelType == FuelType.ELECTRIC) stringResource(R.string.battery_capacity_kwh)
                  else "${stringResource(R.string.volume_unit_label)} (${currentVehicle.volumeUnit.symbol})"
                )
              },
              placeholder = { Text("50") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalOdo = archiveOdoStr.toDoubleOrNull() ?: latestOdometer
          val isExchange = (mode == ArchiveExchangeMode.EXCHANGE)

          val replacement = if (isExchange) {
            val yr = newYearStr.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val initOdo = newInitialOdoStr.toDoubleOrNull() ?: 0.0
            val tankCap = newTankCapacityStr.toDoubleOrNull() ?: 50.0
            val volUnit = if (newFuelType == FuelType.ELECTRIC) VolumeUnit.KWH else currentVehicle.volumeUnit
            val econUnit = EconomyUnit.getDefault(newFuelType, currentVehicle.distanceUnit, volUnit)

            VehicleProfile(
              id = 0,
              name = if (newName.isNotBlank()) newName.trim() else "New Car",
              makeModel = if (newMakeModel.isNotBlank()) newMakeModel.trim() else "Car",
              year = yr,
              fuelType = newFuelType,
              purchaseDateMillis = archiveDateMillis,
              purchasePrice = 0.0,
              initialOdometer = initOdo,
              currencySymbol = currentVehicle.currencySymbol,
              distanceUnit = currentVehicle.distanceUnit,
              volumeUnit = volUnit,
              economyUnit = econUnit,
              tankCapacity = tankCap,
              isArchived = false
            )
          } else {
            null
          }

          val reasonStr = context.getString(selectedReason.labelRes)
          onConfirm(
            archiveDateMillis,
            finalOdo,
            reasonStr,
            isExchange,
            replacement
          )
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Text(if (mode == ArchiveExchangeMode.EXCHANGE) stringResource(R.string.exchange_vehicle_btn) else stringResource(R.string.archive_vehicle_btn))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(stringResource(R.string.action_cancel))
      }
    }
  )
}

package today.takaki.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import today.takaki.R
import today.takaki.data.model.DistanceUnit
import today.takaki.data.model.EconomyUnit
import today.takaki.data.model.FuelType
import today.takaki.data.model.VehicleProfile
import today.takaki.data.model.VolumeUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSettingsDialog(
  vehicle: VehicleProfile,
  onDismiss: () -> Unit,
  onSave: (VehicleProfile) -> Unit
) {
  val context = LocalContext.current

  var name by remember { mutableStateOf(vehicle.name) }
  var makeModel by remember { mutableStateOf(vehicle.makeModel) }
  var yearStr by remember { mutableStateOf(vehicle.year.toString()) }
  var selectedFuelType by remember { mutableStateOf(vehicle.fuelType) }
  var purchaseDateMillis by remember { mutableStateOf(vehicle.purchaseDateMillis) }
  var initialOdometerStr by remember { mutableStateOf(vehicle.initialOdometer.toString()) }
  var currencySymbol by remember { mutableStateOf(vehicle.currencySymbol) }
  var distanceUnit by remember { mutableStateOf(vehicle.distanceUnit) }
  var volumeUnit by remember { mutableStateOf(vehicle.volumeUnit) }
  var economyUnit by remember { mutableStateOf(vehicle.economyUnit) }
  var tankCapacityStr by remember { mutableStateOf(vehicle.tankCapacity.toString()) }

  var fuelTypeExpanded by remember { mutableStateOf(false) }
  var distExpanded by remember { mutableStateOf(false) }
  var volExpanded by remember { mutableStateOf(false) }
  var economyExpanded by remember { mutableStateOf(false) }

  val calendar = Calendar.getInstance().apply { timeInMillis = purchaseDateMillis }
  val datePickerDialog = DatePickerDialog(
    context,
    { _, year, month, dayOfMonth ->
      val newCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
      }
      purchaseDateMillis = newCal.timeInMillis
    },
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH),
    calendar.get(Calendar.DAY_OF_MONTH)
  )

  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("vehicle_settings_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = stringResource(R.string.vehicle_settings_title),
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
        // Section 1: Vehicle Information
        Text(
          text = stringResource(R.string.vehicle_profile_title),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(stringResource(R.string.vehicle_name)) },
          placeholder = { Text("e.g. My Civic, Tesla Model Y") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = makeModel,
            onValueChange = { makeModel = it },
            label = { Text(stringResource(R.string.make_and_model)) },
            placeholder = { Text("Toyota RAV4") },
            singleLine = true,
            modifier = Modifier.weight(1.3f)
          )

          OutlinedTextField(
            value = yearStr,
            onValueChange = { yearStr = it },
            label = { Text(stringResource(R.string.vehicle_year)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(0.7f)
          )
        }

        // Fuel / Energy Type Selector
        ExposedDropdownMenuBox(
          expanded = fuelTypeExpanded,
          onExpandedChange = { fuelTypeExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = stringResource(selectedFuelType.nameRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.fuel_type_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelTypeExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = fuelTypeExpanded,
            onDismissRequest = { fuelTypeExpanded = false }
          ) {
            FuelType.values().forEach { type ->
              DropdownMenuItem(
                text = { Text(stringResource(type.nameRes)) },
                onClick = {
                  selectedFuelType = type
                  if (type == FuelType.ELECTRIC) {
                    volumeUnit = VolumeUnit.KWH
                    economyUnit = if (distanceUnit == DistanceUnit.MILES) EconomyUnit.MI_PER_KWH else EconomyUnit.KWH_PER_100KM
                  } else if (volumeUnit == VolumeUnit.KWH) {
                    volumeUnit = VolumeUnit.LITERS
                    economyUnit = if (distanceUnit == DistanceUnit.MILES) EconomyUnit.MPG_US else EconomyUnit.L_PER_100KM
                  } else {
                    economyUnit = EconomyUnit.getDefault(type, distanceUnit, volumeUnit)
                  }
                  fuelTypeExpanded = false
                }
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Section 2: Ownership History
        Text(
          text = stringResource(R.string.ownership_history_title),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Purchase Date Picker
        Surface(
          onClick = { datePickerDialog.show() },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = stringResource(R.string.purchase_date_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = dateFormat.format(Date(purchaseDateMillis)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            IconButton(onClick = { datePickerDialog.show() }) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = stringResource(R.string.select_purchase_date_cd),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Starting Odometer
        OutlinedTextField(
          value = initialOdometerStr,
          onValueChange = { initialOdometerStr = it },
          label = { Text("${stringResource(R.string.initial_odometer_label)} (${distanceUnit.symbol})") },
          placeholder = { Text(stringResource(R.string.initial_odometer_placeholder)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Section 3: Currency, Units & Fuel Economy
        Text(
          text = stringResource(R.string.settings_units),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Currency & Tank Capacity (2-column row with ample space)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = currencySymbol,
            onValueChange = { currencySymbol = it },
            label = { Text(stringResource(R.string.currency_label)) },
            placeholder = { Text("$, €, £") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = tankCapacityStr,
            onValueChange = { tankCapacityStr = it },
            label = { Text("${stringResource(R.string.volume_unit_label)} (${volumeUnit.symbol})") },
            placeholder = { Text("50") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        // Distance Unit (Full Width Dropdown)
        ExposedDropdownMenuBox(
          expanded = distExpanded,
          onExpandedChange = { distExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = stringResource(distanceUnit.nameRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.distance_unit_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = distExpanded,
            onDismissRequest = { distExpanded = false }
          ) {
            DistanceUnit.values().forEach { d ->
              DropdownMenuItem(
                text = { Text(stringResource(d.nameRes)) },
                onClick = {
                  distanceUnit = d
                  economyUnit = EconomyUnit.getDefault(selectedFuelType, d, volumeUnit)
                  distExpanded = false
                }
              )
            }
          }
        }

        // Volume Unit (Full Width Dropdown)
        ExposedDropdownMenuBox(
          expanded = volExpanded,
          onExpandedChange = { volExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = stringResource(volumeUnit.nameRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.volume_unit_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = volExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = volExpanded,
            onDismissRequest = { volExpanded = false }
          ) {
            VolumeUnit.values().forEach { v ->
              DropdownMenuItem(
                text = { Text(stringResource(v.nameRes)) },
                onClick = {
                  volumeUnit = v
                  economyUnit = EconomyUnit.getDefault(selectedFuelType, distanceUnit, v)
                  volExpanded = false
                }
              )
            }
          }
        }

        // Fuel Economy Unit (Full Width Dropdown)
        ExposedDropdownMenuBox(
          expanded = economyExpanded,
          onExpandedChange = { economyExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = stringResource(economyUnit.nameRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.economy_unit_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = economyExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = economyExpanded,
            onDismissRequest = { economyExpanded = false }
          ) {
            EconomyUnit.values().forEach { unit ->
              DropdownMenuItem(
                text = {
                  Column {
                    Text(stringResource(unit.nameRes), fontWeight = FontWeight.SemiBold)
                    Text(unit.symbol, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                },
                onClick = {
                  economyUnit = unit
                  economyExpanded = false
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val year = yearStr.toIntOrNull() ?: 2023
          val initOdo = initialOdometerStr.toDoubleOrNull() ?: 0.0
          val cap = tankCapacityStr.toDoubleOrNull() ?: 50.0

          val updated = vehicle.copy(
            name = if (name.isNotBlank()) name.trim() else "My Car",
            makeModel = makeModel.trim(),
            year = year,
            fuelType = selectedFuelType,
            purchaseDateMillis = purchaseDateMillis,
            initialOdometer = initOdo,
            currencySymbol = if (currencySymbol.isNotBlank()) currencySymbol.trim() else "$",
            distanceUnit = distanceUnit,
            volumeUnit = volumeUnit,
            economyUnit = economyUnit,
            tankCapacity = cap
          )
          onSave(updated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(stringResource(R.string.action_cancel))
      }
    }
  )
}


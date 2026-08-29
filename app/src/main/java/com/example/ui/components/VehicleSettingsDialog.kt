package com.example.ui.components

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
import com.example.data.model.DistanceUnit
import com.example.data.model.EconomyUnit
import com.example.data.model.FuelType
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit
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
          text = "Vehicle Setup & Units",
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
          text = "Vehicle Information",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Vehicle Nickname") },
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
            label = { Text("Make & Model") },
            placeholder = { Text("Toyota RAV4") },
            singleLine = true,
            modifier = Modifier.weight(1.3f)
          )

          OutlinedTextField(
            value = yearStr,
            onValueChange = { yearStr = it },
            label = { Text("Year") },
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
            value = selectedFuelType.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fuel / Energy Type") },
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
                text = { Text(type.displayName) },
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
          text = "Ownership & Starting History",
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
                text = "Purchase Date (Since I bought it)",
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
                contentDescription = "Select Purchase Date",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Starting Odometer
        OutlinedTextField(
          value = initialOdometerStr,
          onValueChange = { initialOdometerStr = it },
          label = { Text("Starting Odometer (${distanceUnit.symbol})") },
          placeholder = { Text("e.g. 0 or 15000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Section 3: Currency, Units & Fuel Economy
        Text(
          text = "Currency & Display Units",
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
            label = { Text("Currency") },
            placeholder = { Text("$, €, £") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = tankCapacityStr,
            onValueChange = { tankCapacityStr = it },
            label = { Text("Tank / Battery (${volumeUnit.symbol})") },
            placeholder = { Text("50") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        // Distance & Volume Units (2-column dropdowns)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Distance Unit
          ExposedDropdownMenuBox(
            expanded = distExpanded,
            onExpandedChange = { distExpanded = it },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = "${distanceUnit.label} (${distanceUnit.symbol})",
              onValueChange = {},
              readOnly = true,
              label = { Text("Distance") },
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
                  text = { Text("${d.label} (${d.symbol})") },
                  onClick = {
                    distanceUnit = d
                    economyUnit = EconomyUnit.getDefault(selectedFuelType, d, volumeUnit)
                    distExpanded = false
                  }
                )
              }
            }
          }

          // Volume Unit
          ExposedDropdownMenuBox(
            expanded = volExpanded,
            onExpandedChange = { volExpanded = it },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = "${volumeUnit.label} (${volumeUnit.symbol})",
              onValueChange = {},
              readOnly = true,
              label = { Text("Volume") },
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
                  text = { Text("${v.label} (${v.symbol})") },
                  onClick = {
                    volumeUnit = v
                    economyUnit = EconomyUnit.getDefault(selectedFuelType, distanceUnit, v)
                    volExpanded = false
                  }
                )
              }
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
            value = economyUnit.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fuel / Energy Economy Unit") },
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
                    Text(unit.label, fontWeight = FontWeight.SemiBold)
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
        Text("Save Settings", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}


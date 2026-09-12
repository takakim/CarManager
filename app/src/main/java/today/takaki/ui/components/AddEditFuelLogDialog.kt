package today.takaki.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import today.takaki.data.model.FuelLog
import today.takaki.data.model.FuelType
import today.takaki.data.model.VehicleProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddEditFuelLogDialog(
  vehicle: VehicleProfile,
  initialLog: FuelLog? = null,
  lastOdometer: Double = 0.0,
  lastUnitPrice: Double = 0.0,
  onDismiss: () -> Unit,
  onSave: (
    odometer: Double,
    amount: Double,
    unitPrice: Double,
    totalCost: Double,
    dateMillis: Long,
    isFullTank: Boolean,
    isMissedPrevious: Boolean,
    stationName: String,
    fuelGrade: String,
    notes: String
  ) -> Unit
) {
  val context = LocalContext.current
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol
  val distUnit = vehicle.distanceUnit.symbol

  val defaultOdo = if (initialLog != null) {
    initialLog.odometer.toString()
  } else if (lastOdometer > 0) {
    (lastOdometer + 450.0).toString()
  } else {
    (vehicle.initialOdometer + 450.0).toString()
  }

  val defaultUnitPrice = if (initialLog != null) {
    initialLog.unitPrice.toString()
  } else if (lastUnitPrice > 0) {
    String.format(Locale.US, "%.3f", lastUnitPrice)
  } else {
    if (isElectric) "0.22" else "1.49"
  }

  var odometerInput by remember { mutableStateOf(defaultOdo) }
  var amountInput by remember { mutableStateOf(initialLog?.amount?.toString() ?: "40.0") }
  var unitPriceInput by remember { mutableStateOf(defaultUnitPrice) }
  var totalCostInput by remember {
    mutableStateOf(
      if (initialLog != null) initialLog.totalCost.toString()
      else {
        val a = 40.0
        val p = defaultUnitPrice.toDoubleOrNull() ?: 1.49
        String.format(Locale.US, "%.2f", a * p)
      }
    )
  }

  var selectedDateMillis by remember {
    mutableStateOf(initialLog?.timestamp ?: System.currentTimeMillis())
  }
  var isFullTank by remember { mutableStateOf(initialLog?.isFullTank ?: true) }
  var isMissedPrevious by remember { mutableStateOf(initialLog?.isMissedPrevious ?: false) }
  var stationName by remember { mutableStateOf(initialLog?.stationName ?: "") }
  var fuelGrade by remember {
    mutableStateOf(
      initialLog?.fuelGrade ?: if (isElectric) "Fast Charger" else "Regular"
    )
  }
  var notes by remember { mutableStateOf(initialLog?.notes ?: "") }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Date picker dialog
  val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
  val datePickerDialog = DatePickerDialog(
    context,
    { _, year, month, dayOfMonth ->
      val newCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
      }
      selectedDateMillis = newCal.timeInMillis
    },
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH),
    calendar.get(Calendar.DAY_OF_MONTH)
  )

  val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())

  // Helper calculation logic
  fun updateFromAmount(newAmountStr: String) {
    amountInput = newAmountStr
    val amt = newAmountStr.toDoubleOrNull()
    val price = unitPriceInput.toDoubleOrNull()
    if (amt != null && price != null && amt > 0) {
      totalCostInput = String.format(Locale.US, "%.2f", amt * price)
    }
  }

  fun updateFromUnitPrice(newPriceStr: String) {
    unitPriceInput = newPriceStr
    val amt = amountInput.toDoubleOrNull()
    val price = newPriceStr.toDoubleOrNull()
    if (amt != null && price != null && amt > 0) {
      totalCostInput = String.format(Locale.US, "%.2f", amt * price)
    }
  }

  fun updateFromTotalCost(newTotalStr: String) {
    totalCostInput = newTotalStr
    val total = newTotalStr.toDoubleOrNull()
    val amt = amountInput.toDoubleOrNull()
    if (total != null && amt != null && amt > 0) {
      unitPriceInput = String.format(Locale.US, "%.3f", total / amt)
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_edit_fuel_log_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = stringResource(if (initialLog == null) R.string.add_log_title else R.string.edit_log_title),
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Date Selector Row
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
                text = stringResource(R.string.date_and_time),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = dateFormat.format(Date(selectedDateMillis)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            IconButton(onClick = { datePickerDialog.show() }) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = stringResource(R.string.date_and_time),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Odometer Field
        OutlinedTextField(
          value = odometerInput,
          onValueChange = { odometerInput = it },
          label = { Text("${stringResource(R.string.odometer)} ($distUnit)") },
          placeholder = { Text("e.g. 24500") },
          leadingIcon = {
            Icon(Icons.Default.Speed, contentDescription = null)
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("odometer_input")
        )

        // Amount & Unit Price in Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = amountInput,
            onValueChange = { updateFromAmount(it) },
            label = { Text("${stringResource(R.string.fuel_amount)} ($volUnit)") },
            placeholder = { Text("42.5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("amount_input")
          )

          OutlinedTextField(
            value = unitPriceInput,
            onValueChange = { updateFromUnitPrice(it) },
            label = { Text("${stringResource(R.string.price_per_unit)} ($currency)") },
            placeholder = { Text("1.499") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("unit_price_input")
          )
        }

        // Total Cost Field
        OutlinedTextField(
          value = totalCostInput,
          onValueChange = { updateFromTotalCost(it) },
          label = { Text("${stringResource(R.string.total_cost)} ($currency)") },
          placeholder = { Text("60.00") },
          leadingIcon = {
            Text(
              text = currency,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 12.dp)
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("total_cost_input")
        )

        // Full Tank Switch
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = stringResource(R.string.full_tank),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = stringResource(R.string.full_tank_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isFullTank,
              onCheckedChange = { isFullTank = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
          }
        }

        // Station & Fuel Grade
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text(stringResource(R.string.fuel_station)) },
            placeholder = { Text(stringResource(R.string.fuel_station_hint)) },
            singleLine = true,
            modifier = Modifier
              .weight(1.2f)
              .testTag("station_name_input")
          )

          OutlinedTextField(
            value = fuelGrade,
            onValueChange = { fuelGrade = it },
            label = { Text(stringResource(R.string.fuel_grade)) },
            placeholder = { Text(stringResource(R.string.fuel_grade_hint)) },
            singleLine = true,
            modifier = Modifier
              .weight(0.8f)
              .testTag("fuel_grade_input")
          )
        }

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(stringResource(R.string.notes)) },
          placeholder = { Text(stringResource(R.string.notes_hint)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Error message display
        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val odo = odometerInput.toDoubleOrNull()
          val amt = amountInput.toDoubleOrNull()
          val price = unitPriceInput.toDoubleOrNull()
          val total = totalCostInput.toDoubleOrNull()

          if (odo == null || odo < 0) {
            errorMessage = "Please enter a valid odometer reading"
            return@Button
          }
          if (amt == null || amt <= 0) {
            errorMessage = "Please enter a valid fuel/energy volume"
            return@Button
          }
          if (total == null || total < 0) {
            errorMessage = "Please enter a valid total cost"
            return@Button
          }
          val finalUnitPrice = if (price != null && price > 0) price else total / amt

          errorMessage = null
          onSave(
            odo,
            amt,
            finalUnitPrice,
            total,
            selectedDateMillis,
            isFullTank,
            isMissedPrevious,
            stationName.trim(),
            fuelGrade.trim(),
            notes.trim()
          )
        },
        modifier = Modifier.testTag("save_log_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_log_button")
      ) {
        Text(stringResource(R.string.action_cancel))
      }
    }
  )
}

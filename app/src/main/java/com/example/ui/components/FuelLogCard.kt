package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.data.model.VehicleProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FuelLogCard(
  log: FuelLog,
  previousLog: FuelLog?,
  vehicle: VehicleProfile,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol
  val distUnit = vehicle.distanceUnit.symbol

  val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
  val dateStr = dateFormat.format(Date(log.timestamp))

  // Distance since previous log if exists
  val deltaDistance = if (previousLog != null && log.odometer > previousLog.odometer) {
    log.odometer - previousLog.odometer
  } else null

  // Consumption for this interval if full tank
  val intervalEconomy = if (deltaDistance != null && deltaDistance > 0 && log.isFullTank && log.amount > 0) {
    (log.amount / deltaDistance) * 100.0 // L/100km or kWh/100km
  } else null

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("fuel_log_item_${log.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Date & Total Cost
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                if (isElectric) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
              contentDescription = null,
              tint = if (isElectric) MaterialTheme.colorScheme.onSecondaryContainer
              else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = dateStr,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            if (log.stationName.isNotBlank()) {
              Text(
                text = log.stationName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Total Paid & Unit Price badge
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = String.format(Locale.getDefault(), "%s%.2f", currency, log.totalCost),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = String.format(Locale.getDefault(), "%s%.3f/%s", currency, log.unitPrice, volUnit),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF59E0B)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Stats Pill Row: Odometer, Volume, Delta km, Interval Economy
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Odometer
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = String.format(Locale.getDefault(), "%.0f %s", log.odometer, distUnit),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }

          // Volume amount
          Text(
            text = String.format(Locale.getDefault(), "%.1f %s", log.amount, volUnit),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )

          // Delta distance if available
          if (deltaDistance != null) {
            Text(
              text = String.format(Locale.getDefault(), "+%.0f %s", deltaDistance, distUnit),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.Medium
            )
          }

          // Interval economy if calculated
          if (intervalEconomy != null) {
            Text(
              text = String.format(Locale.getDefault(), "%.1f %s", intervalEconomy, if (isElectric) "kWh/100km" else "L/100km"),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Notes or Fuel Grade if present
      if (log.notes.isNotBlank() || log.fuelGrade.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (log.fuelGrade.isNotBlank()) {
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = log.fuelGrade,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          if (log.notes.isNotBlank()) {
            Text(
              text = log.notes,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Bottom Row: Action Icons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onEdit,
          modifier = Modifier
            .size(36.dp)
            .testTag("edit_log_${log.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Log",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(36.dp)
            .testTag("delete_log_${log.id}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete Log",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

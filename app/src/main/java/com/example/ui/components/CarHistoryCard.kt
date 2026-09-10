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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.VehicleHistorySummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CarHistoryCard(
  summary: VehicleHistorySummary,
  onSelectAsActive: () -> Unit,
  onExchangeClick: () -> Unit,
  onExportClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val vehicle = summary.vehicle
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val distUnit = vehicle.distanceUnit.symbol
  val volUnit = vehicle.volumeUnit.symbol
  val econUnit = vehicle.economyUnit.symbol

  val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
  val purchaseDateStr = dateFormat.format(Date(vehicle.purchaseDateMillis))
  val endDateStr = if (vehicle.isArchived && vehicle.archiveDateMillis != null) {
    dateFormat.format(Date(vehicle.archiveDateMillis))
  } else {
    "Present"
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("car_history_card_${vehicle.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (summary.isActive) {
        MaterialTheme.colorScheme.surface
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (summary.isActive) 3.dp else 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Avatar, Name/Model, and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
              if (isElectric) Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF)))
              else if (summary.isActive) Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
              else Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = vehicle.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${vehicle.year} ${vehicle.makeModel} • ${vehicle.fuelType.displayName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (summary.isActive) {
            Color(0xFF10B981).copy(alpha = 0.15f)
          } else {
            Color(0xFFF59E0B).copy(alpha = 0.15f)
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (summary.isActive) Icons.Default.CheckCircle else Icons.Default.Archive,
              contentDescription = null,
              tint = if (summary.isActive) Color(0xFF10B981) else Color(0xFFD97706),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (summary.isActive) "Active" else "Archived",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (summary.isActive) Color(0xFF059669) else Color(0xFFB45309)
            )
          }
        }
      }

      // Archive reason banner if archived
      if (vehicle.isArchived && vehicle.archiveReason.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Reason: ${vehicle.archiveReason}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(14.dp))

      // User requirement: "summary total avg consumption and running costs"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Metric 1: Running Costs
        HistoryMetricBox(
          title = "Total Running Costs",
          value = "$currency${String.format(Locale.getDefault(), "%,.2f", summary.totalSpent)}",
          subtitle = if (summary.totalDistance > 0) {
            "$currency${String.format(Locale.getDefault(), "%.3f", summary.costPerDistance)} / $distUnit"
          } else {
            "No driving logged"
          },
          icon = Icons.Default.Paid,
          iconTint = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )

        // Metric 2: Total Average Consumption
        HistoryMetricBox(
          title = "Total Avg Consumption",
          value = if (summary.averageEconomy > 0) {
            String.format(Locale.getDefault(), "%.1f %s", summary.averageEconomy, econUnit)
          } else {
            "--"
          },
          subtitle = "${String.format(Locale.getDefault(), "%.1f", summary.totalVolume)} $volUnit total consumed",
          icon = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
          iconTint = if (isElectric) Color(0xFF00B0FF) else Color(0xFFF59E0B),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2 of metrics: Distance Driven & Ownership Duration
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HistoryMetricBox(
          title = "Distance Driven",
          value = "${String.format(Locale.getDefault(), "%,.0f", summary.totalDistance)} $distUnit",
          subtitle = "Odo: ${String.format(Locale.getDefault(), "%.0f", vehicle.initialOdometer)} -> ${String.format(Locale.getDefault(), "%.0f", summary.latestOdometer)}",
          icon = Icons.Default.Speed,
          iconTint = Color(0xFF8B5CF6),
          modifier = Modifier.weight(1f)
        )

        HistoryMetricBox(
          title = "Ownership Period",
          value = "${summary.ownershipDays} days",
          subtitle = "$purchaseDateStr - $endDateStr (${summary.logsCount} refuels)",
          icon = Icons.Default.DirectionsCar,
          iconTint = Color(0xFF3B82F6),
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom Action Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!summary.isActive) {
          Button(
            onClick = onSelectAsActive,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Set as Active", fontSize = 13.sp)
          }
        } else {
          Button(
            onClick = onExchangeClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Exchange / Archive", fontSize = 13.sp)
          }
        }

        OutlinedButton(
          onClick = onExportClick,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(0.7f)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Export", fontSize = 13.sp)
        }

        if (vehicle.isArchived) {
          OutlinedButton(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            modifier = Modifier.size(42.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryMetricBox(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

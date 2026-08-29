package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.VehicleProfile
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VehicleProfileScreen(
  uiState: UiState,
  onEditVehicleClick: () -> Unit,
  onLoadSampleClick: () -> Unit,
  onClearDataClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val vehicle = uiState.vehicle
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol
  val distUnit = vehicle.distanceUnit.symbol

  val sinceSum = uiState.sincePurchaseSummary
  val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

  val now = System.currentTimeMillis()
  val ownershipDays = ((now - vehicle.purchaseDateMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
  val ownershipMonths = (ownershipDays / 30.4375)

  var showClearConfirm by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("vehicle_profile_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Vehicle & Ownership",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "Lifetime ownership stats since purchase",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onEditVehicleClick,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("edit_vehicle_profile_button")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit")
        }
      }
    }

    // Vehicle Badge Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  MaterialTheme.colorScheme.surface
                )
              )
            )
            .padding(18.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                  if (isElectric) MaterialTheme.colorScheme.secondaryContainer
                  else MaterialTheme.colorScheme.primaryContainer
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = if (isElectric) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp)
              )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                text = vehicle.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "${vehicle.year} ${vehicle.makeModel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Surface(
                color = if (isElectric) Color(0x3306B6D4) else Color(0x3310B981),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = vehicle.fuelType.displayName,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isElectric) Color(0xFF0284C7) else Color(0xFF047857),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // Ownership Summary ("Since I Bought It")
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Total Ownership Spending",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Since purchased on ${dateFormat.format(Date(vehicle.purchaseDateMillis))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          // 2x2 Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OwnershipItemBox(
              title = "Total Fuel Spending",
              value = String.format(Locale.getDefault(), "%s%.2f", currency, sinceSum.totalSpent),
              subtitle = "across ${sinceSum.logCount} refuels",
              icon = Icons.Default.Paid,
              modifier = Modifier.weight(1f)
            )

            OwnershipItemBox(
              title = "Weighted Avg Price",
              value = if (sinceSum.avgPricePaid > 0) {
                String.format(Locale.getDefault(), "%s%.3f", currency, sinceSum.avgPricePaid)
              } else "--",
              subtitle = "per $volUnit",
              icon = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OwnershipItemBox(
              title = "Total Distance",
              value = String.format(Locale.getDefault(), "%.0f %s", sinceSum.totalDistance, distUnit),
              subtitle = "from initial ${String.format(Locale.getDefault(), "%.0f", vehicle.initialOdometer)} $distUnit",
              icon = Icons.Default.Speed,
              modifier = Modifier.weight(1f)
            )

            OwnershipItemBox(
              title = "Ownership Period",
              value = String.format(Locale.getDefault(), "%.1f mo", ownershipMonths),
              subtitle = "$ownershipDays days of driving",
              icon = Icons.Default.CalendarToday,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Vehicle Configuration Details
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Configuration & Units",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          ConfigRow(label = "Currency Symbol", value = vehicle.currencySymbol)
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
          ConfigRow(label = "Distance Unit", value = "${vehicle.distanceUnit.label} (${vehicle.distanceUnit.symbol})")
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
          ConfigRow(label = "Volume Unit", value = "${vehicle.volumeUnit.label} (${vehicle.volumeUnit.symbol})")
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
          ConfigRow(label = "Initial Odometer", value = "${vehicle.initialOdometer} ${vehicle.distanceUnit.symbol}")
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
          ConfigRow(label = "Tank / Battery Capacity", value = "${vehicle.tankCapacity} ${vehicle.volumeUnit.symbol}")
        }
      }
    }

    // Quick Actions / Tools
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = onLoadSampleClick,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("load_sample_data_button")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Load Sample 8-Month Fuel Data")
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("clear_all_data_button")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset & Clear All Refuel Logs")
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp))
    }
  }

  // Clear Confirmation Dialog
  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All Refuel Logs?") },
      text = { Text("This will permanently delete all recorded refuel logs for this vehicle. You can reload sample data at any time.") },
      confirmButton = {
        Button(
          onClick = {
            onClearDataClick()
            showClearConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Yes, Clear Data")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun OwnershipItemBox(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun ConfigRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold
    )
  }
}

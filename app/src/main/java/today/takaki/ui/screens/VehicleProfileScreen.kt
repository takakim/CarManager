package today.takaki.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import today.takaki.data.model.FuelType
import today.takaki.data.model.VehicleProfile
import today.takaki.ui.components.CarHistoryCard
import today.takaki.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VehicleScreenSubTab {
  ACTIVE_VEHICLE,
  CAR_HISTORY
}

@Composable
fun VehicleProfileScreen(
  uiState: UiState,
  onEditVehicleClick: () -> Unit,
  onExchangeVehicleClick: () -> Unit,
  onImportExportClick: () -> Unit,
  onSelectVehicleAsActive: (Int) -> Unit,
  onDeleteVehicle: (Int) -> Unit,
  onLoadSampleClick: () -> Unit,
  onClearDataClick: () -> Unit,
  onToggleSmartAdvisor: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var currentSubTab by remember { mutableStateOf(VehicleScreenSubTab.ACTIVE_VEHICLE) }

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
  var deletingVehicleId by remember { mutableStateOf<Int?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("vehicle_profile_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Sub-Tabs: Active Car vs Car History & Garage
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        TabRow(
          selectedTabIndex = currentSubTab.ordinal,
          containerColor = Color.Transparent,
          modifier = Modifier.fillMaxWidth()
        ) {
          Tab(
            selected = currentSubTab == VehicleScreenSubTab.ACTIVE_VEHICLE,
            onClick = { currentSubTab = VehicleScreenSubTab.ACTIVE_VEHICLE },
            text = {
              Text(
                text = "Active Car",
                fontWeight = if (currentSubTab == VehicleScreenSubTab.ACTIVE_VEHICLE) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
          Tab(
            selected = currentSubTab == VehicleScreenSubTab.CAR_HISTORY,
            onClick = { currentSubTab = VehicleScreenSubTab.CAR_HISTORY },
            text = {
              Text(
                text = "Car History (${uiState.carHistorySummaries.size})",
                fontWeight = if (currentSubTab == VehicleScreenSubTab.CAR_HISTORY) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }
    }

    if (currentSubTab == VehicleScreenSubTab.ACTIVE_VEHICLE) {
      // --- ACTIVE VEHICLE VIEW ---

      // Header with Action Buttons
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Vehicle & Ownership",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = "Stats and specifications for your active car",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = onExchangeVehicleClick,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("exchange_vehicle_button")
            ) {
              Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Exchange", fontSize = 13.sp)
            }

            Button(
              onClick = onEditVehicleClick,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("edit_vehicle_profile_button")
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Edit", fontSize = 13.sp)
            }
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
                    if (isElectric) Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF)))
                    else Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(30.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = vehicle.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${vehicle.year} ${vehicle.makeModel}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isElectric) Color(0xFF00E5FF).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                  Text(
                    text = vehicle.fuelType.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isElectric) Color(0xFF0288D1) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Lifetime Ownership Metrics Grid
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Lifetime Ownership Statistics",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Purchased on ${dateFormat.format(Date(vehicle.purchaseDateMillis))}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OwnershipItemBox(
                title = "Total Fuel / Energy Cost",
                value = "$currency${String.format(Locale.getDefault(), "%,.2f", sinceSum.totalSpent)}",
                subtitle = "${sinceSum.logCount} refuels logged",
                icon = Icons.Default.Paid,
                modifier = Modifier.weight(1f)
              )

              OwnershipItemBox(
                title = "Lifetime Average Economy",
                value = if (sinceSum.averageEconomy > 0) {
                  String.format(Locale.getDefault(), "%.1f %s", sinceSum.averageEconomy, vehicle.economyUnit.symbol)
                } else {
                  "--"
                },
                subtitle = "${String.format(Locale.getDefault(), "%.1f", sinceSum.totalVolume)} $volUnit total consumed",
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
                title = "Distance Driven",
                value = "${String.format(Locale.getDefault(), "%,.0f", sinceSum.totalDistance)} $distUnit",
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

      // Configuration Details
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Configuration & Display Units",
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
            ConfigRow(label = "Economy Unit", value = "${vehicle.economyUnit.label} (${vehicle.economyUnit.symbol})")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ConfigRow(label = "Initial Odometer", value = "${vehicle.initialOdometer} ${vehicle.distanceUnit.symbol}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
            ConfigRow(label = "Tank / Battery Capacity", value = "${vehicle.tankCapacity} ${vehicle.volumeUnit.symbol}")
          }
        }
      }

      // On-Device Smart Advisor Settings Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_advisor_settings_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(
                    if (uiState.isSmartAdvisorEnabled)
                      MaterialTheme.colorScheme.primaryContainer
                    else
                      MaterialTheme.colorScheme.surfaceVariant
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = if (uiState.isSmartAdvisorEnabled)
                    MaterialTheme.colorScheme.primary
                  else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text(
                  text = "On-Device Smart Advisor",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Period cost breakdown & vehicle efficiency tips calculated locally on your phone. 100% private, runs offline, zero API fees.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
              checked = uiState.isSmartAdvisorEnabled,
              onCheckedChange = onToggleSmartAdvisor,
              modifier = Modifier.testTag("smart_advisor_profile_toggle")
            )
          }
        }
      }

      // Tools & Data Management
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Data Management & Tools",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Import / Export Button
            Button(
              onClick = onImportExportClick,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("open_import_export_button")
            ) {
              Icon(Icons.Default.ImportExport, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Import / Export Refuel Data (CSV & JSON)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
              onClick = onLoadSampleClick,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("load_sample_data_button")
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Load Sample Active & Archived Cars")
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
              Text("Reset & Clear Active Car Logs")
            }
          }
        }
      }

    } else {
      // --- CAR HISTORY & GARAGE VIEW ---
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Car History & Garage",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = "Summary total average consumption and running costs for all vehicles",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = onExchangeVehicleClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Exchange", fontSize = 13.sp)
          }
        }
      }

      // History Cards for all vehicles
      items(uiState.carHistorySummaries, key = { it.vehicle.id }) { summary ->
        CarHistoryCard(
          summary = summary,
          onSelectAsActive = { onSelectVehicleAsActive(summary.vehicle.id) },
          onExchangeClick = onExchangeVehicleClick,
          onExportClick = onImportExportClick,
          onDeleteClick = { deletingVehicleId = summary.vehicle.id }
        )
      }

      if (uiState.carHistorySummaries.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(12.dp))
              Text("No vehicles recorded in garage.", fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(onClick = onLoadSampleClick) {
                Text("Load Sample Vehicles & History")
              }
            }
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
      title = { Text("Clear Refuel Logs?") },
      text = { Text("This will permanently delete recorded refuel logs for ${vehicle.name}. You can reload sample data at any time.") },
      confirmButton = {
        Button(
          onClick = {
            onClearDataClick()
            showClearConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Yes, Clear Logs")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Vehicle Confirmation Dialog
  if (deletingVehicleId != null) {
    val targetCar = uiState.allVehicles.find { it.id == deletingVehicleId }
    AlertDialog(
      onDismissRequest = { deletingVehicleId = null },
      title = { Text("Delete Vehicle from History?") },
      text = { Text("Permanently delete ${targetCar?.name ?: "this vehicle"} and all associated refuel records from your garage?") },
      confirmButton = {
        Button(
          onClick = {
            deletingVehicleId?.let { onDeleteVehicle(it) }
            deletingVehicleId = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { deletingVehicleId = null }) {
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

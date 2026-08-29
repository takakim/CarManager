package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.ui.components.AddEditFuelLogDialog
import com.example.ui.components.VehicleSettingsDialog
import com.example.ui.viewmodel.FuelTrackerViewModel

enum class MainTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
  DASHBOARD("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
  LOGS("Logs", Icons.Filled.FormatListBulleted, Icons.Outlined.FormatListBulleted),
  ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
  VEHICLE("Vehicle", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar)
}

@Composable
fun MainScreen(
  viewModel: FuelTrackerViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

  var showAddLogDialog by remember { mutableStateOf(false) }
  var editingLog by remember { mutableStateOf<FuelLog?>(null) }
  var deletingLog by remember { mutableStateOf<FuelLog?>(null) }
  var showVehicleSettingsDialog by remember { mutableStateOf(false) }

  val isElectric = uiState.vehicle.fuelType == FuelType.ELECTRIC

  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        MainTab.values().forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.title,
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = tab.title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddLogDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("add_refuel_fab")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Log Refuel",
          modifier = Modifier.size(26.dp)
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentTab) {
        MainTab.DASHBOARD -> {
          DashboardScreen(
            uiState = uiState,
            onSelectFilter = { viewModel.setFilter(it) },
            onAddLogClick = { showAddLogDialog = true },
            onEditLogClick = { editingLog = it },
            onDeleteLogClick = { deletingLog = it },
            onViewAllLogsClick = { currentTab = MainTab.LOGS },
            onOpenSettingsClick = { showVehicleSettingsDialog = true },
            onLoadSampleClick = { viewModel.loadSampleData() }
          )
        }
        MainTab.LOGS -> {
          LogsHistoryScreen(
            logs = uiState.logs,
            vehicle = uiState.vehicle,
            onAddLogClick = { showAddLogDialog = true },
            onEditLogClick = { editingLog = it },
            onDeleteLogClick = { deletingLog = it }
          )
        }
        MainTab.ANALYTICS -> {
          AnalyticsScreen(
            uiState = uiState
          )
        }
        MainTab.VEHICLE -> {
          VehicleProfileScreen(
            uiState = uiState,
            onEditVehicleClick = { showVehicleSettingsDialog = true },
            onLoadSampleClick = { viewModel.loadSampleData() },
            onClearDataClick = { viewModel.clearAllData() }
          )
        }
      }
    }
  }

  // Add Refuel Dialog
  if (showAddLogDialog) {
    val lastOdo = uiState.logs.maxOfOrNull { it.odometer } ?: uiState.vehicle.initialOdometer
    val lastPrice = uiState.logs.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0

    AddEditFuelLogDialog(
      vehicle = uiState.vehicle,
      initialLog = null,
      lastOdometer = lastOdo,
      lastUnitPrice = lastPrice,
      onDismiss = { showAddLogDialog = false },
      onSave = { odo, amt, price, total, date, full, missed, station, grade, notes ->
        viewModel.addLog(odo, amt, price, total, date, full, missed, station, grade, notes)
        showAddLogDialog = false
      }
    )
  }

  // Edit Refuel Dialog
  if (editingLog != null) {
    AddEditFuelLogDialog(
      vehicle = uiState.vehicle,
      initialLog = editingLog,
      onDismiss = { editingLog = null },
      onSave = { odo, amt, price, total, date, full, missed, station, grade, notes ->
        val updated = editingLog!!.copy(
          odometer = odo,
          amount = amt,
          unitPrice = price,
          totalCost = total,
          timestamp = date,
          isFullTank = full,
          isMissedPrevious = missed,
          stationName = station,
          fuelGrade = grade,
          notes = notes
        )
        viewModel.updateLog(updated)
        editingLog = null
      }
    )
  }

  // Delete Confirmation Dialog
  if (deletingLog != null) {
    AlertDialog(
      onDismissRequest = { deletingLog = null },
      title = { Text("Delete Refuel Record?") },
      text = { Text("Are you sure you want to delete this ${String.format("%.1f %s", deletingLog!!.amount, uiState.vehicle.volumeUnit.symbol)} refuel entry from history?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteLog(deletingLog!!)
            deletingLog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { deletingLog = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Vehicle Settings Dialog
  if (showVehicleSettingsDialog) {
    VehicleSettingsDialog(
      vehicle = uiState.vehicle,
      onDismiss = { showVehicleSettingsDialog = false },
      onSave = { updated ->
        viewModel.updateVehicleProfile(updated)
        showVehicleSettingsDialog = false
      }
    )
  }
}

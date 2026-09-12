package today.takaki.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import today.takaki.R
import today.takaki.data.model.FuelLog
import today.takaki.data.model.VehicleProfile
import today.takaki.ui.components.FuelLogCard
import java.util.Locale

@Composable
fun LogsHistoryScreen(
  logs: List<FuelLog>,
  vehicle: VehicleProfile,
  onAddLogClick: () -> Unit,
  onEditLogClick: (FuelLog) -> Unit,
  onDeleteLogClick: (FuelLog) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredLogs = remember(logs, searchQuery) {
    if (searchQuery.isBlank()) logs
    else {
      val query = searchQuery.trim().lowercase(Locale.getDefault())
      logs.filter {
        it.stationName.lowercase(Locale.getDefault()).contains(query) ||
            it.fuelGrade.lowercase(Locale.getDefault()).contains(query) ||
            it.notes.lowercase(Locale.getDefault()).contains(query)
      }
    }
  }

  val totalSpentAll = logs.sumOf { it.totalCost }
  val totalVolumeAll = logs.sumOf { it.amount }
  val avgPriceAll = if (totalVolumeAll > 0) totalSpentAll / totalVolumeAll else 0.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("logs_history_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
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
            text = stringResource(R.string.tab_logs),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "${logs.size} ${stringResource(R.string.tab_logs).lowercase(Locale.getDefault())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onAddLogClick,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_log_history_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.size(4.dp))
          Text(stringResource(R.string.action_save).let { stringResource(R.string.tab_overview) /* or + */; "+" })
        }
      }
    }

    // Top Summary Banner
    item {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = stringResource(R.string.total_spent),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = String.format(Locale.getDefault(), "%s%.2f", vehicle.currencySymbol, totalSpentAll),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Column {
            Text(
              text = stringResource(R.string.total_volume),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = String.format(Locale.getDefault(), "%.1f %s", totalVolumeAll, vehicle.volumeUnit.symbol),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = stringResource(R.string.avg_price_paid),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (avgPriceAll > 0) {
                String.format(Locale.getDefault(), "%s%.3f/%s", vehicle.currencySymbol, avgPriceAll, vehicle.volumeUnit.symbol)
              } else "--",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF59E0B)
            )
          }
        }
      }
    }

    // Search Bar
    if (logs.isNotEmpty()) {
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(stringResource(R.string.search_logs_hint)) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_logs_hint))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_close))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_logs_input")
        )
      }
    }

    if (filteredLogs.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.LocalGasStation,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_matching_logs) else stringResource(R.string.no_logs_title),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredLogs, key = { it.id }) { log ->
        val logIndex = logs.indexOf(log)
        val prevLog = if (logIndex + 1 < logs.size) logs[logIndex + 1] else null

        FuelLogCard(
          log = log,
          previousLog = prevLog,
          vehicle = vehicle,
          onEdit = { onEditLogClick(log) },
          onDelete = { onDeleteLogClick(log) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp))
    }
  }
}

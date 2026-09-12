package today.takaki.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import today.takaki.data.model.FuelType
import today.takaki.data.model.PeriodSummary
import today.takaki.data.model.TimeFilter
import today.takaki.ui.components.AiInsightsCard
import today.takaki.ui.components.AvgPriceCard
import today.takaki.ui.components.FuelLogCard
import today.takaki.ui.components.PriceHistoryChart
import today.takaki.ui.components.SpendingChart
import today.takaki.ui.components.SpendingHeroCard
import today.takaki.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun DashboardScreen(
  uiState: UiState,
  onSelectFilter: (TimeFilter) -> Unit,
  onAddLogClick: () -> Unit,
  onEditLogClick: (FuelLog) -> Unit,
  onDeleteLogClick: (FuelLog) -> Unit,
  onViewAllLogsClick: () -> Unit,
  onOpenSettingsClick: () -> Unit,
  onSelectVehicle: (Int) -> Unit = {},
  onLoadSampleClick: () -> Unit,
  onAnalyzeAiClick: (TimeFilter) -> Unit = {},
  onToggleSmartAdvisor: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val vehicle = uiState.vehicle
  val currentSummary = uiState.currentSummary
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Row with App Title and Vehicle settings button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          val archivedText = if (vehicle.isArchived) " " + stringResource(R.string.archived_tag) else ""
          Text(
            text = "${vehicle.name} • ${vehicle.makeModel}$archivedText",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onOpenSettingsClick,
          modifier = Modifier.testTag("open_vehicle_settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = stringResource(R.string.vehicle_settings_title),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    // Multiple Vehicles Switcher (if user has multiple cars in garage)
    if (uiState.allVehicles.size > 1) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.tab_vehicle) + ":",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          uiState.allVehicles.forEach { v ->
            val isSelected = v.id == vehicle.id
            val vArchived = if (v.isArchived) " " + stringResource(R.string.archived_tag) else ""
            FilterChip(
              selected = isSelected,
              onClick = { onSelectVehicle(v.id) },
              label = {
                Text(
                  text = "${v.name}$vArchived",
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = if (v.fuelType == FuelType.ELECTRIC) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              }
            )
          }
        }
      }
    }

    // Time Filter Chips Row: [This Week] [This Month] [This Year] [Since Purchase] [All Time]
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TimeFilter.values().forEach { filter ->
          val isSelected = uiState.selectedFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = { onSelectFilter(filter) },
            label = {
              Text(
                text = stringResource(filter.nameRes),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("filter_chip_${filter.name}")
          )
        }
      }
    }

    // Empty state callout if no logs exist yet
    if (uiState.logs.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = stringResource(R.string.no_logs_title),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = stringResource(R.string.no_logs_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = onAddLogClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.add_log_title))
              }
              OutlinedButton(onClick = onLoadSampleClick) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.load_sample_data))
              }
            }
          }
        }
      }
    } else {
      // 1. Hero Spending & Average Price Summary Card
      item {
        SpendingHeroCard(
          summary = currentSummary,
          vehicle = vehicle
        )
      }

      // 1.5. AI Period Advisor & Insights Card
      item {
        AiInsightsCard(
          analysis = uiState.aiAnalysis,
          isAnalyzing = uiState.isAiAnalyzing,
          currentFilter = uiState.selectedFilter,
          isEnabled = uiState.isSmartAdvisorEnabled,
          onToggleEnabled = onToggleSmartAdvisor,
          onAnalyzeClick = { onAnalyzeAiClick(uiState.selectedFilter) }
        )
      }

      // 2. Multi-Period Quick Spending Grid (Week, Month, Year, Since Purchase)
      item {
        MultiPeriodSpendingOverview(
          uiState = uiState,
          onSelectFilter = onSelectFilter
        )
      }

      // 3. Average Price Paid & Savings Analysis Card
      item {
        AvgPriceCard(
          summary = currentSummary,
          allTimeAvgPrice = uiState.allTimeAvgPrice,
          vehicle = vehicle
        )
      }

      // 4. Spending Breakdown Bar Chart
      item {
        SpendingChart(
          bars = currentSummary.chartBars,
          timeFilter = uiState.selectedFilter,
          currencySymbol = vehicle.currencySymbol,
          volumeUnit = vehicle.volumeUnit.symbol
        )
      }

      // 5. Price Paid Trajectory Line Chart
      item {
        PriceHistoryChart(
          pricePoints = currentSummary.pricePoints,
          avgPrice = currentSummary.avgPricePaid,
          currencySymbol = vehicle.currencySymbol,
          volumeUnit = vehicle.volumeUnit.symbol
        )
      }

      // 6. Recent Logs Section Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.recent_activity),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = onViewAllLogsClick) {
            Text("${stringResource(R.string.view_all_logs)} (${uiState.logs.size})")
          }
        }
      }

      // Show top 3 recent logs
      items(uiState.logs.take(3), key = { it.id }) { log ->
        val logIndex = uiState.logs.indexOf(log)
        val prevLog = if (logIndex + 1 < uiState.logs.size) uiState.logs[logIndex + 1] else null
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
      Spacer(modifier = Modifier.height(60.dp)) // Extra space for FAB / bottom bar
    }
  }
}

@Composable
private fun MultiPeriodSpendingOverview(
  uiState: UiState,
  onSelectFilter: (TimeFilter) -> Unit
) {
  val currency = uiState.vehicle.currencySymbol
  val volUnit = uiState.vehicle.volumeUnit.symbol

  Column {
    Text(
      text = stringResource(R.string.spending_chart_title),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PeriodStatMiniCard(
        title = stringResource(R.string.filter_this_week),
        spent = uiState.weekSummary.totalSpent,
        avgPrice = uiState.weekSummary.avgPricePaid,
        currency = currency,
        volUnit = volUnit,
        isSelected = uiState.selectedFilter == TimeFilter.THIS_WEEK,
        onClick = { onSelectFilter(TimeFilter.THIS_WEEK) },
        modifier = Modifier.weight(1f)
      )

      PeriodStatMiniCard(
        title = stringResource(R.string.filter_this_month),
        spent = uiState.monthSummary.totalSpent,
        avgPrice = uiState.monthSummary.avgPricePaid,
        currency = currency,
        volUnit = volUnit,
        isSelected = uiState.selectedFilter == TimeFilter.THIS_MONTH,
        onClick = { onSelectFilter(TimeFilter.THIS_MONTH) },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PeriodStatMiniCard(
        title = stringResource(R.string.filter_this_year),
        spent = uiState.yearSummary.totalSpent,
        avgPrice = uiState.yearSummary.avgPricePaid,
        currency = currency,
        volUnit = volUnit,
        isSelected = uiState.selectedFilter == TimeFilter.THIS_YEAR,
        onClick = { onSelectFilter(TimeFilter.THIS_YEAR) },
        modifier = Modifier.weight(1f)
      )

      PeriodStatMiniCard(
        title = stringResource(R.string.filter_all_time),
        spent = uiState.sincePurchaseSummary.totalSpent,
        avgPrice = uiState.sincePurchaseSummary.avgPricePaid,
        currency = currency,
        volUnit = volUnit,
        isSelected = uiState.selectedFilter == TimeFilter.SINCE_PURCHASE,
        onClick = { onSelectFilter(TimeFilter.SINCE_PURCHASE) },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun PeriodStatMiniCard(
  title: String,
  spent: Double,
  avgPrice: Double,
  currency: String,
  volUnit: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    onClick = onClick,
    modifier = modifier.testTag("period_card_${title.replace(" ", "_").lowercase(Locale.getDefault())}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
      else MaterialTheme.colorScheme.surface
    ),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = String.format(Locale.getDefault(), "%s%.2f", currency, spent),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = if (avgPrice > 0) {
          String.format(Locale.getDefault(), "avg %s%.3f/%s", currency, avgPrice, volUnit)
        } else "no logs",
        style = MaterialTheme.typography.labelSmall,
        color = if (avgPrice > 0) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

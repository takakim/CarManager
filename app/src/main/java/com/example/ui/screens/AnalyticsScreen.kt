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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelType
import com.example.data.model.PeriodSummary
import com.example.data.model.TimeFilter
import com.example.ui.components.PriceHistoryChart
import com.example.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun AnalyticsScreen(
  uiState: UiState,
  modifier: Modifier = Modifier
) {
  val vehicle = uiState.vehicle
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol
  val distUnit = vehicle.distanceUnit.symbol

  val allTimeSum = uiState.allTimeSummary
  val sincePurchaseSum = uiState.sincePurchaseSummary

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("analytics_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Consumption Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "Price trends, consumption rates & spending cadence",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 1. Average Price Comparison Across All Timeframes
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Average Price Paid Comparison",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Icon(
              imageVector = Icons.Default.PriceCheck,
              contentDescription = null,
              tint = Color(0xFFF59E0B)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          PriceComparisonRow(
            title = "This Week Average",
            price = uiState.weekSummary.avgPricePaid,
            currency = currency,
            volUnit = volUnit
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

          PriceComparisonRow(
            title = "This Month Average",
            price = uiState.monthSummary.avgPricePaid,
            currency = currency,
            volUnit = volUnit
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

          PriceComparisonRow(
            title = "This Year Average",
            price = uiState.yearSummary.avgPricePaid,
            currency = currency,
            volUnit = volUnit
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

          PriceComparisonRow(
            title = "Since I Bought It (All-Time)",
            price = sincePurchaseSum.avgPricePaid,
            currency = currency,
            volUnit = volUnit,
            isHighlight = true
          )
        }
      }
    }

    // 2. Full History Price Curve
    item {
      PriceHistoryChart(
        pricePoints = sincePurchaseSum.pricePoints,
        avgPrice = sincePurchaseSum.avgPricePaid,
        currencySymbol = currency,
        volumeUnit = volUnit
      )
    }

    // 3. Driving Cadence & Running Costs
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Cost & Driving Cadence",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AnalyticsMetricBox(
              title = "Cost per $distUnit",
              value = if (sincePurchaseSum.costPerDistance > 0) {
                String.format(Locale.getDefault(), "%s%.3f", currency, sincePurchaseSum.costPerDistance)
              } else "--",
              subtitle = "per $distUnit driven",
              icon = Icons.Default.TrendingUp,
              modifier = Modifier.weight(1f)
            )

            AnalyticsMetricBox(
              title = if (isElectric) "Energy Economy" else "Fuel Economy",
              value = if (sincePurchaseSum.averageEconomy > 0) {
                String.format(Locale.getDefault(), "%.1f", sincePurchaseSum.averageEconomy)
              } else "--",
              subtitle = vehicle.economyUnit.symbol,
              icon = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AnalyticsMetricBox(
              title = "Spending / Day",
              value = if (sincePurchaseSum.spendingPerDay > 0) {
                String.format(Locale.getDefault(), "%s%.2f", currency, sincePurchaseSum.spendingPerDay)
              } else "--",
              subtitle = "avg daily cost",
              icon = Icons.Default.CalendarMonth,
              modifier = Modifier.weight(1f)
            )

            AnalyticsMetricBox(
              title = "Spending / Month",
              value = if (sincePurchaseSum.spendingPerMonth > 0) {
                String.format(Locale.getDefault(), "%s%.2f", currency, sincePurchaseSum.spendingPerMonth)
              } else "--",
              subtitle = "projected monthly",
              icon = Icons.Default.CalendarMonth,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp))
    }
  }
}

@Composable
private fun PriceComparisonRow(
  title: String,
  price: Double,
  currency: String,
  volUnit: String,
  isHighlight: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = if (isHighlight) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
      fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
      color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )

    Text(
      text = if (price > 0) String.format(Locale.getDefault(), "%s%.3f / %s", currency, price, volUnit) else "No data",
      style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun AnalyticsMetricBox(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
          fontSize = 12.sp,
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
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
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
import com.example.data.model.PeriodSummary
import com.example.data.model.VehicleProfile
import java.util.Locale

@Composable
fun AvgPriceCard(
  summary: PeriodSummary,
  allTimeAvgPrice: Double,
  vehicle: VehicleProfile,
  modifier: Modifier = Modifier
) {
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol

  val avgPrice = summary.avgPricePaid
  val minPrice = summary.minPricePaid
  val maxPrice = summary.maxPricePaid
  val priceSpread = (maxPrice - minPrice).coerceAtLeast(0.0)

  // Savings calculated as (maxPrice - avgPrice) * totalVolume
  val potentialSavings = if (maxPrice > avgPrice && summary.totalVolume > 0) {
    (maxPrice - avgPrice) * summary.totalVolume
  } else 0.0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("avg_price_analysis_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
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
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = "Price Calculation",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Average Price Analysis",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Weighted based on fuel volume purchased",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Average Metric Box
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${summary.timeFilter.title} Average",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (avgPrice > 0) {
                String.format(Locale.getDefault(), "%s%.3f", currency, avgPrice)
              } else "--",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "per $volUnit",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // All-time benchmark comparison
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "All-Time Avg",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (allTimeAvgPrice > 0) {
                String.format(Locale.getDefault(), "%s%.3f / %s", currency, allTimeAvgPrice, volUnit)
              } else "--",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            if (avgPrice > 0 && allTimeAvgPrice > 0) {
              val diff = avgPrice - allTimeAvgPrice
              val diffPercent = (diff / allTimeAvgPrice) * 100.0
              val isLower = diff <= 0
              val diffColor = if (isLower) Color(0xFF10B981) else Color(0xFFEF4444)

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isLower) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = diffColor,
                  modifier = Modifier.size(12.dp)
                )
                Text(
                  text = String.format(Locale.getDefault(), "%.1f%% vs all-time", Math.abs(diffPercent)),
                  fontSize = 11.sp,
                  color = diffColor,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Min & Max Price Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PriceDetailCard(
          title = "Lowest Paid",
          price = minPrice,
          currency = currency,
          volUnit = volUnit,
          icon = Icons.Default.ArrowDownward,
          iconColor = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )

        PriceDetailCard(
          title = "Highest Paid",
          price = maxPrice,
          currency = currency,
          volUnit = volUnit,
          icon = Icons.Default.ArrowUpward,
          iconColor = Color(0xFFEF4444),
          modifier = Modifier.weight(1f)
        )
      }

      if (potentialSavings > 0) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          color = Color(0x1A10B981),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = "Savings",
              tint = Color(0xFF10B981),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = String.format(
                Locale.getDefault(),
                "Saved ~%s%.2f in this period vs peak price (%s%.3f/%s)",
                currency, potentialSavings, currency, maxPrice, volUnit
              ),
              fontSize = 12.sp,
              color = Color(0xFF047857),
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PriceDetailCard(
  title: String,
  price: Double,
  currency: String,
  volUnit: String,
  icon: ImageVector,
  iconColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surface,
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
          contentDescription = title,
          tint = iconColor,
          modifier = Modifier.size(14.dp)
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (price > 0) String.format(Locale.getDefault(), "%s%.3f", currency, price) else "--",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "per $volUnit",
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

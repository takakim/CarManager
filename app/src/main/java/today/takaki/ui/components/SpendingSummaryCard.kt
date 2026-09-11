package today.takaki.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import today.takaki.data.model.FuelType
import today.takaki.data.model.PeriodSummary
import today.takaki.data.model.VehicleProfile
import java.util.Locale

@Composable
fun SpendingHeroCard(
  summary: PeriodSummary,
  vehicle: VehicleProfile,
  modifier: Modifier = Modifier
) {
  val isElectric = vehicle.fuelType == FuelType.ELECTRIC
  val currency = vehicle.currencySymbol
  val volUnit = vehicle.volumeUnit.symbol
  val distUnit = vehicle.distanceUnit.symbol

  val gradientColors = if (isElectric) {
    listOf(Color(0xFF0F766E), Color(0xFF0E7490), Color(0xFF1E293B))
  } else {
    listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF0F172A))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("spending_hero_card"),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(gradientColors))
        .padding(20.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header Row: Time period badge & fuel type icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color(0x33FFFFFF),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = summary.timeFilter.title.uppercase(Locale.getDefault()),
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
              contentDescription = "Fuel Type",
              tint = if (isElectric) Color(0xFF38BDF8) else Color(0xFF34D399),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = vehicle.name,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Spent Main Display
        Text(
          text = "Total Spending",
          color = Color.White.copy(alpha = 0.75f),
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(2.dp))

        AnimatedContent(
          targetState = summary.totalSpent,
          label = "total_spent_anim"
        ) { spent ->
          Text(
            text = String.format(Locale.getDefault(), "%s%.2f", currency, spent),
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Avg Price Paid Highlight Banner
        Surface(
          color = Color(0x2B000000),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0x33FBBF24)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.TrendingUp,
                  contentDescription = "Avg Price",
                  tint = Color(0xFFFBBF24),
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Avg Price Paid",
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 12.sp
                )
                Text(
                  text = if (summary.avgPricePaid > 0) {
                    String.format(Locale.getDefault(), "%s%.3f / %s", currency, summary.avgPricePaid, volUnit)
                  } else "No logs in period",
                  color = Color(0xFFFDE68A),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            if (summary.logCount > 0) {
              Surface(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${summary.logCount} ${if (summary.logCount == 1) "refuel" else "refuels"}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Key Sub-Metrics Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Volume
          SubMetricItem(
            label = "Total Fuel/Energy",
            value = if (summary.totalVolume > 0) String.format(Locale.getDefault(), "%.1f %s", summary.totalVolume, volUnit) else "0 $volUnit",
            icon = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation
          )

          // Distance
          SubMetricItem(
            label = "Distance",
            value = if (summary.totalDistance > 0) String.format(Locale.getDefault(), "%.0f %s", summary.totalDistance, distUnit) else "0 $distUnit",
            icon = Icons.Default.Speed
          )

          // Cost / Distance
          SubMetricItem(
            label = "Cost / $distUnit",
            value = if (summary.costPerDistance > 0) String.format(Locale.getDefault(), "%s%.2f/%s", currency, summary.costPerDistance, distUnit) else "--",
            icon = Icons.Default.TrendingUp
          )
        }
      }
    }
  }
}

@Composable
private fun SubMetricItem(
  label: String,
  value: String,
  icon: ImageVector
) {
  Column(
    modifier = Modifier.padding(horizontal = 4.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.65f),
      fontSize = 11.sp,
      fontWeight = FontWeight.Normal
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      color = Color.White,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

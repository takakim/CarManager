package today.takaki.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import today.takaki.R
import today.takaki.data.model.ChartBarData
import today.takaki.data.model.TimeFilter
import java.util.Locale

@Composable
fun SpendingChart(
  bars: List<ChartBarData>,
  timeFilter: TimeFilter,
  currencySymbol: String,
  volumeUnit: String,
  modifier: Modifier = Modifier
) {
  var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
  val maxSpent = (bars.maxOfOrNull { it.amountSpent } ?: 0.0).coerceAtLeast(1.0)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("spending_chart_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
              .size(32.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = "Spending Chart",
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = stringResource(R.string.spending_breakdown),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = when (timeFilter) {
                TimeFilter.THIS_WEEK -> stringResource(R.string.chart_sub_week)
                TimeFilter.THIS_MONTH -> stringResource(R.string.chart_sub_month)
                TimeFilter.THIS_YEAR -> stringResource(R.string.chart_sub_year)
                TimeFilter.SINCE_PURCHASE -> stringResource(R.string.chart_sub_purchase)
                TimeFilter.ALL_TIME -> stringResource(R.string.chart_sub_all)
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Selected Bar Detail Banner if tapped
      val selectedBar = selectedBarIndex?.let { if (it in bars.indices) bars[it] else null }
      if (selectedBar != null) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${selectedBar.label}:",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, selectedBar.amountSpent),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
              )
              if (selectedBar.volumeAmount > 0) {
                Text(
                  text = String.format(Locale.getDefault(), "%.1f %s", selectedBar.volumeAmount, volumeUnit),
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }
        }
      }

      if (bars.isEmpty() || bars.all { it.amountSpent == 0.0 }) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No fuel/energy logged in this period",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Bar Chart Area
        if (bars.size <= 7) {
          // Fixed row for Week or Month with 4-5 weeks
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
          ) {
            bars.forEachIndexed { index, bar ->
              val isSelected = selectedBarIndex == index
              SingleBarItem(
                bar = bar,
                maxSpent = maxSpent,
                currencySymbol = currencySymbol,
                isSelected = isSelected,
                onClick = {
                  selectedBarIndex = if (isSelected) null else index
                },
                modifier = Modifier.weight(1f)
              )
            }
          }
        } else {
          // Scrollable row for 12 months or multi-month history
          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            itemsIndexed(bars) { index, bar ->
              val isSelected = selectedBarIndex == index
              SingleBarItem(
                bar = bar,
                maxSpent = maxSpent,
                currencySymbol = currencySymbol,
                isSelected = isSelected,
                onClick = {
                  selectedBarIndex = if (isSelected) null else index
                },
                modifier = Modifier.width(42.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SingleBarItem(
  bar: ChartBarData,
  maxSpent: Double,
  currencySymbol: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val ratio = (bar.amountSpent / maxSpent).toFloat().coerceIn(0.02f, 1f)
  val animatedRatio by animateFloatAsState(
    targetValue = ratio,
    animationSpec = tween(durationMillis = 600),
    label = "bar_height_anim"
  )

  val barGradient = if (bar.amountSpent > 0) {
    if (isSelected) {
      listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
    } else {
      listOf(Color(0xFF34D399), Color(0xFF059669))
    }
  } else {
    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
  }

  Column(
    modifier = modifier
      .fillMaxHeight()
      .clickable(onClick = onClick)
      .padding(horizontal = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom
  ) {
    // Value on top of bar if > 0
    if (bar.amountSpent > 0) {
      Text(
        text = String.format(Locale.getDefault(), "%s%.0f", currencySymbol, bar.amountSpent),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Animated Bar
    Box(
      modifier = Modifier
        .fillMaxWidth(0.65f)
        .fillMaxHeight(0.72f * animatedRatio)
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
        .background(Brush.verticalGradient(barGradient))
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Label
    Text(
      text = bar.label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      maxLines = 1
    )
  }
}

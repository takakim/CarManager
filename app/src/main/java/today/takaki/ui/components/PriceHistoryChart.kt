package today.takaki.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import today.takaki.R
import today.takaki.data.model.PricePointData
import java.util.Locale

@Composable
fun PriceHistoryChart(
  pricePoints: List<PricePointData>,
  avgPrice: Double,
  currencySymbol: String,
  volumeUnit: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("price_history_chart_card"),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShowChart,
              contentDescription = "Price Trend",
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = stringResource(R.string.unit_price_fluctuation),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = stringResource(R.string.track_price_sub, volumeUnit),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (pricePoints.size < 2) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = stringResource(R.string.chart_need_more_refuels),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val minP = (pricePoints.minOf { it.price } * 0.95)
        val maxP = (pricePoints.maxOf { it.price } * 1.05)
        val range = (maxP - minP).coerceAtLeast(0.01)

        val strokeColor = MaterialTheme.colorScheme.primary
        val avgLineColor = Color(0xFFF59E0B)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
          ) {
            val width = size.width
            val height = size.height
            val paddingH = 16.dp.toPx()
            val paddingV = 16.dp.toPx()

            val effectiveW = width - (paddingH * 2)
            val effectiveH = height - (paddingV * 2)

            // Draw Avg Price Dashed Line
            if (avgPrice in minP..maxP) {
              val avgY = (paddingV + effectiveH * (1f - ((avgPrice - minP) / range).toFloat()))
              drawLine(
                color = avgLineColor,
                start = Offset(paddingH, avgY),
                end = Offset(width - paddingH, avgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
              )
            }

            // Draw Price Trend Line and Gradient Fill
            val path = Path()
            val fillPath = Path()

            val stepX = effectiveW / (pricePoints.size - 1)

            pricePoints.forEachIndexed { index, point ->
              val x = paddingH + index * stepX
              val y = paddingV + effectiveH * (1f - ((point.price - minP) / range).toFloat())

              if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
              } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
              }

              if (index == pricePoints.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
              }
            }

            // Draw Gradient Area under curve
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(strokeColor.copy(alpha = 0.25f), strokeColor.copy(alpha = 0.02f)),
                startY = paddingV,
                endY = height
              )
            )

            // Draw Line
            drawPath(
              path = path,
              color = strokeColor,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Points
            pricePoints.forEachIndexed { index, point ->
              val x = paddingH + index * stepX
              val y = paddingV + effectiveH * (1f - ((point.price - minP) / range).toFloat())

              drawCircle(
                color = strokeColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
              )
              drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(strokeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.price_paid),
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
              modifier = Modifier
                .width(14.dp)
                .height(3.dp)
                .background(avgLineColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = String.format(Locale.getDefault(), "%s %s%.3f", stringResource(R.string.avg_label_short), currencySymbol, avgPrice),
              fontSize = 11.sp,
              color = avgLineColor,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "${pricePoints.first().dateLabel} -> ${pricePoints.last().dateLabel}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

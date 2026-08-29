package com.example.data.model

enum class TimeFilter(val title: String, val shortLabel: String) {
  THIS_WEEK("This Week", "Week"),
  THIS_MONTH("This Month", "Month"),
  THIS_YEAR("This Year", "Year"),
  SINCE_PURCHASE("Since Purchase", "Since Bought"),
  ALL_TIME("All Time", "All Time")
}

data class ChartBarData(
  val label: String, // e.g. "Mon", "Jan", "2024", "Wk 1"
  val amountSpent: Double,
  val volumeAmount: Double,
  val timestamp: Long
)

data class PricePointData(
  val dateLabel: String,
  val price: Double,
  val timestamp: Long
)

data class PeriodSummary(
  val timeFilter: TimeFilter,
  val startDateMillis: Long,
  val endDateMillis: Long,
  val totalSpent: Double = 0.0,
  val totalVolume: Double = 0.0,
  val logCount: Int = 0,
  val avgPricePaid: Double = 0.0, // Weighted average: totalSpent / totalVolume
  val minPricePaid: Double = 0.0,
  val maxPricePaid: Double = 0.0,
  val latestPricePaid: Double = 0.0,
  val totalDistance: Double = 0.0, // Distance driven in this period
  val averageEconomy: Double = 0.0, // L/100km or MPG
  val costPerDistance: Double = 0.0, // $/km or $/mi
  val spendingPerDay: Double = 0.0,
  val spendingPerWeek: Double = 0.0,
  val spendingPerMonth: Double = 0.0,
  val chartBars: List<ChartBarData> = emptyList(),
  val pricePoints: List<PricePointData> = emptyList()
)

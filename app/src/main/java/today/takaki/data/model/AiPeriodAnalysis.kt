package today.takaki.data.model

data class AiFinding(
  val title: String,
  val description: String,
  val impact: String = "NEUTRAL", // "POSITIVE", "NEUTRAL", "NEGATIVE"
  val category: String = "EFFICIENCY" // "EFFICIENCY", "PRICE", "DISTANCE", "MAINTENANCE"
)

data class AiRecommendation(
  val title: String,
  val description: String,
  val estimatedSavings: String? = null,
  val iconType: String = "SAVINGS" // "SAVINGS", "DRIVING", "STATION", "CHARGE"
)

data class AiPeriodAnalysis(
  val periodTitle: String,
  val timeFilter: TimeFilter,
  val headline: String,
  val efficiencyScore: Int, // 0 - 100
  val efficiencyGrade: String, // "Optimal", "Good", "Moderate", "High Consumption"
  val keyFindings: List<AiFinding>,
  val costAttributionSummary: String,
  val recommendations: List<AiRecommendation>,
  val nextPeriodForecast: String,
  val isAiGenerated: Boolean = true,
  val timestamp: Long = System.currentTimeMillis()
)

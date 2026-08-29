package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChartBarData
import com.example.data.model.DistanceUnit
import com.example.data.model.EconomyUnit
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.data.model.PeriodSummary
import com.example.data.model.PricePointData
import com.example.data.model.TimeFilter
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit
import com.example.data.repository.FuelTrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UiState(
  val vehicle: VehicleProfile = VehicleProfile(),
  val logs: List<FuelLog> = emptyList(),
  val selectedFilter: TimeFilter = TimeFilter.THIS_MONTH,
  val currentSummary: PeriodSummary = PeriodSummary(TimeFilter.THIS_MONTH, 0L, 0L),
  val weekSummary: PeriodSummary = PeriodSummary(TimeFilter.THIS_WEEK, 0L, 0L),
  val monthSummary: PeriodSummary = PeriodSummary(TimeFilter.THIS_MONTH, 0L, 0L),
  val yearSummary: PeriodSummary = PeriodSummary(TimeFilter.THIS_YEAR, 0L, 0L),
  val sincePurchaseSummary: PeriodSummary = PeriodSummary(TimeFilter.SINCE_PURCHASE, 0L, 0L),
  val allTimeSummary: PeriodSummary = PeriodSummary(TimeFilter.ALL_TIME, 0L, 0L),
  val allTimeAvgPrice: Double = 0.0,
  val recentAvgPrice: Double = 0.0,
  val isLoading: Boolean = false,
  val message: String? = null
)

class FuelTrackerViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: FuelTrackerRepository

  private val _selectedFilter = MutableStateFlow(TimeFilter.THIS_MONTH)
  val selectedFilter = _selectedFilter.asStateFlow()

  private val _uiState = MutableStateFlow(UiState(isLoading = true))
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  init {
    val db = AppDatabase.getInstance(application)
    repository = FuelTrackerRepository(db.fuelLogDao(), db.vehicleProfileDao())

    viewModelScope.launch {
      repository.ensureDefaultVehicleCreated()
    }

    viewModelScope.launch {
      combine(
        repository.defaultVehicle,
        repository.getLogsForVehicle(1),
        _selectedFilter
      ) { vehicle, logs, filter ->
        val safeVehicle = vehicle ?: VehicleProfile()

        val weekSum = calculatePeriodSummary(TimeFilter.THIS_WEEK, safeVehicle, logs)
        val monthSum = calculatePeriodSummary(TimeFilter.THIS_MONTH, safeVehicle, logs)
        val yearSum = calculatePeriodSummary(TimeFilter.THIS_YEAR, safeVehicle, logs)
        val sincePurchaseSum = calculatePeriodSummary(TimeFilter.SINCE_PURCHASE, safeVehicle, logs)
        val allTimeSum = calculatePeriodSummary(TimeFilter.ALL_TIME, safeVehicle, logs)

        val activeSum = when (filter) {
          TimeFilter.THIS_WEEK -> weekSum
          TimeFilter.THIS_MONTH -> monthSum
          TimeFilter.THIS_YEAR -> yearSum
          TimeFilter.SINCE_PURCHASE -> sincePurchaseSum
          TimeFilter.ALL_TIME -> allTimeSum
        }

        val allTimeAvg = if (logs.isNotEmpty()) {
          val totSpent = logs.sumOf { it.totalCost }
          val totVol = logs.sumOf { it.amount }
          if (totVol > 0) totSpent / totVol else 0.0
        } else 0.0

        val recentLogs = logs.take(5)
        val recentAvg = if (recentLogs.isNotEmpty()) {
          val rSpent = recentLogs.sumOf { it.totalCost }
          val rVol = recentLogs.sumOf { it.amount }
          if (rVol > 0) rSpent / rVol else 0.0
        } else 0.0

        UiState(
          vehicle = safeVehicle,
          logs = logs,
          selectedFilter = filter,
          currentSummary = activeSum,
          weekSummary = weekSum,
          monthSummary = monthSum,
          yearSummary = yearSum,
          sincePurchaseSummary = sincePurchaseSum,
          allTimeSummary = allTimeSum,
          allTimeAvgPrice = allTimeAvg,
          recentAvgPrice = recentAvg,
          isLoading = false
        )
      }.collect { state ->
        _uiState.value = state
      }
    }
  }

  fun setFilter(filter: TimeFilter) {
    _selectedFilter.value = filter
  }

  fun addLog(
    odometer: Double,
    amount: Double,
    unitPrice: Double,
    totalCost: Double,
    dateMillis: Long,
    isFullTank: Boolean,
    isMissedPrevious: Boolean,
    stationName: String,
    fuelGrade: String,
    notes: String
  ) {
    viewModelScope.launch {
      val log = FuelLog(
        vehicleId = _uiState.value.vehicle.id,
        timestamp = dateMillis,
        odometer = odometer,
        amount = amount,
        unitPrice = unitPrice,
        totalCost = totalCost,
        isFullTank = isFullTank,
        isMissedPrevious = isMissedPrevious,
        stationName = stationName,
        fuelGrade = fuelGrade,
        notes = notes
      )
      repository.insertLog(log)
    }
  }

  fun updateLog(log: FuelLog) {
    viewModelScope.launch {
      repository.updateLog(log)
    }
  }

  fun deleteLog(log: FuelLog) {
    viewModelScope.launch {
      repository.deleteLog(log)
    }
  }

  fun deleteLogById(logId: Long) {
    viewModelScope.launch {
      repository.deleteLogById(logId)
    }
  }

  fun updateVehicleProfile(profile: VehicleProfile) {
    viewModelScope.launch {
      repository.updateVehicle(profile)
    }
  }

  fun loadSampleData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      repository.populateSampleData(_uiState.value.vehicle.id)
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData(_uiState.value.vehicle.id)
    }
  }

  // --- Calculation Helpers ---

  private fun calculatePeriodSummary(
    filter: TimeFilter,
    vehicle: VehicleProfile,
    allLogs: List<FuelLog>
  ): PeriodSummary {
    val now = Calendar.getInstance()
    val startCal = Calendar.getInstance()

    when (filter) {
      TimeFilter.THIS_WEEK -> {
        startCal.firstDayOfWeek = Calendar.MONDAY
        startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
      }
      TimeFilter.THIS_MONTH -> {
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
      }
      TimeFilter.THIS_YEAR -> {
        startCal.set(Calendar.DAY_OF_YEAR, 1)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
      }
      TimeFilter.SINCE_PURCHASE -> {
        startCal.timeInMillis = vehicle.purchaseDateMillis
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
      }
      TimeFilter.ALL_TIME -> {
        val earliestLogTime = allLogs.minOfOrNull { it.timestamp } ?: vehicle.purchaseDateMillis
        startCal.timeInMillis = minOf(earliestLogTime, vehicle.purchaseDateMillis)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
      }
    }

    val startTime = startCal.timeInMillis
    val endTime = now.timeInMillis

    val filteredLogs = allLogs.filter { it.timestamp >= startTime && it.timestamp <= endTime }
      .sortedBy { it.odometer }

    val totalSpent = filteredLogs.sumOf { it.totalCost }
    val totalVolume = filteredLogs.sumOf { it.amount }
    val logCount = filteredLogs.size

    val avgPrice = if (totalVolume > 0) totalSpent / totalVolume else 0.0
    val minPrice = filteredLogs.minOfOrNull { it.unitPrice } ?: 0.0
    val maxPrice = filteredLogs.maxOfOrNull { it.unitPrice } ?: 0.0
    val latestPrice = filteredLogs.maxByOrNull { it.timestamp }?.unitPrice
      ?: (allLogs.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0)

    // Distance calculation
    val totalDistance = when {
      filter == TimeFilter.SINCE_PURCHASE -> {
        val maxOdo = allLogs.maxOfOrNull { it.odometer } ?: vehicle.initialOdometer
        (maxOdo - vehicle.initialOdometer).coerceAtLeast(0.0)
      }
      filteredLogs.size >= 2 -> {
        val minOdo = filteredLogs.minOf { it.odometer }
        val maxOdo = filteredLogs.maxOf { it.odometer }
        (maxOdo - minOdo).coerceAtLeast(0.0)
      }
      filteredLogs.size == 1 && filter != TimeFilter.ALL_TIME -> {
        // Estimate distance since previous log or initial
        val current = filteredLogs.first()
        val prevLog = allLogs.filter { it.odometer < current.odometer }.maxByOrNull { it.odometer }
        if (prevLog != null) (current.odometer - prevLog.odometer).coerceAtLeast(0.0) else 0.0
      }
      else -> {
        if (allLogs.isNotEmpty()) {
          val minOdo = allLogs.minOf { it.odometer }
          val maxOdo = allLogs.maxOf { it.odometer }
          (maxOdo - minOdo).coerceAtLeast(0.0)
        } else 0.0
      }
    }

    // Fuel economy
    val avgEconomy = EconomyUnit.calculate(totalDistance, totalVolume, vehicle.economyUnit)

    val costPerDist = if (totalDistance > 0) totalSpent / totalDistance else 0.0

    // Days in period
    val daysInPeriod = maxOf(1.0, (endTime - startTime).toDouble() / (1000 * 60 * 60 * 24))
    val spendingPerDay = totalSpent / daysInPeriod
    val spendingPerWeek = spendingPerDay * 7.0
    val spendingPerMonth = spendingPerDay * 30.4375

    // Build Chart Bars & Price Points
    val chartBars = generateChartBars(filter, startTime, endTime, filteredLogs)
    val pricePoints = filteredLogs.sortedBy { it.timestamp }.map { log ->
      val df = SimpleDateFormat("MMM dd", Locale.getDefault())
      PricePointData(
        dateLabel = df.format(Date(log.timestamp)),
        price = log.unitPrice,
        timestamp = log.timestamp
      )
    }

    return PeriodSummary(
      timeFilter = filter,
      startDateMillis = startTime,
      endDateMillis = endTime,
      totalSpent = totalSpent,
      totalVolume = totalVolume,
      logCount = logCount,
      avgPricePaid = avgPrice,
      minPricePaid = minPrice,
      maxPricePaid = maxPrice,
      latestPricePaid = latestPrice,
      totalDistance = totalDistance,
      averageEconomy = avgEconomy,
      economyUnit = vehicle.economyUnit,
      costPerDistance = costPerDist,
      spendingPerDay = spendingPerDay,
      spendingPerWeek = spendingPerWeek,
      spendingPerMonth = spendingPerMonth,
      chartBars = chartBars,
      pricePoints = pricePoints
    )
  }

  private fun generateChartBars(
    filter: TimeFilter,
    startTime: Long,
    endTime: Long,
    logs: List<FuelLog>
  ): List<ChartBarData> {
    val bars = mutableListOf<ChartBarData>()
    val cal = Calendar.getInstance()

    when (filter) {
      TimeFilter.THIS_WEEK -> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        cal.timeInMillis = startTime
        for (i in 0..6) {
          val dayStart = cal.timeInMillis
          cal.add(Calendar.DAY_OF_YEAR, 1)
          val dayEnd = cal.timeInMillis
          val dayLogs = logs.filter { it.timestamp in dayStart until dayEnd }
          bars.add(
            ChartBarData(
              label = days[i],
              amountSpent = dayLogs.sumOf { it.totalCost },
              volumeAmount = dayLogs.sumOf { it.amount },
              timestamp = dayStart
            )
          )
        }
      }
      TimeFilter.THIS_MONTH -> {
        cal.timeInMillis = startTime
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        var weekIndex = 1
        var dayPointer = 1
        while (dayPointer <= maxDays) {
          val wStartCal = cal.clone() as Calendar
          wStartCal.set(Calendar.DAY_OF_MONTH, dayPointer)
          val wStart = wStartCal.timeInMillis
          
          val daysInBucket = minOf(7, maxDays - dayPointer + 1)
          wStartCal.add(Calendar.DAY_OF_MONTH, daysInBucket)
          val wEnd = wStartCal.timeInMillis

          val weekLogs = logs.filter { it.timestamp in wStart until wEnd }
          bars.add(
            ChartBarData(
              label = "Wk $weekIndex",
              amountSpent = weekLogs.sumOf { it.totalCost },
              volumeAmount = weekLogs.sumOf { it.amount },
              timestamp = wStart
            )
          )
          dayPointer += daysInBucket
          weekIndex++
        }
      }
      TimeFilter.THIS_YEAR -> {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        cal.timeInMillis = startTime
        val currentYear = cal.get(Calendar.YEAR)
        for (m in 0..11) {
          val mStartCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
          val mEndCal = mStartCal.clone() as Calendar
          mEndCal.add(Calendar.MONTH, 1)

          val mLogs = logs.filter { it.timestamp in mStartCal.timeInMillis until mEndCal.timeInMillis }
          bars.add(
            ChartBarData(
              label = monthNames[m],
              amountSpent = mLogs.sumOf { it.totalCost },
              volumeAmount = mLogs.sumOf { it.amount },
              timestamp = mStartCal.timeInMillis
            )
          )
        }
      }
      TimeFilter.SINCE_PURCHASE, TimeFilter.ALL_TIME -> {
        // Group by month over the past 6-12 months or purchase period
        val df = SimpleDateFormat("MMM yy", Locale.getDefault())
        cal.timeInMillis = startTime
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val now = Calendar.getInstance()

        var iterations = 0
        while (cal.before(now) && iterations < 18) {
          val mStart = cal.timeInMillis
          val label = df.format(cal.time)
          cal.add(Calendar.MONTH, 1)
          val mEnd = cal.timeInMillis

          val mLogs = logs.filter { it.timestamp in mStart until mEnd }
          bars.add(
            ChartBarData(
              label = label,
              amountSpent = mLogs.sumOf { it.totalCost },
              volumeAmount = mLogs.sumOf { it.amount },
              timestamp = mStart
            )
          )
          iterations++
        }
      }
    }
    return bars
  }
}

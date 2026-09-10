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
import com.example.data.model.VehicleHistorySummary
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit
import com.example.data.repository.FuelTrackerRepository
import com.example.data.util.ImportExportHelper
import com.example.data.model.AiPeriodAnalysis
import com.example.data.advisor.OnDeviceSmartAdvisorService
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UiState(
  val vehicle: VehicleProfile = VehicleProfile(),
  val allVehicles: List<VehicleProfile> = emptyList(),
  val carHistorySummaries: List<VehicleHistorySummary> = emptyList(),
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
  val isSmartAdvisorEnabled: Boolean = true,
  val aiAnalysis: AiPeriodAnalysis? = null,
  val isAiAnalyzing: Boolean = false,
  val isLoading: Boolean = false,
  val userMessage: String? = null
)

class FuelTrackerViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: FuelTrackerRepository
  private val onDeviceAdvisorService = OnDeviceSmartAdvisorService()
  private val prefs = application.getSharedPreferences("fuel_tracker_prefs", Context.MODE_PRIVATE)

  private val _selectedFilter = MutableStateFlow(TimeFilter.THIS_MONTH)
  val selectedFilter = _selectedFilter.asStateFlow()

  private val _activeVehicleId = MutableStateFlow<Int?>(null)
  val activeVehicleId = _activeVehicleId.asStateFlow()

  private val _uiState = MutableStateFlow(
    UiState(
      isLoading = true,
      isSmartAdvisorEnabled = prefs.getBoolean("smart_advisor_enabled", true)
    )
  )
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  init {
    val db = AppDatabase.getInstance(application)
    repository = FuelTrackerRepository(db.fuelLogDao(), db.vehicleProfileDao())

    viewModelScope.launch {
      repository.ensureDefaultVehicleCreated()
    }

    viewModelScope.launch {
      combine(
        repository.allVehicles,
        repository.allLogs,
        _activeVehicleId,
        _selectedFilter
      ) { allVehicles, allLogs, activeId, filter ->
        // 1. Pick Active Vehicle:
        val activeVehicle = if (activeId != null) {
          allVehicles.find { it.id == activeId }
            ?: allVehicles.firstOrNull { !it.isArchived }
            ?: allVehicles.firstOrNull()
            ?: VehicleProfile()
        } else {
          allVehicles.firstOrNull { !it.isArchived }
            ?: allVehicles.firstOrNull()
            ?: VehicleProfile()
        }

        // 2. Logs for active vehicle
        val activeLogs = allLogs
          .filter { it.vehicleId == activeVehicle.id }
          .sortedWith(compareByDescending<FuelLog> { it.odometer }.thenByDescending { it.timestamp })

        // 3. Period Summaries for active vehicle
        val weekSum = calculatePeriodSummary(TimeFilter.THIS_WEEK, activeVehicle, activeLogs)
        val monthSum = calculatePeriodSummary(TimeFilter.THIS_MONTH, activeVehicle, activeLogs)
        val yearSum = calculatePeriodSummary(TimeFilter.THIS_YEAR, activeVehicle, activeLogs)
        val sincePurchaseSum = calculatePeriodSummary(TimeFilter.SINCE_PURCHASE, activeVehicle, activeLogs)
        val allTimeSum = calculatePeriodSummary(TimeFilter.ALL_TIME, activeVehicle, activeLogs)

        val activeSum = when (filter) {
          TimeFilter.THIS_WEEK -> weekSum
          TimeFilter.THIS_MONTH -> monthSum
          TimeFilter.THIS_YEAR -> yearSum
          TimeFilter.SINCE_PURCHASE -> sincePurchaseSum
          TimeFilter.ALL_TIME -> allTimeSum
        }

        val allTimeAvg = if (activeLogs.isNotEmpty()) {
          val totSpent = activeLogs.sumOf { it.totalCost }
          val totVol = activeLogs.sumOf { it.amount }
          if (totVol > 0) totSpent / totVol else 0.0
        } else 0.0

        val recentLogs = activeLogs.take(5)
        val recentAvg = if (recentLogs.isNotEmpty()) {
          val rSpent = recentLogs.sumOf { it.totalCost }
          val rVol = recentLogs.sumOf { it.amount }
          if (rVol > 0) rSpent / rVol else 0.0
        } else 0.0

        // 4. Calculate Car History Summaries for ALL vehicles
        val historySummaries = allVehicles.map { v ->
          val vLogs = allLogs.filter { it.vehicleId == v.id }.sortedBy { it.odometer }
          val totalSpent = vLogs.sumOf { it.totalCost }
          val totalVol = vLogs.sumOf { it.amount }

          val maxOdo = if (v.isArchived && v.archiveOdometer != null && v.archiveOdometer > 0) {
            v.archiveOdometer
          } else {
            vLogs.maxOfOrNull { it.odometer } ?: v.initialOdometer
          }

          val totalDist = (maxOdo - v.initialOdometer).coerceAtLeast(0.0)
          val avgEcon = EconomyUnit.calculate(totalDist, totalVol, v.economyUnit)
          val costDist = if (totalDist > 0) totalSpent / totalDist else 0.0
          val avgUnitPrice = if (totalVol > 0) totalSpent / totalVol else 0.0

          val endDate = if (v.isArchived && v.archiveDateMillis != null) v.archiveDateMillis else System.currentTimeMillis()
          val days = ((endDate - v.purchaseDateMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)

          VehicleHistorySummary(
            vehicle = v,
            logsCount = vLogs.size,
            totalSpent = totalSpent,
            totalDistance = totalDist,
            totalVolume = totalVol,
            averageEconomy = avgEcon,
            costPerDistance = costDist,
            avgPricePaid = avgUnitPrice,
            latestOdometer = maxOdo,
            ownershipDays = days,
            isActive = (v.id == activeVehicle.id)
          )
        }

        UiState(
          vehicle = activeVehicle,
          allVehicles = allVehicles,
          carHistorySummaries = historySummaries,
          logs = activeLogs,
          selectedFilter = filter,
          currentSummary = activeSum,
          weekSummary = weekSum,
          monthSummary = monthSum,
          yearSummary = yearSum,
          sincePurchaseSummary = sincePurchaseSum,
          allTimeSummary = allTimeSum,
          allTimeAvgPrice = allTimeAvg,
          recentAvgPrice = recentAvg,
          isSmartAdvisorEnabled = _uiState.value.isSmartAdvisorEnabled,
          aiAnalysis = _uiState.value.aiAnalysis,
          isAiAnalyzing = _uiState.value.isAiAnalyzing,
          isLoading = false,
          userMessage = _uiState.value.userMessage
        )
      }.collect { state ->
        _uiState.value = state
      }
    }
  }

  fun toggleSmartAdvisor(enabled: Boolean) {
    prefs.edit().putBoolean("smart_advisor_enabled", enabled).apply()
    _uiState.update {
      it.copy(
        isSmartAdvisorEnabled = enabled,
        userMessage = if (enabled) "On-Device Smart Advisor enabled" else "Smart Advisor disabled"
      )
    }
    if (enabled && _uiState.value.aiAnalysis == null) {
      requestAiAnalysis()
    }
  }

  fun requestAiAnalysis(filter: TimeFilter? = null) {
    val currentUiState = _uiState.value
    if (!currentUiState.isSmartAdvisorEnabled) return

    val targetFilter = filter ?: _selectedFilter.value
    val targetSummary = when (targetFilter) {
      TimeFilter.THIS_WEEK -> currentUiState.weekSummary
      TimeFilter.THIS_MONTH -> currentUiState.monthSummary
      TimeFilter.THIS_YEAR -> currentUiState.yearSummary
      TimeFilter.SINCE_PURCHASE -> currentUiState.sincePurchaseSummary
      TimeFilter.ALL_TIME -> currentUiState.allTimeSummary
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isAiAnalyzing = true) }
      try {
        val analysis = onDeviceAdvisorService.analyzePeriod(
          vehicle = currentUiState.vehicle,
          summary = targetSummary,
          periodLogs = currentUiState.logs.filter {
            if (targetSummary.startDateMillis > 0 && targetSummary.endDateMillis > 0) {
              it.timestamp in targetSummary.startDateMillis..targetSummary.endDateMillis
            } else true
          },
          allTimeAvgPrice = currentUiState.allTimeAvgPrice,
          allTimeSummary = currentUiState.allTimeSummary
        )
        _uiState.update {
          it.copy(
            aiAnalysis = analysis,
            isAiAnalyzing = false,
            userMessage = "Smart period insights updated"
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isAiAnalyzing = false,
            userMessage = "Analysis error: ${e.localizedMessage}"
          )
        }
      }
    }
  }

  fun setFilter(filter: TimeFilter) {
    _selectedFilter.value = filter
  }

  fun selectVehicle(vehicleId: Int) {
    _activeVehicleId.value = vehicleId
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

  fun addNewVehicle(profile: VehicleProfile) {
    viewModelScope.launch {
      val newId = repository.createVehicle(profile.copy(id = 0, isArchived = false))
      _activeVehicleId.value = newId.toInt()
      _uiState.value = _uiState.value.copy(userMessage = "Added ${profile.name} to garage!")
    }
  }

  fun archiveCurrentVehicle(
    archiveDateMillis: Long,
    archiveOdometer: Double,
    reason: String,
    createReplacement: Boolean,
    replacementVehicle: VehicleProfile? = null
  ) {
    viewModelScope.launch {
      val currentVehicle = _uiState.value.vehicle
      repository.archiveVehicle(currentVehicle.id, archiveDateMillis, archiveOdometer, reason)

      if (createReplacement && replacementVehicle != null) {
        val newId = repository.createVehicle(
          replacementVehicle.copy(
            id = 0,
            isArchived = false,
            archiveDateMillis = null,
            archiveOdometer = null,
            archiveReason = ""
          )
        )
        _activeVehicleId.value = newId.toInt()
        _uiState.value = _uiState.value.copy(
          userMessage = "${currentVehicle.name} archived. Switched to ${replacementVehicle.name}!"
        )
      } else {
        // Switch to any remaining active vehicle
        val remainingActive = _uiState.value.allVehicles.firstOrNull { it.id != currentVehicle.id && !it.isArchived }
        if (remainingActive != null) {
          _activeVehicleId.value = remainingActive.id
        }
        _uiState.value = _uiState.value.copy(
          userMessage = "${currentVehicle.name} archived and saved to Car History."
        )
      }
    }
  }

  fun reactivateVehicle(vehicleId: Int) {
    viewModelScope.launch {
      repository.reactivateVehicle(vehicleId)
      _activeVehicleId.value = vehicleId
      _uiState.value = _uiState.value.copy(userMessage = "Vehicle reactivated as active vehicle.")
    }
  }

  fun deleteVehicle(vehicleId: Int) {
    viewModelScope.launch {
      repository.deleteVehicle(vehicleId)
      val remaining = _uiState.value.allVehicles.filter { it.id != vehicleId }
      val nextActive = remaining.firstOrNull { !it.isArchived } ?: remaining.firstOrNull()
      if (nextActive != null) {
        _activeVehicleId.value = nextActive.id
      }
      _uiState.value = _uiState.value.copy(userMessage = "Vehicle deleted from database.")
    }
  }

  fun loadSampleData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      repository.populateSampleData(_uiState.value.vehicle.id)
      _uiState.value = _uiState.value.copy(userMessage = "Sample active & archived cars loaded!")
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData(_uiState.value.vehicle.id)
      _uiState.value = _uiState.value.copy(userMessage = "Refuel logs cleared.")
    }
  }

  fun clearMessage() {
    _uiState.value = _uiState.value.copy(userMessage = null)
  }

  // --- Import / Export Handlers ---

  fun exportCurrentVehicleCsv(): String {
    return ImportExportHelper.exportToCsv(_uiState.value.vehicle, _uiState.value.logs)
  }

  fun exportCurrentVehicleJson(): String {
    return ImportExportHelper.exportToJson(listOf(_uiState.value.vehicle), _uiState.value.logs)
  }

  fun exportAllDataJson(): String {
    return ImportExportHelper.exportToJson(_uiState.value.allVehicles, _uiState.value.logs)
  }

  fun importCsv(csvText: String, replace: Boolean): Pair<Boolean, String> {
    return try {
      val parsedLogs = ImportExportHelper.parseCsv(csvText, _uiState.value.vehicle.id)
      if (parsedLogs.isEmpty()) {
        Pair(false, "No valid fuel log rows detected in CSV format.")
      } else {
        viewModelScope.launch {
          repository.importLogs(_uiState.value.vehicle.id, parsedLogs, replace)
        }
        Pair(true, "Successfully imported ${parsedLogs.size} logs into ${_uiState.value.vehicle.name}.")
      }
    } catch (e: Exception) {
      Pair(false, "Error parsing CSV: ${e.localizedMessage ?: "Unknown format error"}")
    }
  }

  fun importBackupJson(jsonText: String, replaceAll: Boolean): Pair<Boolean, String> {
    return try {
      val backup = ImportExportHelper.parseJson(jsonText, _uiState.value.vehicle.id)
      if (backup.logs.isEmpty() && backup.vehicles.isEmpty()) {
        Pair(false, "No vehicles or refuels found in the JSON backup.")
      } else {
        viewModelScope.launch {
          repository.importFullBackup(backup, replaceAll)
          if (backup.vehicles.isNotEmpty()) {
            _activeVehicleId.value = backup.vehicles.first().id
          }
        }
        Pair(
          true,
          "Successfully imported ${backup.vehicles.size} vehicles and ${backup.logs.size} logs."
        )
      }
    } catch (e: Exception) {
      Pair(false, "Error reading JSON: ${e.localizedMessage ?: "Invalid JSON syntax"}")
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
      }
      TimeFilter.ALL_TIME -> {
        val minTs = allLogs.minOfOrNull { it.timestamp } ?: vehicle.purchaseDateMillis
        startCal.timeInMillis = minOf(minTs, vehicle.purchaseDateMillis)
      }
    }

    val startTime = startCal.timeInMillis
    val endTime = now.timeInMillis

    // Filter logs within this window
    val periodLogs = allLogs.filter { it.timestamp in startTime..endTime }.sortedBy { it.odometer }

    val totalSpent = periodLogs.sumOf { it.totalCost }
    val totalVolume = periodLogs.sumOf { it.amount }

    // Distance in period:
    val totalDistance = if (periodLogs.isNotEmpty()) {
      val minOdo = if (filter == TimeFilter.SINCE_PURCHASE) {
        vehicle.initialOdometer
      } else {
        periodLogs.first().odometer
      }
      val maxOdo = periodLogs.last().odometer
      (maxOdo - minOdo).coerceAtLeast(0.0)
    } else {
      0.0
    }

    // Price extremes & latest
    val unitPrices = periodLogs.map { it.unitPrice }.filter { it > 0 }
    val avgPrice = if (unitPrices.isNotEmpty()) totalSpent / totalVolume else 0.0
    val minPrice = unitPrices.minOrNull() ?: 0.0
    val maxPrice = unitPrices.maxOrNull() ?: 0.0
    val latestPrice = allLogs.maxByOrNull { it.timestamp }?.unitPrice ?: 0.0

    // Fuel economy
    val avgEconomy = EconomyUnit.calculate(totalDistance, totalVolume, vehicle.economyUnit)
    val costPerDist = if (totalDistance > 0) totalSpent / totalDistance else 0.0

    val daysDiff = ((endTime - startTime) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
    val spendingPerDay = totalSpent / daysDiff
    val spendingPerWeek = spendingPerDay * 7.0
    val spendingPerMonth = spendingPerDay * 30.4375

    val spendingBars = generateSpendingBars(filter, periodLogs, startTime, endTime)
    val priceDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val pricePoints = allLogs.sortedBy { it.timestamp }.takeLast(20).map {
      PricePointData(
        dateLabel = priceDateFormat.format(Date(it.timestamp)),
        price = it.unitPrice,
        timestamp = it.timestamp
      )
    }

    return PeriodSummary(
      timeFilter = filter,
      startDateMillis = startTime,
      endDateMillis = endTime,
      totalSpent = totalSpent,
      totalVolume = totalVolume,
      logCount = periodLogs.size,
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
      chartBars = spendingBars,
      pricePoints = pricePoints
    )
  }

  private fun generateSpendingBars(
    filter: TimeFilter,
    logs: List<FuelLog>,
    startTime: Long,
    endTime: Long
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
        val weeks = listOf("W1", "W2", "W3", "W4", "W5")
        cal.timeInMillis = startTime
        for (w in 0..4) {
          val wStart = cal.timeInMillis
          cal.add(Calendar.DAY_OF_MONTH, 7)
          val wEnd = cal.timeInMillis
          val wLogs = logs.filter { it.timestamp in wStart until wEnd }
          bars.add(
            ChartBarData(
              label = weeks[w],
              amountSpent = wLogs.sumOf { it.totalCost },
              volumeAmount = wLogs.sumOf { it.amount },
              timestamp = wStart
            )
          )
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

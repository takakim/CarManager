package com.example.data.repository

import com.example.data.db.FuelLogDao
import com.example.data.db.VehicleProfileDao
import com.example.data.model.DistanceUnit
import com.example.data.model.EconomyUnit
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit
import com.example.data.util.ParsedBackup
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FuelTrackerRepository(
  private val fuelLogDao: FuelLogDao,
  private val vehicleProfileDao: VehicleProfileDao
) {
  val defaultVehicle: Flow<VehicleProfile?> = vehicleProfileDao.getDefaultVehicle()
  val allVehicles: Flow<List<VehicleProfile>> = vehicleProfileDao.getAllVehicles()
  val activeVehicles: Flow<List<VehicleProfile>> = vehicleProfileDao.getActiveVehicles()
  val archivedVehicles: Flow<List<VehicleProfile>> = vehicleProfileDao.getArchivedVehicles()
  val allLogs: Flow<List<FuelLog>> = fuelLogDao.getAllLogs()

  fun getLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>> =
    fuelLogDao.getLogsForVehicle(vehicleId)

  suspend fun getLogsListForVehicle(vehicleId: Int): List<FuelLog> =
    fuelLogDao.getLogsListForVehicle(vehicleId)

  suspend fun getAllVehiclesDirect(): List<VehicleProfile> =
    vehicleProfileDao.getAllVehiclesDirect()

  suspend fun getAllLogsDirect(): List<FuelLog> =
    fuelLogDao.getAllLogsDirect()

  fun getVehicleById(vehicleId: Int): Flow<VehicleProfile?> =
    vehicleProfileDao.getVehicleById(vehicleId)

  suspend fun getVehicleByIdDirect(vehicleId: Int): VehicleProfile? =
    vehicleProfileDao.getVehicleByIdDirect(vehicleId)

  suspend fun insertLog(log: FuelLog): Long = fuelLogDao.insertLog(log)

  suspend fun updateLog(log: FuelLog) = fuelLogDao.updateLog(log)

  suspend fun deleteLog(log: FuelLog) = fuelLogDao.deleteLog(log)

  suspend fun deleteLogById(logId: Long) = fuelLogDao.deleteLogById(logId)

  suspend fun updateVehicle(profile: VehicleProfile) = vehicleProfileDao.insertOrUpdate(profile)

  suspend fun createVehicle(profile: VehicleProfile): Long = vehicleProfileDao.insertOrUpdate(profile)

  suspend fun archiveVehicle(
    vehicleId: Int,
    archiveDateMillis: Long,
    archiveOdometer: Double,
    reason: String
  ) {
    val existing = vehicleProfileDao.getVehicleByIdDirect(vehicleId) ?: return
    val updated = existing.copy(
      isArchived = true,
      archiveDateMillis = archiveDateMillis,
      archiveOdometer = archiveOdometer,
      archiveReason = reason
    )
    vehicleProfileDao.update(updated)
  }

  suspend fun reactivateVehicle(vehicleId: Int) {
    val existing = vehicleProfileDao.getVehicleByIdDirect(vehicleId) ?: return
    val updated = existing.copy(
      isArchived = false,
      archiveDateMillis = null,
      archiveOdometer = null,
      archiveReason = ""
    )
    vehicleProfileDao.update(updated)
  }

  suspend fun deleteVehicle(vehicleId: Int) {
    fuelLogDao.deleteAllLogsForVehicle(vehicleId)
    vehicleProfileDao.deleteVehicleById(vehicleId)
  }

  suspend fun importLogs(vehicleId: Int, logs: List<FuelLog>, replace: Boolean) {
    if (replace) {
      fuelLogDao.deleteAllLogsForVehicle(vehicleId)
    }
    val logsToInsert = logs.map { it.copy(id = 0L, vehicleId = vehicleId) }
    fuelLogDao.insertLogs(logsToInsert)
  }

  suspend fun importFullBackup(backup: ParsedBackup, replaceAll: Boolean) {
    if (replaceAll) {
      fuelLogDao.clearAllLogs()
    }
    // Insert vehicles
    backup.vehicles.forEach { v ->
      vehicleProfileDao.insertOrUpdate(v)
    }
    // Insert logs
    val logsToInsert = backup.logs.map { it.copy(id = 0L) }
    fuelLogDao.insertLogs(logsToInsert)
  }

  suspend fun ensureDefaultVehicleCreated(): VehicleProfile {
    val existing = vehicleProfileDao.getDefaultVehicleDirect()
    if (existing != null) return existing

    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -8)
    val purchaseDate = cal.timeInMillis

    val defaultProf = VehicleProfile(
      id = 1,
      name = "My Car",
      makeModel = "Toyota Corolla Hybrid",
      year = 2023,
      fuelType = FuelType.HYBRID,
      purchaseDateMillis = purchaseDate,
      purchasePrice = 24500.0,
      initialOdometer = 12000.0,
      currencySymbol = "$",
      distanceUnit = DistanceUnit.KILOMETERS,
      volumeUnit = VolumeUnit.LITERS,
      economyUnit = EconomyUnit.L_PER_100KM,
      tankCapacity = 43.0,
      isArchived = false
    )
    vehicleProfileDao.insertOrUpdate(defaultProf)
    return defaultProf
  }

  suspend fun populateSampleData(targetVehicleId: Int = 1) {
    val now = Calendar.getInstance()

    // 1. ACTIVE VEHICLE (ID 1)
    val purchaseCal = Calendar.getInstance().apply { add(Calendar.MONTH, -8) }
    val purchaseDate = purchaseCal.timeInMillis
    val initialOdo = 14500.0

    val activeVehicle = VehicleProfile(
      id = targetVehicleId,
      name = "Daily Driver",
      makeModel = "Honda Civic 2.0",
      year = 2023,
      fuelType = FuelType.GASOLINE,
      purchaseDateMillis = purchaseDate,
      purchasePrice = 25000.0,
      initialOdometer = initialOdo,
      currencySymbol = "$",
      distanceUnit = DistanceUnit.KILOMETERS,
      volumeUnit = VolumeUnit.LITERS,
      economyUnit = EconomyUnit.L_PER_100KM,
      tankCapacity = 47.0,
      isArchived = false
    )
    vehicleProfileDao.insertOrUpdate(activeVehicle)
    fuelLogDao.deleteAllLogsForVehicle(targetVehicleId)

    val sampleLogs = mutableListOf<FuelLog>()
    var currentOdo = initialOdo + 480.0
    val logCal = purchaseCal.clone() as Calendar
    logCal.add(Calendar.DAY_OF_YEAR, 9)

    val basePrice = 1.48
    val stations = listOf("Shell Express", "Chevron Center", "BP Connect", "Costco Gas", "Mobil 1", "TotalEnergies")
    val grades = listOf("87 Regular", "87 Regular", "89 Plus", "87 Regular", "91 Premium", "87 Regular")

    var index = 0
    while (logCal.before(now)) {
      val priceVariation = Math.sin(index * 0.45) * 0.18 + ((index % 3) * 0.04)
      val unitPrice = Math.round((basePrice + priceVariation) * 100.0) / 100.0
      val liters = 36.0 + (index % 5) * 2.2
      val totalCost = Math.round((liters * unitPrice) * 100.0) / 100.0

      sampleLogs.add(
        FuelLog(
          vehicleId = targetVehicleId,
          timestamp = logCal.timeInMillis,
          odometer = Math.round(currentOdo * 10.0) / 10.0,
          amount = Math.round(liters * 10.0) / 10.0,
          unitPrice = unitPrice,
          totalCost = totalCost,
          isFullTank = true,
          isMissedPrevious = false,
          stationName = stations[index % stations.size],
          fuelGrade = grades[index % grades.size],
          notes = if (index % 4 == 0) "Highway trip" else ""
        )
      )

      val daysStep = 10 + (index % 5)
      logCal.add(Calendar.DAY_OF_YEAR, daysStep)
      val distanceDriven = 460.0 + (index % 6) * 45.0
      currentOdo += distanceDriven
      index++
    }

    val recentCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
    sampleLogs.add(
      FuelLog(
        vehicleId = targetVehicleId,
        timestamp = recentCal.timeInMillis,
        odometer = Math.round(currentOdo * 10.0) / 10.0,
        amount = 39.5,
        unitPrice = 1.54,
        totalCost = 60.83,
        isFullTank = true,
        isMissedPrevious = false,
        stationName = "Shell Express",
        fuelGrade = "87 Regular",
        notes = "Weekly commute"
      )
    )
    fuelLogDao.insertLogs(sampleLogs)

    // 2. EXCHANGED / ARCHIVED VEHICLE (ID 2)
    // Owned from 24 months ago to 8 months ago, then traded in / exchanged for the Civic!
    val archivedVehicleId = 2
    val archStartCal = Calendar.getInstance().apply { add(Calendar.MONTH, -24) }
    val archEndCal = Calendar.getInstance().apply { add(Calendar.MONTH, -8) }

    val archivedVehicle = VehicleProfile(
      id = archivedVehicleId,
      name = "Previous Car",
      makeModel = "Mazda 3 SkyActiv 2.5",
      year = 2018,
      fuelType = FuelType.GASOLINE,
      purchaseDateMillis = archStartCal.timeInMillis,
      purchasePrice = 18000.0,
      initialOdometer = 45000.0,
      currencySymbol = "$",
      distanceUnit = DistanceUnit.KILOMETERS,
      volumeUnit = VolumeUnit.LITERS,
      economyUnit = EconomyUnit.L_PER_100KM,
      tankCapacity = 51.0,
      isArchived = true,
      archiveDateMillis = archEndCal.timeInMillis,
      archiveOdometer = 64850.0,
      archiveReason = "Traded in / Exchanged for Honda Civic"
    )
    vehicleProfileDao.insertOrUpdate(archivedVehicle)
    fuelLogDao.deleteAllLogsForVehicle(archivedVehicleId)

    // Logs for archived car
    val archivedLogs = mutableListOf<FuelLog>()
    var archOdo = 45500.0
    val archLogCal = archStartCal.clone() as Calendar
    archLogCal.add(Calendar.DAY_OF_YEAR, 12)
    var archIdx = 0

    while (archLogCal.before(archEndCal)) {
      val priceVariation = (archIdx % 4) * 0.05
      val unitPrice = Math.round((1.38 + priceVariation) * 100.0) / 100.0
      val liters = 38.5 + (archIdx % 4) * 2.0
      val totalCost = Math.round((liters * unitPrice) * 100.0) / 100.0

      archivedLogs.add(
        FuelLog(
          vehicleId = archivedVehicleId,
          timestamp = archLogCal.timeInMillis,
          odometer = Math.round(archOdo * 10.0) / 10.0,
          amount = Math.round(liters * 10.0) / 10.0,
          unitPrice = unitPrice,
          totalCost = totalCost,
          isFullTank = true,
          isMissedPrevious = false,
          stationName = "Esso On the Run",
          fuelGrade = "87 Regular",
          notes = "Commute"
        )
      )

      archLogCal.add(Calendar.DAY_OF_YEAR, 14)
      archOdo += 490.0 + (archIdx % 3) * 30.0
      archIdx++
    }

    fuelLogDao.insertLogs(archivedLogs)
  }

  suspend fun clearAllData(vehicleId: Int) {
    fuelLogDao.deleteAllLogsForVehicle(vehicleId)
  }
}

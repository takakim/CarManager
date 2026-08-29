package com.example.data.repository

import com.example.data.db.FuelLogDao
import com.example.data.db.VehicleProfileDao
import com.example.data.model.DistanceUnit
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FuelTrackerRepository(
  private val fuelLogDao: FuelLogDao,
  private val vehicleProfileDao: VehicleProfileDao
) {
  val defaultVehicle: Flow<VehicleProfile?> = vehicleProfileDao.getDefaultVehicle()

  fun getLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>> =
    fuelLogDao.getLogsForVehicle(vehicleId)

  suspend fun insertLog(log: FuelLog): Long = fuelLogDao.insertLog(log)

  suspend fun updateLog(log: FuelLog) = fuelLogDao.updateLog(log)

  suspend fun deleteLog(log: FuelLog) = fuelLogDao.deleteLog(log)

  suspend fun deleteLogById(logId: Long) = fuelLogDao.deleteLogById(logId)

  suspend fun updateVehicle(profile: VehicleProfile) = vehicleProfileDao.insertOrUpdate(profile)

  suspend fun ensureDefaultVehicleCreated(): VehicleProfile {
    val existing = vehicleProfileDao.getDefaultVehicleDirect()
    if (existing != null) return existing

    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -8) // Purchased 8 months ago
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
      economyUnit = com.example.data.model.EconomyUnit.L_PER_100KM,
      tankCapacity = 43.0
    )
    vehicleProfileDao.insertOrUpdate(defaultProf)
    return defaultProf
  }

  suspend fun populateSampleData(vehicleId: Int = 1) {
    val now = Calendar.getInstance()

    // 8 months ago purchase
    val purchaseCal = Calendar.getInstance().apply {
      add(Calendar.MONTH, -8)
    }
    val purchaseDate = purchaseCal.timeInMillis
    val initialOdo = 14500.0

    val vehicle = VehicleProfile(
      id = vehicleId,
      name = "My Daily Driver",
      makeModel = "Honda Civic 2.0",
      year = 2023,
      fuelType = FuelType.GASOLINE,
      purchaseDateMillis = purchaseDate,
      purchasePrice = 25000.0,
      initialOdometer = initialOdo,
      currencySymbol = "$",
      distanceUnit = DistanceUnit.KILOMETERS,
      volumeUnit = VolumeUnit.LITERS,
      economyUnit = com.example.data.model.EconomyUnit.L_PER_100KM,
      tankCapacity = 47.0
    )
    vehicleProfileDao.insertOrUpdate(vehicle)

    fuelLogDao.deleteAllLogsForVehicle(vehicleId)

    // Build historical logs starting from purchase date up to this week
    val sampleLogs = mutableListOf<FuelLog>()
    var currentOdo = initialOdo + 480.0 // first fill up after 480 km
    val logCal = purchaseCal.clone() as Calendar
    logCal.add(Calendar.DAY_OF_YEAR, 9) // 9 days after purchase

    val basePrice = 1.48
    val stations = listOf("Shell Express", "Chevron Center", "BP Connect", "Costco Gas", "Mobil 1", "TotalEnergies")
    val grades = listOf("87 Regular", "87 Regular", "89 Plus", "87 Regular", "91 Premium", "87 Regular")

    var index = 0
    while (logCal.before(now)) {
      // Periodic price fluctuations
      val priceVariation = Math.sin(index * 0.45) * 0.18 + ((index % 3) * 0.04)
      val unitPrice = Math.round((basePrice + priceVariation) * 100.0) / 100.0
      val liters = 36.0 + (index % 5) * 2.2
      val totalCost = Math.round((liters * unitPrice) * 100.0) / 100.0

      sampleLogs.add(
        FuelLog(
          vehicleId = vehicleId,
          timestamp = logCal.timeInMillis,
          odometer = Math.round(currentOdo * 10.0) / 10.0,
          amount = Math.round(liters * 10.0) / 10.0,
          unitPrice = unitPrice,
          totalCost = totalCost,
          isFullTank = true,
          isMissedPrevious = false,
          stationName = stations[index % stations.size],
          fuelGrade = grades[index % grades.size],
          notes = if (index % 4 == 0) "Highway road trip refill" else ""
        )
      )

      // Advance by 10 to 14 days
      val daysStep = 10 + (index % 5)
      logCal.add(Calendar.DAY_OF_YEAR, daysStep)
      val distanceDriven = 460.0 + (index % 6) * 45.0
      currentOdo += distanceDriven
      index++
    }

    // Add one refuel in the current week
    val recentCal = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, -2)
    }
    sampleLogs.add(
      FuelLog(
        vehicleId = vehicleId,
        timestamp = recentCal.timeInMillis,
        odometer = Math.round(currentOdo * 10.0) / 10.0,
        amount = 39.5,
        unitPrice = 1.54,
        totalCost = 60.83,
        isFullTank = true,
        isMissedPrevious = false,
        stationName = "Shell Express",
        fuelGrade = "87 Regular",
        notes = "Commute refuel"
      )
    )

    fuelLogDao.insertLogs(sampleLogs)
  }

  suspend fun clearAllData(vehicleId: Int = 1) {
    fuelLogDao.deleteAllLogsForVehicle(vehicleId)
  }
}

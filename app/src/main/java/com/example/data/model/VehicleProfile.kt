package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_profiles")
data class VehicleProfile(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 1,
  val name: String = "My Car",
  val makeModel: String = "Daily Driver",
  val year: Int = 2023,
  val fuelType: FuelType = FuelType.GASOLINE,
  val purchaseDateMillis: Long = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000), // Default ~6 months ago
  val purchasePrice: Double = 0.0,
  val initialOdometer: Double = 0.0,
  val currencySymbol: String = "$",
  val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
  val volumeUnit: VolumeUnit = VolumeUnit.LITERS,
  val economyUnit: EconomyUnit = EconomyUnit.L_PER_100KM,
  val tankCapacity: Double = 50.0,
  val isArchived: Boolean = false,
  val archiveDateMillis: Long? = null,
  val archiveOdometer: Double? = null,
  val archiveReason: String = ""
)

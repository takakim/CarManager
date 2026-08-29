package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs")
data class FuelLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val vehicleId: Int = 1,
  val timestamp: Long = System.currentTimeMillis(),
  val odometer: Double, // Current odometer reading in km or miles
  val amount: Double, // Fuel or energy amount (Liters, Gallons, kWh, kg)
  val unitPrice: Double, // Price paid per unit (e.g. $1.45/L)
  val totalCost: Double, // Total paid for this refuel / charge
  val isFullTank: Boolean = true, // Whether tank/battery was filled to full
  val isMissedPrevious: Boolean = false, // If user skipped logging previous refuels
  val stationName: String = "",
  val fuelGrade: String = "Regular", // e.g. 87 Regular, 91 Premium, Diesel, Supercharger, Level 2
  val notes: String = ""
)

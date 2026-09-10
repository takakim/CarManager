package com.example.data.model

data class VehicleHistorySummary(
  val vehicle: VehicleProfile,
  val logsCount: Int = 0,
  val totalSpent: Double = 0.0,
  val totalDistance: Double = 0.0,
  val totalVolume: Double = 0.0,
  val averageEconomy: Double = 0.0,
  val costPerDistance: Double = 0.0,
  val avgPricePaid: Double = 0.0,
  val latestOdometer: Double = 0.0,
  val ownershipDays: Long = 0L,
  val isActive: Boolean = true
)

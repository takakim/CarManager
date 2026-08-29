package com.example.data.model

enum class FuelType(
  val displayName: String,
  val defaultVolumeUnit: String,
  val defaultPriceUnit: String
) {
  GASOLINE("Gasoline / Petrol", "L", "/L"),
  DIESEL("Diesel", "L", "/L"),
  ELECTRIC("Electric (EV)", "kWh", "/kWh"),
  HYBRID("Hybrid (Petrol)", "L", "/L"),
  LPG("LPG / Autogas", "L", "/L"),
  CNG("CNG", "kg", "/kg")
}

enum class DistanceUnit(val symbol: String, val label: String) {
  KILOMETERS("km", "Kilometers"),
  MILES("mi", "Miles")
}

enum class VolumeUnit(val symbol: String, val label: String) {
  LITERS("L", "Liters"),
  GALLONS_US("gal", "Gallons (US)"),
  GALLONS_UK("gal (UK)", "Gallons (UK)"),
  KWH("kWh", "Kilowatt Hours (kWh)"),
  KG("kg", "Kilograms")
}

enum class EconomyUnit(val symbol: String) {
  L_PER_100KM("L/100km"),
  MPG_US("MPG (US)"),
  MPG_UK("MPG (UK)"),
  KM_PER_L("km/L"),
  KWH_PER_100KM("kWh/100km"),
  MI_PER_KWH("mi/kWh")
}

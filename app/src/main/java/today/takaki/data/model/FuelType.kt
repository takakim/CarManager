package today.takaki.data.model

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

enum class EconomyUnit(val symbol: String, val label: String) {
  L_PER_100KM("L/100km", "Liters per 100 km (L/100km)"),
  MPG_US("MPG (US)", "Miles per Gallon (US)"),
  MPG_UK("MPG (UK)", "Miles per Gallon (UK)"),
  KM_PER_L("km/L", "Kilometers per Liter (km/L)"),
  KWH_PER_100KM("kWh/100km", "kWh per 100 km (kWh/100km)"),
  MI_PER_KWH("mi/kWh", "Miles per kWh (mi/kWh)"),
  KM_PER_KWH("km/kWh", "Kilometers per kWh (km/kWh)"),
  KWH_PER_100MI("kWh/100mi", "kWh per 100 miles (kWh/100mi)"),
  KG_PER_100KM("kg/100km", "kg per 100 km (kg/100km)"),
  KM_PER_KG("km/kg", "Kilometers per kg (km/kg)");

  companion object {
    fun getDefault(fuelType: FuelType, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit): EconomyUnit {
      return when {
        fuelType == FuelType.ELECTRIC || volumeUnit == VolumeUnit.KWH -> {
          if (distanceUnit == DistanceUnit.MILES) MI_PER_KWH else KWH_PER_100KM
        }
        volumeUnit == VolumeUnit.GALLONS_US -> MPG_US
        volumeUnit == VolumeUnit.GALLONS_UK -> MPG_UK
        volumeUnit == VolumeUnit.KG -> KG_PER_100KM
        distanceUnit == DistanceUnit.MILES -> MPG_US
        else -> L_PER_100KM
      }
    }

    fun calculate(distance: Double, volume: Double, economyUnit: EconomyUnit): Double {
      if (distance <= 0.0 || volume <= 0.0) return 0.0
      return when (economyUnit) {
        L_PER_100KM,
        KWH_PER_100KM,
        KWH_PER_100MI,
        KG_PER_100KM -> {
          (volume / distance) * 100.0
        }
        MPG_US,
        MPG_UK,
        KM_PER_L,
        MI_PER_KWH,
        KM_PER_KWH,
        KM_PER_KG -> {
          distance / volume
        }
      }
    }
  }
}

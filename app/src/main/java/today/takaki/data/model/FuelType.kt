package today.takaki.data.model

import androidx.annotation.StringRes
import today.takaki.R

enum class FuelType(
  val displayName: String,
  val defaultVolumeUnit: String,
  val defaultPriceUnit: String,
  @StringRes val nameRes: Int
) {
  GASOLINE("Gasoline / Petrol", "L", "/L", R.string.fuel_gasoline),
  DIESEL("Diesel", "L", "/L", R.string.fuel_diesel),
  ELECTRIC("Electric (EV)", "kWh", "/kWh", R.string.fuel_electric),
  HYBRID("Hybrid (Petrol)", "L", "/L", R.string.fuel_hybrid),
  LPG("LPG / Autogas", "L", "/L", R.string.fuel_lpg),
  CNG("CNG", "kg", "/kg", R.string.fuel_cng)
}

enum class DistanceUnit(val symbol: String, val label: String, @StringRes val nameRes: Int) {
  KILOMETERS("km", "Kilometers", R.string.unit_kilometers),
  MILES("mi", "Miles", R.string.unit_miles)
}

enum class VolumeUnit(val symbol: String, val label: String, @StringRes val nameRes: Int) {
  LITERS("L", "Liters", R.string.unit_liters),
  GALLONS_US("gal", "Gallons (US)", R.string.unit_gallons_us),
  GALLONS_UK("gal (UK)", "Gallons (UK)", R.string.unit_gallons_uk),
  KWH("kWh", "Kilowatt Hours (kWh)", R.string.unit_kwh),
  KG("kg", "Kilograms", R.string.unit_kg)
}

enum class EconomyUnit(val symbol: String, val label: String, @StringRes val nameRes: Int) {
  L_PER_100KM("L/100km", "Liters per 100 km (L/100km)", R.string.econ_l_per_100km),
  MPG_US("MPG (US)", "Miles per Gallon (US)", R.string.econ_mpg_us),
  MPG_UK("MPG (UK)", "Miles per Gallon (UK)", R.string.econ_mpg_uk),
  KM_PER_L("km/L", "Kilometers per Liter (km/L)", R.string.econ_km_per_l),
  KWH_PER_100KM("kWh/100km", "kWh per 100 km (kWh/100km)", R.string.econ_kwh_per_100km),
  MI_PER_KWH("mi/kWh", "Miles per kWh (mi/kWh)", R.string.econ_mi_per_kwh),
  KM_PER_KWH("km/kWh", "Kilometers per kWh (km/kWh)", R.string.econ_km_per_kwh),
  KWH_PER_100MI("kWh/100mi", "kWh per 100 miles (kWh/100mi)", R.string.econ_kwh_per_100mi),
  KG_PER_100KM("kg/100km", "kg per 100 km (kg/100km)", R.string.econ_kg_per_100km),
  KM_PER_KG("km/kg", "Kilometers per kg (km/kg)", R.string.econ_km_per_kg);

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

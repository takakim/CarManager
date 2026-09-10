package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.DistanceUnit
import com.example.data.model.EconomyUnit
import com.example.data.model.FuelLog
import com.example.data.model.FuelType
import com.example.data.model.VehicleProfile
import com.example.data.model.VolumeUnit

class Converters {
  @TypeConverter
  fun fromFuelType(value: FuelType?): String? = value?.name

  @TypeConverter
  fun toFuelType(value: String?): FuelType =
    value?.let { runCatching { FuelType.valueOf(it) }.getOrDefault(FuelType.GASOLINE) } ?: FuelType.GASOLINE

  @TypeConverter
  fun fromDistanceUnit(value: DistanceUnit?): String? = value?.name

  @TypeConverter
  fun toDistanceUnit(value: String?): DistanceUnit =
    value?.let { runCatching { DistanceUnit.valueOf(it) }.getOrDefault(DistanceUnit.KILOMETERS) } ?: DistanceUnit.KILOMETERS

  @TypeConverter
  fun fromVolumeUnit(value: VolumeUnit?): String? = value?.name

  @TypeConverter
  fun toVolumeUnit(value: String?): VolumeUnit =
    value?.let { runCatching { VolumeUnit.valueOf(it) }.getOrDefault(VolumeUnit.LITERS) } ?: VolumeUnit.LITERS

  @TypeConverter
  fun fromEconomyUnit(value: EconomyUnit?): String? = value?.name

  @TypeConverter
  fun toEconomyUnit(value: String?): EconomyUnit =
    value?.let { runCatching { EconomyUnit.valueOf(it) }.getOrDefault(EconomyUnit.L_PER_100KM) } ?: EconomyUnit.L_PER_100KM
}

@Database(
  entities = [VehicleProfile::class, FuelLog::class],
  version = 3,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun fuelLogDao(): FuelLogDao
  abstract fun vehicleProfileDao(): VehicleProfileDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "fuel_tracker.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}

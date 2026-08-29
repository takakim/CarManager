package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VehicleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleProfileDao {
  @Query("SELECT * FROM vehicle_profiles WHERE id = :id LIMIT 1")
  fun getVehicleById(id: Int): Flow<VehicleProfile?>

  @Query("SELECT * FROM vehicle_profiles ORDER BY id ASC LIMIT 1")
  fun getDefaultVehicle(): Flow<VehicleProfile?>

  @Query("SELECT * FROM vehicle_profiles ORDER BY id ASC LIMIT 1")
  suspend fun getDefaultVehicleDirect(): VehicleProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(profile: VehicleProfile): Long

  @Update
  suspend fun update(profile: VehicleProfile)
}

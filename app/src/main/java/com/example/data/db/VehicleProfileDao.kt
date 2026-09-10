package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VehicleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleProfileDao {
  @Query("SELECT * FROM vehicle_profiles ORDER BY isArchived ASC, id DESC")
  fun getAllVehicles(): Flow<List<VehicleProfile>>

  @Query("SELECT * FROM vehicle_profiles ORDER BY isArchived ASC, id DESC")
  suspend fun getAllVehiclesDirect(): List<VehicleProfile>

  @Query("SELECT * FROM vehicle_profiles WHERE isArchived = 0 ORDER BY id ASC")
  fun getActiveVehicles(): Flow<List<VehicleProfile>>

  @Query("SELECT * FROM vehicle_profiles WHERE isArchived = 1 ORDER BY archiveDateMillis DESC")
  fun getArchivedVehicles(): Flow<List<VehicleProfile>>

  @Query("SELECT * FROM vehicle_profiles WHERE id = :id LIMIT 1")
  fun getVehicleById(id: Int): Flow<VehicleProfile?>

  @Query("SELECT * FROM vehicle_profiles WHERE id = :id LIMIT 1")
  suspend fun getVehicleByIdDirect(id: Int): VehicleProfile?

  @Query("SELECT * FROM vehicle_profiles WHERE isArchived = 0 ORDER BY id ASC LIMIT 1")
  fun getDefaultVehicle(): Flow<VehicleProfile?>

  @Query("SELECT * FROM vehicle_profiles WHERE isArchived = 0 ORDER BY id ASC LIMIT 1")
  suspend fun getDefaultVehicleDirect(): VehicleProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(profile: VehicleProfile): Long

  @Update
  suspend fun update(profile: VehicleProfile)

  @Delete
  suspend fun deleteVehicle(profile: VehicleProfile)

  @Query("DELETE FROM vehicle_profiles WHERE id = :id")
  suspend fun deleteVehicleById(id: Int)
}


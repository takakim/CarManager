package today.takaki.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import today.takaki.data.model.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
  @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY odometer DESC, timestamp DESC")
  fun getLogsForVehicle(vehicleId: Int): Flow<List<FuelLog>>

  @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY odometer DESC, timestamp DESC")
  suspend fun getLogsListForVehicle(vehicleId: Int): List<FuelLog>

  @Query("SELECT * FROM fuel_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<FuelLog>>

  @Query("SELECT * FROM fuel_logs ORDER BY timestamp DESC")
  suspend fun getAllLogsDirect(): List<FuelLog>

  @Query("SELECT * FROM fuel_logs WHERE id = :logId")
  suspend fun getLogById(logId: Long): FuelLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: FuelLog): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLogs(logs: List<FuelLog>)

  @Update
  suspend fun updateLog(log: FuelLog)

  @Delete
  suspend fun deleteLog(log: FuelLog)

  @Query("DELETE FROM fuel_logs WHERE id = :logId")
  suspend fun deleteLogById(logId: Long)

  @Query("DELETE FROM fuel_logs WHERE vehicleId = :vehicleId")
  suspend fun deleteAllLogsForVehicle(vehicleId: Int)

  @Query("DELETE FROM fuel_logs")
  suspend fun clearAllLogs()
}

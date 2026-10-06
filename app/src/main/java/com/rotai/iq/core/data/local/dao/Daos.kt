package com.rotai.iq.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rotai.iq.core.data.local.entity.DailyFinancialEntity
import com.rotai.iq.core.data.local.entity.DriverGoalEntity
import com.rotai.iq.core.data.local.entity.DriverPreferenceEntity
import com.rotai.iq.core.data.local.entity.RideEvaluationEntity
import com.rotai.iq.core.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isActive = 1 LIMIT 1")
    fun getActiveVehicle(): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(vehicle: VehicleEntity)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM driver_goals WHERE id = 'default_goal' LIMIT 1")
    fun getGoal(): Flow<DriverGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: DriverGoalEntity)
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM driver_preferences WHERE id = 'default_preferences' LIMIT 1")
    fun getPreferences(): Flow<DriverPreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pref: DriverPreferenceEntity)
}

@Dao
interface RideDao {
    @Query("SELECT * FROM ride_evaluations ORDER BY evaluatedAt DESC")
    fun getAllEvaluations(): Flow<List<RideEvaluationEntity>>

    @Query("SELECT * FROM ride_evaluations WHERE id = :id LIMIT 1")
    suspend fun getEvaluationById(id: String): RideEvaluationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: RideEvaluationEntity)

    @Query("DELETE FROM ride_evaluations WHERE id = :id")
    suspend fun deleteEvaluation(id: String)
}

@Dao
interface FinancialDao {
    @Query("SELECT * FROM daily_financials ORDER BY date DESC")
    fun getAllDailyFinancials(): Flow<List<DailyFinancialEntity>>

    @Query("SELECT * FROM daily_financials WHERE date = :date LIMIT 1")
    fun getDailyFinancial(date: String): Flow<DailyFinancialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(daily: DailyFinancialEntity)
}

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_records ORDER BY odometerKm DESC")
    fun getAllFuelRecords(): Flow<List<com.rotai.iq.core.data.local.entity.FuelRecordEntity>>

    @Query("SELECT * FROM fuel_records WHERE isFullTank = 1 ORDER BY odometerKm DESC LIMIT 2")
    suspend fun getLastTwoFullTankRecords(): List<com.rotai.iq.core.data.local.entity.FuelRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: com.rotai.iq.core.data.local.entity.FuelRecordEntity)

    @Query("DELETE FROM fuel_records WHERE id = :id")
    suspend fun deleteFuelRecord(id: String)
}

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records ORDER BY odometerKm DESC")
    fun getAllMaintenanceRecords(): Flow<List<com.rotai.iq.core.data.local.entity.MaintenanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRecord(record: com.rotai.iq.core.data.local.entity.MaintenanceRecordEntity)

    @Query("DELETE FROM maintenance_records WHERE id = :id")
    suspend fun deleteMaintenanceRecord(id: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM vehicle_expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<com.rotai.iq.core.data.local.entity.VehicleExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: com.rotai.iq.core.data.local.entity.VehicleExpenseEntity)

    @Query("DELETE FROM vehicle_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)
}

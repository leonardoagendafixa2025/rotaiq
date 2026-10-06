package com.rotai.iq.core.data.repository

import com.rotai.iq.core.data.local.db.RotaIqDatabase
import com.rotai.iq.core.domain.model.DailyFinancialSummary
import com.rotai.iq.core.domain.model.DriverGoal
import com.rotai.iq.core.domain.model.DriverPreference
import com.rotai.iq.core.domain.model.RideEvaluation
import com.rotai.iq.core.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RotaIqRepository {
    fun getActiveVehicle(): Flow<Vehicle>
    suspend fun saveVehicle(vehicle: Vehicle)

    fun getDriverGoal(): Flow<DriverGoal>
    suspend fun saveDriverGoal(goal: DriverGoal)

    fun getDriverPreference(): Flow<DriverPreference>
    suspend fun saveDriverPreference(preference: DriverPreference)

    fun getAllEvaluations(): Flow<List<RideEvaluation>>
    suspend fun saveEvaluation(evaluation: RideEvaluation)
    suspend fun deleteEvaluation(id: String)

    fun getDailyFinancial(date: String): Flow<DailyFinancialSummary?>
    suspend fun saveDailyFinancial(summary: DailyFinancialSummary)

    fun getAllFuelRecords(): Flow<List<com.rotai.iq.core.domain.model.FuelRecord>>
    suspend fun getLastTwoFullTankRecords(): List<com.rotai.iq.core.domain.model.FuelRecord>
    suspend fun saveFuelRecord(record: com.rotai.iq.core.domain.model.FuelRecord)
    suspend fun deleteFuelRecord(id: String)

    fun getAllMaintenanceRecords(): Flow<List<com.rotai.iq.core.domain.model.MaintenanceRecord>>
    suspend fun saveMaintenanceRecord(record: com.rotai.iq.core.domain.model.MaintenanceRecord)
    suspend fun deleteMaintenanceRecord(id: String)

    fun getAllExpenses(): Flow<List<com.rotai.iq.core.domain.model.VehicleExpense>>
    suspend fun saveExpense(expense: com.rotai.iq.core.domain.model.VehicleExpense)
    suspend fun deleteExpense(id: String)
}

class RotaIqRepositoryImpl(
    private val database: RotaIqDatabase
) : RotaIqRepository {

    private val vehicleDao = database.vehicleDao()
    private val goalDao = database.goalDao()
    private val preferenceDao = database.preferenceDao()
    private val rideDao = database.rideDao()
    private val financialDao = database.financialDao()
    private val fuelDao = database.fuelDao()
    private val maintenanceDao = database.maintenanceDao()
    private val expenseDao = database.expenseDao()

    override fun getActiveVehicle(): Flow<Vehicle> {
        return vehicleDao.getActiveVehicle().map { entity ->
            entity?.toDomain() ?: Vehicle()
        }
    }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        vehicleDao.insertOrUpdate(vehicle.toEntity())
    }

    override fun getDriverGoal(): Flow<DriverGoal> {
        return goalDao.getGoal().map { entity ->
            entity?.toDomain() ?: DriverGoal()
        }
    }

    override suspend fun saveDriverGoal(goal: DriverGoal) {
        goalDao.insertOrUpdate(goal.toEntity())
    }

    override fun getDriverPreference(): Flow<DriverPreference> {
        return preferenceDao.getPreferences().map { entity ->
            entity?.toDomain() ?: DriverPreference()
        }
    }

    override suspend fun saveDriverPreference(preference: DriverPreference) {
        preferenceDao.insertOrUpdate(preference.toEntity())
    }

    override fun getAllEvaluations(): Flow<List<RideEvaluation>> {
        return rideDao.getAllEvaluations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveEvaluation(evaluation: RideEvaluation) {
        rideDao.insertEvaluation(evaluation.toEntity())
    }

    override suspend fun deleteEvaluation(id: String) {
        rideDao.deleteEvaluation(id)
    }

    override fun getDailyFinancial(date: String): Flow<DailyFinancialSummary?> {
        return financialDao.getDailyFinancial(date).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveDailyFinancial(summary: DailyFinancialSummary) {
        financialDao.insertOrUpdate(summary.toEntity())
    }

    override fun getAllFuelRecords(): Flow<List<com.rotai.iq.core.domain.model.FuelRecord>> {
        return fuelDao.getAllFuelRecords().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLastTwoFullTankRecords(): List<com.rotai.iq.core.domain.model.FuelRecord> {
        return fuelDao.getLastTwoFullTankRecords().map { it.toDomain() }
    }

    override suspend fun saveFuelRecord(record: com.rotai.iq.core.domain.model.FuelRecord) {
        fuelDao.insertFuelRecord(record.toEntity())
    }

    override suspend fun deleteFuelRecord(id: String) {
        fuelDao.deleteFuelRecord(id)
    }

    override fun getAllMaintenanceRecords(): Flow<List<com.rotai.iq.core.domain.model.MaintenanceRecord>> {
        return maintenanceDao.getAllMaintenanceRecords().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveMaintenanceRecord(record: com.rotai.iq.core.domain.model.MaintenanceRecord) {
        maintenanceDao.insertMaintenanceRecord(record.toEntity())
    }

    override suspend fun deleteMaintenanceRecord(id: String) {
        maintenanceDao.deleteMaintenanceRecord(id)
    }

    override fun getAllExpenses(): Flow<List<com.rotai.iq.core.domain.model.VehicleExpense>> {
        return expenseDao.getAllExpenses().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveExpense(expense: com.rotai.iq.core.domain.model.VehicleExpense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.deleteExpense(id)
    }
}

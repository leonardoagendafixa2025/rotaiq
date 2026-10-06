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
}

class RotaIqRepositoryImpl(
    private val database: RotaIqDatabase
) : RotaIqRepository {

    private val vehicleDao = database.vehicleDao()
    private val goalDao = database.goalDao()
    private val preferenceDao = database.preferenceDao()
    private val rideDao = database.rideDao()
    private val financialDao = database.financialDao()

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
}

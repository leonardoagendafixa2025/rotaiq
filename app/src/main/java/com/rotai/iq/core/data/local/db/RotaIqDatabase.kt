package com.rotai.iq.core.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rotai.iq.core.data.local.dao.FinancialDao
import com.rotai.iq.core.data.local.dao.GoalDao
import com.rotai.iq.core.data.local.dao.PreferenceDao
import com.rotai.iq.core.data.local.dao.RideDao
import com.rotai.iq.core.data.local.dao.VehicleDao
import com.rotai.iq.core.data.local.entity.DailyFinancialEntity
import com.rotai.iq.core.data.local.entity.DriverGoalEntity
import com.rotai.iq.core.data.local.entity.DriverPreferenceEntity
import com.rotai.iq.core.data.local.entity.RideEvaluationEntity
import com.rotai.iq.core.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        DriverGoalEntity::class,
        DriverPreferenceEntity::class,
        RideEvaluationEntity::class,
        DailyFinancialEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RotaIqDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun goalDao(): GoalDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun rideDao(): RideDao
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: RotaIqDatabase? = null

        fun getDatabase(context: Context): RotaIqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RotaIqDatabase::class.java,
                    "rota_iq_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

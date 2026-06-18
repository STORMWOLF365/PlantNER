package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FarmProfileEntity::class,
        PlantScanEntity::class,
        SensorReading::class,
        ChatMessageEntity::class,
        CropGuideEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmProfileDao(): FarmProfileDao
    abstract fun plantScanDao(): PlantScanDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cropGuideDao(): CropGuideDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plantner_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

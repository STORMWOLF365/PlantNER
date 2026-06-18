package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmProfileDao {
    @Query("SELECT * FROM farm_profile WHERE id = 1")
    fun getFarmProfile(): Flow<FarmProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: FarmProfileEntity)
}

@Dao
interface PlantScanDao {
    @Query("SELECT * FROM plant_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<PlantScanEntity>>

    @Query("SELECT * FROM plant_scans WHERE id = :id")
    fun getScanById(id: Int): Flow<PlantScanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: PlantScanEntity): Long

    @Delete
    suspend fun deleteScan(scan: PlantScanEntity)

    @Query("UPDATE plant_scans SET isResolved = :isResolved WHERE id = :id")
    suspend fun updateScanResolution(id: Int, isResolved: Boolean)
}

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadings(limit: Int = 50): Flow<List<SensorReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReading)

    @Query("DELETE FROM sensor_readings")
    suspend fun clearReadings()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface CropGuideDao {
    @Query("SELECT * FROM crop_guides ORDER BY cropName ASC, stage ASC")
    fun getAllGuides(): Flow<List<CropGuideEntity>>

    @Query("SELECT * FROM crop_guides WHERE cropName = :cropName AND stage = :stage")
    suspend fun getGuide(cropName: String, stage: String): CropGuideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: CropGuideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuides(guides: List<CropGuideEntity>)

    @Query("DELETE FROM crop_guides WHERE cropName = :cropName")
    suspend fun deleteGuidesForCrop(cropName: String)

    @Query("DELETE FROM crop_guides")
    suspend fun clearGuides()
}


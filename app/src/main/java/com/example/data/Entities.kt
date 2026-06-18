package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farm_profile")
data class FarmProfileEntity(
    @PrimaryKey val id: Int = 1,
    val farmName: String,
    val location: String,
    val farmSize: String,
    val primaryCrops: String,
    val equipment: String = "",
    val budget: Double = 0.0,
    val region: String = "Nigeria",
    val phoneNumber: String = "+234 801 234 5678"
)

@Entity(tableName = "plant_scans")
data class PlantScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val cropType: String,
    val diseaseName: String,
    val confidence: Double,
    val severity: String,
    val description: String,
    val symptoms: String,
    val treatmentPlan: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Float,
    val humidity: Float,
    val soilMoisture: Float
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_guides", primaryKeys = ["cropName", "stage"])
data class CropGuideEntity(
    val cropName: String,
    val stage: String,
    val practices: String, // stored joined by "|||"
    val diagnosis: String,
    val basePriceUsd: Double,
    val stageMultiplier: Double,
    val baseYieldPerAcreKg: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)


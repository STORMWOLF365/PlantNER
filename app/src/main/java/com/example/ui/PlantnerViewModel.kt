package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Analyzing : ScanUiState
    data class Success(val scan: PlantScanEntity) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class PlantnerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = PlantnerRepository(db)

    // User Session State
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    // Farm Profile
    private val _farmProfile = MutableStateFlow<FarmProfileEntity?>(null)
    val farmProfile: StateFlow<FarmProfileEntity?> = _farmProfile.asStateFlow()

    // Scans
    private val _allScans = MutableStateFlow<List<PlantScanEntity>>(emptyList())
    val allScans: StateFlow<List<PlantScanEntity>> = _allScans.asStateFlow()

    // Crop Guides
    private val _allGuides = MutableStateFlow<List<CropGuideEntity>>(emptyList())
    val allGuides: StateFlow<List<CropGuideEntity>> = _allGuides.asStateFlow()

    // Scan State
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Sensors
    private val _sensorReadings = MutableStateFlow<List<SensorReading>>(emptyList())
    val sensorReadings: StateFlow<List<SensorReading>> = _sensorReadings.asStateFlow()

    // Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Persistent User-Defined Sensor Thresholds & Application Preferences
    private val prefs = application.getSharedPreferences("plantner_sensor_thresholds", android.content.Context.MODE_PRIVATE)

    // Persistent settings state
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _diseaseAlerts = MutableStateFlow(prefs.getBoolean("disease_alerts", true))
    val diseaseAlerts: StateFlow<Boolean> = _diseaseAlerts.asStateFlow()

    private val _weatherAlerts = MutableStateFlow(prefs.getBoolean("weather_alerts", true))
    val weatherAlerts: StateFlow<Boolean> = _weatherAlerts.asStateFlow()

    private val _dailyTipsAlerts = MutableStateFlow(prefs.getBoolean("daily_tips_alerts", true))
    val dailyTipsAlerts: StateFlow<Boolean> = _dailyTipsAlerts.asStateFlow()

    private val _isSubscribed = MutableStateFlow(prefs.getBoolean("is_subscribed", false))
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _tempMin = MutableStateFlow(prefs.getFloat("temp_min", 18.0f))
    val tempMin = _tempMin.asStateFlow()

    private val _tempMax = MutableStateFlow(prefs.getFloat("temp_max", 35.0f))
    val tempMax = _tempMax.asStateFlow()

    private val _soilMin = MutableStateFlow(prefs.getFloat("soil_min", 30.0f))
    val soilMin = _soilMin.asStateFlow()

    private val _soilMax = MutableStateFlow(prefs.getFloat("soil_max", 80.0f))
    val soilMax = _soilMax.asStateFlow()

    private val _humidityMin = MutableStateFlow(prefs.getFloat("humidity_min", 40.0f))
    val humidityMin = _humidityMin.asStateFlow()

    private val _humidityMax = MutableStateFlow(prefs.getFloat("humidity_max", 85.0f))
    val humidityMax = _humidityMax.asStateFlow()

    private val _activePreset = MutableStateFlow(prefs.getString("active_preset", "Custom") ?: "Custom")
    val activePreset = _activePreset.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "English") ?: "English")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        prefs.edit().putString("selected_language", language).apply()
    }

    fun updateThresholds(tMin: Float, tMax: Float, sMin: Float, sMax: Float, hMin: Float, hMax: Float, preset: String) {
        _tempMin.value = tMin
        _tempMax.value = tMax
        _soilMin.value = sMin
        _soilMax.value = sMax
        _humidityMin.value = hMin
        _humidityMax.value = hMax
        _activePreset.value = preset

        prefs.edit().apply {
            putFloat("temp_min", tMin)
            putFloat("temp_max", tMax)
            putFloat("soil_min", sMin)
            putFloat("soil_max", sMax)
            putFloat("humidity_min", hMin)
            putFloat("humidity_max", hMax)
            putString("active_preset", preset)
            apply()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

    fun setDiseaseAlerts(enabled: Boolean) {
        _diseaseAlerts.value = enabled
        prefs.edit().putBoolean("disease_alerts", enabled).apply()
    }

    fun setWeatherAlerts(enabled: Boolean) {
        _weatherAlerts.value = enabled
        prefs.edit().putBoolean("weather_alerts", enabled).apply()
    }

    fun setDailyTipsAlerts(enabled: Boolean) {
        _dailyTipsAlerts.value = enabled
        prefs.edit().putBoolean("daily_tips_alerts", enabled).apply()
    }

    fun toggleSubscription() {
        val newValue = !_isSubscribed.value
        _isSubscribed.value = newValue
        prefs.edit().putBoolean("is_subscribed", newValue).apply()
    }

    init {
        viewModelScope.launch {
            // Seed base configuration
            repository.ensureDefaultFarmProfile()
            repository.seedMockSensorDataIfEmpty()
            repository.seedDefaultCropGuidesIfEmpty()

            // Observe data
            launch {
                repository.farmProfile.collect { profile ->
                    _farmProfile.value = profile
                }
            }
            launch {
                repository.allScans.collect { scans ->
                    _allScans.value = scans
                }
            }
            launch {
                repository.allGuides.collect { guides ->
                    _allGuides.value = guides
                }
            }
            launch {
                repository.recentReadings.collect { readings ->
                    _sensorReadings.value = readings
                }
            }
            launch {
                repository.allMessages.collect { messages ->
                    _chatMessages.value = messages
                }
            }
        }
    }

    // Authentication Actions
    fun loginUser(email: String) {
        _currentUser.value = email
    }

    fun signupUser(email: String, farmName: String, primaryCrops: String) {
        _currentUser.value = email
        viewModelScope.launch {
            repository.updateFarmProfile(
                FarmProfileEntity(
                    id = 1,
                    farmName = farmName,
                    location = "My Farm Coordinate",
                    farmSize = "10 Acres",
                    primaryCrops = primaryCrops,
                    equipment = "Basic tools",
                    budget = 2000.0,
                    region = "Nigeria",
                    phoneNumber = "+234 801 234 5678"
                )
            )
        }
    }

    fun logoutUser() {
        _currentUser.value = null
    }

    fun updateFarmProfile(name: String, location: String, size: String, crops: String, equipment: String, budget: Double, region: String, phoneNumber: String) {
        viewModelScope.launch {
            repository.updateFarmProfile(
                FarmProfileEntity(
                    id = 1,
                    farmName = name,
                    location = location,
                    farmSize = size,
                    primaryCrops = crops,
                    equipment = equipment,
                    budget = budget,
                    region = region,
                    phoneNumber = phoneNumber
                )
            )
        }
    }

    // Image Diagnostic Action
    fun analyzePlant(bitmap: Bitmap, cropHint: String) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Analyzing
            try {
                // Call Gemini Client for diagnostics
                val resultScan = GeminiClient.analyzePlantImage(bitmap, cropHint)
                
                // Save bitmap to file system for offline rendering
                val filename = "scan_${UUID.randomUUID()}.jpg"
                val file = File(getApplication<Application>().filesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                val finalScan = resultScan.copy(imagePath = file.absolutePath)
                val newId = repository.insertScan(finalScan)
                
                _scanState.value = ScanUiState.Success(finalScan.copy(id = newId.toInt()))
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "Failed analyzing image")
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun toggleScanResolution(id: Int, isResolved: Boolean) {
        viewModelScope.launch {
            repository.updateScanResolution(id, isResolved)
        }
    }

    fun deleteScan(scan: PlantScanEntity) {
        viewModelScope.launch {
            repository.deleteScan(scan)
        }
    }

    // Sensor Operations
    fun triggerSensorTick() {
        viewModelScope.launch {
            repository.generateNewSensorReading()
        }
    }

    fun clearAllSensors() {
        viewModelScope.launch {
            repository.clearReadings()
        }
    }

    // Chat Actions
    fun sendMessageToDoctor(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessageEntity(text = text, isUser = true)
            repository.insertMessage(userMsg)

            _isChatLoading.value = true
            try {
                val history = _chatMessages.value
                val replyText = GeminiClient.chatWithDoctor(history, text)
                val systemMsg = ChatMessageEntity(text = replyText, isUser = false)
                repository.insertMessage(systemMsg)
            } catch (e: Exception) {
                val errorMsg = ChatMessageEntity(text = "Dr. Green is offline. error: ${e.message}", isUser = false)
                repository.insertMessage(errorMsg)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun updateCropGuide(
        cropName: String,
        stage: String,
        practices: List<String>,
        diagnosis: String,
        basePriceUsd: Double,
        stageMultiplier: Double,
        baseYieldPerAcreKg: Double
    ) {
        viewModelScope.launch {
            repository.insertGuide(
                CropGuideEntity(
                    cropName = cropName,
                    stage = stage,
                    practices = practices.joinToString("|||"),
                    diagnosis = diagnosis,
                    basePriceUsd = basePriceUsd,
                    stageMultiplier = stageMultiplier,
                    baseYieldPerAcreKg = baseYieldPerAcreKg,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }
}

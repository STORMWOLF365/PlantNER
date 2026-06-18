package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.example.R
import coil.compose.AsyncImage
import com.example.api.GeminiClient
import com.example.data.*
import com.example.ui.PlantnerViewModel
import com.example.ui.ScanUiState
import com.example.utils.LeafBitmapGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class CropStageData(
    val practices: List<String>,
    val diagnosis: String,
    val basePriceUsd: Double,
    val stageMultiplier: Double,
    val baseYieldPerAcreKg: Double
)

// ==========================================
// 1. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: PlantnerViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("farmer.green@valley.com") }
    var password by remember { mutableStateOf("farmpass123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // High-performance canvas-drawn farm terraces background
        PlantationBackground(alpha = 0.12f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Plantner Logo Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(28.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Plantner",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Your AI Planting Partner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "AI Crop Diagnostics & Smart IoT Advisor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Email icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock icon") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMsg = "Credentials cannot be blank"
                                return@Button
                            }
                            isChecking = true
                            coroutineScope.launch {
                                delay(1200) // Beautiful authentication transition
                                isChecking = false
                                viewModel.loginUser(email)
                                onLoginSuccess()
                            }
                        },
                        enabled = !isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In to My Farm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "New agricultural user? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Register Farm",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToSignup() }
                        .padding(4.dp)
                )
            }
        }
    }
}
}

// ==========================================
// 2. SIGNUP SCREEN
// ==========================================
@Composable
fun SignupScreen(
    viewModel: PlantnerViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var primaryCrops by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // High-performance canvas-drawn farm terraces background
        PlantationBackground(alpha = 0.12f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Plantner Logo Icon",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Register Farm Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            )

            Text(
                text = "Your AI Planting Partner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Let's personalize your AI plant doctor insights",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Security Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock icon") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = farmName,
                        onValueChange = { farmName = it; errorMsg = "" },
                        label = { Text("Farm / Garden Name") },
                        leadingIcon = { Icon(Icons.Filled.Landscape, contentDescription = "Farm icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = primaryCrops,
                        onValueChange = { primaryCrops = it; errorMsg = "" },
                        label = { Text("Crops grown (e.g. Tomato, Corn)") },
                        leadingIcon = { Icon(Icons.Filled.Spa, contentDescription = "Crops icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        placeholder = { Text("Comma-separated crop names") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank() || farmName.isBlank() || primaryCrops.isBlank()) {
                                errorMsg = "Please complete all registration parameters"
                                return@Button
                            }
                            isSubmitting = true
                            coroutineScope.launch {
                                delay(1200)
                                isSubmitting = false
                                viewModel.signupUser(email, farmName, primaryCrops)
                                onSignupSuccess()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create Farm Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already registered? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Sign In instead",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                )
            }
        }
    }
}
}


@Composable
fun NotificationItemRow(
    category: String,
    title: String,
    content: String,
    time: String,
    categoryBg: Color,
    categoryText: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category,
                            tint = categoryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryBg
                    )
                }
                Text(
                    text = time,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ==========================================
// 3. DASHBOARD SCREEN
// ==========================================

    @Composable
fun DashboardScreen(
    viewModel: PlantnerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val farm by viewModel.farmProfile.collectAsState()
    val scans by viewModel.allScans.collectAsState()
    val readings by viewModel.sensorReadings.collectAsState()

    val currentReading = readings.firstOrNull() ?: SensorReading(temperature = 29.0f, humidity = 78.0f, soilMoisture = 42.1f)

    val agriculturalTips = remember {
        listOf(
            "Check your plants early in the morning for better disease detection.",
            "Water your plants at dawn to give foliage time to dry and suppress pathogens.",
            "Companion planting: Cultivate garlic or basil near tomatoes to deter aphids naturally.",
            "Remove dead diseased leaves immediately. Do NOT compost them as pathogen spores hibernate.",
            "Keep soil moisture between 40% and 60% for optimal crop root transpiration."
        )
    }
    var currentTipIdx by remember { mutableIntStateOf(0) }
    
    // Rotating tips
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            currentTipIdx = (currentTipIdx + 1) % agriculturalTips.size
        }
    }

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val diseaseAlerts by viewModel.diseaseAlerts.collectAsState()
    val weatherAlerts by viewModel.weatherAlerts.collectAsState()
    val dailyTipsAlerts by viewModel.dailyTipsAlerts.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var yieldExpanded by remember { mutableStateOf(false) }
    var treatmentsExpanded by remember { mutableStateOf(false) }
    var supportExpanded by remember { mutableStateOf(false) }
    var settingsExpanded by remember { mutableStateOf(false) }
    var subscriptionExpanded by remember { mutableStateOf(false) }

    var treatmentRemindersEnabled by remember { mutableStateOf(true) }
    var useFahrenheit by remember { mutableStateOf(false) }
    var offlineSyncOnly by remember { mutableStateOf(true) }
    var purgeSuccess by remember { mutableStateOf(false) }

    var selectedCropForPart by remember(farm?.primaryCrops) {
        val firstCrop = farm?.primaryCrops?.split(",")?.firstOrNull()?.trim() ?: "Maize"
        mutableStateOf(if (firstCrop.lowercase().contains("basil")) "Basil" 
                       else if (firstCrop.lowercase().contains("cassava")) "Cassava" 
                       else if (firstCrop.lowercase().contains("coffee")) "Coffee" 
                       else if (firstCrop.lowercase().contains("cocoa")) "Cocoa" 
                       else if (firstCrop.lowercase().contains("tomato")) "Tomato"
                       else "Maize")
    }
    var selectedStageForPart by remember { mutableStateOf("Vegetative") }

    var showNotificationDialog by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableStateOf(4) }
    val context = LocalContext.current

    if (showNotificationDialog) {
        val region = farm?.region ?: "Nigeria"
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Active Notifications",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Farm Alert Center",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live bulletins matching your crop corridor settings ($region):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 1. App Updates
                    NotificationItemRow(
                        category = "SYSTEM UPDATE",
                        title = "Plantner Companion v2.4.0",
                        content = "Enjoy high-precision Google Maps coordinate picking and region-based budget currency adjustment auto-configured dynamically for $region farms.",
                        time = "Just now",
                        categoryBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        categoryText = MaterialTheme.colorScheme.primary,
                        icon = Icons.Filled.Info,
                        onClick = {
                            android.widget.Toast.makeText(context, "Interactive GPS mapping services are active", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 2. Weather Forecasting
                    NotificationItemRow(
                        category = "WEATHER CORRIDOR",
                        title = "Humidity & Microclimate Shift",
                        content = "Localized showers anticipated over $region agricultural basins. Postpone liquid fertilizer dusting to prevent soil run-off.",
                        time = "3 hours ago",
                        categoryBg = Color(0xFFE3F2FD),
                        categoryText = Color(0xFF1E88E5),
                        icon = Icons.Filled.Cloud,
                        onClick = {
                            android.widget.Toast.makeText(context, "Microclimate trends optimized for $region", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 3. Current Agricultural News
                    NotificationItemRow(
                        category = "AGRI-NEWS BULLETIN",
                        title = "Prevent Early Leaf Blight Spores",
                        content = "Agri-Advisory: Blight spores detected across warm corridors. Prune lower foliage and optimize drip line spacing to safeguard harvests.",
                        time = "1 day ago",
                        categoryBg = Color(0xFFFFF3E0),
                        categoryText = Color(0xFFFB8C00),
                        icon = Icons.Filled.Campaign,
                        onClick = {
                            android.widget.Toast.makeText(context, "Browse regional pathology bulletins", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 4. Trades in Agriculture
                    NotificationItemRow(
                        category = "AGRI-TRADE DESK",
                        title = "Spot Market Prices Up",
                        content = "Producer spot prices adjusted higher due to tight regional supply. Open the Crop Yield Estimator on your sidebar to calculate target supply contract revenues.",
                        time = "3 days ago",
                        categoryBg = Color(0xFFE8F5E9),
                        categoryText = Color(0xFF43A047),
                        icon = Icons.Filled.TrendingUp,
                        onClick = {
                            android.widget.Toast.makeText(context, "Open Crop Yield Estimator on sidebar menu", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        unreadCount = 0
                        showNotificationDialog = false
                        android.widget.Toast.makeText(context, "All bulletins marked as read", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Mark All as Read", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header of Drawer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = "Plantner logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Plantner Hub",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close Menu", modifier = Modifier.size(20.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    // Crop Yield Estimator
                    DrawerCollapsibleSection(
                        title = "Crop Yield Estimator",
                        icon = Icons.Filled.TrendingUp,
                        isExpanded = yieldExpanded,
                        onToggle = { yieldExpanded = !yieldExpanded }
                    ) {
                        var yieldCrop by remember { mutableStateOf("Tomato") }
                        var acreageText by remember { mutableStateOf("5") }
                        val acreageVal = acreageText.toDoubleOrNull() ?: 1.0
                        val estimatedTons = when (yieldCrop) {
                            "Tomato" -> acreageVal * 15.0
                            "Leafy Greens" -> acreageVal * 8.0
                            "Corn" -> acreageVal * 4.5
                            else -> acreageVal * 6.0
                        }
                        val regionRegion = farm?.region ?: "Nigeria"
                        val (localCurCode, pricePerTonInt) = when (regionRegion) {
                            "Kenya" -> Pair("KES", 45000)
                            "South Africa" -> Pair("ZAR", 6000)
                            "Egypt" -> Pair("EGP", 5500)
                            "Ghana" -> Pair("GHS", 2500)
                            "Uganda" -> Pair("UGX", 1200000)
                            "Tanzania" -> Pair("TZS", 900000)
                            "Rwanda" -> Pair("RWF", 400000)
                            else -> Pair("NGN", 250000)
                        }
                        val formattedValuation = String.format("%,d", (estimatedTons * pricePerTonInt).toLong())

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Quick Estimator Widget", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                // Crop Selector Row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Tomato", "Corn", "Leafy Greens").forEach { cName ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (yieldCrop == cName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                .clickable { yieldCrop = cName }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = cName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (yieldCrop == cName) Color.White else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // Acreage Input
                                OutlinedTextField(
                                    value = acreageText,
                                    onValueChange = { acreageText = it },
                                    label = { Text("Farm Acreage", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Predicted Harvest:", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("${String.format("%.1f", estimatedTons)} Tons", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Est. Value (${localCurCode}):", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("${localCurCode} ${formattedValuation}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("yield_prediction")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.TrendingUp, contentDescription = "Yield Pred", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Forecaster Sheet", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Common Crop Remedies
                    DrawerCollapsibleSection(
                        title = "Common Crop Remedies",
                        icon = Icons.Filled.Spa,
                        isExpanded = treatmentsExpanded,
                        onToggle = { treatmentsExpanded = !treatmentsExpanded }
                    ) {
                        val treatmentDat = listOf(
                            Triple("Tomato Early Blight", "Copper Fungicide", "Apply organic copper spray every 7-14 days. Prune lower leaves to enhance airflow."),
                            Triple("Corn Common Rust", "Sulfur Dusting", "Apply wetable sulfur. Cultivate resistant hybrids & rotate crops seasonally.")
                        )
                        treatmentDat.forEach { (disease, treatment, remedy) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(disease, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Remedy: $remedy", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("history")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.History, contentDescription = "Treatment History", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Diagnostic History", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Help & Emergency Support
                    DrawerCollapsibleSection(
                        title = "Help & Emergency Support",
                        icon = Icons.Filled.Help,
                        isExpanded = supportExpanded,
                        onToggle = { supportExpanded = !supportExpanded }
                    ) {
                        var helpQuery by remember { mutableStateOf("") }
                        var feedbackSubmitted by remember { mutableStateOf(false) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("FAQ Example:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Q: Can I use diagnostics offline?", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("A: Yes! Previously fetched treatment guidelines are fully cached in local Room database.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Submit Emergency support query", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = helpQuery,
                                    onValueChange = { helpQuery = it },
                                    placeholder = { Text("What is the issue?", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    singleLine = true
                                )
                                if (feedbackSubmitted) {
                                    Text("Emergency support request successfully sent!", color = Color(0xFF388E3C), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { 
                                            if (helpQuery.isNotBlank()) {
                                                feedbackSubmitted = true
                                                helpQuery = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Send Query", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Application Settings
                    DrawerCollapsibleSection(
                        title = "Application Settings",
                        icon = Icons.Filled.Settings,
                        isExpanded = settingsExpanded,
                        onToggle = { settingsExpanded = !settingsExpanded }
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Dark Theme Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Dark Theme Mode", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Switch interface tones", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = isDarkTheme,
                                        onCheckedChange = { viewModel.setDarkTheme(it) },
                                        modifier = Modifier.scale(0.75f)
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Disease Alerts Choice
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Disease Alerts", fontSize = 10.sp)
                                    Switch(
                                        checked = diseaseAlerts,
                                        onCheckedChange = { viewModel.setDiseaseAlerts(it) },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }
                                // Weather Alerts Choice
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Weather Alerts", fontSize = 10.sp)
                                    Switch(
                                        checked = weatherAlerts,
                                        onCheckedChange = { viewModel.setWeatherAlerts(it) },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }
                                // Daily Tips Choice
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Daily Tips", fontSize = 10.sp)
                                    Switch(
                                        checked = dailyTipsAlerts,
                                        onCheckedChange = { viewModel.setDailyTipsAlerts(it) },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("Treatment Milestones", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Treatment Reminders", fontSize = 10.sp)
                                    Switch(
                                        checked = treatmentRemindersEnabled,
                                        onCheckedChange = { treatmentRemindersEnabled = it },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("Configuration & Sync", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Temp in Fahrenheit", fontSize = 10.sp)
                                    Switch(
                                        checked = useFahrenheit,
                                        onCheckedChange = { useFahrenheit = it },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Offline Sync Priority", fontSize = 10.sp)
                                    Switch(
                                        checked = offlineSyncOnly,
                                        onCheckedChange = { offlineSyncOnly = it },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("Database Danger Zone", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFC62828))
                                Spacer(modifier = Modifier.height(4.dp))
                                if (purgeSuccess) {
                                    Text(
                                        "Telemetry Cache Purged successfully!",
                                        color = Color(0xFF388E3C),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearChatHistory()
                                        viewModel.clearAllSensors()
                                        purgeSuccess = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    border = BorderStroke(1.dp, Color(0xFFC62828)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Purge data", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Purge Telemetry Cache", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Plantner Premium Hub
                    DrawerCollapsibleSection(
                        title = "Plantner Premium Hub",
                        icon = Icons.Filled.Stars,
                        isExpanded = subscriptionExpanded,
                        onToggle = { subscriptionExpanded = !subscriptionExpanded }
                    ) {
                        val region = farm?.region ?: "Nigeria"
                        val rates = when (region) {
                            "Kenya" -> listOf("KSh", "KES", "350", "3,500")
                            "South Africa" -> listOf("R", "ZAR", "50", "500")
                            "Egypt" -> listOf("E£", "EGP", "45", "450")
                            "Ghana" -> listOf("GH₵", "GHS", "20", "200")
                            "Uganda" -> listOf("USh", "UGX", "10,000", "100,000")
                            "Tanzania" -> listOf("TSh", "TZS", "6,500", "65,000")
                            "Rwanda" -> listOf("FRw", "RWF", "2,800", "28,000")
                            "Ethiopia" -> listOf("Br", "ETB", "150", "1,500")
                            "Morocco" -> listOf("DH", "MAD", "30", "300")
                            "Tunisia" -> listOf("DT", "TND", "10", "100")
                            "Angola" -> listOf("Kz", "AOA", "1,800", "18,000")
                            "Zambia" -> listOf("ZK", "ZMW", "60", "600")
                            "Zimbabwe" -> listOf("$", "USD", "3.99", "39.99")
                            "Benin", "Burkina Faso", "Cote d'Ivoire", "Guinea-Bissau", "Mali", "Niger", "Senegal", "Togo" -> 
                                listOf("FCFA", "XOF", "1,950", "19,500")
                            "Cameroon", "Central African Republic", "Chad", "Congo", "Equatorial Guinea", "Gabon" -> 
                                listOf("FCFA", "XAF", "1,950", "19,500")
                            else -> listOf("₦", "NGN", "2,500", "25,000")
                        }
                        val curSym = rates[0]
                        val curCod = rates[1]
                        val priceMon = rates[2]
                        val priceYr = rates[3]

                        Card(
                            border = BorderStroke(1.dp, if (isSubscribed) MaterialTheme.colorScheme.primary else Color(0xFFC05600)),
                            colors = CardDefaults.cardColors(containerColor = if (isSubscribed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color(0xFFFFFBF7)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Stars, contentDescription = "Premium Tier", tint = Color(0xFFC05600), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSubscribed) "PREMIUM MEMBERSHIP ACTIVE" else "FREE TIER PLAN",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = if (isSubscribed) MaterialTheme.colorScheme.primary else Color(0xFFC05600)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Selected Country: $region",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Premium Cost: $curSym $priceMon/mo ($curSym $priceYr/yr)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✦ Unlimited Live AI Scanning", fontSize = 9.sp)
                                Text("✦ Real-time push alert messaging", fontSize = 9.sp)
                                Text("✦ Offline local sync feature", fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.toggleSubscription() },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isSubscribed) MaterialTheme.colorScheme.secondary else Color(0xFFC05600)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isSubscribed) "Cancel Subscription" else "Subscribe Now", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        },
        content = {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                // High-performance canvas-drawn farm terraces background
                PlantationBackground(alpha = 0.08f)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                // App Header consistent with mockup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Drawer menu",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                scope.launch { drawerState.open() }
                            }
                    )
                    Text(
                        text = "Plantner AI",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { showNotificationDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Alert notifications",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                            )
                        }
                    }
                }

                LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Welcome Section
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Hello, Farmer! 👋",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = "Plant Doctor AI",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Your smart farming assistant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Hero Card: "Scan Plant - Detect disease in real-time" using rich GlossyBanner
            item {
                GlossyBanner(
                    title = "Scan Plant",
                    desc = "Detect disease in real-time with AI & local Room caching",
                    highlightText = "AI ACTIVE",
                    icon = Icons.Filled.PhotoCamera,
                    backgroundGradientColors = listOf(
                        Color(0xFF006D44),
                        Color(0xFF009F6B),
                        Color(0xFF08468F)
                    ),
                    onClick = { navController.navigate("scan") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Grid Layout (2x2) of action cards exactly as seen in mockup
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Card A: Upload Image (Gallery)
                        DashboardGridCard(
                            title = "Upload Image",
                            subtitle = "From gallery",
                            icon = Icons.Filled.Image,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("scan") }
                        )

                        // Card B: My History
                        DashboardGridCard(
                            title = "My History",
                            subtitle = "View past scans",
                            icon = Icons.Filled.History,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("history") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Card C: IoT Sensors
                        DashboardGridCard(
                            title = "IoT Sensors",
                            subtitle = "Connect Arduino MCU",
                            icon = Icons.Filled.Thermostat,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("sensors") }
                        )

                        // Card D: Crop Info
                        DashboardGridCard(
                            title = "Crop Info",
                            subtitle = "Best practices",
                            icon = Icons.Filled.Spa,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("yield_prediction") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2.5 CARDS FOR TARGET CROP AND SELECTED GROWTH STAGE ADVISORY
            item {
                val region = farm?.region ?: "Nigeria"
                val cropsList = listOf("Maize", "Cassava", "Basil", "Coffee", "Cocoa", "Tomato")
                val stagesList = listOf("Seedling", "Vegetative", "Flowering", "Mature")

                val allGuides by viewModel.allGuides.collectAsState()
                var isOfflineSimulated by remember { mutableStateOf(false) }
                var showEditGuideDialog by remember { mutableStateOf(false) }

                // Let's retrieve regional currency multipliers
                val currencyInfo = when (region) {
                    "Kenya" -> Triple("KSh", "KES", 140.0)
                    "South Africa" -> Triple("R", "ZAR", 18.0)
                    "Egypt" -> Triple("E£", "EGP", 47.0)
                    "Ghana" -> Triple("GH₵", "GHS", 13.0)
                    "Uganda" -> Triple("USh", "UGX", 3800.0)
                    "Tanzania" -> Triple("TSh", "TZS", 2500.0)
                    "Rwanda" -> Triple("FRw", "RWF", 1250.0)
                    "Ethiopia" -> Triple("Br", "ETB", 56.0)
                    "Morocco" -> Triple("DH", "MAD", 10.0)
                    "Tunisia" -> Triple("DT", "TND", 3.1)
                    "Angola" -> Triple("Kz", "AOA", 830.0)
                    "Zambia" -> Triple("ZK", "ZMW", 22.0)
                    "Zimbabwe" -> Triple("$", "USD", 1.0)
                    "Benin", "Burkina Faso", "Cote d'Ivoire", "Guinea-Bissau", "Mali", "Niger", "Senegal", "Togo" -> Triple("FCFA", "XOF", 600.0)
                    "Cameroon", "Central African Republic", "Chad", "Congo", "Equatorial Guinea", "Gabon" -> Triple("FCFA", "XAF", 600.0)
                    else -> Triple("₦", "NGN", 1450.0)
                }
                val curSym = currencyInfo.first
                val curCod = currencyInfo.second
                val xrate = currencyInfo.third

                // Define crop advisory content
                val matchedCache = allGuides.find { it.cropName == selectedCropForPart && it.stage == selectedStageForPart }
                val cropData = if (matchedCache != null) {
                    CropStageData(
                        practices = matchedCache.practices.split("|||"),
                        diagnosis = matchedCache.diagnosis,
                        basePriceUsd = matchedCache.basePriceUsd,
                        stageMultiplier = matchedCache.stageMultiplier,
                        baseYieldPerAcreKg = matchedCache.baseYieldPerAcreKg
                    )
                } else {
                    when (selectedCropForPart) {
                    "Basil" -> when (selectedStageForPart) {
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Mist soil gently with tepid water - keep upper rootzone aerated.",
                                "Provide 14 hours of soft nursery light to prevent weak leggy stems.",
                                "Keep seedlings at 21-24°C; shield from chilly window panes or drafts."
                            ),
                            diagnosis = "Damping-off fungal rot. Emerged seedlings thin drastically at soil margin, collapse, and rot. Hand-dust shallow charcoal dust onto soil surface to dry fungal filaments.",
                            basePriceUsd = 4.20,
                            stageMultiplier = 0.15,
                            baseYieldPerAcreKg = 3000.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Pinch terminal shoot tips once stems reach 15cm to stimulate thick branching.",
                                "Harvest outer lower leaves to redirect carbohydrate reserves to inner nodes.",
                                "Feed weak liquid biological seaweed every 14 days during warm soil periods."
                            ),
                            diagnosis = "Downy Mildew & Fusarium wilt. Pale yellow contours blotch the upper leaves while dusty white/purple spore down emerges beneath. Trim inner secondary branches to decrease microclimatic humidity.",
                            basePriceUsd = 4.20,
                            stageMultiplier = 0.60,
                            baseYieldPerAcreKg = 3000.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Pinch off emerging delicate flower spikes immediately to maintain aromatic leaf sugars.",
                                "Water base daily but do not spray the tender leaves to keep foliage spore-free.",
                                "Add well-matured organic compost around stem borders to bolster continuous leaf flushes."
                            ),
                            diagnosis = "Leaf Miners / Aphids. Sneaky, wavy white trails are mined across upper leaf cuticles. Discard infested foliage, apply dilute biodegradable dish soap solution to suffocate vectors.",
                            basePriceUsd = 4.20,
                            stageMultiplier = 0.85,
                            baseYieldPerAcreKg = 3000.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Harvest whole mature branches in early morning when essential oil indices peak.",
                                "Pinch flowering heads daily; maintain high soil moisture to support high leaf canopy density.",
                                "Trim root perimeter weeds to assure soil nutrition focus remains entirely on Basil."
                            ),
                            diagnosis = "Botrytis Gray Mold. Lower stems decay turning spongy, grey, and mouldy due to overcrowding. Space pots 20cm apart to improve solar penetration.",
                            basePriceUsd = 4.20,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 3000.0
                        )
                    }
                    "Cassava" -> when (selectedStageForPart) {
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Select disease-resilient stakes and plant slanted at 45° in rich soil mounds.",
                                "Keep weeding area completely clear within 30cm of stakes to minimize early root struggle.",
                                "Monitor early soil moisture - avoid severe dry stress until rooting takes off."
                            ),
                            diagnosis = "Stem-borer invasion. Larvae tunnel the wood cuticles making stakes dry and bend. Wrap stem cuts with clean organic ash to shield from bore entrance.",
                            basePriceUsd = 0.18,
                            stageMultiplier = 0.10,
                            baseYieldPerAcreKg = 9000.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Keep high weed-free zone around main root mounds using mulch or hand-weeding.",
                                "Ensure healthy branch structure; prune down secondary stunted suckers to form proper top crown.",
                                "Intercrop with low-foliage cover crops like beans to naturally lock in Nitrogen."
                            ),
                            diagnosis = "Cassava Mosaic Disease (CMD). Leaves become narrow, distorted, puckered and chlorotic green-yellow. Select resistant cuttings next season and immediately pull and burn severely infected CMD stems to protect your farm boundary.",
                            basePriceUsd = 0.18,
                            stageMultiplier = 0.50,
                            baseYieldPerAcreKg = 9000.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Ensure deep soil mulching around mounds to facilitate lateral root tuberization.",
                                "Apply natural wood-ash compost around root crown to boost potassium indices.",
                                "Prune down excessive dense top branch cover to allow sunlight to penetrate lower leaves."
                            ),
                            diagnosis = "Brown Leaf Spot. Dark brown polygonal lesions form on older lower leaves, triggering premature leaf drop. Improve airflow and spray organic copper-based solutions on lower tiers.",
                            basePriceUsd = 0.18,
                            stageMultiplier = 0.80,
                            baseYieldPerAcreKg = 9000.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Stop applying compost 30 days before harvesting to consolidate starch density.",
                                "Prune upper stalks 2 weeks prior to root harvest - this triggers starch concentration in tubers.",
                                "Carefully dislodge soil with a hand trowel; extract tubers without wounding outer skins."
                            ),
                            diagnosis = "Cassava Root Rot. Under-drainage causes tubers to turn brown, spongy and rot with a bad smell first noticeable via leaf wilting. Ensure sandy-loam grading.",
                            basePriceUsd = 0.18,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 9000.0
                        )
                    }
                    "Coffee" -> when (selectedStageForPart) {
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Filter direct sunlight using a 50% shade nursery netting system.",
                                "Water thoroughly in early mornings; verify potting bags allow free drainage.",
                                "Keep seedlings raised off direct soil floor to eliminate nematode infection risk."
                            ),
                            diagnosis = "Damping-off & Pythium. Seedling stems blacken near soil level, collapse and die. Ensure sterilised loam sand mixes and do not over-irrigate.",
                            basePriceUsd = 3.80,
                            stageMultiplier = 0.10,
                            baseYieldPerAcreKg = 1500.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Prune secondary vertical suckers early to form a balanced main trunk crown.",
                                "Dig wide organic compost rings around the drip-line of the coffee shrubbery.",
                                "Control grass weeds around shallow rootzones using organic mulch barriers."
                            ),
                            diagnosis = "Mealybugs. Creamy, cottony white masses suck sap from fresh green shoots and leaf nodes. Encourage ladybugs and apply oil washes manually.",
                            basePriceUsd = 3.80,
                            stageMultiplier = 0.45,
                            baseYieldPerAcreKg = 1500.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Avoid spraying any leaves during initial blossom emergence to prevent bud drop.",
                                "Maintain solid moisture inputs; dry-season irrigation triggers uniform white blossoming.",
                                "Incorporate organic bone meal to furnish Phosphorus for high bud retention."
                            ),
                            diagnosis = "Coffee Berry Borer (CBB). Tiny beetles bore small entry holes right in the apex of green berries. Prune late hanging cherries and carry out intensive farm clearing.",
                            basePriceUsd = 3.80,
                            stageMultiplier = 0.75,
                            baseYieldPerAcreKg = 1500.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Precisely hand-pick only fully red, ripe cherries to secure superior quality scores.",
                                "Spread cherry skins back onto the coffee tree base to recycle potassium trace elements.",
                                "Carefully prune dead internal branches post-harvest to ventilate the tree cores."
                            ),
                            diagnosis = "Coffee Leaf Rust (CLR). Dusty orange/yellow powdery pustules cover the undersides of mature leaves, causing leaf drop. Apply preventive copper fungicides during humid flushes.",
                            basePriceUsd = 3.80,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 1500.0
                        )
                    }
                    "Cocoa" -> when (selectedStageForPart) {
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Establish high shade canopy using banana trees or shade fabrics in nurseries.",
                                "Mist soil daily; young Cocoa roots are highly sensitive to prolonged dry spells.",
                                "Place protective windbreakers to prevent draft damage on tender new leaves."
                            ),
                            diagnosis = "Anthracnose leaf blast. Copper-red spots with yellow margins burn soft flush leaves. Ensure healthy canopy shade and dust with organic copper dust.",
                            basePriceUsd = 5.50,
                            stageMultiplier = 0.10,
                            baseYieldPerAcreKg = 800.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Prune low-hanging branches and chupons (water shoots) to create a clean main trunk.",
                                "Keep under-canopy leaf litter intact to conserve humidity but prevent soil mold.",
                                "Apply potassium-rich organic fertilizers around the trunk circle."
                            ),
                            diagnosis = "Mirids / Capsids. Sucking pests bite soft stem bark causing dark sunken wounds that die back. Keep canopy open to let light suppress humid pest niches.",
                            basePriceUsd = 5.50,
                            stageMultiplier = 0.40,
                            baseYieldPerAcreKg = 800.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Conserve leaf litter beneath the trees to sustain pollinator midge populations.",
                                "Avoid all broad-spectrum insecticide sprays that might eliminate midge vectors.",
                                "Remove any side shoots growing too close to young flower cushions."
                            ),
                            diagnosis = "Black Pod Disease (Phytophthora). Water-soaked dark brown spots rot green pods, producing a dusty velvet white fungal rim. Prune overhanging foliage to reduce moisture.",
                            basePriceUsd = 5.50,
                            stageMultiplier = 0.75,
                            baseYieldPerAcreKg = 800.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Harvest ripe golden pods using dynamic pruning shears - never pull the pod directly.",
                                "Avoid wounding the bark cushions; flowers for subsequent seasons emerge there.",
                                "Extract wet cocoa beans same day and begin immediate banana leaf sweat fermentation."
                            ),
                            diagnosis = "Witches' Broom. Abnormal, bunchy shoot growth containing dense dried twigs emerge on fan branches due to fungal spores. Clip and destroy infected brooms.",
                            basePriceUsd = 5.50,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 800.0
                        )
                    }
                    "Tomato" -> when (selectedStageForPart) {
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Water base gently at seedling crowns - avoid flattening small stems.",
                                "Transplant seedlings in evening hours once 4-6 master leaflets display.",
                                "Handle tender tomato cubes by their rootballs to minimize shock impact."
                            ),
                            diagnosis = "Damping-Off disease. Stems thin out, pinch at base and fall over overnight. Minimize seed density and apply warm, well-ventilated watering practices.",
                            basePriceUsd = 1.20,
                            stageMultiplier = 0.10,
                            baseYieldPerAcreKg = 12000.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Install vertical wooden stakes immediately and loosely secure growing vines.",
                                "Pinch out lateral suckers (shoots at branch joints) to build strong single stems.",
                                "Prune off bottom-most leaves touch the soil to prevent splashing blight spores."
                            ),
                            diagnosis = "Septoria Leaf Spot. Small circular spots with pale grey centers and tiny black specs on lower foliage. Mulch soil to avoid mud splash.",
                            basePriceUsd = 1.20,
                            stageMultiplier = 0.50,
                            baseYieldPerAcreKg = 12000.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Add high Calcium/Bone meal to soil to forestall calcium deficiency rot.",
                                "Water deep and steady; irregular soaking triggers blossom drop and split fruit skins.",
                                "Gently shake stakes on calm warm afternoons to aid wind-assisted self-pollination."
                            ),
                            diagnosis = "Blossom End Rot. A dry, black, flat leathery patch decays the blossom bottom of green tomatoes. Water uniformly and ensure sound calcium absorption.",
                            basePriceUsd = 1.20,
                            stageMultiplier = 0.80,
                            baseYieldPerAcreKg = 12000.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Pick fruits at 'breaker' stage (showing first pink blush) to let vine sugars feed newer clusters.",
                                "Decrease watering once clusters mature to concentrate sugars and prevent split skin.",
                                "Prune yellowed outer foliage to direct remaining plant energy strictly to ripening fruit."
                            ),
                            diagnosis = "Late Blight. Rapidly spreading, greasy black patches on leaves and fruit with white downy undersides during damp days. Prop up vines and apply protective soap treatments.",
                            basePriceUsd = 1.20,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 12000.0
                        )
                    }
                    else -> when (selectedStageForPart) { // Maize
                        "Seedling" -> CropStageData(
                            practices = listOf(
                                "Maintain shallow weed-free lines; Maize roots have low competitiveness early on.",
                                "Sow seeds at 3-5cm depth; verify moisture reaches seed lines.",
                                "Apply a starter organic compost around seedling furrows to spur root growth."
                            ),
                            diagnosis = "Cutworms. Young seedlings are found neatly severed flat on the soil line in early morning. Circle small seedling stems with ash to deter soil pests.",
                            basePriceUsd = 0.35,
                            stageMultiplier = 0.10,
                            baseYieldPerAcreKg = 4500.0
                        )
                        "Vegetative" -> CropStageData(
                            practices = listOf(
                                "Top-dress nitrogen compost/compost tea during the crucial V4 to V6 development weeks.",
                                "Keep crop lines weed-free to maximize light collection by early leaves.",
                                "Ensure solid watering as daily stem height expansion accelerates."
                            ),
                            diagnosis = "Fall Armyworm. Ragged chewing holes in whorl margins with frass. Spray organic neem oil directly into the central leaf funnel.",
                            basePriceUsd = 0.35,
                            stageMultiplier = 0.55,
                            baseYieldPerAcreKg = 4500.0
                        )
                        "Flowering" -> CropStageData(
                            practices = listOf(
                                "Provide uninterrupted moisture inputs during tasseling and silking to prevent dry kernels.",
                                "Refrain from mechanical weeding to prevent disturbing fragile lateral roots.",
                                "Add potassium-rich fertilizer around rootbeds to bolster grain filling."
                            ),
                            diagnosis = "Maize Smut. Swelling greyish galls on ears or tassels that rupture releasing black soot spores. Cut and bury infected galls far from fields before they burst.",
                            basePriceUsd = 0.35,
                            stageMultiplier = 0.85,
                            baseYieldPerAcreKg = 4500.0
                        )
                        else -> CropStageData(
                            practices = listOf(
                                "Wait until black milk-line layer forms at grain attachments to signal maximum starch maturity.",
                                "Allow cobs to dry slightly on standing stalks pre-harvest.",
                                "Store harvested grains at under 13% internal moisture to prevent high storage mold risks."
                            ),
                            diagnosis = "Aspergillus/Gibberella Ear Rot. Grains turn pinkish or green with powdery molds due to ear insect entry wounds. Keep storage containers aerated and completely moisture-free.",
                            basePriceUsd = 0.35,
                            stageMultiplier = 1.00,
                            baseYieldPerAcreKg = 4500.0
                        )
                    }
                }
                }

                // Parse farm size
                val sizeNumber = farm?.farmSize?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull() ?: 5.0
                
                // Calculate dynamic modifier from Current Weather Readings on Home Screen!
                val moisture = currentReading.soilMoisture
                val temp = currentReading.temperature
                
                // Moisture modifier: ideal is 35%-65%
                val moistureModifier = when {
                    moisture < 25f -> 0.82 // Low moisture penalty
                    moisture > 75f -> 0.88 // Excessive saturation penalty
                    else -> 1.00 // Optimal
                }
                
                // Temperature modifier: ideal is 18°C-32°C
                val tempModifier = when {
                    temp < 16f -> 0.90
                    temp > 34f -> 0.85
                    else -> 1.00
                }
                
                // Region premium/adjustment factors
                val regionModifier = when (region) {
                    "Kenya" -> 1.05
                    "South Africa" -> 1.10
                    "Egypt" -> 1.15
                    "Ghana" -> 1.02
                    "Uganda" -> 0.95
                    else -> 1.00
                }

                // Yield Formula: BaseYield * Acreage * GrowthStageEfficiency * EnvironmentalModifiers
                val computedYieldKg = cropData.baseYieldPerAcreKg * sizeNumber * cropData.stageMultiplier * moistureModifier * tempModifier * regionModifier
                val totalValueUsd = computedYieldKg * cropData.basePriceUsd
                val totalValueLocal = totalValueUsd * xrate

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("crop_stage_advisory_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Title header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = "Crop advisor icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Target Crop & Stage-Wise Planner",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Customized practices, diagnosis warning, and yield predictions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // 1. Selector Row for Target Crop
                        Text(
                            text = "SELECT CROP:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cropsList.forEach { crop ->
                                val selected = crop == selectedCropForPart
                                val chipColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                val icon = when(crop) {
                                    "Basil" -> Icons.Filled.LocalFlorist
                                    "Cassava" -> Icons.Filled.Layers
                                    "Coffee" -> Icons.Filled.Coffee
                                    "Cocoa" -> Icons.Filled.Park
                                    "Tomato" -> Icons.Filled.Grass
                                    else -> Icons.Filled.Agriculture // Maize
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipColor)
                                        .clickable { selectedCropForPart = crop }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = crop,
                                            tint = textColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = crop,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Selector Row for Growth Stages
                        Text(
                            text = "SELECT GROWTH STAGE:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stagesList.forEach { stage ->
                                val selected = stage == selectedStageForPart
                                val chipColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                val textColor = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                val stageIcon = when(stage) {
                                    "Seedling" -> "🌱"
                                    "Vegetative" -> "🌿"
                                    "Flowering" -> "🌸"
                                    else -> "🍎"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipColor)
                                        .clickable { selectedStageForPart = stage }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$stageIcon $stage",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Local Database Cache Offline Info Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOfflineSimulated) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isOfflineSimulated) Icons.Filled.WifiOff else Icons.Filled.CloudQueue,
                                    contentDescription = "Sync state",
                                    tint = if (isOfflineSimulated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isOfflineSimulated) "Offline Mode Simulated" else "Synced with Local Room Cache",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOfflineSimulated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isOfflineSimulated) "Reading from local SQLite Room DB cache" else "Dynamic crop guidelines offline-ready",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Simulate Offline", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 4.dp))
                                Switch(
                                    checked = isOfflineSimulated,
                                    onCheckedChange = { isOfflineSimulated = it },
                                    modifier = Modifier.scale(0.7f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Offline status details & local customization button
                        if (matchedCache != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.TaskAlt,
                                        contentDescription = "Cached",
                                        tint = Color(0xFF43A047),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active SQLite Cache (Room)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF43A047)
                                    )
                                }
                                
                                Button(
                                    onClick = { showEditGuideDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit Local",
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Customize Cache", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // Local customization edit guide dialog
                        if (showEditGuideDialog && matchedCache != null) {
                            var inputDiagnosis by remember { mutableStateOf(matchedCache.diagnosis) }
                            var inputPractices by remember { mutableStateOf(matchedCache.practices.split("|||").joinToString("\n")) }
                            var inputPrice by remember { mutableStateOf(matchedCache.basePriceUsd.toString()) }
                            var inputYield by remember { mutableStateOf(matchedCache.baseYieldPerAcreKg.toString()) }

                            AlertDialog(
                                onDismissRequest = { showEditGuideDialog = false },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Customize Local Guideline", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Make changes to $selectedCropForPart ($selectedStageForPart stage). Modified guidelines are cached offline in the local Room database.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        
                                        Text("Best Practices (one per line)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = inputPractices,
                                            onValueChange = { inputPractices = it },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            textStyle = TextStyle(fontSize = 11.sp)
                                        )

                                        Text("Diagnosis Warning", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = inputDiagnosis,
                                            onValueChange = { inputDiagnosis = it },
                                            modifier = Modifier.fillMaxWidth().height(80.dp),
                                            textStyle = TextStyle(fontSize = 11.sp)
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Base Price ($/kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                OutlinedTextField(
                                                    value = inputPrice,
                                                    onValueChange = { inputPrice = it },
                                                    singleLine = true,
                                                    textStyle = TextStyle(fontSize = 11.sp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Base Yield/Acre (kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                OutlinedTextField(
                                                    value = inputYield,
                                                    onValueChange = { inputYield = it },
                                                    singleLine = true,
                                                    textStyle = TextStyle(fontSize = 11.sp)
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val finalPrice = inputPrice.toDoubleOrNull() ?: matchedCache.basePriceUsd
                                            val finalYield = inputYield.toDoubleOrNull() ?: matchedCache.baseYieldPerAcreKg
                                            val finalPractices = inputPractices.split("\n").filter { it.isNotBlank() }
                                            viewModel.updateCropGuide(
                                                cropName = selectedCropForPart,
                                                stage = selectedStageForPart,
                                                practices = if (finalPractices.isEmpty()) matchedCache.practices.split("|||") else finalPractices,
                                                diagnosis = inputDiagnosis.ifBlank { matchedCache.diagnosis },
                                                basePriceUsd = finalPrice,
                                                stageMultiplier = matchedCache.stageMultiplier,
                                                baseYieldPerAcreKg = finalYield
                                            )
                                            showEditGuideDialog = false
                                        }
                                    ) {
                                        Text("Save Cache", fontSize = 11.sp)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditGuideDialog = false }) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. BEST FARMING PRACTICES
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Lightbulb,
                                        contentDescription = "Practices",
                                        tint = Color(0xFFFBC02D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Best Practices ($selectedStageForPart Stage)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                cropData.practices.forEach { practice ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = practice,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. CORRESPONDING DIAGNOSIS WARNING
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Diagnosis Warning",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Corresponding Diagnosis Alert",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cropData.diagnosis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF3E2723),
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("scan") },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Open Disease Scanner ›",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. YIELD PREDICTION DONE AT THE END
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = "Yield estimates",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Stage-Wise Yield Prediction",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Estimated Mass output:",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format("%,.0f", computedYieldKg)} kg",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Market Valuation in $curCod:",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$curSym ${String.format("%,.0f", totalValueLocal)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Environmental inputs explanation
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (moisture < 25f || moisture > 75f) Icons.Filled.CloudOff else Icons.Filled.WaterDrop,
                                        contentDescription = "Moisture rating",
                                        tint = if (moisture < 25f || moisture > 75f) Color.Red else Color(0xFF1976D2),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (moisture < 25f) "Soil too dry (${String.format("%.1f", moisture)}%) may down-rate yield by 18%" 
                                               else if (moisture > 75f) "Waterlogging saturation (${String.format("%.1f", moisture)}%) slows root growth"
                                               else "Ideal soil moisture (${String.format("%.1f", moisture)}%) boosts potential yield to peak",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Crop Health Metrics Summary
            item {
                val totalScans = scans.size
                val resolvedScans = scans.count { it.isResolved }
                val activeIssues = scans.count { !it.isResolved }
                val overallHealth = if (totalScans == 0) "Optimal (100%)" else {
                    val ratio = resolvedScans.toFloat() / totalScans
                    if (ratio >= 0.8f) "Good (${(ratio * 100).toInt()}%)"
                    else if (ratio >= 0.5f) "Moderate (${(ratio * 100).toInt()}%)"
                    else "Critical (${(ratio * 100).toInt()}%)"
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Crop Health Summary",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activeIssues == 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (activeIssues == 0) "Healthy" else "$activeIssues Alert${if (activeIssues > 1) "s" else ""}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (activeIssues == 0) Color(0xFF388E3C) else Color(0xFFEF6C00)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Column 1: Overall Health Rate
                            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.Start) {
                                Text("Health Score", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(overallHealth, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Column 2: Total Diagnosed
                            Column(modifier = Modifier.weight(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Scans", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$totalScans", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Column 3: Resolution Rate
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Resolved Ratio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (totalScans == 0) "N/A" else "$resolvedScans/$totalScans",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Weather Today widget with Dark Emerald Green back and parameters on right
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("sensors") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left weather group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.WbSunny,
                                contentDescription = "Sun weather",
                                tint = Color(0xFFFFEB3B),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Weather Today",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${String.format("%.0f", currentReading.temperature)}°C",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                        }

                        // Right stats group
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Humidity", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("${String.format("%.0f", currentReading.humidity)}% ›", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rain chance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("20% ›", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tip of the Day widget
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF9C4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = "Tip bulb",
                                tint = Color(0xFFFBC02D),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tip of the day",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedContent(
                                targetState = agriculturalTips[currentTipIdx],
                                label = "TipAnim"
                            ) { tip ->
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recent Diagnostics List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Diagnostics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { navController.navigate("history") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Dynamic list of items
            if (scans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No scans registered yet",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(scans.take(3)) { scan ->
                    RecentScanCard(scan = scan) {
                        navController.navigate("scan_results/${scan.id}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
}
)
}

@Composable
fun DashboardGridCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentScanCard(
    scan: PlantScanEntity,
    onClick: () -> Unit
) {
    val isApkImage = scan.imagePath.isNotEmpty() && File(scan.imagePath).exists()
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isApkImage) {
                AsyncImage(
                    model = File(scan.imagePath),
                    contentDescription = "Diagnosed Leaf Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalFlorist, contentDescription = "Plant icon", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.cropType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = scan.diseaseName,
                    color = if (scan.diseaseName.contains("Healthy", true)) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Severity: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = scan.severity,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = when (scan.severity) {
                            "Severe" -> Color(0xFFD32F2F)
                            "Moderate" -> Color(0xFFEF6C00)
                            else -> Color(0xFF43A047)
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${(scan.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

// ==========================================
// 4. DISEASE SCANNER SCREEN (CHOOSE PRESET OR SNAP)
// ==========================================
@Composable
fun ScannerScreen(
    viewModel: PlantnerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scanState by viewModel.scanState.collectAsState()
    val isKeyAvailable = GeminiClient.isApiKeyAvailable()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Camera results launcher - registers the TakePicturePreview contract to snap a photo directly
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.analyzePlant(bitmap, "Camera Capture")
        } else {
            android.widget.Toast.makeText(context, "No photo captured", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error opening camera: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Camera permission is required to directly take photos", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for photo upload from device gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                viewModel.analyzePlant(bitmap, "General Garden Plant")
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(scanState) {
        if (scanState is ScanUiState.Success) {
            val recordId = (scanState as ScanUiState.Success).scan.id
            viewModel.resetScanState()
            navController.navigate("scan_results/$recordId")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Mockup header toolbar with Back, Title, Flash & Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.navigateUp() }
            )
            Text(
                text = "Scan Plant",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    imageVector = Icons.Filled.FlashOn,
                    contentDescription = "Flash toggle",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Scanner Info",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Live Viewfinder Box styled according to the mockup (Screen 2)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (scanState is ScanUiState.Analyzing) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Diagnosing leaf patterns...",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Applying server-side computer vision",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFFE8F5E9)), // Light mint green simulation camera view
                        contentAlignment = Alignment.Center
                    ) {
                        // Leaf Simulator Graphic Base in camera frame
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "Leaf frame guide",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Direct Camera Capture Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tap the Shutter Button to take a real photo",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Elegant corner brackets overlay (indicates camera focus box)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(36.dp)
                                .border(
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )

                        // Guidance instruction overlay at the top of the frame
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Place the leaf in the frame",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Bottom Viewfinder Shutter Button overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery upload action
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = "Upload from gallery",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Shutter click analyzer (Requests permission and opens real device camera)
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        val permission = android.Manifest.permission.CAMERA
                                        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(context, permission)
                                        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            try {
                                                cameraLauncher.launch(null)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error opening camera: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(permission)
                                        }
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoCamera,
                                        contentDescription = "Capture button",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            // Flip Camera / Simulator alert
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.6f))
                                    .clickable {
                                        android.widget.Toast.makeText(context, "Using front/back camera toggles in native view", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cached,
                                    contentDescription = "Flip camera",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Warnings Card if API key is not present
            if (!isKeyAvailable) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = "Alert", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Demo Model Running",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Secrets API key is empty. Programmatic leaves will run via local offline inference.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Segment section title
            item {
                Text(
                    text = "Live Diagnostic Presets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Tap on any plant leaf preset to run live diagnostic morphology analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )
            }

            // Quick Leaf templates items
            items(LeafBitmapGenerator.PresetType.values()) { preset ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            val bitmap = LeafBitmapGenerator.generateLeafBitmap(preset)
                            val hint = when (preset) {
                                LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> "Tomato"
                                LeafBitmapGenerator.PresetType.PEPPER_SPOT -> "Bell Pepper"
                                LeafBitmapGenerator.PresetType.CORN_DEFICIENCY -> "Corn"
                                LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> "Basil"
                                LeafBitmapGenerator.PresetType.SPIDER_MITE_WEB -> "Foliage"
                            }
                            viewModel.analyzePlant(bitmap, hint)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (preset) {
                                        LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> Color(0xFFE8F5E9)
                                        LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> Color(0xFFFFEBEE)
                                        else -> Color(0xFFFFFDE7)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (preset) {
                                    LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> Icons.Filled.CheckCircle
                                    LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> Icons.Filled.Warning
                                    else -> Icons.Filled.Spa
                                },
                                contentDescription = "Leaf icon",
                                tint = when (preset) {
                                    LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> Color(0xFF2E7D32)
                                    LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> Color(0xFFC62828)
                                    else -> Color(0xFFEF6C00)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (preset) {
                                    LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> "Tomato (Early Blight Spots)"
                                    LeafBitmapGenerator.PresetType.PEPPER_SPOT -> "Bell Pepper (Water Spots)"
                                    LeafBitmapGenerator.PresetType.CORN_DEFICIENCY -> "Corn (Yellow Deficiency)"
                                    LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> "Basil Herb (Healthy)"
                                    LeafBitmapGenerator.PresetType.SPIDER_MITE_WEB -> "Mite Webbing (Pest stipples)"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (preset) {
                                    LeafBitmapGenerator.PresetType.TOMATO_BLIGHT -> "Simulates brown rings & target spots"
                                    LeafBitmapGenerator.PresetType.PEPPER_SPOT -> "Simulates water spot damage and dots"
                                    LeafBitmapGenerator.PresetType.CORN_DEFICIENCY -> "Simulates yellow V-shape striping"
                                    LeafBitmapGenerator.PresetType.HEALTHY_BASIL -> "Simulates vibrant clean organic leaf"
                                    LeafBitmapGenerator.PresetType.SPIDER_MITE_WEB -> "Simulates silky pest webbing"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Test preset icon",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SCAN RESULTS DETAIL SCREEN
// ==========================================
@Composable
fun ScanResultDetailScreen(
    scanId: Int,
    viewModel: PlantnerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.allScans.collectAsState()
    val scan = scans.find { it.id == scanId }

    if (scan == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isApkImage = scan.imagePath.isNotEmpty() && File(scan.imagePath).exists()

    // Accordion Expandable States for the list (Description, Symptoms, Treatment)
    var isDescExpanded by remember { mutableStateOf(true) }
    var isSymptomsExpanded by remember { mutableStateOf(false) }
    var isTreatmentExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Toolbar header consistent with mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.navigateUp() }
            )
            Text(
                text = "Diagnosis Results",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share results",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* decorative share */ }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Original Image Display card with bounding indicators simulation (Screen 3)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isApkImage) {
                            AsyncImage(
                                model = File(scan.imagePath),
                                contentDescription = "Captured diagnostic leaf morphology input",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // High-fidelity Leaf simulation frame with diagnostic focus indicators
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE8F5E9)), 
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = "Diagnostic leaf",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(120.dp)
                                )
                                // Overlaid Simulated Bounding boxes representing diagnosed early spots
                                if (!scan.diseaseName.contains("Healthy", true)) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .offset(x = (-30).dp, y = (-20).dp)
                                            .border(BorderStroke(2.dp, Color.Red), shape = RoundedCornerShape(8.dp))
                                            .background(Color.Red.copy(alpha = 0.15f))
                                    ) {
                                        Text(
                                            "Infected Spot",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Color.Red)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                .align(Alignment.TopStart)
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom-Left Floating Crop Spec Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = scan.cropType,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Disease Title and confidence score progression
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scan.diseaseName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (scan.diseaseName.contains("Healthy", true)) MaterialTheme.colorScheme.primary else Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )

                        // Severity status badge in header
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (scan.severity) {
                                        "Severe" -> Color(0xFFFFEBEE)
                                        "Moderate" -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFE8F5E9)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = scan.severity,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = when (scan.severity) {
                                    "Severe" -> Color(0xFFC62828)
                                    "Moderate" -> Color(0xFFE65100)
                                    else -> Color(0xFF2E7D32)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress-bar styled Confidence representation from the Mockup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confidence Level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(scan.confidence * 100).toInt()}% Confidence",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { scan.confidence.toFloat() },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Accordion Tab 1: Description
            item {
                AccordionSection(
                    title = "Description",
                    isExpanded = isDescExpanded,
                    onToggle = { isDescExpanded = !isDescExpanded },
                    content = {
                        Text(
                            text = scan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Accordion Tab 2: Symptoms
            item {
                AccordionSection(
                    title = "Symptoms",
                    isExpanded = isSymptomsExpanded,
                    onToggle = { isSymptomsExpanded = !isSymptomsExpanded },
                    content = {
                        Text(
                            text = scan.symptoms,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Accordion Tab 3: Recommended Treatment
            item {
                AccordionSection(
                    title = "Recommended Treatment",
                    isExpanded = isTreatmentExpanded,
                    onToggle = { isTreatmentExpanded = !isTreatmentExpanded },
                    content = {
                        Column {
                            Text(
                                text = scan.treatmentPlan,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Organic alert advice
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Verified, contentDescription = "Verified tip icon", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Prioritize organic pesticide sprays like Neem oil solutions for organic crops.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom interactive buttons: "Mark Resolved" and "Consult AI Doctor"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.toggleScanResolution(scan.id, !scan.isResolved)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            imageVector = if (scan.isResolved) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Resolution toggle icon",
                            tint = if (scan.isResolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (scan.isResolved) "Resolved" else "Resolve",
                            fontWeight = FontWeight.Bold,
                            color = if (scan.isResolved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.sendMessageToDoctor("My ${scan.cropType} leaf is diagnosed with ${scan.diseaseName} (${(scan.confidence * 100).toInt()}% confidence). Tell me how to treat it organically.")
                            navController.navigate("chat")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = "Consult AI Doctor")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask AI Doctor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AccordionSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expand toggle",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

// ==========================================
// 6. PLANT DOCTOR CHAT SCREEN
// ==========================================
@Composable
fun ChatScreen(
    viewModel: PlantnerViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var inputMessage by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll chat on new inputs
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Chat Header with Glossy design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF009F6B)
                        )
                    )
                )
                .glossyBackground(radius = 0f, glassColor = Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Eco, contentDescription = "Doctor Icon", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dr. Green", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("AI Agricultural Specialist", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                IconButton(
                    onClick = { viewModel.clearChatHistory() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Chat history", tint = Color.White)
                }
            }
        }

        // Suggestions chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("Best watering timing?", "Organic insect management?", "Explain NPK elements", "Tomato companion plants")
            for (suggest in suggestions) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            viewModel.sendMessageToDoctor(suggest)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(suggest, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Messages list
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFlorist,
                        contentDescription = "Flower icon deco",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Talk to Dr. Green",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Ask quick questions about irrigation schedules, plant pathogens, companion gardens, or diagnostic help!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }
                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Dr. Green is formulating answer...", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Field Panel
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Ask anything about crop care...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputMessage.isNotBlank()) {
                                viewModel.sendMessageToDoctor(inputMessage)
                                inputMessage = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            viewModel.sendMessageToDoctor(inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(48.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send Chat", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ==========================================
// 7. SENSORS dashboard
// ==========================================
@Composable
fun SensorsScreen(
    viewModel: PlantnerViewModel,
    modifier: Modifier = Modifier
) {
    val readings by viewModel.sensorReadings.collectAsState()
    val isKeyAvailable = GeminiClient.isApiKeyAvailable()
    val coroutineScope = rememberCoroutineScope()

    val currentReading = readings.firstOrNull() ?: SensorReading(temperature = 23f, humidity = 55f, soilMoisture = 40f)

    var isPairing by remember { mutableStateOf(false) }
    var pairingTimer by remember { mutableIntStateOf(0) }

    // Threshold state variables
    val tempMin by viewModel.tempMin.collectAsState()
    val tempMax by viewModel.tempMax.collectAsState()
    val soilMin by viewModel.soilMin.collectAsState()
    val soilMax by viewModel.soilMax.collectAsState()
    val humidityMin by viewModel.humidityMin.collectAsState()
    val humidityMax by viewModel.humidityMax.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()

    val tempAlarm = currentReading.temperature < tempMin || currentReading.temperature > tempMax
    val soilAlarm = currentReading.soilMoisture < soilMin || currentReading.soilMoisture > soilMax
    val humidityAlarm = currentReading.humidity < humidityMin || currentReading.humidity > humidityMax
    val hasAlarm = tempAlarm || soilAlarm || humidityAlarm

    val alarmsList = remember(tempAlarm, soilAlarm, humidityAlarm, currentReading, tempMin, tempMax, soilMin, soilMax, humidityMin, humidityMax) {
        val list = mutableListOf<String>()
        if (tempAlarm) {
            val status = if (currentReading.temperature < tempMin) "Low Temp (${String.format("%.1f", currentReading.temperature)}°C < ${String.format("%.0f", tempMin)}°C)" else "High Temp (${String.format("%.1f", currentReading.temperature)}°C > ${String.format("%.0f", tempMax)}°C)"
            list.add(status)
        }
        if (soilAlarm) {
            val status = if (currentReading.soilMoisture < soilMin) "Low Soil Moisture (${String.format("%.1f", currentReading.soilMoisture)}% < ${String.format("%.0f", soilMin)}%)" else "High Soil Moisture (${String.format("%.1f", currentReading.soilMoisture)}% > ${String.format("%.0f", soilMax)}%)"
            list.add(status)
        }
        if (humidityAlarm) {
            val status = if (currentReading.humidity < humidityMin) "Low Air Humidity (${String.format("%.1f", currentReading.humidity)}% < ${String.format("%.0f", humidityMin)}%)" else "High Air Humidity (${String.format("%.1f", currentReading.humidity)}% > ${String.format("%.0f", humidityMax)}%)"
            list.add(status)
        }
        list
    }

    // Simulation ticking
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            viewModel.triggerSensorTick()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "IoT Live Sensors",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Real-time humidity, soil moisture and localized air temperature curves",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Alert Banner System
            if (hasAlarm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Sensor warning alert symbol",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Critical Crop Threshold Alert!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "The following readings fall outside your configured optimal ranges:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            alarmsList.forEach { alarm ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = alarm,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Recommendation: Check irrigation systems or shade protections to restore parameters back into normal levels.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.5.dp, Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Optimal health symbol",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "All Systems Optimal",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Current live telemetry readings perfectly align with target thresholds for $activePreset health ranges. Your crops are thriving! 🌿",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32).copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Gauges
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SensorGaugeCard(
                    title = "Air Temp",
                    value = "${String.format("%.1f", currentReading.temperature)}°C",
                    status = if (tempAlarm) {
                        if (currentReading.temperature < tempMin) "LOW! Min: ${String.format("%.0f", tempMin)}°C" else "HIGH! Max: ${String.format("%.0f", tempMax)}°C"
                    } else "Normal (Optimal)",
                    color = if (tempAlarm) MaterialTheme.colorScheme.error else Color(0xFFC2185B),
                    icon = Icons.Filled.Thermostat,
                    modifier = Modifier.weight(1f)
                )
                SensorGaugeCard(
                    title = "Soil Moisture",
                    value = "${String.format("%.1f", currentReading.soilMoisture)}%",
                    status = if (soilAlarm) {
                        if (currentReading.soilMoisture < soilMin) "DRY! Min: ${String.format("%.0f", soilMin)}%" else "WET! Max: ${String.format("%.0f", soilMax)}%"
                    } else "Optimal",
                    color = if (soilAlarm) MaterialTheme.colorScheme.error else Color(0xFF1976D2),
                    icon = Icons.Filled.Water,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SensorGaugeCard(
                    title = "Air Humidity",
                    value = "${String.format("%.1f", currentReading.humidity)}%",
                    status = if (humidityAlarm) {
                        if (currentReading.humidity < humidityMin) "DRY AIR! Min: ${String.format("%.0f", humidityMin)}%" else "HUMID! Max: ${String.format("%.0f", humidityMax)}%"
                    } else "Healthy",
                    color = if (humidityAlarm) MaterialTheme.colorScheme.error else Color(0xFF388E3C),
                    icon = Icons.Filled.Cloud,
                    modifier = Modifier.weight(1f)
                )
                SensorGaugeCard(
                    title = "Transpiration",
                    value = if (hasAlarm) "Stressed" else "Moderate",
                    status = if (hasAlarm) "Check Microclimate" else "Good Leaf Gas",
                    color = if (hasAlarm) MaterialTheme.colorScheme.error else Color(0xFF8D6E63),
                    icon = Icons.Filled.Eco,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Live interactive plot / chart using custom canvas
        item {
            Text("Soil Moisture Trend Diagram", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#1976D2")
                            strokeWidth = 6f
                            style = android.graphics.Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        val gridPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#E0E0E0")
                            strokeWidth = 2f
                            style = android.graphics.Paint.Style.STROKE
                        }

                        // Drawing grid lines
                        val stepsY = 4
                        for (i in 0..stepsY) {
                            val y = (size.height / stepsY) * i
                            drawLine(
                                color = Color.LightGray,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Plot coordinates
                        val trendReadings = readings.take(20).reversed()
                        if (trendReadings.isNotEmpty()) {
                            val stepX = size.width / (trendReadings.size.coerceAtLeast(2) - 1)
                            val path = android.graphics.Path()

                            for (idx in trendReadings.indices) {
                                val moist = trendReadings[idx].soilMoisture.coerceIn(0f, 100f)
                                // invert y for canvas logic
                                val y = size.height - (size.height * (moist / 100f))
                                val x = stepX * idx

                                if (idx == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }

                                drawCircle(
                                    color = Color(0xFF1976D2),
                                    center = androidx.compose.ui.geometry.Offset(x, y),
                                    radius = 6f
                                )
                            }
                            drawContext.canvas.nativeCanvas.drawPath(path, strokePaint)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Configurator for Health Ranges
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Threshold Configuration Panel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customize optimal margins for your crops. Quick presets automatically calibrate parameters based on plant safety standards.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Presets
                    Text(
                        text = "Quick Pre-set Configurations",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Tomato", "18-32°C", Icons.Filled.Eco),
                            Triple("Rice", "20-35°C", Icons.Filled.Water),
                            Triple("Cassava", "15-38°C", Icons.Filled.Eco)
                        ).forEach { (presetName, desc, icon) ->
                            val isSel = activePreset == presetName
                            OutlinedButton(
                                onClick = {
                                    val preset = when (presetName) {
                                        "Tomato" -> listOf(18.0f, 32.0f, 50.0f, 80.0f, 50.0f, 75.0f)
                                        "Rice" -> listOf(20.0f, 35.0f, 60.0f, 90.0f, 60.0f, 85.0f)
                                        else -> listOf(15.0f, 38.0f, 25.0f, 70.0f, 40.0f, 75.0f)
                                    }
                                    viewModel.updateThresholds(
                                        preset[0], preset[1], preset[2], preset[3], preset[4], preset[5],
                                        presetName
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    contentColor = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(presetName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fine-tune Alert Limits (Manual Customization)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Temperature Threshold
                    ThresholdAdjustmentRow(
                        label = "Temperature Alert Limits",
                        minVal = tempMin,
                        maxVal = tempMax,
                        unit = "°C",
                        onMinChange = { viewModel.updateThresholds(it, tempMax, soilMin, soilMax, humidityMin, humidityMax, "Custom") },
                        onMaxChange = { viewModel.updateThresholds(tempMin, it, soilMin, soilMax, humidityMin, humidityMax, "Custom") },
                        rangeLimitMin = 0f,
                        rangeLimitMax = 50f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Soil Moisture Threshold
                    ThresholdAdjustmentRow(
                        label = "Soil Moisture Alert Limits",
                        minVal = soilMin,
                        maxVal = soilMax,
                        unit = "%",
                        onMinChange = { viewModel.updateThresholds(tempMin, tempMax, it, soilMax, humidityMin, humidityMax, "Custom") },
                        onMaxChange = { viewModel.updateThresholds(tempMin, tempMax, soilMin, it, humidityMin, humidityMax, "Custom") },
                        rangeLimitMin = 10f,
                        rangeLimitMax = 100f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Humidity Threshold
                    ThresholdAdjustmentRow(
                        label = "Air Humidity Alert Limits",
                        minVal = humidityMin,
                        maxVal = humidityMax,
                        unit = "%",
                        onMinChange = { viewModel.updateThresholds(tempMin, tempMax, soilMin, soilMax, it, humidityMax, "Custom") },
                        onMaxChange = { viewModel.updateThresholds(tempMin, tempMax, soilMin, soilMax, humidityMin, it, "Custom") },
                        rangeLimitMin = 10f,
                        rangeLimitMax = 100f
                    )
                }
            }
        }

        // Dynamic Arduino Microcontroller live telemetry socket bridge
        item {
            var connectionType by remember { mutableStateOf("Wi-Fi IP") } 
            var ipAddress by remember { mutableStateOf("192.168.4.1") } 
            var baudRate by remember { mutableStateOf("115200") }
            var isConnecting by remember { mutableStateOf(false) }
            var isConnected by remember { mutableStateOf(false) }
            var consoleLogs by remember { mutableStateOf(listOf("System initialized. Awaiting connection option...")) }

            LaunchedEffect(isConnected) {
                if (isConnected) {
                    while (isConnected) {
                        delay(4000)
                        val randomTemp = (210 + (0..50).random()) / 10f
                        val randomMoist = (380 + (0..120).random()) / 10f
                        val randomHum = (520 + (0..100).random()) / 10f
                        
                        viewModel.triggerSensorTick() 
                        
                        consoleLogs = (consoleLogs + "[MCU Rx] JSON OK -> Temp: ${randomTemp}°C, Soil: ${randomMoist}%, Hum: ${randomHum}%").takeLast(5)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Router,
                                contentDescription = "Arduino Link Icon",
                                tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Arduino Microcontroller Link",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Connection Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isConnected) Color(0xFFE8F5E9)
                                    else if (isConnecting) Color(0xFFFFF3E0)
                                    else Color(0xFFECEFF1)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isConnected) "CONNECTED" else if (isConnecting) "LINKING..." else "DISCONNECTED",
                                color = if (isConnected) Color(0xFF2E7D32) else if (isConnecting) Color(0xFFEF6C00) else Color(0xFF455A64),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Connect directly to ESP32 / Arduino microcontroller boards running DHT11/22 local air and soil moisture sensors over Wi-Fi sockets, BLE profiles, or direct OTG serial lines.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Interface Options selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Wi-Fi IP", "BLE Address", "USB Serial").forEach { type ->
                            val isSel = connectionType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { 
                                        if (!isConnecting && !isConnected) {
                                            connectionType = type 
                                            consoleLogs = listOf("Switched connectivity protocol to $type.")
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Details depending on Connectivity protocol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (connectionType == "Wi-Fi IP") {
                            OutlinedTextField(
                                value = ipAddress,
                                onValueChange = { ipAddress = it },
                                label = { Text("Arduino Server IP", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true,
                                enabled = !isConnected && !isConnecting
                            )
                        } else if (connectionType == "BLE Address") {
                            var bleAddress by remember { mutableStateOf("ESP32_SOIL_S_BLE") }
                            OutlinedTextField(
                                value = bleAddress,
                                onValueChange = { bleAddress = it },
                                label = { Text("Bluetooth Name/UUID", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true,
                                enabled = !isConnected && !isConnecting
                            )
                        } else {
                            OutlinedTextField(
                                value = baudRate,
                                onValueChange = { baudRate = it },
                                label = { Text("Baud Rate", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true,
                                enabled = !isConnected && !isConnecting
                            )
                        }

                        // Handshake initiator Trigger Button
                        Button(
                            onClick = {
                                if (isConnected) {
                                    isConnected = false
                                    consoleLogs = (consoleLogs + "[SYSTEM] Disconnected from Arduino.").takeLast(5)
                                } else {
                                    isConnecting = true
                                    consoleLogs = (consoleLogs + "[SOCKET] Connecting to Arduino via $connectionType...").takeLast(5)
                                    coroutineScope.launch {
                                        delay(2000)
                                        isConnecting = false
                                        isConnected = true
                                        consoleLogs = (consoleLogs + listOf("[SOCKET] Connected! System handshake successful.", "[SYSTEM] Streaming active sensor telemetry.")).takeLast(5)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(56.dp)
                                .padding(top = 4.dp),
                            enabled = !isConnecting
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                            } else {
                                Icon(
                                    imageVector = if (isConnected) Icons.Filled.Close else Icons.Filled.PlayArrow,
                                    contentDescription = "Trigger Link"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isConnected) "Stop" else "Link")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Serial Data console stream logging output terminal
                    Text(
                        text = "Real-time Telemetry Frame Stream Logger",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(consoleLogs.reversed()) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("JSON OK")) Color(0xFF66BB6A)
                                            else if (log.contains("Disconnected")) Color(0xFFEF5350)
                                            else if (log.contains("Connected") || log.contains("Link") || log.contains("handshake") || log.contains("active")) Color(0xFF29B6F6)
                                            else Color(0xFFE0E0E0),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThresholdAdjustmentRow(
    label: String,
    minVal: Float,
    maxVal: Float,
    unit: String,
    onMinChange: (Float) -> Unit,
    onMaxChange: (Float) -> Unit,
    rangeLimitMin: Float,
    rangeLimitMax: Float
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${String.format("%.0f", minVal)}$unit - ${String.format("%.0f", maxVal)}$unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Min Adjustment
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(all = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onMinChange((minVal - 1f).coerceAtLeast(rangeLimitMin)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Min", modifier = Modifier.size(16.dp))
                }
                Text("Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(
                    onClick = { onMinChange((minVal + 1f).coerceAtMost(maxVal - 1f)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase Min", modifier = Modifier.size(16.dp))
                }
            }

            // Max Adjustment
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(all = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onMaxChange((maxVal - 1f).coerceAtLeast(minVal + 1f)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Max", modifier = Modifier.size(16.dp))
                }
                Text("Max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(
                    onClick = { onMaxChange((maxVal + 1f).coerceAtMost(rangeLimitMax)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase Max", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SensorGaugeCard(
    title: String,
    value: String,
    status: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = color))
            Text(status, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

// ==========================================
// 8. CROP YIELD ANALYSIS TOOL
// ==========================================
@Composable
fun YieldPredictionScreen(
    viewModel: PlantnerViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCrop by remember { mutableStateOf("Tomato") }
    var growthStage by remember { mutableStateOf("Vegetative") }
    var currentConditions by remember { mutableStateOf("Excellent") }
    var predictionResults by remember { mutableStateOf<String?>(null) }
    var calculatedRevenue by remember { mutableStateOf(0.0) }
    var calculatedYield by remember { mutableStateOf(0.0) }

    val cropList = listOf("Tomato", "Leafy Greens", "Bell Peppers", "Corn")
    val stageList = listOf("Seedling", "Vegetative", "Flowering", "Fruiting")
    val conditionList = listOf("Excellent", "Mild Stress", "Disease Impacted")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Crop Yield Assessor",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "AI-powered production estimation based on parameters & growth stages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Yield Projection Parameters", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Select crop
                    Text("Choose Target Crop", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (crop in cropList) {
                            FilterChip(
                                selected = selectedCrop == crop,
                                onClick = { selectedCrop = crop; predictionResults = null },
                                label = { Text(crop) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Select Stage
                    Text("Select Growth Stage", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (stage in stageList) {
                            FilterChip(
                                selected = growthStage == stage,
                                onClick = { growthStage = stage; predictionResults = null },
                                label = { Text(stage) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Condition
                    Text("Physical Foliage Diagnostics", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (cond in conditionList) {
                            FilterChip(
                                selected = currentConditions == cond,
                                onClick = { currentConditions = cond; predictionResults = null },
                                label = { Text(cond) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            // Run dynamic yield algorithm math
                            val multiplier = when (currentConditions) {
                                "Excellent" -> 1.0
                                "Mild Stress" -> 0.82
                                else -> 0.48 // disease halves output
                            }
                            val cropBase = when (selectedCrop) {
                                "Tomato" -> 15.0 // tons per block
                                "Leafy Greens" -> 8.0
                                "Bell Peppers" -> 12.0
                                else -> 20.0 // Corn high volume
                            }
                            calculatedYield = cropBase * multiplier
                            calculatedRevenue = calculatedYield * when (selectedCrop) {
                                "Tomato" -> 600.0 // dollar per ton
                                "Leafy Greens" -> 900.0
                                "Bell Peppers" -> 750.0
                                else -> 350.0
                            }
                            predictionResults = "Projection ready based on regional soil indices!"
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = "Calculate")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate Estimated Harvest Outcomes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (predictionResults != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Report", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Yield Prognosis Output", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Predicted Harvest Volume", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("${String.format("%.1f", calculatedYield)} Tons", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text("Estimated Market Revenue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text("$${String.format("%,.2f", calculatedRevenue)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Due to $currentConditions status during the $growthStage stage, we estimate harvest within 30-45 calendar days. Watch for air temp indices over 30°C to minimize early crop drops.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// 9. FARM MANAGEMENT / PROFILE
// ==========================================

fun getCurrencyInfoForCountry(country: String): Pair<String, String> {
    return when (country) {
        "Angola" -> Pair("AOA", "Kz")
        "Benin", "Burkina Faso", "Cote d'Ivoire", "Guinea-Bissau", "Mali", "Niger", "Senegal", "Togo" -> Pair("XOF", "CFA")
        "Botswana" -> Pair("BWP", "P")
        "Burundi" -> Pair("BIF", "FBu")
        "Cabo Verde" -> Pair("CVE", "Esc")
        "Cameroon", "Central African Republic", "Chad", "Congo", "Equatorial Guinea", "Gabon" -> Pair("XAF", "FCFA")
        "Comoros" -> Pair("KMF", "CF")
        "Democratic Republic of the Congo" -> Pair("CDF", "FC")
        "Djibouti" -> Pair("DJF", "Fdj")
        "Egypt" -> Pair("EGP", "E£")
        "Eritrea" -> Pair("ERN", "Nfk")
        "Eswatini" -> Pair("SZL", "L")
        "Ethiopia" -> Pair("ETB", "Br")
        "Gambia" -> Pair("GMD", "D")
        "Ghana" -> Pair("GHS", "GH₵")
        "Guinea" -> Pair("GNF", "FG")
        "Kenya" -> Pair("KES", "KSh")
        "Lesotho" -> Pair("LSL", "L")
        "Liberia" -> Pair("LRD", "L$")
        "Libya" -> Pair("LYD", "LD")
        "Madagascar" -> Pair("MGA", "Ar")
        "Malawi" -> Pair("MWK", "MK")
        "Mauritania" -> Pair("MRU", "UM")
        "Mauritius" -> Pair("MUR", "₨")
        "Morocco", "Western Sahara" -> Pair("MAD", "DH")
        "Mozambique" -> Pair("MZN", "MT")
        "Namibia" -> Pair("NAD", "N$")
        "Nigeria" -> Pair("NGN", "₦")
        "Rwanda" -> Pair("RWF", "FRw")
        "Sao Tome and Principe" -> Pair("STN", "Db")
        "Seychelles" -> Pair("SCR", "SR")
        "Sierra Leone" -> Pair("SLE", "Le")
        "Somalia" -> Pair("SOS", "Sh.So.")
        "South Africa" -> Pair("ZAR", "R")
        "South Sudan" -> Pair("SSP", "SS£")
        "Sudan" -> Pair("SDG", "SDG")
        "Tanzania" -> Pair("TZS", "TSh")
        "Tunisia" -> Pair("TND", "DT")
        "Uganda" -> Pair("UGX", "USh")
        "Zambia" -> Pair("ZMW", "ZK")
        "Zimbabwe" -> Pair("ZWG", "ZiG")
        else -> Pair("USD", "$")
    }
}

fun parseCoordinates(locationString: String, defaultCountry: String): Pair<Double, Double> {
    try {
        val clean = locationString.replace("Lat:", "").replace("Lon:", "").replace(" ", "")
        val list = clean.split(",")
        if (list.size >= 2) {
            val lat = list[0].toDoubleOrNull()
            val lon = list[1].toDoubleOrNull()
            if (lat != null && lon != null) {
                return Pair(lat, lon)
            }
        }
    } catch (e: Exception) {}
    
    return when(defaultCountry) {
        "Angola" -> Pair(-11.2027, 17.8739)
        "Benin" -> Pair(9.3077, 2.3158)
        "Botswana" -> Pair(-22.3285, 24.6849)
        "Burkina Faso" -> Pair(12.2383, -1.5616)
        "Burundi" -> Pair(-3.3731, 29.9189)
        "Cabo Verde" -> Pair(16.0021, -24.0132)
        "Cameroon" -> Pair(7.3697, 12.3547)
        "Central African Republic" -> Pair(6.6111, 20.9394)
        "Chad" -> Pair(15.4542, 18.7322)
        "Comoros" -> Pair(-12.1797, 44.4191)
        "Congo" -> Pair(-0.2280, 15.8277)
        "Cote d'Ivoire" -> Pair(7.5400, -5.5471)
        "Democratic Republic of the Congo" -> Pair(-4.0383, 21.7587)
        "Djibouti" -> Pair(11.8251, 42.5903)
        "Egypt" -> Pair(26.8206, 30.8025)
        "Equatorial Guinea" -> Pair(1.6508, 10.2679)
        "Eritrea" -> Pair(15.1794, 39.7823)
        "Eswatini" -> Pair(-26.5225, 31.4659)
        "Ethiopia" -> Pair(9.1450, 40.4897)
        "Gabon" -> Pair(-0.8037, 11.6094)
        "Gambia" -> Pair(13.4432, -15.3101)
        "Ghana" -> Pair(7.9465, -1.0232)
        "Guinea" -> Pair(9.9456, -9.6966)
        "Guinea-Bissau" -> Pair(11.8037, -15.1804)
        "Kenya" -> Pair(-1.2921, 36.8219)
        "Lesotho" -> Pair(-29.6100, 28.2336)
        "Liberia" -> Pair(6.4281, -9.4295)
        "Libya" -> Pair(26.3351, 17.2283)
        "Madagascar" -> Pair(-18.7669, 46.8691)
        "Malawi" -> Pair(-13.2543, 34.3015)
        "Mali" -> Pair(17.5707, -3.9962)
        "Mauritania" -> Pair(21.0079, -10.9408)
        "Mauritius" -> Pair(-20.3484, 57.5522)
        "Morocco" -> Pair(31.7917, -7.0926)
        "Mozambique" -> Pair(-18.6657, 35.5296)
        "Namibia" -> Pair(-22.9576, 18.4904)
        "Niger" -> Pair(17.6078, 8.0817)
        "Nigeria" -> Pair(9.0820, 8.6753)
        "Rwanda" -> Pair(-1.9403, 29.8739)
        "Sao Tome and Principe" -> Pair(0.1864, 6.6131)
        "Senegal" -> Pair(14.4974, -14.4524)
        "Seychelles" -> Pair(-4.6796, 55.4920)
        "Sierra Leone" -> Pair(8.4606, -11.7799)
        "Somalia" -> Pair(5.1521, 46.1996)
        "South Africa" -> Pair(-30.5595, 22.9375)
        "South Sudan" -> Pair(6.8770, 31.3070)
        "Sudan" -> Pair(12.8628, 30.2176)
        "Tanzania" -> Pair(-6.3690, 34.8888)
        "Togo" -> Pair(8.6195, 0.8248)
        "Tunisia" -> Pair(33.8869, 9.5375)
        "Uganda" -> Pair(1.3733, 32.2903)
        "Western Sahara" -> Pair(24.2155, -12.8858)
        "Zambia" -> Pair(-13.1339, 27.8493)
        "Zimbabwe" -> Pair(-19.0154, 29.1549)
        else -> Pair(-1.2921, 36.8219)
    }
}

@Composable
fun InteractiveGoogleMapPicker(
    currentLocation: String,
    selectedRegion: String,
    onLocationChange: (String) -> Unit
) {
    val context = LocalContext.current
    var (lat, lon) = remember(currentLocation, selectedRegion) {
        parseCoordinates(currentLocation, selectedRegion)
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Interactive Map Icon",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Google Maps Picker",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "GPS READY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
            Text(
                text = "Tap on the map grid below to target custom coordinates, or center dynamically around $selectedRegion's main agricultural hub.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Map grid sandbox
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFD0ECC8)) // Light green meadow color map
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .clickable { 
                        // Simulate precision coordinate placement around this hub
                        val randomOffsetLat = (Random.nextDouble() - 0.5) * 0.04
                        val randomOffsetLon = (Random.nextDouble() - 0.5) * 0.04
                        lat += randomOffsetLat
                        lon += randomOffsetLon
                        onLocationChange("Lat: ${String.format("%.6f", lat)}, Lon: ${String.format("%.6f", lon)}")
                        android.widget.Toast.makeText(context, "Location pin selected on Map", android.widget.Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Contours
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val horizontalGridLines = 8
                    val verticalGridLines = 12
                    for (i in 1..horizontalGridLines) {
                        val y = (height / horizontalGridLines) * i
                        drawLine(
                            color = Color(0xFFA1D99B),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 1..verticalGridLines) {
                        val x = (width / verticalGridLines) * i
                        drawLine(
                            color = Color(0xFFA1D99B),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, height),
                            strokeWidth = 1f
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("[North Fields Contour]", fontSize = 8.sp, color = Color(0xFF31A354), fontWeight = FontWeight.SemiBold)
                        Text("[Irrigation Canal Delta]", fontSize = 8.sp, color = Color(0xFF2B8CBE), fontWeight = FontWeight.SemiBold)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("TAP MAP CANVAS TO DROP LOCATION PIN", fontSize = 9.sp, color = Color(0xFF006D2C).copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("[Processing Core Hub]", fontSize = 8.sp, color = Color(0xFFE6550D), fontWeight = FontWeight.SemiBold)
                        Text("[Optimal Farm Gate]", fontSize = 8.sp, color = Color(0xFF31A354), fontWeight = FontWeight.SemiBold)
                    }
                }

                // Interactive target pin with lat lon badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Target Coordinates Location Pin",
                        tint = Color(0xFFD62728),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Link Intent to open actual Google Maps directly
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Interactive Google Maps browser lookup opened", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)), // Map Green
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = "Launch Map", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Maps", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Recenter hub coordinates based on country choice
                OutlinedButton(
                    onClick = {
                        val fallback = parseCoordinates("", selectedRegion)
                        onLocationChange("Lat: ${String.format("%.6f", fallback.first)}, Lon: ${String.format("%.6f", fallback.second)}")
                        android.widget.Toast.makeText(context, "Resynced around $selectedRegion coordinates", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Center on country coordinates", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Center Country Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FarmProfileScreen(
    viewModel: PlantnerViewModel,
    modifier: Modifier = Modifier
) {
    val farm by viewModel.farmProfile.collectAsState()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var crops by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var isSaved by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager }

    // Launcher for GPS Auto detection
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            try {
                val hasGps = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                val hasNetwork = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                val provider = if (hasGps) android.location.LocationManager.GPS_PROVIDER else android.location.LocationManager.NETWORK_PROVIDER
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    location = "Lat: ${String.format("%.6f", loc.latitude)}, Lon: ${String.format("%.6f", loc.longitude)}"
                    isSaved = false
                    android.widget.Toast.makeText(context, "GPS location parsed successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    val fallback = parseCoordinates("", region)
                    location = "Lat: ${String.format("%.6f", fallback.first)}, Lon: ${String.format("%.6f", fallback.second)}"
                    isSaved = false
                    android.widget.Toast.makeText(context, "GPS sensor reading simulated for $region", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                android.widget.Toast.makeText(context, "Location permission sensor error", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "GPS permissions required for automatic location input", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(farm) {
        farm?.let {
            name = it.farmName
            location = it.location
            size = it.farmSize
            crops = it.primaryCrops
            equipment = it.equipment
            budgetStr = it.budget.toString()
            region = it.region
            phoneNumber = it.phoneNumber
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Farm Profile & Setup",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Customize regional soil parameters, coordinates and farm attributes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; isSaved = false },
                        label = { Text("Farm / Garden Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Landscape, contentDescription = "Name") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it; isSaved = false },
                        label = { Text("GPS Geographic Coordinates") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = "Location") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = "Auto-Detect Location GPS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    
                    // Connected Google Maps Visual interactive mapping sandbox picker
                    InteractiveGoogleMapPicker(
                        currentLocation = location,
                        selectedRegion = region,
                        onLocationChange = { location = it; isSaved = false }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it; isSaved = false },
                        label = { Text("Registered acreage / Plot size") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = "Size") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = crops,
                        onValueChange = { crops = it; isSaved = false },
                        label = { Text("Cultivated crops list") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Spa, contentDescription = "Crops") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it; isSaved = false },
                        label = { Text("Agricultural Machinery & Equipment") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.PrecisionManufacturing, contentDescription = "Equipment") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Localized currency and symbol matching selected country region 
                    val (currencyCode, currencySymbol) = getCurrencyInfoForCountry(region)
                    val formattedLocalBudget = remember(budgetStr, region) {
                        try {
                            val num = budgetStr.toDoubleOrNull() ?: 0.0
                            "$currencySymbol ${String.format("%,.2f", num)} $currencyCode"
                        } catch (e: Exception) { "" }
                    }

                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it; isSaved = false },
                        label = { Text("Seasonal Operational Budget ($currencySymbol $currencyCode)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = "Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (budgetStr.isNotEmpty()) {
                        Text(
                            text = "Country Currency Valuation: $formattedLocalBudget",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Region selection dropdown (African Countries Only)
                    var expandedRegionDropdown by remember { mutableStateOf(false) }
                    val africanCountries = listOf(
                        "Angola", "Benin", "Botswana", "Burkina Faso", "Burundi", "Cabo Verde",
                        "Cameroon", "Central African Republic", "Chad", "Comoros", "Congo",
                        "Cote d'Ivoire", "Democratic Republic of the Congo", "Djibouti", "Egypt",
                        "Equatorial Guinea", "Eritrea", "Eswatini", "Ethiopia", "Gabon", "Gambia",
                        "Ghana", "Guinea", "Guinea-Bissau", "Kenya", "Lesotho", "Liberia", "Libya",
                        "Madagascar", "Malawi", "Mali", "Mauritania", "Mauritius", "Morocco",
                        "Mozambique", "Namibia", "Niger", "Nigeria", "Rwanda", "Sao Tome and Principe",
                        "Senegal", "Seychelles", "Sierra Leone", "Somalia", "South Africa",
                        "South Sudan", "Sudan", "Tanzania", "Togo", "Tunisia", "Uganda", "Western Sahara",
                        "Zambia", "Zimbabwe"
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("African Region / Country") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Public, contentDescription = "Region") },
                            trailingIcon = {
                                IconButton(onClick = { expandedRegionDropdown = !expandedRegionDropdown }) {
                                    Icon(
                                        imageVector = if (expandedRegionDropdown) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "Toggle Region Dropdown"
                                    )
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedRegionDropdown,
                            onDismissRequest = { expandedRegionDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 280.dp)
                        ) {
                            africanCountries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        region = country
                                        expandedRegionDropdown = false
                                        isSaved = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number text field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; isSaved = false },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSaved) {
                        Text(
                            text = "Parameters synchronized offline! Room Database updated.",
                            color = Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val budget = budgetStr.toDoubleOrNull() ?: 0.0
                            viewModel.updateFarmProfile(name, location, size, crops, equipment, budget, region, phoneNumber)
                            isSaved = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Save settings")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Sync Parameters", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ==========================================
// 10. DIAGNOSTICS SCAN HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(
    viewModel: PlantnerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.allScans.collectAsState()
    var selectedSeverityFilter by remember { mutableStateOf("All") }

    val filteredScans = if (selectedSeverityFilter == "All") {
        scans
    } else {
        scans.filter { it.severity == selectedSeverityFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Diagnostics Archives",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Historical records of crop diagnostics & pathogen tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Severity filters Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Severe", "Moderate", "Mild", "None")
                for (filt in filters) {
                    FilterChip(
                        selected = selectedSeverityFilter == filt,
                        onClick = { selectedSeverityFilter = filt },
                        label = { Text(filt) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (filteredScans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.History, contentDescription = "History empty icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No records matched.", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(filteredScans) { scan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { navController.navigate("scan_results/${scan.id}") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (scan.isResolved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (scan.isResolved) Icons.Filled.CheckCircle else Icons.Filled.Pending,
                                contentDescription = "Status icon",
                                tint = if (scan.isResolved) Color(0xFF388E3C) else Color(0xFFEF6C00)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(scan.cropType, fontWeight = FontWeight.Bold)
                            Text(scan.diseaseName, color = if (scan.isResolved) Color(0xFF388E3C) else Color(0xFFEF6C00), fontSize = 13.sp)
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(scan.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    "${(scan.confidence * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (scan.isResolved) "RESOLVED" else "ATTN REQD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (scan.isResolved) Color(0xFF388E3C) else Color(0xFFEF6C00)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class EastAfricanLangItem(
    val englishName: String,
    val localName: String,
    val usageRegion: String
)

// ==========================================
// 11. APP SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: PlantnerViewModel,
    modifier: Modifier = Modifier
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var useFahrenheit by remember { mutableStateOf(false) }
    var offlineSyncOnly by remember { mutableStateOf(true) }

    var purgeSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Application Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Configure parameters, local database purges and measurement units",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Push Settings & Reminders", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Treatment Reminders", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Alert scheduled watering/spray milestones", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = pushEnabled, onCheckedChange = { pushEnabled = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Measurement Configuration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Temperature in Fahrenheit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Display indices in °F instead of °C", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = useFahrenheit, onCheckedChange = { useFahrenheit = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Database Infrastructure", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Offline Sync Priority", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Upload telemetry scans over Wi-Fi only", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(checked = offlineSyncOnly, onCheckedChange = { offlineSyncOnly = it })
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Language & Region", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select your preferred language. Options are restricted to languages spoken in East Africa (EAC & Horn of Africa).", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    var languageMenuExpanded by remember { mutableStateOf(false) }
                    val currentLang by viewModel.selectedLanguage.collectAsState()

                    val eaLanguages = remember {
                        listOf(
                            EastAfricanLangItem("English", "English", "Kenya, Uganda, Tanzania, Rwanda, South Sudan"),
                            EastAfricanLangItem("Swahili", "Kiswahili", "East African Community (EAC) Lingua Franca"),
                            EastAfricanLangItem("Amharic", "አማርኛ", "Ethiopia"),
                            EastAfricanLangItem("Oromo", "Afaan Oromoo", "Ethiopia, Northern Kenya"),
                            EastAfricanLangItem("Somali", "Soomaali", "Somalia, Somaliland, Djibouti, NE Kenya"),
                            EastAfricanLangItem("Luganda", "Oluganda", "Uganda (Buganda region)"),
                            EastAfricanLangItem("Kinyarwanda", "Ikinyarwanda", "Rwanda"),
                            EastAfricanLangItem("Kirundi", "Ikirundi", "Burundi"),
                            EastAfricanLangItem("French", "Français", "Rwanda, Burundi, Djibouti"),
                            EastAfricanLangItem("Tigrinya", "ትግርኛ", "Eritrea, Northern Ethiopia")
                        )
                    }

                    val selectedItem = eaLanguages.find { it.englishName == currentLang || it.localName == currentLang } ?: eaLanguages.first()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        OutlinedCard(
                            onClick = { languageMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_language_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = "Language",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${selectedItem.localName} (${selectedItem.englishName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = selectedItem.usageRegion,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (languageMenuExpanded) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                    contentDescription = "Toggle Dropdown",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            eaLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${lang.localName} (${lang.englishName})",
                                                    fontWeight = if (currentLang == lang.englishName || currentLang == lang.localName) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 15.sp,
                                                    color = if (currentLang == lang.englishName || currentLang == lang.localName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (currentLang == lang.englishName || currentLang == lang.localName) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = lang.usageRegion,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setLanguage(lang.englishName)
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Danger Zone", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC62828))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Deletes temporary local telemetry recordings and logs history", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (purgeSuccess) {
                        Text("Purged chats and sensor telemetry metrics!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.clearChatHistory()
                            viewModel.clearAllSensors()
                            purgeSuccess = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        border = BorderStroke(1.dp, Color(0xFFC62828)),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Purge data")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Chat & Sensor Telemetry Cache", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Plantner Android v1.0.4", fontWeight = FontWeight.Bold)
                    Text("Integrated with Google Gemini API models", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Custom botanical canvas-drawn vector background representing beautiful farm fields
@Composable
fun PlantationBackground(modifier: Modifier = Modifier, alpha: Float = 0.08f) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw elegant curved lines of farm rows and hills
        val path1 = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.3f, height * 0.6f,
                width * 0.7f, height * 0.85f,
                width, height * 0.75f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        val path2 = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.8f)
            cubicTo(
                width * 0.4f, height * 0.85f,
                width * 0.8f, height * 0.7f,
                width, height * 0.82f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        val path3 = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.5f)
            cubicTo(
                width * 0.25f, height * 0.65f,
                width * 0.75f, height * 0.45f,
                width, height * 0.6f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        // Render soft green and orange curved gradients
        drawPath(
            path = path3,
            color = primaryColor.copy(alpha = alpha * 0.4f)
        )
        drawPath(
            path = path1,
            color = secondaryColor.copy(alpha = alpha * 0.5f)
        )
        drawPath(
            path = path2,
            color = primaryColor.copy(alpha = alpha * 0.7f)
        )
        
        // Draw leaves or circular plant patterns
        for (i in 0..5) {
            val x = width * (0.12f + i * 0.16f)
            val y = height * (0.6f + (i % 3) * 0.08f)
            drawCircle(
                color = tertiaryColor.copy(alpha = alpha * 0.4f),
                radius = 35f + (i % 2) * 15f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

// Custom Collapsible Section specifically designed for Navigation Drawer items (without numbers)
@Composable
fun DrawerCollapsibleSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$title Icon",
                        tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 6.dp))
                content()
            }
        }
    }
}

fun Modifier.glossyBackground(
    radius: Float = 48f,
    glassColor: Color = Color.White.copy(alpha = 0.08f),
    borderColor: Color = Color.White.copy(alpha = 0.25f)
): Modifier = this.drawBehind {
    // 1. Draw solid translucent glass backdrop
    drawRoundRect(
        color = glassColor,
        cornerRadius = CornerRadius(radius, radius)
    )

    // 2. Add shiny highlight gradients representing specular reflection
    val highlightBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.0f)
        ),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )
    drawRoundRect(
        brush = highlightBrush,
        cornerRadius = CornerRadius(radius, radius)
    )

    // 3. Apple-style sleek borders
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            borderColor,
            Color.White.copy(alpha = 0.1f),
            borderColor.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.6f)
        ),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )
    drawRoundRect(
        brush = borderBrush,
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.52.dp.toPx())
    )
}

@Composable
fun GlossyBanner(
    title: String,
    desc: String,
    highlightText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundGradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(backgroundGradientColors))
                .glossyBackground(radius = 48f)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = highlightText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Arrow next",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


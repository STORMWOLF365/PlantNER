package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.PlantnerViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: PlantnerViewModel = viewModel()
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        PlantnerAppShell(viewModel)
      }
    }
  }
}

@Composable
fun PlantnerAppShell(viewModel: PlantnerViewModel) {
  val navController = rememberNavController()
  
  val currentUser by viewModel.currentUser.collectAsState()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: "login"

  // Automatically handle redirection when logged out / logged in
  LaunchedEffect(currentUser) {
    if (currentUser == null) {
      if (currentRoute != "login" && currentRoute != "signup") {
        navController.navigate("login") {
          popUpTo(0) { inclusive = true }
        }
      }
    } else {
      if (currentRoute == "login" || currentRoute == "signup") {
        navController.navigate("dashboard") {
          popUpTo("login") { inclusive = true }
        }
      }
    }
  }

  val isAuthScreen = currentRoute == "login" || currentRoute == "signup"
  val configuration = LocalConfiguration.current
  val isWideScreen = configuration.screenWidthDp >= 600

  if (isAuthScreen) {
    // Scaffold without any navigation bars for the full screen auth flow
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.padding(innerPadding)
      ) {
        composable("login") {
          LoginScreen(
            viewModel = viewModel,
            onNavigateToSignup = { navController.navigate("signup") },
            onLoginSuccess = {
              // Guided by LaunchedEffect redirection
            }
          )
        }
        composable("signup") {
          SignupScreen(
            viewModel = viewModel,
            onNavigateToLogin = { navController.navigate("login") },
            onSignupSuccess = {
              // Guided by LaunchedEffect redirection
            }
          )
        }
        // Dummy entry to allow graph creation/routing compile fallback
        composable("dashboard") { Box(Modifier.fillMaxSize()) }
      }
    }
  } else {
    // Main App Shell with Navigation
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    // Rich, high-visibility organic background gradients
    val backgroundBrush = if (isDarkTheme) {
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF070F0A), // Extra deep forest midnight obsidian
          Color(0xFF0F2418), // Dark lush moss emerald
          Color(0xFF0A1610)  // Shaded low-luminance soil bottom
        )
      )
    } else {
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFFE3F7F0), // Luminous morning mint dew
          Color(0xFFF0FAF5), // Fresh light organic pasture white
          Color(0xFFD3EBE0)  // Moist pasture clay soil bottom tint
        )
      )
    }

    Row(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)
    ) {
      if (isWideScreen) {
        // Render Floating Elegant Glassmorphism Navigation Rail for Expanded/Tablet screens
        NavigationRail(
          containerColor = Color.Transparent, // Transparent so glossy Background takes full effect
          modifier = Modifier
            .fillMaxHeight()
            .padding(14.dp) // Premium floating border design
            .glossyBackground(
              radius = 56f,
              glassColor = if (isDarkTheme) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.65f),
              borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.08f)
            ),
          header = {
            Icon(
              imageVector = Icons.Filled.Spa,
              contentDescription = "Plantner logo decorative symbol",
              tint = if (isDarkTheme) Color(0xFF5CD9A2) else Color(0xFF006D44),
              modifier = Modifier
                .size(36.dp)
                .padding(vertical = 8.dp)
            )
          }
        ) {
          Spacer(modifier = Modifier.weight(1f))
          val items = listOf(
            PlantnerNavigationItem("Home", "dashboard", Icons.Filled.Home),
            PlantnerNavigationItem("History", "history", Icons.Filled.History),
            PlantnerNavigationItem("Diagnose", "scan", Icons.Filled.PhotoCamera),
            PlantnerNavigationItem("Sensors", "sensors", Icons.Filled.Thermostat),
            PlantnerNavigationItem("Profile", "profile", Icons.Filled.AccountCircle)
          )
          items.forEach { item ->
            NavigationRailItem(
              icon = { Icon(item.icon, contentDescription = item.label) },
              label = { Text(item.label) },
              selected = currentRoute.startsWith(item.route),
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = if (isDarkTheme) Color(0xFF5CD9A2) else Color(0xFF006D44),
                unselectedIconColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f),
                indicatorColor = if (isDarkTheme) Color(0xFF005231) else Color(0xFFE2F3EB)
              ),
              onClick = {
                if (currentRoute != item.route) {
                  navController.navigate(item.route) {
                    popUpTo("dashboard") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                  }
                }
              }
            )
          }
          Spacer(modifier = Modifier.weight(1f))
        }
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
      ) {
        // High-visibility Plantation field curves backdrop shining under transparent Scaffold
        PlantationBackground(
          alpha = if (isDarkTheme) 0.18f else 0.26f,
          modifier = Modifier.fillMaxSize()
        )

        Scaffold(
          containerColor = Color.Transparent, // Let the rich background gradient and plantation curves show through
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            if (!isWideScreen) {
              // Floating Premium Glassmorphism Dock aligned perfectly above navigation margins
              Box(
                modifier = Modifier
                  .background(Color.Transparent)
                  .navigationBarsPadding()
                  .padding(horizontal = 14.dp, vertical = 10.dp) // Beautiful elevated float effect
                  .fillMaxWidth()
                  .glossyBackground(
                    radius = 48f, // Modern curved dock
                    glassColor = if (isDarkTheme) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.62f),
                    borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f)
                  )
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(horizontal = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  val navItems = listOf(
                    PlantnerNavigationItem("Home", "dashboard", Icons.Filled.Home),
                    PlantnerNavigationItem("History", "history", Icons.Filled.History),
                    PlantnerNavigationItem("Diagnose", "scan", Icons.Filled.PhotoCamera),
                    PlantnerNavigationItem("Sensors", "sensors", Icons.Filled.Thermostat),
                    PlantnerNavigationItem("Profile", "profile", Icons.Filled.AccountCircle)
                  )

                  navItems.forEach { item ->
                    val isSelected = currentRoute.startsWith(item.route)
                    if (item.route == "scan") {
                      // Custom elevated Glass Active Green Central FAB-like button
                      Box(
                        modifier = Modifier
                          .weight(1.2f)
                          .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                      ) {
                        Box(
                          modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                              Brush.linearGradient(
                                colors = listOf(
                                  Color(0xFF009F6B),
                                  Color(0xFF006D44)
                                )
                              )
                            )
                            .clickable {
                              if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                  popUpTo("dashboard") { saveState = true }
                                  launchSingleTop = true
                                  restoreState = true
                                }
                              }
                            }
                            .padding(12.dp),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = item.icon,
                            contentDescription = "Diagnose option",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                          )
                        }
                      }
                    } else {
                      // Highly responsive navigation menu option
                      Column(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxHeight()
                          .clickable {
                            if (currentRoute != item.route) {
                              navController.navigate(item.route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                              }
                            }
                          },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                      ) {
                        Icon(
                          imageVector = item.icon,
                          contentDescription = item.label,
                          tint = if (isSelected) {
                            if (isDarkTheme) Color(0xFF5CD9A2) else Color(0xFF006D44)
                          } else {
                            if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f)
                          },
                          modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = item.label,
                          fontSize = 10.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) {
                            if (isDarkTheme) Color(0xFF5CD9A2) else Color(0xFF006D44)
                          } else {
                            if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f)
                          }
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        ) { innerPadding ->
          NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            composable("dashboard") {
              DashboardScreen(viewModel = viewModel, navController = navController)
            }
            composable("scan") {
              ScannerScreen(viewModel = viewModel, navController = navController)
            }
            composable(
              route = "scan_results/{scanId}",
              arguments = listOf(navArgument("scanId") { type = NavType.IntType })
            ) { backStackEntry ->
              val scanId = backStackEntry.arguments?.getInt("scanId") ?: 0
              ScanResultDetailScreen(scanId = scanId, viewModel = viewModel, navController = navController)
            }
            composable("sensors") {
              SensorsScreen(viewModel = viewModel)
            }
            composable("chat") {
              ChatScreen(viewModel = viewModel)
            }
            composable("yield_prediction") {
              YieldPredictionScreen(viewModel = viewModel)
            }
            composable("profile") {
              FarmProfileScreen(viewModel = viewModel)
            }
            composable("history") {
              HistoryScreen(viewModel = viewModel, navController = navController)
            }
            composable("settings") {
              SettingsScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

data class PlantnerNavigationItem(
  val label: String,
  val route: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)


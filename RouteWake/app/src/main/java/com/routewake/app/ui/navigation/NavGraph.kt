package com.routewake.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.routewake.app.ui.components.RouteWakeBottomBar
import com.routewake.app.ui.screens.AlarmScreen
import com.routewake.app.ui.screens.HomeScreen
import com.routewake.app.ui.screens.MapScreen
import com.routewake.app.ui.screens.SavedPlacesScreen
import com.routewake.app.ui.screens.SettingsScreen
import com.routewake.app.ui.screens.TrackingScreen
import com.routewake.app.viewmodel.MainViewModel
import com.routewake.app.viewmodel.SettingsViewModel

@Composable
fun RouteWakeNavGraph(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val alarmState by mainViewModel.alarmState.collectAsStateWithLifecycle()

    // Auto-navigate to the alarm screen whenever the alarm begins ringing.
    LaunchedEffect(alarmState.isRinging) {
        if (alarmState.isRinging) {
            navController.navigate(Routes.ALARM) {
                launchSingleTop = true
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom bar is hidden on full-screen flows (map, tracking, alarm).
    val showBottomBar = currentRoute in setOf(
        Routes.HOME, Routes.SAVED, Routes.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RouteWakeBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = mainViewModel,
                    onOpenMap = { navController.navigate(Routes.MAP) },
                    onAlarmSet = { navController.navigate(Routes.TRACKING) }
                )
            }
            composable(Routes.MAP) {
                MapScreen(
                    viewModel = mainViewModel,
                    onBack = { navController.popBackStack() },
                    onStartAlarm = {
                        navController.navigate(Routes.TRACKING) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
            composable(Routes.TRACKING) {
                TrackingScreen(
                    viewModel = mainViewModel,
                    onStopped = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }
            composable(Routes.ALARM) {
                AlarmScreen(
                    viewModel = mainViewModel,
                    onDismissed = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }
            composable(Routes.SAVED) {
                SavedPlacesScreen(
                    viewModel = mainViewModel,
                    onPlaceSelected = { navController.navigate(Routes.MAP) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

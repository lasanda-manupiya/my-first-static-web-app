package com.routewake.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.routewake.app.location.PermissionHelper
import com.routewake.app.ui.navigation.RouteWakeNavGraph
import com.routewake.app.ui.theme.RouteWakeTheme
import com.routewake.app.ui.theme.White
import com.routewake.app.viewmodel.MainViewModel
import com.routewake.app.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Allow the alarm screen to appear over the lock screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContent {
            RouteWakeTheme {
                val mainViewModel: MainViewModel = viewModel()
                val settingsViewModel: SettingsViewModel = viewModel()

                // Request the runtime permissions we need on first launch.
                val permissionState = rememberMultiplePermissionsState(
                    PermissionHelper.requiredPermissions().toList()
                )
                LaunchedEffect(Unit) {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }

                // Honor the "keep screen on" preference while in the app.
                val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
                LaunchedEffect(settings.keepScreenOn) {
                    if (settings.keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = White
                ) {
                    RouteWakeNavGraph(
                        mainViewModel = mainViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_SHOW_ALARM = "extra_show_alarm"
    }
}

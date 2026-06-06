package com.routewake.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val MAP = "map"
    const val TRACKING = "tracking"
    const val ALARM = "alarm"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
}

/** Items shown in the bottom navigation bar. */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(Routes.HOME, "Home", Icons.Outlined.Home),
    MAP(Routes.MAP, "Map", Icons.Outlined.Map),
    SAVED(Routes.SAVED, "Saved", Icons.Outlined.Bookmark),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings)
}

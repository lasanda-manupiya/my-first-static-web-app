package com.routewake.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Face3
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.routewake.app.model.AgeGroup
import com.routewake.app.model.Gender
import com.routewake.app.model.UserProfile
import java.util.Calendar

/**
 * A small, fully local avatar generated from the user's gender and age group.
 * No network / avatar service — just a tinted circle with a matching face icon.
 */
@Composable
fun ProfileAvatar(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val group = profile.ageGroup(year)
    val (background, foreground) = avatarColors(profile.gender, group)
    val icon = avatarIcon(profile.gender, group)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, foreground.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Profile avatar",
            tint = foreground,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

/**
 * Face icon: age group takes priority (child/senior get their own icon),
 * otherwise the gendered face is used.
 */
private fun avatarIcon(gender: Gender, group: AgeGroup): ImageVector = when (group) {
    AgeGroup.CHILD -> Icons.Filled.ChildCare
    AgeGroup.SENIOR -> Icons.Filled.Elderly
    else -> when (gender) {
        Gender.MALE -> Icons.Filled.Face
        Gender.FEMALE -> Icons.Filled.Face3
        Gender.UNSPECIFIED -> Icons.Filled.Person
    }
}

/** Background + foreground colors, tinted by gender with an age-based shade. */
private fun avatarColors(gender: Gender, group: AgeGroup): Pair<Color, Color> {
    val base = when (gender) {
        Gender.MALE -> Color(0xFF2D9CDB)      // blue
        Gender.FEMALE -> Color(0xFFEB5FA6)    // pink
        Gender.UNSPECIFIED -> Color(0xFF2ECC71) // brand green
    }
    // Slightly lighten/darken by life stage for a bit of variety.
    val shadeFactor = when (group) {
        AgeGroup.CHILD -> 1.15f
        AgeGroup.TEEN -> 1.07f
        AgeGroup.ADULT -> 1.0f
        AgeGroup.SENIOR -> 0.9f
        AgeGroup.UNKNOWN -> 1.0f
    }
    val fg = base.scaleLightness(shadeFactor)
    val bg = fg.copy(alpha = 0.16f)
    return bg to fg
}

private fun Color.scaleLightness(factor: Float): Color = Color(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
    alpha = alpha
)

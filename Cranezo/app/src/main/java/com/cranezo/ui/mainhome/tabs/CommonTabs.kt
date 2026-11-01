package com.cranezo.ui.mainhome.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.StatusChip
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.theme.*

/** Stand-in for bottom bar tabs whose content is not built yet. */
@Composable
fun TabPlaceholderScreen(
    title: String,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimen_20),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = dimen_24
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconBadge(icon = icon, accent = AmberPrimary, size = dimen_56)
                Spacer(modifier = Modifier.height(dimen_14))
                Textview(
                    text = title,
                    size = font_20,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = stringResource(R.string.tab_coming_soon),
                    size = font_12,
                    color = appColors.textSecondary
                )
            }
        }
    }
}

/** Profile tab shared by every role: who is signed in, the appearance setting and log out. */
@Composable
fun HomeProfileScreen(
    state: MainHomeUiState,
    onIntent: (MainHomeIntent) -> Unit = {}
) {
    val themeMode by AppThemeState.currentThemeMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_16)
    ) {
        Textview(
            text = stringResource(R.string.tab_profile),
            size = font_24,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        GlassCard(contentPadding = dimen_16) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(dimen_56)
                        .clip(CircleShape)
                        .background(AmberPrimary.copy(alpha = 0.18f))
                        .border(dimen_1, AmberPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Textview(
                        text = state.userName.take(1).uppercase().ifEmpty { "C" },
                        size = font_22,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(dimen_14))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = state.userName.ifBlank { stringResource(R.string.home_profile_default_name) },
                        size = font_18,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(dimen_4))
                    StatusChip(text = stringResource(state.role.titleRes), color = AmberPrimary)
                    if (state.mobileNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(dimen_4))
                        Textview(text = state.mobileNumber, size = font_12, color = appColors.textSecondary)
                    }
                    if (state.memberId.isNotBlank()) {
                        Textview(
                            text = stringResource(R.string.home_profile_id, state.memberId),
                            size = font_12,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }

        SectionTitle(text = stringResource(R.string.home_appearance), subtitle = stringResource(R.string.home_appearance_subtitle))

        GlassCard(contentPadding = dimen_8) {
            AppThemeMode.entries.forEach { mode ->
                val isSelected = mode == themeMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(MainHomeIntent.ThemeSelected(mode)) }
                        .padding(horizontal = dimen_8, vertical = dimen_12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimen_18)
                            .border(
                                width = dimen_1 * 1.5f,
                                color = if (isSelected) AmberPrimary else appColors.textHint,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(dimen_10)
                                    .clip(CircleShape)
                                    .background(AmberPrimary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(dimen_12))
                    Textview(
                        text = stringResource(mode.titleRes),
                        size = font_14,
                        color = if (isSelected) appColors.textPrimary else appColors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        CommonGradientButton(
            text = stringResource(R.string.home_logout),
            gradientColors = listOf(Red, DarkRed, Maroon),
            textColor = Color.White,
            onClick = { onIntent(MainHomeIntent.Logout) }
        )
    }
}

@Preview(name = "Profile tab - Dark", showBackground = true, heightDp = 800)
@Composable
private fun HomeProfileDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ScreenBackground {
            HomeProfileScreen(MainHomeUiState(userName = "Arun Kumar", memberId = "ONR659897"))
        }
    }
}

@Preview(name = "Placeholder tab - Light", showBackground = true, heightDp = 500)
@Composable
private fun TabPlaceholderLightPreview() {
    CranezoTheme(darkTheme = false) {
        ScreenBackground {
            TabPlaceholderScreen(stringResource(R.string.tab_my_cranes), Icons.Default.PrecisionManufacturing)
        }
    }
}

package com.cranezo.ui.myprofile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.myprofile.components.ChoiceRow
import com.cranezo.ui.myprofile.components.GroupLabel
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import com.cranezo.ui.myprofile.model.AppearanceOption
import com.cranezo.ui.myprofile.model.LanguageOption
import com.cranezo.ui.myprofile.model.MyProfileIntent
import com.cranezo.ui.myprofile.model.MyProfileUiState
import com.cranezo.ui.myprofile.model.NotificationGroup
import com.cranezo.ui.myprofile.model.NotificationItem
import com.cranezo.ui.theme.*

// ---------------------------------------------------------------------------------------------
// Language
// ---------------------------------------------------------------------------------------------

/** The two letters on the left of a language row: "Aa" for English, the first letter otherwise. */
private fun languageBadge(id: Int?, name: String?): String = when (id) {
    1 -> "Aa"
    else -> name?.take(1).orEmpty()
}

@Preview(showBackground = true, heightDp = 700)
@Composable
fun ProfileLanguageScreen(
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        languages = listOf(
            LanguageOption(1, "English", "Continue in English", true),
            LanguageOption(2, "తెలుగు", "తెలుగులో కొనసాగించండి", false),
            LanguageOption(3, "हिन्दी", "हिंदी में जारी रखें", false)
        ),
        selectedLanguageId = 1
    )
    LaunchedEffect(Unit) { vm?.onIntent(MyProfileIntent.LoadLanguages) }

    MyProfileScaffold(
        title = stringResource(R.string.mp_language),
        subtitle = stringResource(R.string.mp_language_subtitle),
        onBack = onBack,
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.mp_save_language),
                enabled = state.selectedLanguageId != null,
                onClick = { vm?.onIntent(MyProfileIntent.SaveLanguage) }
            )
        }
    ) {
        state.languages.forEach { language ->
            ChoiceRow(
                title = language.name.orEmpty(),
                description = language.description,
                selected = language.id == state.selectedLanguageId,
                onClick = { language.id?.let { vm?.onIntent(MyProfileIntent.SelectLanguage(it)) } },
                leading = {
                    Box(
                        modifier = Modifier
                            .size(dimen_40)
                            .clip(RoundedCornerShape(dimen_10))
                            .background(appColors.subtleFill),
                        contentAlignment = Alignment.Center
                    ) {
                        Textview(
                            text = languageBadge(language.id, language.name),
                            size = font_16,
                            color = AmberPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Appearance
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 700)
@Composable
fun AppearanceScreen(
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        appearances = listOf(
            AppearanceOption("system", "System Default", "Match your device setting", true),
            AppearanceOption("light", "Light Mode", "Always use the light theme", false),
            AppearanceOption("dark", "Dark Mode", "Always use the dark theme", false)
        ),
        selectedAppearance = "system"
    )
    LaunchedEffect(Unit) { vm?.onIntent(MyProfileIntent.LoadAppearance) }

    // Leaving without saving undoes the preview
    val leave: () -> Unit = {
        vm?.onIntent(MyProfileIntent.CancelAppearance)
        onBack()
    }
    BackHandler(onBack = leave)

    MyProfileScaffold(
        title = stringResource(R.string.mp_appearance),
        subtitle = stringResource(R.string.mp_appearance_subtitle),
        onBack = leave,
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.mp_save),
                enabled = state.selectedAppearance != null,
                onClick = { vm?.onIntent(MyProfileIntent.SaveAppearance) }
            )
        }
    ) {
        state.appearances.forEach { option ->
            ChoiceRow(
                title = option.name.orEmpty(),
                description = option.description,
                selected = option.id == state.selectedAppearance,
                onClick = { option.id?.let { vm?.onIntent(MyProfileIntent.SelectAppearance(it)) } }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Notification settings
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationSettingsScreen(
    vm: MyProfileViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: MyProfileUiState(
        notificationGroups = listOf(
            NotificationGroup(
                "Booking Updates",
                listOf(
                    NotificationItem("booking_confirmations", "Booking Confirmations", "Get notified when your booking is confirmed", true),
                    NotificationItem("job_updates", "Job Updates", "Receive updates about your ongoing jobs", false)
                )
            )
        ),
        notificationPrefs = mapOf("booking_confirmations" to true, "job_updates" to false)
    )
    LaunchedEffect(Unit) { vm?.onIntent(MyProfileIntent.LoadNotifications) }

    MyProfileScaffold(
        title = stringResource(R.string.mp_notification_settings),
        subtitle = stringResource(R.string.mp_notification_subtitle),
        onBack = onBack,
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.mp_save_preferences),
                onClick = { vm?.onIntent(MyProfileIntent.SaveNotifications) }
            )
        }
    ) {
        // Rendered as the server sent it: new toggles can appear without an app update
        state.notificationGroups.forEach { group ->
            GroupLabel(group.group.orEmpty())
            GlassCard(contentPadding = dimen_4) {
                group.items.orEmpty().forEach { item ->
                    val key = item.key ?: return@forEach
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimen_10, vertical = dimen_10),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Textview(
                                text = item.title.orEmpty(),
                                size = font_14,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!item.subtitle.isNullOrBlank()) {
                                Textview(text = item.subtitle, size = font_11, color = appColors.textSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.width(dimen_8))
                        Switch(
                            checked = state.notificationPrefs[key] ?: (item.enabled == true),
                            onCheckedChange = { vm?.onIntent(MyProfileIntent.ToggleNotification(key, it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AmberPrimary,
                                uncheckedThumbColor = appColors.textSecondary,
                                uncheckedTrackColor = appColors.subtleFill,
                                uncheckedBorderColor = appColors.subtleBorder
                            )
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Help & Support: there is no API for it yet
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 500)
@Composable
fun HelpSupportScreen(onBack: () -> Unit = {}) {
    MyProfileScaffold(title = stringResource(R.string.mp_help_support), onBack = onBack) {
        Spacer(modifier = Modifier.height(dimen_24))
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = dimen_24) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconBadge(icon = Icons.Default.SupportAgent, accent = AmberPrimary, size = dimen_56)
                Spacer(modifier = Modifier.height(dimen_14))
                Textview(
                    text = stringResource(R.string.mp_help_support),
                    size = font_20,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = stringResource(R.string.tab_coming_soon),
                    size = font_12,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

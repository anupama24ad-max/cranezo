package com.cranezo.ui.mainhome.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.mainhome.components.GlassTextButton
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.StatusChip
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.mainhome.model.Dest
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.theme.*

@Composable
fun OperatorHomeScreen(
    state: MainHomeUiState,
    onNavigate: (String) -> Unit = {},
    onIntent: (MainHomeIntent) -> Unit = {}
) {
    val data = state.operator

    com.cranezo.ui.mainhome.components.HomeScreenColumn(
        spacing = dimen_16,
        hero = {
            HomeHeader(
                name = state.userName.ifBlank { stringResource(R.string.home_operator_default_name) },
                subtitle = stringResource(R.string.home_operator_subtitle)
            )
        }
    ) {

        // Availability
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(
                    shape = RoundedCornerShape(dimen_16),
                    borderColor = if (state.isOnline) SuccessGreen else Color.Unspecified
                )
                .padding(dimen_14),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Default.PowerSettingsNew,
                accent = if (state.isOnline) SuccessGreen else AmberPrimary,
                size = dimen_44
            )
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = if (state.isOnline) stringResource(R.string.home_online) else stringResource(R.string.home_offline),
                    size = font_16,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Textview(
                    text = if (state.isOnline) stringResource(R.string.home_online_subtitle) else stringResource(R.string.home_offline_subtitle),
                    size = font_11,
                    color = appColors.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimen_12))
                    .background(
                        Brush.horizontalGradient(
                            if (state.isOnline) listOf(Red, DarkRed)
                            else listOf(SuccessGreen, Color(0xFF00A844))
                        )
                    )
                    .clickable { onIntent(MainHomeIntent.ToggleOnline) }
                    .padding(horizontal = dimen_14, vertical = dimen_8),
                contentAlignment = Alignment.Center
            ) {
                Textview(
                    text = if (state.isOnline) stringResource(R.string.home_go_offline) else stringResource(R.string.home_go_online),
                    size = font_12,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Assigned crane
        GlassCard(contentPadding = dimen_14) {
            SectionTitle(
                text = stringResource(R.string.home_assigned_crane),
                icon = Icons.Default.PrecisionManufacturing,
                actionText = stringResource(R.string.home_view_details)
            )
            Spacer(modifier = Modifier.height(dimen_12))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Default.PrecisionManufacturing,
                    accent = AmberPrimary,
                    size = dimen_56
                )
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = data.craneName,
                        size = font_16,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Textview(text = data.craneNumber, size = font_12, color = appColors.textSecondary)
                    Textview(text = data.craneType, size = font_12, color = appColors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Textview(text = stringResource(R.string.home_assigned_owner), size = font_11, color = appColors.textSecondary)
                    Textview(
                        text = data.ownerName,
                        size = font_12,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(dimen_6))
                    StatusChip(text = data.craneStatus, color = SuccessGreen)
                }
            }
        }

        // Today's jobs
        GlassCard(contentPadding = dimen_14) {
            SectionTitle(
                text = stringResource(R.string.home_todays_jobs),
                actionText = stringResource(R.string.common_view_all),
                onActionClick = { onNavigate(Dest.Jobs.route) }
            )
            Spacer(modifier = Modifier.height(dimen_12))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .size(dimen_48)
                        .clip(RoundedCornerShape(dimen_12))
                        .background(appColors.subtleFill),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Textview(
                        text = data.jobDay,
                        size = font_16,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Textview(text = data.jobMonth, size = font_10, color = appColors.primaryText)
                }
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        text = data.jobClient,
                        size = font_14,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Textview(text = data.jobLocation, size = font_12, color = appColors.textSecondary)
                    Textview(text = data.jobTime, size = font_11, color = appColors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(text = data.jobStatus, color = InfoBlue)
                    Spacer(modifier = Modifier.height(dimen_8))
                    GlassTextButton(text = stringResource(R.string.home_view_job), onClick = { onNavigate(Dest.Jobs.route) })
                }
            }
        }

        // Earnings
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.CurrencyRupee, accent = AmberPrimary, size = dimen_40)
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(text = stringResource(R.string.home_todays_earnings), size = font_12, color = appColors.textSecondary)
                    Textview(
                        text = data.todaysEarnings,
                        size = font_22,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Textview(
                    text = stringResource(R.string.home_view_earnings),
                    size = font_12,
                    color = AmberPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(Dest.Earnings.route) }
                )
            }
        }

        // Documents
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.VerifiedUser, accent = SuccessGreen, size = dimen_40)
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(text = stringResource(R.string.home_document_status), size = font_12, color = appColors.textSecondary)
                    Textview(
                        text = data.documentStatus,
                        size = font_14,
                        color = appColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Textview(
                    text = stringResource(R.string.home_view_documents),
                    size = font_12,
                    color = AmberPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(Dest.Profile.route) }
                )
            }
        }
    }
}

@Preview(name = "Operator Home - Dark", showBackground = true, heightDp = 1000)
@Composable
private fun OperatorHomeDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ScreenBackground { OperatorHomeScreen(MainHomeUiState(userName = "Ravi Kumar")) }
    }
}

@Preview(name = "Operator Home - Light", showBackground = true, heightDp = 1000)
@Composable
private fun OperatorHomeLightPreview() {
    CranezoTheme(darkTheme = false) {
        ScreenBackground { OperatorHomeScreen(MainHomeUiState(userName = "Ravi Kumar", isOnline = true)) }
    }
}

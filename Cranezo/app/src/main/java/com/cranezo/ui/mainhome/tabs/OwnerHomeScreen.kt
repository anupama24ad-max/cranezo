package com.cranezo.ui.mainhome.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.MetricCard
import com.cranezo.ui.mainhome.components.QuickActionTile
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.StatTile
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.mainhome.components.TwoColumnRow
import com.cranezo.ui.mainhome.components.twoDigits
import com.cranezo.ui.mainhome.model.Dest
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.theme.*
import com.cranezo.ui.components.Textview

@Composable
fun OwnerHomeScreen(
    state: MainHomeUiState,
    onNavigate: (String) -> Unit = {},
    onAddCrane: () -> Unit = {}
) {
    val fleet = state.owner.fleet

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_16)
    ) {
        HomeHeader(
            name = state.userName.ifBlank { stringResource(R.string.home_owner_default_name) },
            subtitle = stringResource(R.string.home_owner_subtitle)
        )

        GlassCard(contentPadding = dimen_14, backgroundColor = DarkBluish, isGlassy = false) {
            SectionTitle(text = stringResource(R.string.home_fleet_overview), icon = Icons.Default.PrecisionManufacturing)
            Spacer(modifier = Modifier.height(dimen_12))
            Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
                TwoColumnRow(
                    left = {
                        StatTile(twoDigits(fleet.totalCranes), stringResource(R.string.home_total_cranes), Icons.Default.PrecisionManufacturing, InfoBlue, it)
                    },
                    right = {
                        StatTile(twoDigits(fleet.availableCranes), stringResource(R.string.home_available_cranes), Icons.Default.CheckCircle, SuccessGreen, it)
                    }
                )
                TwoColumnRow(
                    left = {
                        StatTile(twoDigits(fleet.busyCranes), stringResource(R.string.home_busy_cranes), Icons.Default.Schedule, AmberPrimary, it)
                    },
                    right = {
                        StatTile(twoDigits(fleet.pendingVerification), stringResource(R.string.home_pending_verification), Icons.Default.VerifiedUser, InfoBlue, it)
                    }
                )
            }
        }

        TwoColumnRow(
            left = {
                MetricCard(
                    title = stringResource(R.string.home_todays_jobs),
                    value = twoDigits(state.owner.todaysJobs),
                    caption = stringResource(R.string.home_jobs_scheduled),
                    icon = Icons.Default.Work,
                    accent = AmberPrimary,
                    modifier = it,
                    onClick = { onNavigate(Dest.Jobs.route) }
                )
            },
            right = {
                MetricCard(
                    title = stringResource(R.string.home_earnings),
                    value = state.owner.todaysEarnings,
                    caption = stringResource(R.string.home_todays_earnings_caption),
                    icon = Icons.Default.CurrencyRupee,
                    accent = SuccessGreen,
                    modifier = it,
                    onClick = { onNavigate(Dest.Earnings.route) }
                )
            }
        )

        // Customers near your cranes asking for a crane: accept first and the job is yours
        val context = androidx.compose.ui.platform.LocalContext.current
        GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary, onClick = {
            context.startActivity(
                com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.CRANE_REQUESTS)
            )
        }) {
            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.rq_owner_title), size = font_16, color = appColors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Textview(stringResource(R.string.rq_owner_card_sub), size = font_12, color = appColors.textSecondary)
                }
                if (state.owner.craneRequests > 0) {
                    com.cranezo.ui.booking.components.BookingTag(state.owner.craneRequests.toString(), AmberPrimary)
                    Spacer(modifier = Modifier.width(dimen_8))
                }
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AmberPrimary
                )
            }
        }

        // Paid bookings still waiting for an operator: open Jobs to assign one
        if (state.owner.jobsNeedingOperator > 0) {
            GlassCard(contentPadding = dimen_14, borderColor = androidx.compose.ui.graphics.Color(0xFFEF4444), onClick = { onNavigate(Dest.Jobs.route) }) {
                Textview(
                    stringResource(R.string.rq_jobs_need_operator, state.owner.jobsNeedingOperator), size = font_14,
                    color = appColors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Textview(stringResource(R.string.rq_jobs_need_operator_sub), size = font_12, color = appColors.textSecondary)
            }
        }

        // One action. My Cranes, Jobs and Earnings are already in the bottom bar and the cards above.
        CommonGradientButton(
            text = stringResource(R.string.home_add_crane),
            leftIcon = Icons.Default.Add,
            onClick = onAddCrane
        )
    }
}

@Preview(name = "Owner Home - Dark", showBackground = true, heightDp = 900)
@Composable
private fun OwnerHomeDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ScreenBackground { OwnerHomeScreen(MainHomeUiState(userName = "Arun Kumar")) }
    }
}

@Preview(name = "Owner Home - Light", showBackground = true, heightDp = 900)
@Composable
private fun OwnerHomeLightPreview() {
    CranezoTheme(darkTheme = false) {
        ScreenBackground { OwnerHomeScreen(MainHomeUiState(userName = "Arun Kumar")) }
    }
}

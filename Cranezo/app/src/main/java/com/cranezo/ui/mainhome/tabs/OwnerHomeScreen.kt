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
import com.cranezo.ui.mainhome.components.CurvedHero
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
    ) {
        CurvedHero {
            Column {
                HomeHeader(
                    name = state.userName.ifBlank { stringResource(R.string.home_owner_default_name) },
                    subtitle = stringResource(R.string.home_owner_subtitle)
                )
                Spacer(modifier = Modifier.height(dimen_16))
                Textview(
                    stringResource(R.string.home_todays_earnings_caption),
                    size = font_11,
                    color = appColors.textSecondary
                )
                Textview(
                    state.owner.todaysEarnings,
                    size = font_26,
                    color = appColors.primaryText,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

    Column(
        modifier = Modifier
            .padding(horizontal = dimen_20)
            .padding(top = dimen_16, bottom = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_16)
    ) {

        // Fleet Overview: its own panel (deep navy in dark mode, glass in light mode) holding the 4 tiles
        GlassCard(
            contentPadding = dimen_14,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(dimen_20),
            backgroundColor = if (appColors.isDark) DarkBluish.copy(alpha = 0.55f) else androidx.compose.ui.graphics.Color.Unspecified
        ) {
            SectionTitle(
                text = stringResource(R.string.home_fleet_overview),
                icon = R.drawable.fleet_ic
            )
            Spacer(modifier = Modifier.height(dimen_14))
            Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
                TwoColumnRow(
                    left = {
                        StatTile(
                            twoDigits(fleet.totalCranes),
                            stringResource(R.string.home_total_cranes),
                            R.drawable.crane_ic,
                            InfoBlue,
                            it
                        )
                    },
                    right = {
                        StatTile(
                            twoDigits(fleet.availableCranes),
                            stringResource(R.string.home_available_cranes),
                            R.drawable.crane_ic,
                            SuccessGreen,
                            it
                        )
                    }
                )
                TwoColumnRow(
                    left = {
                        StatTile(
                            twoDigits(fleet.busyCranes),
                            stringResource(R.string.home_busy_cranes),
                            R.drawable.crane_ic,
                            AmberPrimary,
                            it
                        )
                    },
                    right = {
                        StatTile(
                            twoDigits(fleet.pendingVerification),
                            stringResource(R.string.home_pending_verification),
                            R.drawable.doc_ic,
                            InfoBlue,
                            it
                        )
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
                    icon = R.drawable.my_jobs_ic,
                    accent = AmberPrimary,
                    isBorder = true,
                    modifier = it,
                    onClick = { onNavigate(Dest.Jobs.route) }
                )
            },
            right = {
                MetricCard(
                    title = stringResource(R.string.home_earnings),
                    value = state.owner.todaysEarnings,
                    caption = stringResource(R.string.home_todays_earnings_caption),
                    icon = R.drawable.earnings_ic,
                    accent = SuccessGreen,
                    isBorder = true,
                    modifier = it,
                    onClick = { onNavigate(Dest.Earnings.route) }
                )
            }
        )

        // Quick Actions: Add Crane (highlighted), My Cranes, Jobs, Earnings
        Column {
            Textview(
                stringResource(R.string.home_quick_actions),
                size = font_16,
                color = appColors.textPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_2))
            Textview(
                stringResource(R.string.home_quick_actions_subtitle),
                size = font_11,
                color = appColors.textSecondary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
            TwoColumnRow(
                left = {
                    QuickActionTile(
                        title = stringResource(R.string.home_add_crane),
                        subtitle = stringResource(R.string.home_add_crane_subtitle),
                        icon = R.drawable.add_ic,
                        accent = AmberPrimary,
                        modifier = it,
                        highlighted = true,
                        onClick = onAddCrane
                    )
                },
                right = {
                    QuickActionTile(
                        title = stringResource(R.string.tab_my_cranes),
                        subtitle = stringResource(R.string.home_my_cranes_subtitle),
                        icon = R.drawable.crane_ic,
                        accent = InfoBlue,
                        modifier = it,
                        onClick = { onNavigate(Dest.MyCranes.route) }
                    )
                }
            )
            TwoColumnRow(
                left = {
                    QuickActionTile(
                        title = stringResource(R.string.tab_jobs),
                        subtitle = stringResource(R.string.home_jobs_subtitle),
                        icon = R.drawable.sky_blue_my_job_ic,
                        accent = InfoBlue,
                        modifier = it,
                        onClick = { onNavigate(Dest.Jobs.route) }
                    )
                },
                right = {
                    QuickActionTile(
                        title = stringResource(R.string.home_earnings),
                        subtitle = stringResource(R.string.home_earnings_subtitle),
                        icon = R.drawable.earnings_ic,
                        accent = InfoBlue,
                        modifier = it,
                        onClick = { onNavigate(Dest.Earnings.route) }
                    )
                }
            )
        }

        // Customers near your cranes asking for a crane: accept first and the job is yours
        val context = androidx.compose.ui.platform.LocalContext.current
        GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary, onClick = {
            context.startActivity(
                com.cranezo.ui.finance.FinanceActivity.intent(
                    context,
                    com.cranezo.ui.finance.FinanceRoutes.CRANE_REQUESTS
                )
            )
        }) {
            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(
                        stringResource(R.string.rq_owner_title),
                        size = font_16,
                        color = appColors.textPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Textview(
                        stringResource(R.string.rq_owner_card_sub),
                        size = font_12,
                        color = appColors.textSecondary
                    )
                }
                if (state.owner.craneRequests > 0) {
                    com.cranezo.ui.booking.components.BookingTag(
                        state.owner.craneRequests.toString(),
                        AmberPrimary
                    )
                    Spacer(modifier = Modifier.width(dimen_8))
                }
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AmberPrimary
                )
            }
        }

        // Paid bookings still waiting for an operator: open Jobs to assign one
        if (state.owner.jobsNeedingOperator > 0) {
            GlassCard(
                contentPadding = dimen_14,
                borderColor = androidx.compose.ui.graphics.Color(0xFFEF4444),
                onClick = { onNavigate(Dest.Jobs.route) }) {
                Textview(
                    stringResource(R.string.rq_jobs_need_operator, state.owner.jobsNeedingOperator),
                    size = font_14,
                    color = appColors.textPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Textview(
                    stringResource(R.string.rq_jobs_need_operator_sub),
                    size = font_12,
                    color = appColors.textSecondary
                )
            }
        }
    }
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

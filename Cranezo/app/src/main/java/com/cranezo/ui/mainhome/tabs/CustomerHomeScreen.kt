package com.cranezo.ui.mainhome.tabs

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cranezo.ui.components.ActionPill
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SectionTitle
import com.cranezo.ui.mainhome.components.StatusChip
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.mainhome.model.Dest
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.theme.*
import androidx.compose.ui.graphics.Color

@Composable
fun CustomerHomeScreen(
    state: MainHomeUiState,
    onNavigate: (String) -> Unit = {}
) {
    val displayName = buildString {
        append(state.userName.ifBlank { stringResource(R.string.home_customer_default_name) })
        if (state.memberId.isNotBlank()) append(", #${state.memberId}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_16)
    ) {
        HomeHeader(
            name = displayName,
            subtitle = stringResource(R.string.home_customer_subtitle)
        )

        // Hero banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(
                    shape = RoundedCornerShape(dimen_20),
                    backgroundColor = AmberPrimary.copy(alpha = 0.08f)
                )
                .padding(dimen_20)
        ) {
            Icon(
                imageVector = Icons.Default.PrecisionManufacturing,
                contentDescription = null,
                tint = AmberPrimary.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(dimen_100)
            )
            Column {
                Textview(
                    text = stringResource(R.string.home_hero_1),
                    size = font_26,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Textview(
                    text = stringResource(R.string.home_hero_2),
                    size = font_26,
                    color = AmberPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(
                    text = stringResource(R.string.home_hero_subtitle),
                    size = font_12,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(dimen_14))
                CommonGradientButton(
                    text = stringResource(R.string.home_hero_button),
                    modifier = Modifier.width(170.dp),
                    gradientColors = listOf(LightYellow, DarkYellow),
                    textColor = Color.Black,
                    textSize = font_14,
                    height = dimen_40,
                    rightIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    iconSize = dimen_16,
                    onClick = { onNavigate(Dest.BookCrane.route) }
                )
            }
        }

        // Promo banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(RoundedCornerShape(dimen_20))
                .padding(dimen_18),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = stringResource(R.string.home_promo_get_up_to),
                    size = font_11,
                    color = appColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Textview(
                    text = stringResource(R.string.home_promo_off),
                    size = font_30,
                    color = AmberPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Textview(
                    text = stringResource(R.string.home_promo_subtitle),
                    size = font_12,
                    color = appColors.textSecondary
                )
            }
            ActionPill(text = stringResource(R.string.home_promo_button), onClick = { onNavigate(Dest.BookCrane.route) })
        }

        SectionTitle(text = stringResource(R.string.home_quick_access))
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            QuickAccessTile(
                title = stringResource(R.string.home_track_crane),
                subtitle = stringResource(R.string.home_track_crane_subtitle),
                icon = Icons.Default.LocationOn,
                accent = AmberPrimary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Dest.Jobs.route) }
            )
            QuickAccessTile(
                title = stringResource(R.string.tab_my_jobs),
                subtitle = stringResource(R.string.home_my_jobs_subtitle),
                icon = Icons.Default.Work,
                accent = InfoBlue,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Dest.Jobs.route) }
            )
            QuickAccessTile(
                title = stringResource(R.string.home_need_help),
                subtitle = stringResource(R.string.home_need_help_subtitle),
                icon = Icons.Default.SupportAgent,
                accent = AmberPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        state.customer.recentBooking?.let { booking ->
            SectionTitle(
                text = stringResource(R.string.home_recent_booking),
                actionText = stringResource(R.string.common_view_all),
                onActionClick = { onNavigate(Dest.Jobs.route) }
            )
            GlassCard(contentPadding = dimen_14) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        icon = Icons.Default.PrecisionManufacturing,
                        accent = AmberPrimary,
                        size = dimen_44
                    )
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(
                            text = booking.bookingId,
                            size = font_11,
                            color = appColors.textSecondary
                        )
                        Textview(
                            text = booking.craneName,
                            size = font_14,
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Textview(
                            text = "${booking.location} · ${booking.date}",
                            size = font_11,
                            color = appColors.textSecondary,
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.width(dimen_8))
                    Column(horizontalAlignment = Alignment.End) {
                        StatusChip(text = booking.status, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(dimen_6))
                        Textview(
                            text = booking.amount,
                            size = font_14,
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .glassEffect(RoundedCornerShape(dimen_14))
            .clickable { onClick() }
            .padding(dimen_12)
    ) {
        IconBadge(icon = icon, accent = accent, size = dimen_36)
        Spacer(modifier = Modifier.height(dimen_10))
        Textview(
            text = title,
            size = font_13,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
        Textview(text = subtitle, size = font_11, color = appColors.textSecondary, maxLines = 2)
    }
}

@Preview(name = "Customer Home - Dark", showBackground = true, heightDp = 1000)
@Composable
private fun CustomerHomeDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ScreenBackground {
            CustomerHomeScreen(MainHomeUiState(userName = "Rahul", memberId = "CUST659897"))
        }
    }
}

@Preview(name = "Customer Home - Light", showBackground = true, heightDp = 1000)
@Composable
private fun CustomerHomeLightPreview() {
    CranezoTheme(darkTheme = false) {
        ScreenBackground {
            CustomerHomeScreen(MainHomeUiState(userName = "Rahul", memberId = "CUST659897"))
        }
    }
}

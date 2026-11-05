package com.cranezo.ui.ownerjobs

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.ownerjobs.model.ComparedToPrevious
import com.cranezo.ui.ownerjobs.model.EarningJobCrane
import com.cranezo.ui.ownerjobs.model.EarningsBlock
import com.cranezo.ui.ownerjobs.model.EarningsPeriod
import com.cranezo.ui.ownerjobs.model.EarningsTrend
import com.cranezo.ui.ownerjobs.model.OwnerEarningJob
import com.cranezo.ui.ownerjobs.model.OwnerEarningsIntent
import com.cranezo.ui.ownerjobs.model.OwnerEarningsState
import com.cranezo.ui.ownerjobs.model.OwnerEarningsSummary
import com.cranezo.ui.ownerjobs.model.PeriodCard
import com.cranezo.ui.ownerjobs.model.SettleInfo
import com.cranezo.ui.ownerjobs.model.SettlementBlock
import com.cranezo.ui.ownerjobs.model.TrendPoint
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees
import java.util.Locale
import kotlin.math.abs

private val previewState = OwnerEarningsState(
    summary = OwnerEarningsSummary(
        period = EarningsPeriod("today"), earnings = EarningsBlock(42105.0, 5.0, 2105.0, 0.0, 40000.0), jobs_completed = 1,
        compared_to_previous = ComparedToPrevious("yesterday", 35000.0, 12.0),
        cards = mapOf("today" to PeriodCard(40000.0, 1), "week" to PeriodCard(92500.0, 3), "month" to PeriodCard(184500.0, 6), "all" to PeriodCard(672300.0, 20)),
        settlement = SettlementBlock(SettleInfo(150000.0, 4, "2025-09-14T10:00:00.000Z"), SettleInfo(34500.0, 1, expected_in_days = 3))
    ),
    trend = EarningsTrend("month", List(12) { TrendPoint("k$it", "${it + 1}", (it * 3000).toDouble()) }, TrendPoint("k8", "9", 24000.0), 190000.0),
    jobs = listOf(OwnerEarningJob("1", "CRZ-1", EarningJobCrane("CRN001", "50 Ton Mobile Crane"), "OMR, Chennai", "2025-09-14T09:00:00.000Z", 42500.0, 40375.0, "pending", "Pending")),
    loaded = true
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
fun OwnerEarningsTab(
    vm: OwnerEarningsViewModel? = null,
    userName: String = "",
    onOpenJob: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: previewState
    val onIntent: (OwnerEarningsIntent) -> Unit = { vm?.onIntent(it) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onIntent(OwnerEarningsIntent.Load) }
    var showAll by remember { mutableStateOf(false) }

    val summary = state.summary
    val earnings = summary?.earnings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        HomeHeader(name = userName, subtitle = stringResource(R.string.oe_subtitle))
        Textview(stringResource(R.string.op_earnings_title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)

        // Period switch
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf("today" to R.string.oe_today, "week" to R.string.oe_this_week, "month" to R.string.oe_this_month, "all" to R.string.oe_all_time).forEach { (key, label) ->
                val selected = key == state.period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimen_16))
                        .background(if (selected) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
                        .clickable { onIntent(OwnerEarningsIntent.SelectPeriod(key)) }
                        .padding(vertical = dimen_8),
                    contentAlignment = Alignment.Center
                ) {
                    Textview(stringResource(label), size = font_11, maxLines = 1, color = if (selected) appColors.primaryText else appColors.textPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // Header card: net earnings of the period
        GlassCard(contentPadding = dimen_14) {
            Textview(
                stringResource(when (state.period) { "week" -> R.string.oe_week_earnings; "month" -> R.string.oe_month_earnings; "all" -> R.string.oe_total_earnings; else -> R.string.op_todays_earnings }),
                size = font_12, color = appColors.textSecondary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Textview(rupees(earnings?.net_earnings ?: 0.0), size = font_32, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Textview(stringResource(R.string.oe_jobs_completed, summary?.jobs_completed ?: 0), size = font_11, color = appColors.textSecondary)
                    Textview(periodText(summary?.period), size = font_10, color = appColors.textSecondary)
                }
            }
            // Hidden when there is nothing to compare with
            val compared = summary?.compared_to_previous
            val change = compared?.change_percent
            if (change != null) {
                val up = change >= 0
                Spacer(modifier = Modifier.height(dimen_6))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(dimen_8)).background((if (up) SuccessGreen else Red).copy(alpha = 0.16f)).padding(horizontal = dimen_8, vertical = dimen_2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = if (up) SuccessGreen else Red, modifier = Modifier.size(dimen_12))
                    Textview(
                        stringResource(R.string.oe_compared, String.format(Locale.US, "%.0f", abs(change)), compared.label.orEmpty()),
                        size = font_10, color = if (up) SuccessGreen else Red
                    )
                }
            }
        }

        // Four period tiles in a 2 x 2 grid, the same panel and tiles as the owner dashboard (Fleet Overview)
        GlassCard(contentPadding = dimen_14, backgroundColor = DarkBluish, isGlassy = false) {
            val tile: @Composable (String, Int, androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color, Modifier) -> Unit =
                { key, label, icon, accent, mod ->
                    val card = summary?.cards?.get(key)
                    com.cranezo.ui.mainhome.components.StatTile(
                        value = rupees(card?.net ?: 0.0),
                        label = "${stringResource(label)} · ${stringResource(R.string.oe_n_jobs, card?.jobs ?: 0)}",
                        icon = icon,
                        accent = accent,
                        modifier = mod.fillMaxHeight().clickable { onIntent(OwnerEarningsIntent.SelectPeriod(key)) }
                    )
                }
            Column(verticalArrangement = Arrangement.spacedBy(dimen_10)) {
                com.cranezo.ui.mainhome.components.TwoColumnRow(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    left = { tile("today", R.string.oe_today, Icons.Default.Schedule, AmberPrimary, it) },
                    right = { tile("week", R.string.oe_this_week, Icons.Default.DateRange, com.cranezo.ui.mainhome.components.InfoBlue, it) }
                )
                com.cranezo.ui.mainhome.components.TwoColumnRow(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    left = { tile("month", R.string.oe_this_month, Icons.Default.CalendarMonth, SuccessGreen, it) },
                    right = { tile("all", R.string.oe_total, Icons.Default.CheckCircle, com.cranezo.ui.mainhome.components.InfoBlue, it) }
                )
            }
        }

        // Breakdown
        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.oe_breakdown), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            PriceLine(stringResource(R.string.oe_gross), rupees(earnings?.gross_earnings))
            PriceLine(stringResource(R.string.oe_platform_fee, (earnings?.platform_fee_percent ?: 0.0).toInt()), "- ${rupees(earnings?.platform_fee)}", valueColor = Red)
            if ((earnings?.other_deductions ?: 0.0) > 0) PriceLine(stringResource(R.string.oe_other_deductions), "- ${rupees(earnings?.other_deductions)}", valueColor = Red)
            Spacer(modifier = Modifier.height(dimen_6))
            PriceLine(stringResource(R.string.oe_net), rupees(earnings?.net_earnings), bold = true)
        }

        // Settlement status covers all time
        Textview(stringResource(R.string.oe_settlement_status), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            val settled = summary?.settlement?.settled
            val pending = summary?.settlement?.pending
            GlassCard(modifier = Modifier.weight(1f), contentPadding = dimen_12, borderColor = SuccessGreen) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(dimen_18))
                    Spacer(modifier = Modifier.width(dimen_6))
                    Textview(stringResource(R.string.op_settled), size = font_12, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
                Textview(rupees(settled?.amount ?: 0.0), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                if (settled?.last_settled_at != null) Textview(stringResource(R.string.oe_last_settled, formatServerDate(settled.last_settled_at)), size = font_10, color = appColors.textSecondary)
            }
            GlassCard(modifier = Modifier.weight(1f), contentPadding = dimen_12, borderColor = AmberPrimary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
                    Spacer(modifier = Modifier.width(dimen_6))
                    Textview(stringResource(R.string.oe_pending), size = font_12, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                }
                Textview(rupees(pending?.amount ?: 0.0), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                if (pending?.expected_by != null) {
                    Textview(
                        if ((pending.expected_in_days ?: 0) <= 0) stringResource(R.string.oe_settlement_due) else stringResource(R.string.oe_settled_in_days, pending.expected_in_days ?: 0),
                        size = font_10, color = appColors.textSecondary
                    )
                }
            }
        }

        TrendCard(state, onIntent)

        // Recent completed jobs: the first 3, View All shows the whole list
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.oe_recent_jobs), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (state.jobs.size > 3 || state.jobsHasMore) {
                Textview(
                    stringResource(if (showAll) R.string.oe_show_less else R.string.common_view_all), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showAll = !showAll }
                )
            }
        }
        (if (showAll) state.jobs else state.jobs.take(3)).forEach { job ->
            GlassCard(contentPadding = dimen_12, minHeight = dimen_88, onClick = { job.booking_id?.let(onOpenJob) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = job.crane?.primary_photo, modifier = Modifier.size(dimen_56))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(job.crane?.title.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview(listOfNotNull(job.crane?.crane_code, job.location).joinToString(" • "), size = font_10, color = appColors.textSecondary, maxLines = 1)
                        Textview(formatServerDateTime(job.completed_at), size = font_10, color = appColors.textSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(rupees(job.amount), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        BookingTag(job.settlement_label.orEmpty(), if (job.settlement_status == "settled") SuccessGreen else AmberPrimary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        if (showAll && state.jobsHasMore) {
            LaunchedEffect(state.jobs.size) { onIntent(OwnerEarningsIntent.LoadMoreJobs) }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** "17 Sep 2025", a range, or "All time". */
@Composable
private fun periodText(period: EarningsPeriod?): String {
    if (period == null) return ""
    if (period.from == null) return stringResource(R.string.oe_all_time)
    val from = formatServerDate(period.from)
    val to = formatServerDate(period.to)
    return if (from == to) from else "$from - $to"
}

// ---------------------------------------------------------------------------------------------
// Earnings Trend: one bar per point, the peak highlighted with its amount
// ---------------------------------------------------------------------------------------------

@Composable
private fun TrendCard(state: OwnerEarningsState, onIntent: (OwnerEarningsIntent) -> Unit) {
    val trend = state.trend
    val points = trend?.points.orEmpty()
    val barColor = AmberPrimary.copy(alpha = 0.45f)
    val peakColor = AmberPrimary

    GlassCard(contentPadding = dimen_14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.oe_trend), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            listOf("week" to R.string.oe_range_week, "month" to R.string.oe_range_month, "year" to R.string.oe_range_year).forEach { (key, label) ->
                val selected = key == state.range
                Box(
                    modifier = Modifier.padding(start = dimen_6).clip(RoundedCornerShape(dimen_8))
                        .background(if (selected) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
                        .clickable { onIntent(OwnerEarningsIntent.SelectRange(key)) }
                        .padding(horizontal = dimen_8, vertical = dimen_3)
                ) {
                    Textview(stringResource(label), size = font_10, color = if (selected) appColors.primaryText else appColors.textSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_10))

        val peak = trend?.peak
        if (peak == null || points.isEmpty()) {
            Textview(stringResource(R.string.oe_no_trend), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(vertical = dimen_24))
            return@GlassCard
        }

        Textview(stringResource(R.string.oe_peak, rupees(peak.net), peak.label.orEmpty()), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
        val max = (points.maxOfOrNull { it.net ?: 0.0 } ?: 1.0).coerceAtLeast(1.0)
        Canvas(modifier = Modifier.fillMaxWidth().height(dimen_100).padding(top = dimen_6)) {
            val slot = size.width / points.size
            val barWidth = slot * 0.6f
            points.forEachIndexed { i, p ->
                val h = (size.height * ((p.net ?: 0.0) / max)).toFloat().coerceAtLeast(2f)
                drawRoundRect(
                    color = if (p.key == peak.key) peakColor else barColor,
                    topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
        // Every few labels, so they stay readable
        val step = ((points.size + 5) / 6).coerceAtLeast(1)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.filterIndexed { i, _ -> i % step == 0 }.forEach { Textview(it.label.orEmpty(), size = font_10, color = appColors.textSecondary) }
        }
    }
}

package com.cranezo.ui.ownerjobs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cranezo.ui.components.glassEffect
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
    jobs = listOf(OwnerEarningJob("1", "CRZ-1", EarningJobCrane("CRN001", "50 Ton Mobile Crane"), "OMR, Chennai", "2025-09-14T09:00:00.000Z", 29400L, 42500.0, 40375.0, "settled", "Settled")),
    loaded = true
)

@Preview(showBackground = true, heightDp = 1500)
@Composable
fun OwnerEarningsTab(
    vm: OwnerEarningsViewModel? = null,
    userName: String = "",
    onOpenJob: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: previewState
    val onIntent: (OwnerEarningsIntent) -> Unit = { vm?.onIntent(it) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onIntent(OwnerEarningsIntent.Load) }
    // a push about a job, request or payment: show the change right away
    com.cranezo.push.OnPush { onIntent(OwnerEarningsIntent.Load) }
    var showAll by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val summary = state.summary
    val earnings = summary?.earnings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_14)
    ) {
        HomeHeader(name = userName, subtitle = stringResource(R.string.home_owner_subtitle))

        // Title
        Column {
            Textview(stringResource(R.string.op_earnings_title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.oe_subtitle), size = font_12, color = appColors.textSecondary)
        }

        // Period switch: outlined pills, the selected one in amber
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf("today" to R.string.oe_today, "week" to R.string.oe_this_week, "month" to R.string.oe_this_month, "all" to R.string.oe_all_time).forEach { (key, label) ->
                val selected = key == state.period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimen_12))
                        .background(if (selected) AmberPrimary.copy(alpha = 0.12f) else appColors.subtleFill)
                        .border(dimen_1, if (selected) AmberPrimary else appColors.textHint.copy(alpha = 0.3f), RoundedCornerShape(dimen_12))
                        .clickable { onIntent(OwnerEarningsIntent.SelectPeriod(key)) }
                        .padding(vertical = dimen_10),
                    contentAlignment = Alignment.Center
                ) {
                    Textview(
                        stringResource(label), size = font_12, maxLines = 1,
                        color = if (selected) AmberPrimary else appColors.textPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Hero card: the period's net earnings, the date, jobs done and the change, with an amber glow on the right
        GlassCard(contentPadding = dimen_16, shape = RoundedCornerShape(dimen_20), minHeight = dimen_140) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                0.45f to androidx.compose.ui.graphics.Color.Transparent,
                                1f to AmberPrimary.copy(alpha = if (appColors.isDark) 0.22f else 0.14f)
                            )
                        )
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Textview(
                            stringResource(when (state.period) { "week" -> R.string.oe_week_earnings; "month" -> R.string.oe_month_earnings; "all" -> R.string.oe_total_earnings; else -> R.string.op_todays_earnings }),
                            size = font_13, color = appColors.textSecondary, modifier = Modifier.weight(1f)
                        )
                        // date chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimen_8))
                                .border(dimen_1, appColors.textHint.copy(alpha = 0.3f), RoundedCornerShape(dimen_8))
                                .padding(horizontal = dimen_8, vertical = dimen_4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                            Spacer(modifier = Modifier.width(dimen_4))
                            Textview(heroDate(summary?.period), size = font_11, color = appColors.textPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(dimen_10))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Textview(rupees(earnings?.net_earnings ?: 0.0), size = font_32, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Textview(stringResource(R.string.oe_jobs_completed, summary?.jobs_completed ?: 0), size = font_12, color = appColors.textSecondary)
                    }
                    // change against the previous period: hidden when there is nothing to compare with
                    val compared = summary?.compared_to_previous
                    val change = compared?.change_percent
                    if (change != null) {
                        val up = change >= 0
                        Spacer(modifier = Modifier.height(dimen_10))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(dimen_6)).background((if (up) SuccessGreen else Red).copy(alpha = 0.16f))
                                    .padding(horizontal = dimen_6, vertical = dimen_2),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = if (up) SuccessGreen else Red, modifier = Modifier.size(dimen_12))
                                Textview(
                                    (if (up) "+" else "-") + String.format(Locale.US, "%.0f", abs(change)) + "%",
                                    size = font_11, color = if (up) SuccessGreen else Red, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(dimen_8))
                            Textview(stringResource(R.string.oe_compared_to, compared.label.orEmpty()), size = font_11, color = appColors.textSecondary)
                        }
                    }
                }
            }
        }

        // Four period tiles in one row
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf(
                Triple("today", R.string.oe_today, Icons.Default.Schedule to AmberPrimary),
                Triple("week", R.string.oe_this_week, Icons.Default.DateRange to com.cranezo.ui.mainhome.components.InfoBlue),
                Triple("month", R.string.oe_this_month, Icons.Default.CalendarMonth to SuccessGreen),
                Triple("all", R.string.oe_total_earnings, Icons.Default.BarChart to androidx.compose.ui.graphics.Color(0xFFA855F7))
            ).forEach { (key, label, look) ->
                val card = summary?.cards?.get(key)
                val selected = key == state.period
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .glassEffect(RoundedCornerShape(dimen_12), inset = true, borderColor = if (selected) AmberPrimary else androidx.compose.ui.graphics.Color.Unspecified)
                        .clickable { onIntent(OwnerEarningsIntent.SelectPeriod(key)) }
                        .padding(horizontal = dimen_8, vertical = dimen_10)
                ) {
                    Box(
                        modifier = Modifier.size(dimen_24).clip(RoundedCornerShape(dimen_6)).background(look.second.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(look.first, contentDescription = null, tint = look.second, modifier = Modifier.size(dimen_14)) }
                    Spacer(modifier = Modifier.height(dimen_6))
                    Textview(stringResource(label), size = font_10, color = appColors.textSecondary, maxLines = 1)
                    Textview(rupees(card?.net ?: 0.0), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                    Textview(stringResource(R.string.oe_n_jobs, card?.jobs ?: 0), size = font_10, color = appColors.textSecondary, maxLines = 1)
                }
            }
        }

        // Breakdown of the selected period
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(stringResource(R.string.oe_breakdown), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                Spacer(modifier = Modifier.width(dimen_4))
                Textview(periodText(summary?.period), size = font_10, color = appColors.textSecondary)
            }
            Spacer(modifier = Modifier.height(dimen_12))
            BreakdownRow(stringResource(R.string.oe_gross), rupees(earnings?.gross_earnings))
            BreakdownRow(stringResource(R.string.oe_platform_fee, (earnings?.platform_fee_percent ?: 0.0).toInt()), "- ${rupees(earnings?.platform_fee)}")
            BreakdownRow(stringResource(R.string.oe_other_deductions), "- ${rupees(earnings?.other_deductions ?: 0.0)}", info = true)
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_8).height(dimen_1).background(appColors.textHint.copy(alpha = 0.25f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(stringResource(R.string.oe_net), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Textview(rupees(earnings?.net_earnings), size = font_20, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }

        // Settlement status (all time)
        SectionHeader(stringResource(R.string.oe_settlement_status)) {
            context.startActivity(com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.PLATFORM))
        }
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            val settled = summary?.settlement?.settled
            val pending = summary?.settlement?.pending
            SettleCard(
                amount = settled?.amount ?: 0.0, label = stringResource(R.string.op_settled), color = SuccessGreen, icon = Icons.Default.CheckCircle,
                note = settled?.last_settled_at?.let { stringResource(R.string.oe_last_settled, formatServerDate(it)) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            SettleCard(
                amount = pending?.amount ?: 0.0, label = stringResource(R.string.oe_pending), color = AmberPrimary, icon = Icons.Default.Schedule,
                note = if (pending?.expected_by == null) null
                else if ((pending.expected_in_days ?: 0) <= 0) stringResource(R.string.oe_settlement_due)
                else stringResource(R.string.oe_settled_on, formatServerDate(pending.expected_by)),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        TrendCard(state, onIntent)

        // Recent completed jobs: the first 3, View All shows the whole list
        SectionHeader(stringResource(R.string.oe_recent_jobs), actionText = if (showAll) stringResource(R.string.oe_show_less) else null) { showAll = !showAll }
        (if (showAll) state.jobs else state.jobs.take(3)).forEach { job ->
            GlassCard(contentPadding = dimen_12, minHeight = dimen_80, onClick = { job.booking_id?.let(onOpenJob) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = job.crane?.primary_photo, modifier = Modifier.size(dimen_56))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(job.crane?.title.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview(listOfNotNull(job.crane?.crane_code, job.location).joinToString(" • "), size = font_10, color = appColors.textSecondary, maxLines = 1)
                        Textview(
                            listOfNotNull(formatServerDate(job.completed_at), job.duration_seconds?.let { hoursMinutes(it) }).joinToString(" • "),
                            size = font_10, color = appColors.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(rupees(job.amount), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(dimen_4))
                        val paid = job.settlement_status == "settled"
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(dimen_6)).background((if (paid) SuccessGreen else AmberPrimary).copy(alpha = 0.16f))
                                .padding(horizontal = dimen_6, vertical = dimen_2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(dimen_5).clip(androidx.compose.foundation.shape.CircleShape).background(if (paid) SuccessGreen else AmberPrimary))
                            Spacer(modifier = Modifier.width(dimen_4))
                            Textview(if (paid) stringResource(R.string.ow_paid) else job.settlement_label.orEmpty(), size = font_10, color = if (paid) SuccessGreen else AmberPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.width(dimen_4))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        if (state.loaded && state.jobs.isEmpty()) {
            Textview(stringResource(R.string.oe_no_jobs), size = font_12, color = appColors.textSecondary)
        }
        if (showAll && state.jobsHasMore) {
            LaunchedEffect(state.jobs.size) { onIntent(OwnerEarningsIntent.LoadMoreJobs) }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** Title on the left, "View All >" on the right */
@Composable
private fun SectionHeader(title: String, actionText: String? = null, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Textview(title, size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.clickable { onAction() }.padding(dimen_4), verticalAlignment = Alignment.CenterVertically) {
            Textview(actionText ?: stringResource(R.string.common_view_all), size = font_12, color = appColors.textSecondary, fontWeight = FontWeight.Medium)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_16))
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, info: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_4), verticalAlignment = Alignment.CenterVertically) {
        Textview(label, size = font_13, color = appColors.textSecondary)
        if (info) {
            Spacer(modifier = Modifier.width(dimen_4))
            Icon(Icons.Default.Info, contentDescription = null, tint = appColors.textHint, modifier = Modifier.size(dimen_14))
        }
        Spacer(modifier = Modifier.weight(1f))
        Textview(value, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
    }
}

/** Settled / Pending: icon in a circle, amount, label in its colour and a small note */
@Composable
private fun SettleCard(
    amount: Double, label: String, color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector, note: String?, modifier: Modifier
) {
    Row(
        modifier = modifier
            .glassEffect(RoundedCornerShape(dimen_14), backgroundColor = color.copy(alpha = 0.08f), borderColor = color.copy(alpha = 0.7f))
            .padding(dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(dimen_36).clip(androidx.compose.foundation.shape.CircleShape).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(dimen_20))
        }
        Spacer(modifier = Modifier.width(dimen_10))
        Column {
            Textview(rupees(amount), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Textview(label, size = font_12, color = color, fontWeight = FontWeight.SemiBold)
            note?.let { Textview(it, size = font_10, color = appColors.textSecondary, maxLines = 2) }
        }
    }
}

private fun hoursMinutes(seconds: Long): String {
    val m = seconds / 60
    return if (m >= 60) "${m / 60}h ${m % 60}m" else "${m}m"
}

/** "Wed, 17 Sep 2025" for today, otherwise the period */
@Composable
private fun heroDate(period: EarningsPeriod?): String =
    if (period?.key == null || period.key == "today") java.text.SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).format(java.util.Date())
    else periodText(period)

/** "17 Sep 2025", a range, or "All time". */
@Composable
private fun periodText(period: EarningsPeriod?): String {
    if (period == null) return ""
    if (period.from == null) return stringResource(R.string.oe_all_time)
    val from = formatServerDate(period.from)
    val to = formatServerDate(period.to)
    return if (from == to) from else "$from - $to"
}

private fun shortAmount(v: Double): String = when {
    v >= 100_000 -> String.format(Locale.US, "%.1fL", v / 100_000).replace(".0L", "L")
    v >= 1000 -> "${(v / 1000).toInt()}K"
    else -> v.toInt().toString()
}

// ---------------------------------------------------------------------------------------------
// Earnings Trend: amber bars, the peak highlighted with a bubble, amounts on the left
// ---------------------------------------------------------------------------------------------

@Composable
private fun TrendCard(state: OwnerEarningsState, onIntent: (OwnerEarningsIntent) -> Unit) {
    val trend = state.trend
    val points = trend?.points.orEmpty()

    GlassCard(contentPadding = dimen_14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
            Spacer(modifier = Modifier.width(dimen_6))
            Textview(stringResource(R.string.oe_trend), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            // segmented Week / Month / Year
            Row(
                modifier = Modifier.clip(RoundedCornerShape(dimen_10)).background(appColors.subtleFill).padding(dimen_2)
            ) {
                listOf("week" to R.string.oe_range_week, "month" to R.string.oe_range_month, "year" to R.string.oe_range_year).forEach { (key, label) ->
                    val selected = key == state.range
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(dimen_8))
                            .background(if (selected) AmberPrimary.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent)
                            .border(dimen_1, if (selected) AmberPrimary else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(dimen_8))
                            .clickable { onIntent(OwnerEarningsIntent.SelectRange(key)) }
                            .padding(horizontal = dimen_10, vertical = dimen_4)
                    ) {
                        Textview(stringResource(label), size = font_11, color = if (selected) AmberPrimary else appColors.textSecondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_12))

        val peak = trend?.peak
        if (peak == null || points.isEmpty()) {
            Textview(stringResource(R.string.oe_no_trend), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(vertical = dimen_24))
            return@GlassCard
        }

        val max = (points.maxOfOrNull { it.net ?: 0.0 } ?: 1.0).coerceAtLeast(1.0)
        val peakIndex = points.indexOfFirst { it.key == peak.key }.coerceAtLeast(0)
        val gridColor = appColors.textHint.copy(alpha = 0.18f)
        val chartHeight = dimen_140
        Row {
            // amounts on the left: max, 3/4, 1/2, 1/4, 0
            Column(modifier = Modifier.height(chartHeight).padding(top = dimen_36), verticalArrangement = Arrangement.SpaceBetween) {
                listOf(1.0, 0.75, 0.5, 0.25, 0.0).forEach { f -> Textview(shortAmount(max * f), size = font_9_sp, color = appColors.textSecondary) }
            }
            Spacer(modifier = Modifier.width(dimen_6))
            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.weight(1f).height(chartHeight)) {
                val bubbleSpace = dimen_36
                val barsHeight = maxHeight - bubbleSpace
                Canvas(modifier = Modifier.fillMaxWidth().height(barsHeight).align(Alignment.BottomStart)) {
                    // grid lines
                    for (i in 0..4) {
                        val y = size.height * i / 4f
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }
                    val slot = size.width / points.size
                    val barWidth = slot * 0.55f
                    points.forEachIndexed { i, p ->
                        val h = (size.height * ((p.net ?: 0.0) / max)).toFloat().coerceAtLeast(2f)
                        val isPeak = i == peakIndex
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(AmberPrimary.copy(alpha = if (isPeak) 1f else 0.9f), AmberPrimary.copy(alpha = if (isPeak) 0.7f else 0.35f)),
                                startY = size.height - h, endY = size.height
                            ),
                            topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        if (isPeak) {
                            drawCircle(AmberPrimary, radius = 5f, center = Offset(i * slot + slot / 2, size.height - h))
                        }
                    }
                }
                // bubble over the peak bar
                val slotDp = maxWidth / points.size
                val peakTop = bubbleSpace + barsHeight * (1f - ((peak.net ?: 0.0) / max).toFloat())
                val bubbleWidth = dimen_80
                val x = (slotDp * peakIndex + slotDp / 2 - bubbleWidth / 2).coerceIn(0.dp, maxWidth - bubbleWidth)
                Column(
                    modifier = Modifier
                        .offset(x = x, y = (peakTop - dimen_40).coerceAtLeast(0.dp))
                        .width(bubbleWidth)
                        .clip(RoundedCornerShape(dimen_8))
                        .background(appColors.bgBase)
                        .border(dimen_1, AmberPrimary, RoundedCornerShape(dimen_8))
                        .padding(vertical = dimen_3),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Textview(rupees(peak.net), size = font_11, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                    Textview(peak.label.orEmpty(), size = font_9_sp, color = appColors.textSecondary, maxLines = 1)
                }
            }
        }
        // labels under the bars, every few so they stay readable
        val step = ((points.size + 5) / 6).coerceAtLeast(1)
        Row(modifier = Modifier.fillMaxWidth().padding(start = dimen_28, top = dimen_4), horizontalArrangement = Arrangement.SpaceBetween) {
            points.filterIndexed { i, _ -> i % step == 0 }.forEach { Textview(it.label.orEmpty(), size = font_10, color = appColors.textSecondary) }
        }
    }
}

private val font_9_sp = 9.sp

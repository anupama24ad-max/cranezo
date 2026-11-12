package com.cranezo.ui.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.myprofile.components.MyProfileScaffold
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.cranezo.ui.components.GlassCard
import com.cranezo.utils.compactMinutes
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.serverMillis
import kotlinx.coroutines.delay
import com.cranezo.ui.theme.*

/** Frame of every Book a Crane screen. [step] 1..5 shows the progress line under the title. */
@Composable
fun BookingScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.bk_title),
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    step: Int? = null,
    bottom: (@Composable () -> Unit)? = null,
    success: Boolean = false,
    followKeyboard: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    MyProfileScaffold(
        followKeyboard = followKeyboard,
        success = success,
        title = title,
        subtitle = subtitle,
        action = action,
        onBack = onBack,
        modifier = modifier,
        bottom = bottom,
        topContent = if (step != null) {
            { StepIndicator(step) }
        } else null,
        content = content
    )
}

@Composable
fun StepIndicator(current: Int) {
    val labels = listOf(
        R.string.bk_step_type, R.string.bk_step_capacity, R.string.bk_step_location,
        // "When" includes how long: 4 steps
        R.string.bk_step_date_time
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimen_16, vertical = dimen_8),
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { index, label ->
            val number = index + 1
            val reached = number <= current
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(dimen_1)
                            .background(if (index > 0 && reached) AmberPrimary else if (index > 0) appColors.subtleBorder else Color.Transparent)
                    )
                    Box(
                        modifier = Modifier
                            .size(dimen_22)
                            .clip(CircleShape)
                            .background(if (reached) AmberPrimary else appColors.subtleFill),
                        contentAlignment = Alignment.Center
                    ) {
                        Textview(
                            text = number.toString(),
                            size = font_11,
                            color = if (reached) Color.Black else appColors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(dimen_1)
                            .background(
                                if (index < labels.size - 1 && number < current) AmberPrimary
                                else if (index < labels.size - 1) appColors.subtleBorder else Color.Transparent
                            )
                    )
                }
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = stringResource(label),
                    size = font_10,
                    color = if (number == current) AmberPrimary else appColors.textSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** "Back" and "Continue" side by side. [onBack] null leaves only the main button. */
@Composable
fun BottomButtons(
    mainText: String,
    onMain: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
        if (onBack != null) {
            CommonGradientButton(
                text = stringResource(R.string.common_back),
                modifier = Modifier.weight(0.4f),
                height = dimen_52,
                onClick = onBack,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill),
                textColor = appColors.textPrimary
            )
        }
        CommonGradientButton(
            text = mainText,
            modifier = Modifier.weight(0.6f),
            height = dimen_52,
            onClick = onMain,
            enabled = enabled
        )
    }
}

/** Amber check in a circle, shown on the chosen card. */
@Composable
fun SelectedCheck(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimen_24)
            .clip(CircleShape)
            .background(AmberPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(dimen_14))
    }
}

/** Glass card with an amber border when [selected]. */
@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(if (selected) appColors.primaryTint else Color.Transparent, RoundedCornerShape(dimen_16))
            .glassEffect(
                shape = RoundedCornerShape(dimen_16),
                borderColor = if (selected) AmberPrimary else Color.Unspecified,
                borderWidth = 1.5.dp
            )
            .clip(RoundedCornerShape(dimen_16))
            .clickable { onClick() },
        content = content
    )
}

/** One line of the price breakdown: label on the left, amount (or "Included") on the right. */
@Composable
fun PriceLine(label: String, value: String, bold: Boolean = false, valueColor: Color = appColors.textPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimen_3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Textview(
            text = label,
            size = if (bold) font_14 else font_12,
            color = if (bold) appColors.textPrimary else appColors.textSecondary,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Textview(
            text = value,
            size = if (bold) font_18 else font_13,
            color = if (bold) AmberPrimary else valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Status / info pill such as "Completed", "Verified" or "CRN018": a rounded pill with a soft tinted fill, a thin
 * border in the same colour, room around the text and one line only, so neighbouring words never touch it.
 */
@Composable
fun BookingTag(text: String, color: Color, modifier: Modifier = Modifier) {
    val pill = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(pill)
            .background(color.copy(alpha = 0.14f))
            .border(dimen_1, color.copy(alpha = 0.38f), pill)
            .padding(horizontal = dimen_10, vertical = dimen_4),
        contentAlignment = Alignment.Center
    ) {
        Textview(text = text, size = font_11, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

private fun Modifier.androidxHeightIn(min: androidx.compose.ui.unit.Dp): Modifier = this.heightIn(min = min)

/**
 * The work clock of a job: how long the crane has really worked (from "Start job", hold time not counted),
 * ticking every second, against the booked hours. No "time left": the real time of work is what counts, and
 * time beyond the booked hours is shown in red (it is charged at the booking's hourly rate).
 * Before the work starts it shows the booked hours and window.
 * [timers] come from the server (working_seconds at server time); the clock adds the time since they arrived.
 * Used by the customer (Job In Progress), owner (job monitoring) and operator (step 4).
 */
@Composable
fun FixedHoursCard(
    startAt: String?,
    endAt: String?,
    durationLabel: String?,
    modifier: Modifier = Modifier,
    timers: com.cranezo.ui.operator.model.JobTimers? = null
) {
    val started = timers?.work_started_at != null
    val running = started && timers?.work_ended_at == null && timers?.is_waiting != true
    // when these numbers arrived: the clock counts on from here
    val receivedAt = remember(timers?.working_seconds, timers?.is_waiting, timers?.work_ended_at) { System.currentTimeMillis() }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(running) {
        while (running) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val start = serverMillis(startAt)
    val end = serverMillis(endAt)
    // booked seconds: the booked window (it already includes approved extra time)
    val bookedSeconds = if (start != null && end != null && end > start) (end - start) / 1000L else null
    val worked = (timers?.working_seconds ?: 0L) + if (running) ((now - receivedAt) / 1000L).coerceAtLeast(0L) else 0L
    val overSeconds = bookedSeconds?.let { worked - it } ?: 0L
    val over = started && overSeconds > 0
    val progress = if (bookedSeconds != null && bookedSeconds > 0) (worked.toFloat() / bookedSeconds).coerceIn(0f, 1f) else 0f

    GlassCard(modifier = modifier, contentPadding = dimen_20) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (started) {
                Textview(stringResource(R.string.fh_work_time), size = font_13, color = appColors.textSecondary)
                Textview(clock(worked), size = 36.sp, color = if (over) Red else appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(
                    stringResource(R.string.fh_of_booked, durationLabel ?: "-"),
                    size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                )
            } else {
                Textview(stringResource(R.string.fh_booked_hours), size = font_13, color = appColors.textSecondary)
                Textview(durationLabel ?: "-", size = 32.sp, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(
                    "${formatServerTime(startAt)}  –  ${formatServerTime(endAt)}",
                    size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (started) {
            Spacer(modifier = Modifier.height(dimen_16))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(dimen_8).clip(CircleShape),
                color = if (over) Red else AmberPrimary, trackColor = appColors.subtleFill
            )
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(
                text = when {
                    over -> stringResource(R.string.fh_over_by, clock(overSeconds))
                    timers?.is_waiting == true -> stringResource(R.string.fh_clock_paused)
                    else -> stringResource(R.string.fh_started_at, formatServerTime(timers?.work_started_at))
                },
                size = font_14, color = if (over) Red else appColors.textSecondary, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** 2:05:09 / 05:09 */
private fun clock(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0L)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, sec) else String.format(java.util.Locale.US, "%02d:%02d", m, sec)
}

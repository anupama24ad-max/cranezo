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
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    MyProfileScaffold(
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
        R.string.bk_step_date_time, R.string.bk_step_duration
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
                    size = font_8,
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

/** Small outlined tag such as "Available" or "Verified". */
@Composable
fun BookingTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimen_8))
            .background(color.copy(alpha = 0.16f))
            .border(dimen_1, color.copy(alpha = 0.5f), RoundedCornerShape(dimen_8))
            .padding(horizontal = dimen_6, vertical = dimen_2)
    ) {
        Textview(text = text, size = font_10, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

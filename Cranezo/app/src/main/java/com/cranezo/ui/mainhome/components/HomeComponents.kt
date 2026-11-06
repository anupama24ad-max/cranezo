package com.cranezo.ui.mainhome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cranezo.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.theme.*
import java.util.Calendar

/** "Good morning," / "Good afternoon," / "Good evening," in the chosen language */
@Composable
fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}

fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

val SuccessGreen = Color(0xFF00C853)
val InfoBlue = Color(0xFF3B82F6)

@Composable
fun HomeHeader(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showNotification: Boolean = true,
    // Unread notifications: the dot is hidden at 0; null keeps the dot (screens without the number)
    unread: Int? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Textview(text = greeting(), size = font_11, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_2))
            Textview(
                text = name,
                size = font_22,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(dimen_2))
            Textview(text = subtitle, size = font_11, color = appColors.textSecondary)
        }
        if (showNotification) {
            val context = androidx.compose.ui.platform.LocalContext.current
            // the bell keeps its own count fresh: when shown, every 30 seconds and when a push arrives
            androidx.compose.runtime.LaunchedEffect(Unit) {
                while (true) {
                    com.cranezo.push.UnreadBadge.refresh(context)
                    kotlinx.coroutines.delay(30_000)
                }
            }
            val badge = unread ?: com.cranezo.push.UnreadBadge.count.value
            Box(
                modifier = Modifier
                    .size(dimen_40)
                    .glassEffect(CircleShape)
                    .clickable { context.startActivity(com.cranezo.ui.support.SupportActivity.intent(context, com.cranezo.ui.support.SupportRoutes.NOTIFICATIONS)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.common_notifications),
                    tint = AmberPrimary,
                    modifier = Modifier.size(dimen_20)
                )
                // Unread count: hidden when there is nothing unread
                if ((badge ?: 0) > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = dimen_2, end = dimen_2)
                            .size(dimen_16)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Textview(if ((badge ?: 0) > 9) "9+" else badge.toString(), size = font_10, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(dimen_18)
            )
            Spacer(modifier = Modifier.width(dimen_8))
        }
        Column(modifier = Modifier.weight(1f)) {
            Textview(
                text = text,
                size = font_14,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Textview(text = subtitle, size = font_11, color = appColors.textSecondary)
            }
        }
        if (actionText != null) {
            Textview(
                text = actionText,
                size = font_12,
                color = AmberPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

/** Rounded square holding an icon, tinted by [accent]. */
@Composable
fun IconBadge(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = dimen_32,
    // A solid accent circle with a dark icon, e.g. the "+" of Add Crane
    filled: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(if (filled) CircleShape else RoundedCornerShape(dimen_8))
            .background(
                if (filled) Brush.verticalGradient(listOf(LightYellow, DarkYellow))
                else Brush.verticalGradient(listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.12f)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (filled) Color.Black else accent,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

/** Number + label tile, e.g. "05 / Total Cranes". */
@Composable
fun StatTile(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    // Sits on the Fleet Overview glass card, so it is the darker inset level
    Row(
        modifier = modifier
            .glassEffect(RoundedCornerShape(dimen_12), inset = true)
            .padding(horizontal = dimen_10, vertical = dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, accent = accent, size = dimen_36)
        Spacer(modifier = Modifier.width(dimen_10))
        Column {
            Textview(
                text = value,
                size = font_18,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Textview(text = label, size = font_10, color = appColors.textSecondary, maxLines = 2)
        }
    }
}

/** Card with a title, a big value and a caption, e.g. "Earnings / ₹24,500 / Today's earnings". */
@Composable
fun MetricCard(
    title: String,
    value: String,
    caption: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(dimen_16),
        contentPadding = dimen_12,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, accent = accent, size = dimen_28)
            Spacer(modifier = Modifier.width(dimen_8))
            Textview(
                text = title,
                size = font_11,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(dimen_14))
        Textview(
            text = value,
            size = font_22,
            color = appColors.textPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Textview(text = caption, size = font_10, color = appColors.textSecondary)
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .glassEffect(
                shape = RoundedCornerShape(dimen_12),
                backgroundColor = if (highlighted) AmberPrimary.copy(alpha = 0.12f) else Color.Unspecified,
                borderColor = if (highlighted) AmberPrimary else Color.Unspecified,
                inset = !highlighted
            )
            .clickable { onClick() }
            .padding(horizontal = dimen_10, vertical = dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, accent = accent, size = dimen_36, filled = highlighted)
        Spacer(modifier = Modifier.width(dimen_10))
        Column {
            Textview(
                text = title,
                size = font_13,
                color = appColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Textview(text = subtitle, size = font_10, color = appColors.textSecondary, maxLines = 2)
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimen_10))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = dimen_8, vertical = dimen_3),
        contentAlignment = Alignment.Center
    ) {
        Textview(text = text, size = font_11, color = color, fontWeight = FontWeight.SemiBold)
    }
}

/** Small outlined glass button, e.g. "View Job". */
@Composable
fun GlassTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .glassEffect(
                shape = RoundedCornerShape(dimen_10),
                borderColor = AmberPrimary
            )
            .clickable { onClick() }
            .padding(horizontal = dimen_14, vertical = dimen_6),
        contentAlignment = Alignment.Center
    ) {
        Textview(text = text, size = font_12, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TwoColumnRow(
    modifier: Modifier = Modifier,
    left: @Composable (Modifier) -> Unit,
    right: @Composable (Modifier) -> Unit
) {
    // both cards of a row get the same height, like the design
    Row(
        modifier = modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        left(Modifier.weight(1f).fillMaxHeight())
        right(Modifier.weight(1f).fillMaxHeight())
    }
}

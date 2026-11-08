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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
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
    // small text after the name, e.g. the customer ID "#100001"
    tag: String? = null,
    // Unread notifications: the dot is hidden at 0; null keeps the dot (screens without the number)
    unread: Int? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Textview(text = greeting(), size = font_14, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_2))
            Row(verticalAlignment = Alignment.Bottom) {
                Textview(
                    text = name,
                    size = font_22,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!tag.isNullOrBlank()) {
                    Textview(
                        text = " $tag",
                        size = font_12,
                        color = appColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.padding(start = dimen_2, bottom = dimen_3)
                    )
                }
            }
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
            // outer box is not clipped, so the count sits on the edge of the round button, not inside it
            Box {
                Box(
                    modifier = Modifier
                        .size(dimen_40)
                        .glassEffect(CircleShape)
                        .clickable {
                            context.startActivity(
                                com.cranezo.ui.support.SupportActivity.intent(
                                    context,
                                    com.cranezo.ui.support.SupportRoutes.NOTIFICATIONS
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.common_notifications),
                        tint = AmberPrimary,
                        modifier = Modifier.size(dimen_20)
                    )
                }
                // Unread count: hidden when there is nothing unread
                if ((badge ?: 0) > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = dimen_6, y = (-6).dp)
                            .size(dimen_18)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFEF4444))
                            .border(dimen_1, androidx.compose.ui.graphics.Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Textview(
                            if ((badge ?: 0) > 9) "9+" else badge.toString(),
                            size = 9.sp,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Curved navy header at the top of a home screen, the same in light and dark mode.
 * It runs edge to edge with rounded bottom corners. Everything inside is drawn with the dark
 * tokens, so text and the bell stay readable on the navy.
 */
@Composable
fun CurvedHero(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = dimen_24,
    content: @Composable () -> Unit
) {
    val colors = appColors
    // the navy sits behind the status bar, so its icons must be light while this header is shown
    androidx.compose.runtime.DisposableEffect(Unit) {
        com.cranezo.core.composecore.HeroStatusBar.show()
        onDispose { com.cranezo.core.composecore.HeroStatusBar.hide() }
    }
    val shape = RoundedCornerShape(bottomStart = dimen_32, bottomEnd = dimen_32)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(colors.heroTop, colors.heroBottom)))
            .drawBehind {
                // amber glow in the top right corner
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(colors.heroGlow.copy(alpha = 0.32f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.75f
                    )
                )
            }
            // the navy is drawn first, then this pushes the content below the status bar
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = dimen_20, end = dimen_20, top = dimen_16, bottom = bottomPadding)
    ) {
        CompositionLocalProvider(LocalAppColors provides DarkAppColors) { content() }
    }
}

/**
 * Scrolling home screen: the curved navy [hero] on top, then the [content] with side padding.
 * The first card slides over the bottom of the hero.
 */
@Composable
fun HomeScreenColumn(
    spacing: Dp,
    hero: @Composable () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        CurvedHero { hero() }
        Column(
            modifier = Modifier
                .padding(horizontal = dimen_20)
                .padding(top = dimen_16, bottom = dimen_16),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content
        )
    }
}

/** Pulls the content up by [amount] so it overlaps the bottom of the hero above it */
fun Modifier.pullUp(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val px = amount.roundToPx()
    layout(placeable.width, placeable.height - px) { placeable.placeRelative(0, -px) }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) = SectionTitleContent(
    text = text,
    modifier = modifier,
    subtitle = subtitle,
    icon = icon?.let { rememberVectorPainter(it) },
    actionText = actionText,
    onActionClick = onActionClick
)

/** Same as above, with a drawable resource as the icon, e.g. `icon = R.drawable.fleet_ic`. */
@Composable
fun SectionTitle(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) = SectionTitleContent(
    text = text,
    modifier = modifier,
    subtitle = subtitle,
    icon = painterResource(icon),
    actionText = actionText,
    onActionClick = onActionClick
)

@Composable
private fun SectionTitleContent(
    text: String,
    modifier: Modifier,
    subtitle: String?,
    icon: Painter?,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                painter = icon,
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
) = IconBadge(
    painter = rememberVectorPainter(icon),
    accent = accent,
    modifier = modifier,
    size = size,
    filled = filled
)

/**
 * Same badge with a drawable, e.g. `IconBadge(iconRes = R.drawable.ic_crane, accent = AmberPrimary)`.
 * Set [tinted] = false for coloured images (PNG / multi-colour vectors) so they keep their own colours.
 */
@Composable
fun IconBadge(
    @DrawableRes iconRes: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = dimen_32,
    filled: Boolean = false,
    tinted: Boolean = true,
    isBorder: Boolean = false,
) = IconBadge(
    painter = painterResource(iconRes),
    accent = accent,
    modifier = modifier,
    size = size,
    filled = filled,
    tinted = tinted,
    isBorder = isBorder,
)

@Composable
fun IconBadge(
    painter: Painter,
    accent: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = dimen_32,
    filled: Boolean = false,
    tinted: Boolean = true,
    isBorder: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(if (filled) CircleShape else RoundedCornerShape(dimen_8))
            .background(
                if (filled) Brush.verticalGradient(listOf(LightYellow, DarkYellow))
                else Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.22f),
                        accent.copy(alpha = 0.12f)
                    )
                )
            )
            .then(
                if (isBorder) {
                    Modifier.border(
                        width =dimen_1,
                        color = accent.copy(alpha = 0.5f),
                        shape = if (filled) CircleShape else RoundedCornerShape(dimen_8)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = when {
                !tinted -> Color.Unspecified
                filled -> Color.Black
                else -> accent
            },
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

/** Number + label tile, e.g. "05 / Total Cranes". */
@Composable
fun StatTile(
    value: String,
    label: String,
    icon: Int,
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
        IconBadge(iconRes = icon, accent = accent, size = dimen_36)
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
    @DrawableRes icon: Int,
    accent: Color,
    isBorder: Boolean = false,
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
            IconBadge(iconRes = icon, accent = accent, size = dimen_28, isBorder = isBorder)
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
    @DrawableRes  icon:  Int,
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
        IconBadge(iconRes = icon, accent = accent, size = dimen_36, filled = highlighted)
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
        Textview(
            text = text,
            size = font_12,
            color = appColors.primaryText,
            fontWeight = FontWeight.SemiBold
        )
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
        modifier = modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        left(Modifier
            .weight(1f)
            .fillMaxHeight())
        right(Modifier
            .weight(1f)
            .fillMaxHeight())
    }
}

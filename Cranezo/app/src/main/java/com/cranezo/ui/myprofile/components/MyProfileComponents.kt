package com.cranezo.ui.myprofile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.components.rememberImeScrollState
import com.cranezo.ui.cranes.components.CraneScreenHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.myprofile.model.DocStatus
import com.cranezo.ui.theme.*

/**
 * Frame of every My Profile screen: background, header with back button, scrolling content and an
 * optional button pinned under it (it moves up with the keyboard).
 */
@Composable
fun MyProfileScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    bottom: (@Composable () -> Unit)? = null,
    topContent: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    success: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        // Success screens: soft green shade from the top fading into the page
        if (success) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF22C55E).copy(alpha = if (appColors.isDark) 0.30f else 0.26f), Color(0xFF22C55E).copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            CraneScreenHeader(title = title, subtitle = subtitle, onBack = onBack, action = action)
            topContent?.invoke()
            // breathing room between the header and the page on every screen
            Spacer(modifier = Modifier.height(dimen_12))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberImeScrollState())
                    .padding(horizontal = dimen_20),
                verticalArrangement = Arrangement.spacedBy(dimen_12),
                content = content
            )
            if (bottom != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, appColors.bgBase.copy(alpha = 0.85f))))
                        .padding(horizontal = dimen_20, vertical = dimen_12)
                ) { bottom() }
            }
        }
    }
}

/** Small grey caption above a group of rows, e.g. "ACCOUNT". */
@Composable
fun GroupLabel(text: String) {
    Textview(
        text = text.uppercase(),
        size = font_11,
        color = appColors.textSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = dimen_4, top = dimen_4)
    )
}

/** A glass card that holds menu rows one under the other. */
@Composable
fun MenuGroup(content: @Composable ColumnScope.() -> Unit) {
    GlassCard(contentPadding = dimen_4) {
        val scope = this
        val lineColor = appColors.textHint.copy(alpha = 0.28f)
        val lines = androidx.compose.runtime.remember { MenuLines() }
        // rows stacked like a Column, with a thin line between every two rows (after the icon)
        androidx.compose.ui.layout.Layout(
            content = { scope.content() },
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val start = dimen_58.toPx()
                    lines.ys.forEach { y ->
                        drawLine(lineColor, Offset(start, y.toFloat()), Offset(size.width - dimen_12.toPx(), y.toFloat()), strokeWidth = dimen_1.toPx())
                    }
                }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minHeight = 0)) }
            val height = placeables.sumOf { it.height }
            layout(constraints.maxWidth, height) {
                var y = 0
                val ys = mutableListOf<Int>()
                val visible = placeables.filter { it.height > 0 }
                visible.forEachIndexed { i, p ->
                    p.placeRelative(0, y)
                    y += p.height
                    if (i < visible.size - 1) ys += y
                }
                lines.ys = ys
            }
        }
    }
}

/** Where the lines between menu rows go (filled in during layout, read when drawing) */
private class MenuLines { var ys: List<Int> = emptyList() }

/** One row of a menu: icon, title, optional subtitle and a trailing value or chevron. */
@Composable
fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingText: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimen_16))
            .clickable { onClick() }
            .padding(horizontal = dimen_10, vertical = dimen_10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, accent = AmberPrimary, size = dimen_36)
        Spacer(modifier = Modifier.width(dimen_12))
        Column(modifier = Modifier.weight(1f)) {
            Textview(
                text = title,
                size = font_14,
                color = appColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (subtitle != null) {
                Textview(text = subtitle, size = font_11, color = appColors.textSecondary, maxLines = 2)
            }
        }
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(dimen_8))
            Textview(
                text = trailingText,
                size = font_12,
                color = AmberPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        trailing?.let {
            Spacer(modifier = Modifier.width(dimen_8))
            it()
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = appColors.textSecondary,
            modifier = Modifier.size(dimen_20)
        )
    }
}

/**
 * Badge of a document, Aadhar or PAN: Verified (green), Pending (amber), Rejected (red) and, when
 * [status] is null, Not Uploaded (grey). [serverLabel] is only used for a status this app doesn't know.
 */
@Composable
fun StatusBadge(
    status: Int?,
    modifier: Modifier = Modifier,
    serverLabel: String? = null
) {
    val (color, icon, label) = when (status) {
        DocStatus.VERIFIED -> Triple(SuccessGreen, Icons.Default.CheckCircle, stringResource(R.string.mp_status_verified))
        DocStatus.PENDING -> Triple(AmberPrimary, Icons.Default.Schedule, stringResource(R.string.mp_status_pending))
        DocStatus.REJECTED -> Triple(Red, Icons.Default.Cancel, stringResource(R.string.mp_status_rejected))
        null -> Triple(appColors.textSecondary, Icons.Default.UploadFile, stringResource(R.string.mp_status_not_uploaded))
        else -> Triple(AmberPrimary, Icons.Default.Schedule, serverLabel.orEmpty())
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimen_12))
            .background(color.copy(alpha = 0.14f))
            .border(dimen_1, color.copy(alpha = 0.55f), RoundedCornerShape(dimen_12))
            .padding(horizontal = dimen_8, vertical = dimen_3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(dimen_12))
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(text = label, size = font_11, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Selectable glass row of the Language and Appearance lists, with the amber check when selected. */
@Composable
fun ChoiceRow(
    title: String,
    description: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(
                shape = RoundedCornerShape(dimen_16),
                borderColor = if (selected) AmberPrimary else Color.Unspecified
            )
            .clickable { onClick() }
            .padding(dimen_14),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.let {
            it()
            Spacer(modifier = Modifier.width(dimen_12))
        }
        Column(modifier = Modifier.weight(1f)) {
            Textview(
                text = title,
                size = font_15,
                color = appColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (!description.isNullOrBlank()) {
                Textview(text = description, size = font_12, color = appColors.textSecondary)
            }
        }
        Box(
            modifier = Modifier
                .size(dimen_22)
                .clip(CircleShape)
                .background(if (selected) AmberPrimary else Color.Transparent)
                .border(dimen_1, if (selected) AmberPrimary else appColors.subtleBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(dimen_14)
                )
            }
        }
    }
}

/** "Are you sure you want to logout?" with Cancel / Logout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutConfirmSheet(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = dimen_20)
                .padding(bottom = dimen_16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Textview(
                text = stringResource(R.string.mp_logout_title),
                size = font_20,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(
                text = stringResource(R.string.mp_logout_message),
                size = font_14,
                color = appColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimen_20))
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                CommonGradientButton(
                    text = stringResource(R.string.common_cancel),
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill),
                    textColor = appColors.textPrimary
                )
                CommonGradientButton(
                    text = stringResource(R.string.mp_logout),
                    modifier = Modifier.weight(1f),
                    onClick = onConfirm,
                    gradientColors = listOf(Red, DarkRed, Maroon),
                    textColor = Color.White
                )
            }
        }
    }
}

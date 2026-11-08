package com.cranezo.ui.cranes.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.model.ApprovalStatus
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl

// Pieces shared by the crane screens (My Cranes list, Crane Details, Upload Document, Crane Submitted).

fun approvalColor(status: Int?): Color = when (status) {
    ApprovalStatus.VERIFIED -> SuccessGreen
    ApprovalStatus.REJECTED -> Red
    else -> AmberPrimary
}

private fun approvalIcon(status: Int?): ImageVector = when (status) {
    ApprovalStatus.VERIFIED -> Icons.Default.VerifiedUser
    ApprovalStatus.REJECTED -> Icons.Default.Cancel
    else -> Icons.Default.Schedule
}

/** Text of the badge: the API's approval_label, with a fallback for the three known statuses. */
@Composable
fun approvalLabel(crane: Crane): String =
    crane.approval_label?.takeIf { it.isNotBlank() } ?: stringResource(
        when (crane.approval_status) {
            ApprovalStatus.VERIFIED -> R.string.crane_status_verified
            ApprovalStatus.REJECTED -> R.string.crane_status_rejected
            else -> R.string.crane_status_under_review
        }
    )

/** Outlined badge: "Verified" (green), "Under Review" (amber) or "Rejected" (red). */
@Composable
fun CraneStatusBadge(
    status: Int?,
    label: String,
    modifier: Modifier = Modifier
) {
    val color = approvalColor(status)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimen_10))
            .background(color.copy(alpha = 0.14f))
            .border(dimen_1, color.copy(alpha = 0.55f), RoundedCornerShape(dimen_10))
            .padding(horizontal = dimen_8, vertical = dimen_3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = approvalIcon(status),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(dimen_12)
        )
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(text = label, size = font_11, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** A crane photo from the API path, with a crane icon while it loads or when there is none. */
@Composable
fun CraneImage(
    path: String?,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(dimen_12),
    /** Crop fills the box; Fit shows the whole picture */
    contentScale: ContentScale = ContentScale.Crop,
    /** Room around the picture inside the box (e.g. to keep it clear of a label strip) */
    imagePadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val url = fileUrl(path)
    var failed by remember(url) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(appColors.subtleFill),
        contentAlignment = Alignment.Center
    ) {
        // the icon is only for "no picture" or "picture failed"; it must not show through a loaded image
        if (url == null || failed) Icon(
            imageVector = Icons.Default.PrecisionManufacturing,
            contentDescription = null,
            tint = appColors.textHint,
            modifier = Modifier.size(dimen_28)
        )
        if (url != null) {
            ShimmerImage(
                model = url,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.matchParentSize().padding(imagePadding),
                onError = { failed = true }
            )
        }
    }
}

/** "Mobile Crane | 50 Ton", leaving out whichever part the API did not send. */
fun craneSummaryLine(crane: Crane): String =
    listOfNotNull(crane.crane_type_name, crane.capacity_name).filter { it.isNotBlank() }.joinToString("  |  ")

/** One card of the My Cranes list. */
@Composable
fun CraneListCard(
    crane: Crane,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(dimen_16),
        contentPadding = dimen_10,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CraneImage(
                path = crane.primary_photo ?: crane.photoUrls.firstOrNull(),
                modifier = Modifier.size(dimen_88)
            )
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(
                    text = crane.crane_code.orEmpty(),
                    size = font_16,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(dimen_2))
                Textview(
                    text = craneSummaryLine(crane),
                    size = font_12,
                    color = appColors.textSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(dimen_14)
                    )
                    Spacer(modifier = Modifier.width(dimen_2))
                    Textview(
                        text = crane.location.orEmpty(),
                        size = font_12,
                        color = appColors.textSecondary,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(dimen_8))
                CraneStatusBadge(crane.approval_status, approvalLabel(crane))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = appColors.textSecondary,
                modifier = Modifier.size(dimen_24)
            )
        }
    }
}

/**
 * Explains the crane's status. Under Review and Rejected cranes are not bookable, so they always get
 * a banner; a rejected one shows the reason prominently so the owner can fix it and save again.
 */
@Composable
fun CraneStatusBanner(
    crane: Crane,
    modifier: Modifier = Modifier
) {
    val status = crane.approval_status
    if (status == ApprovalStatus.VERIFIED) return

    val color = approvalColor(status)
    val rejected = status == ApprovalStatus.REJECTED
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(
                shape = RoundedCornerShape(dimen_14),
                backgroundColor = color.copy(alpha = 0.10f),
                borderColor = color.copy(alpha = 0.7f)
            )
            .padding(dimen_12)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = approvalIcon(status),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(dimen_18)
            )
            Spacer(modifier = Modifier.width(dimen_8))
            Textview(
                text = stringResource(if (rejected) R.string.crane_banner_rejected_title else R.string.crane_banner_review_title),
                size = font_14,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        val message = crane.status_message?.takeIf { it.isNotBlank() }
            ?: stringResource(if (rejected) R.string.crane_banner_rejected_default else R.string.crane_banner_review_default)
        Spacer(modifier = Modifier.height(dimen_6))
        Textview(text = message, size = font_12, color = appColors.textPrimary)

        if (rejected && !crane.rejection_reason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(dimen_8))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimen_10))
                    .background(Red.copy(alpha = 0.14f))
                    .padding(dimen_10)
            ) {
                Textview(
                    text = stringResource(R.string.crane_rejection_reason),
                    size = font_11,
                    color = Red,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(dimen_2))
                Textview(
                    text = crane.rejection_reason,
                    size = font_13,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_6))
        Textview(
            text = stringResource(R.string.crane_banner_not_bookable),
            size = font_11,
            color = appColors.textSecondary
        )
    }
}

/** Glass card with a numbered amber circle and a title, as in the Add Crane design. */
@Composable
fun NumberedSection(
    number: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimen_16),
        contentPadding = dimen_14
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(dimen_22)
                    .clip(CircleShape)
                    .border(dimen_1, AmberPrimary, CircleShape)
                    .background(AmberPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Textview(
                    text = number.toString(),
                    size = font_12,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(dimen_10))
            Column {
                Textview(text = title, size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Textview(text = subtitle, size = font_11, color = appColors.textSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_12))
        content()
    }
}

/** Header row used by the crane screens: back button, centred title and subtitle, optional action. */
@Composable
fun CraneScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimen_20, vertical = dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.cranezo.ui.components.CommonBackButton(onClick = onBack, size = dimen_36, iconSize = dimen_22)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimen_8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Textview(
                text = title,
                size = font_18,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (subtitle != null) {
                Textview(
                    text = subtitle,
                    size = font_11,
                    color = appColors.textSecondary,
                    maxLines = 1
                )
            }
        }
        // Keeps the title centred when there is no action
        Box(modifier = Modifier.widthIn(min = dimen_36), contentAlignment = Alignment.CenterEnd) {
            action?.invoke()
        }
    }
}

/** Small outlined glass button such as Edit, View or Replace, with an optional leading icon. */
@Composable
fun CraneSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = dimen_30
) {
    Row(
        modifier = modifier
            .height(height)
            .glassEffect(RoundedCornerShape(dimen_10))
            .clickable { onClick() }
            .padding(horizontal = dimen_12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appColors.textPrimary,
                modifier = Modifier.size(dimen_14)
            )
            Spacer(modifier = Modifier.width(dimen_4))
        }
        Textview(text = text, size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

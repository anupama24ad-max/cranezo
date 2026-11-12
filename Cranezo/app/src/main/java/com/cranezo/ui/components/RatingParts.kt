package com.cranezo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import java.util.Locale

/**
 * Stars and avatars, drawn the same way everywhere: one star size per text size, one amber, the number and the
 * count on the same line as the star. Use these instead of a loose Icon + Text, which never lined up.
 */

/** ★ 4.8 (12): the star is the height of the text, so they sit on one line */
@Composable
fun RatingChip(
    rating: Double?,
    count: Int? = null,
    textSize: TextUnit = font_12,
    modifier: Modifier = Modifier
) {
    if (rating == null) return
    // a star a little taller than the text looks level with it
    val star: Dp = if (textSize.value >= 14f) dimen_16 else dimen_14
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(star))
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(String.format(Locale.US, "%.1f", rating), size = textSize, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(dimen_4))
            Textview("($count)", size = textSize, color = appColors.textSecondary, maxLines = 1)
        }
    }
}

/** Five equal stars: [rating] of them filled */
@Composable
fun StarRow(rating: Int, size: Dp = dimen_16, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) AmberPrimary else appColors.textHint,
                modifier = Modifier.size(size)
            )
        }
    }
}

/** A round photo of a person; their first letter on an amber disc while there is no photo (or it fails) */
@Composable
fun Avatar(photo: String?, name: String?, size: Dp = dimen_40, modifier: Modifier = Modifier) {
    val url = fileUrl(photo)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AmberPrimary.copy(alpha = 0.16f))
            .border(dimen_1, AmberPrimary.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Textview(
            text = name.orEmpty().trim().take(1).uppercase().ifEmpty { "?" },
            size = if (size >= dimen_48) font_20 else font_14,
            color = AmberPrimary,
            fontWeight = FontWeight.Bold
        )
        if (url != null) {
            ShimmerImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        }
    }
}

package com.cranezo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.ui.theme.*

/**
 * A friendly empty page for a list with nothing in it yet: a round icon, a title, one line that says what to do,
 * and (optionally) the button that does it. Centered, so the page never looks broken or unfinished.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    accent: Color = AmberPrimary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimen_48, bottom = dimen_24, start = dimen_16, end = dimen_16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(dimen_88).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(dimen_60).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(dimen_30))
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
        Textview(title, size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (!body.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(body, size = font_14, color = appColors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = dimen_12))
        }
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(dimen_20))
            CommonGradientButton(text = actionText, onClick = onAction)
        }
    }
}

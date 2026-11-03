package com.cranezo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.R
import com.cranezo.ui.theme.AmberPrimary
import com.cranezo.ui.theme.appColors
import com.cranezo.ui.theme.dimen_10
import com.cranezo.ui.theme.dimen_14
import com.cranezo.ui.theme.dimen_44
import com.cranezo.ui.theme.font_14

private val ConfirmRed = Color(0xFFEF4444)

/**
 * "Are you sure?" shown on the page where the button was, instead of a popup.
 * The button that opened it is replaced by this card until the user picks Cancel or the action.
 */
@Composable
fun InlineConfirm(
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.common_cancel),
    danger: Boolean = true
) {
    GlassCard(modifier = modifier, contentPadding = dimen_14, borderColor = if (danger) ConfirmRed else AmberPrimary) {
        Textview(message, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(dimen_10))
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
            CommonGradientButton(
                text = cancelText, modifier = Modifier.weight(1f), height = dimen_44, onClick = onCancel,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
            CommonGradientButton(
                text = confirmText, modifier = Modifier.weight(1f), height = dimen_44, onClick = onConfirm,
                gradientColors = if (danger) listOf(ConfirmRed, ConfirmRed) else listOf(AmberPrimary, AmberPrimary),
                textColor = if (danger) Color.White else Color.Black
            )
        }
    }
}

package com.cranezo.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.R
import com.cranezo.ui.theme.*
import com.cranezo.utils.PayMethod

/**
 * "Select Payment Method": the app's own picker (UPI, card, net banking, more) shown before Razorpay opens.
 * [onPick] gets the chosen method; Razorpay then opens straight on it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSheet(amountText: String, onPick: (PayMethod) -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf(PayMethod.UPI) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = dimen_20).padding(bottom = dimen_16),
            verticalArrangement = Arrangement.spacedBy(dimen_10)
        ) {
            Textview(stringResource(R.string.pm_title), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_2))
            MethodRow(selected == PayMethod.UPI, R.string.pm_upi, R.string.pm_upi_sub, { UpiMark() }) { selected = PayMethod.UPI }
            MethodRow(selected == PayMethod.CARD, R.string.pm_card, R.string.pm_card_sub, { MethodIcon(Icons.Default.CreditCard) }) { selected = PayMethod.CARD }
            MethodRow(selected == PayMethod.NETBANKING, R.string.pm_netbanking, R.string.pm_netbanking_sub, { MethodIcon(Icons.Default.AccountBalance) }) { selected = PayMethod.NETBANKING }
            MethodRow(selected == PayMethod.MORE, R.string.pm_more, R.string.pm_more_sub, { MethodIcon(Icons.Default.MoreHoriz) }) { selected = PayMethod.MORE }
            Spacer(modifier = Modifier.height(dimen_4))
            CommonGradientButton(text = stringResource(R.string.pm_pay, amountText), onClick = { onPick(selected) })
        }
    }
}

@Composable
private fun MethodRow(selected: Boolean, title: Int, sub: Int, icon: @Composable () -> Unit, onClick: () -> Unit) {
    val shape = RoundedCornerShape(dimen_16)
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(if (selected) AmberPrimary.copy(alpha = 0.12f) else appColors.subtleFill)
            .border(dimen_1, if (selected) AmberPrimary else appColors.textHint.copy(alpha = 0.2f), shape)
            .clickable { onClick() }.padding(dimen_14),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(dimen_14))
        Column(modifier = Modifier.weight(1f)) {
            Textview(stringResource(title), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Textview(stringResource(sub), size = font_11, color = appColors.textSecondary, maxLines = 1)
        }
        // the round radio on the right
        Box(
            modifier = Modifier.size(dimen_24).clip(CircleShape).border(dimen_2, if (selected) AmberPrimary else appColors.textHint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Box(modifier = Modifier.size(dimen_12).clip(CircleShape).background(AmberPrimary))
        }
    }
}

@Composable
private fun MethodIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.size(dimen_48).clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = appColors.textPrimary, modifier = Modifier.size(dimen_22)) }
}

@Composable
private fun UpiMark() {
    Box(
        modifier = Modifier.size(dimen_48).clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill),
        contentAlignment = Alignment.Center
    ) { Textview("UPI", size = font_14, color = AmberPrimary, fontWeight = FontWeight.ExtraBold) }
}

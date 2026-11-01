package com.cranezo.ui.cranes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneStatusBadge
import com.cranezo.ui.cranes.components.approvalLabel
import com.cranezo.ui.cranes.components.craneSummaryLine
import com.cranezo.ui.cranes.model.CranesUiState
import com.cranezo.ui.theme.*

/**
 * Crane Submitted: the card of the design, sitting at the bottom of the screen. The text under the
 * title is the `message` the create API sent back.
 */
@Composable
fun CraneSubmittedScreen(
    state: CranesUiState,
    onViewDetails: () -> Unit,
    onBackToList: () -> Unit
) {
    val crane = state.submitted

    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_12, vertical = dimen_12)
                    .glassEffect(RoundedCornerShape(dimen_26))
                    .padding(dimen_20),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(dimen_64)
                        .clip(CircleShape)
                        .border(dimen_1, AmberPrimary.copy(alpha = 0.7f), CircleShape)
                        .background(AmberPrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, null, tint = AmberPrimary, modifier = Modifier.size(dimen_32))
                }
                Spacer(modifier = Modifier.height(dimen_14))
                Textview(
                    text = stringResource(R.string.crane_submitted_title),
                    size = font_22,
                    color = appColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(
                    text = state.submittedMessage?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.crane_submitted_default_message),
                    size = font_12,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                if (crane != null) {
                    Spacer(modifier = Modifier.height(dimen_16))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassEffect(RoundedCornerShape(dimen_14), inset = true)
                            .padding(dimen_10),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CraneImage(
                            path = crane.primary_photo ?: crane.photoUrls.firstOrNull(),
                            modifier = Modifier.size(dimen_56)
                        )
                        Spacer(modifier = Modifier.width(dimen_10))
                        Column(modifier = Modifier.weight(1f)) {
                            Textview(
                                text = crane.crane_code.orEmpty(),
                                size = font_14,
                                color = appColors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Textview(
                                text = craneSummaryLine(crane),
                                size = font_11,
                                color = appColors.textSecondary,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                                Textview(text = crane.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(dimen_4))
                            CraneStatusBadge(crane.approval_status, approvalLabel(crane))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimen_18))
                CommonGradientButton(
                    text = stringResource(R.string.crane_view_details),
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Color.Black,
                    enabled = crane?.id != null,
                    onClick = onViewDetails
                )
                Spacer(modifier = Modifier.height(dimen_10))
                // Outlined button: no fill, amber edge and text
                CommonGradientButton(
                    text = stringResource(R.string.crane_back_to_list),
                    gradientColors = listOf(Color.Transparent, Color.Transparent),
                    textColor = AmberPrimary,
                    modifier = Modifier.border(dimen_1, AmberPrimary, RoundedCornerShape(dimen_34)),
                    onClick = onBackToList
                )
            }
        }
    }
}

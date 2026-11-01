package com.cranezo.ui.verifyotp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassOtpField
import com.cranezo.ui.components.GlassResendOtpTimer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.verticalScroll
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.rememberImeScrollState
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.TextviewWithGradient
import com.cranezo.ui.theme.*
import com.cranezo.ui.verifyotp.model.VerifyOtpIntent
import com.cranezo.ui.verifyotp.model.VerifyOtpUiState

@Composable
fun VerifyOtpScreen(
    vm: VerifyOtpViewModel? = null,
    onBackClick: () -> Unit = {},
    onEditPhoneClick: () -> Unit = {},
    onEmergencyCallClick: () -> Unit = {}
) {
    val uiState = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: VerifyOtpUiState(phoneNumber = "+91 98765 43210")
    val otp = uiState.otp
    val phoneNumber = uiState.phoneNumber

    ScreenBackground(darkImageRes = R.drawable.otp_bg) {
        // --- 3. Screen Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimen_24, vertical = dimen_20)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberImeScrollState())
        ) {
            CommonBackButton(
                onClick = onBackClick,
            )

            Spacer(modifier = Modifier.height(dimen_30))

            // Header Texts
            Textview(
                text = stringResource(R.string.otp_heading_1),
                size = font_32,
                color = appColors.textPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(dimen_2))
            TextviewWithGradient(
                text = stringResource(R.string.otp_heading_2),
                size = font_32,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(dimen_12))

            Textview(
                text = stringResource(R.string.otp_sent_to),
                size = font_14,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(dimen_4))

            Textview(
                text = phoneNumber,
                size = font_16,
                color = appColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimen_8))

            TextviewWithGradient(
                text = stringResource(R.string.common_edit),
                size = font_14,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onEditPhoneClick() }
            )


            // Spacious area showing the JCB worker
            Spacer(modifier = Modifier.weight(1f))

            // --- 4. OTP Input & Actions Area ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 6 Glassy OTP cells
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    GlassOtpField(
                        value = otp,
                        onValueChange = { vm?.onIntent(VerifyOtpIntent.OtpChanged(it)) },
                        cells = 6,
                        cellWidth = dimen_45,
                        cellHeight = dimen_66,
                        cornerRadius = dimen_8,
                        gap = dimen_8,
                        onDone = { vm?.onIntent(VerifyOtpIntent.VerifyOtp) }
                    )
                }

                Spacer(modifier = Modifier.height(dimen_18))

                // Resend OTP in 00:45 Timer
                GlassResendOtpTimer(
                    onResendOtp = { vm?.onIntent(VerifyOtpIntent.ResendOtp) },
                    totalSeconds = 45,
                    text = stringResource(R.string.otp_resend_in)
                )

                Spacer(modifier = Modifier.height(dimen_30))

                // Get OTP Gradient Button
                CommonGradientButton(
                    text = stringResource(R.string.otp_verify_continue),
                    gradientColors = listOf(LightYellow, DarkYellow),
                    textColor = Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Black,
                    iconSize = dimen_16,
                    onClick = { vm?.onIntent(VerifyOtpIntent.VerifyOtp) }
                )

                Spacer(modifier = Modifier.height(dimen_20))

                // Call for Emergency Red Gradient Button
                CommonGradientButton(
                    text = stringResource(R.string.common_call_emergency),
                    gradientColors = listOf(
                        Red,
                        DarkRed,
                        Maroon
                    ),
                    textColor = White,
                    leftImage = R.drawable.ambulance_ic,
                    leftIconTint = White,
                    iconSpacing = dimen_16,
                    onClick = onEmergencyCallClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerifyOtpScreenPreview() {
    CranezoTheme {
        VerifyOtpScreen()
    }
}
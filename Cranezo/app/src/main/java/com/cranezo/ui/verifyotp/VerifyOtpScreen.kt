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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassOtpField
import com.cranezo.ui.components.GlassResendOtpTimer
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.TextviewWithGradient
import com.cranezo.ui.theme.*

@Composable
fun VerifyOtpScreen(
    vm: VerifyOtpViewModel? = null,
    onBackClick: () -> Unit = {},
    onEditPhoneClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onEmergencyCallClick: () -> Unit = {}
) {
    val otp by vm?.otp
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val phoneNumber by vm?.phoneNumber
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("+91 98765 43210") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Background Image ---
        Image(
            painter = painterResource(id = R.drawable.otp_bg),
            contentDescription = "OTP Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- 2. Cinematic Gradient Overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // --- 3. Screen Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimen_24, vertical = dimen_20)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            CommonBackButton(
                onClick = {
                },
            )

            Spacer(modifier = Modifier.height(dimen_30))

            // Header Texts
            Textview(
                text = "Verify",
                size = font_32,
                color = White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(dimen_2))
            TextviewWithGradient(
                text = "Your Number",
                size = font_32,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(dimen_12))

            Textview(
                text = "We've sent a 6- digit OTP to",
                size = font_14,
                color = Grey,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(dimen_4))

            Textview(
                text = phoneNumber,
                size = font_16,
                color = White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimen_8))

            TextviewWithGradient(
                text = "Edit",
                size = font_14,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {

                }
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
                        onValueChange = { vm?.onOtpChange(it) },
                        cells = 6,
                        cellWidth = dimen_45,
                        cellHeight = dimen_66,
                        cornerRadius = dimen_8,
                        gap = dimen_8,
                        onDone = {

                        }
                    )
                }

                Spacer(modifier = Modifier.height(dimen_18))

                // Resend OTP in 00:45 Timer
                GlassResendOtpTimer(
                    onResendOtp = { vm?.resendOtp() },
                    totalSeconds = 45,
                    text = "Resend OTP in "
                )

                Spacer(modifier = Modifier.height(dimen_30))

                // Get OTP Gradient Button
                CommonGradientButton(
                    text = "Verify & Continue",
                    gradientColors = listOf(LightYellow, DarkYellow),
                    textColor = Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Black,
                    iconSize = dimen_16,
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(dimen_20))

                // Call for Emergency Red Gradient Button
                CommonGradientButton(
                    text = "Call for Emergency",
                    gradientColors = listOf(
                        Red,
                        DarkRed,
                        Maroon
                    ),
                    textColor = White,
                    leftImage = R.drawable.ambulance_ic,
                    leftIconTint = White,
                    iconSpacing = dimen_16,
                    onClick = { }
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
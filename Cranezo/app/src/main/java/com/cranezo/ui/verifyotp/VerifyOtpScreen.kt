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
import com.cranezo.ui.theme.*

@Composable
fun VerifyOtpScreen(
    vm: VerifyOtpViewModel? = null,
    onBackClick: () -> Unit = {},
    onEditPhoneClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onEmergencyCallClick: () -> Unit = {}
) {
    val otp by vm?.otp ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val phoneNumber by vm?.phoneNumber ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("+91 98765 43210") }

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
                .statusBarsPadding()
        ) {
            // Common Circular Glass Back Button
            CommonBackButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = dimen_20, top = dimen_14)
            )

            Spacer(modifier = Modifier.height(dimen_28))

            // Header Texts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20)
            ) {
                Textview(
                    text = "Verify",
                    size = font_32,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_2))
                Textview(
                    text = "Your Number",
                    size = font_32,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(dimen_12))

                Textview(
                    text = "We've sent a 6- digit OTP to",
                    size = font_14,
                    color = MutedSlate,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(dimen_4))

                Textview(
                    text = phoneNumber,
                    size = font_15,
                    color = White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(dimen_8))

                Textview(
                    text = "Edit",
                    size = font_14,
                    color = AmberPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onEditPhoneClick() }
                )
            }

            // Spacious area showing the JCB worker
            Spacer(modifier = Modifier.weight(1f))

            // --- 4. OTP Input & Actions Area ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20)
                    .navigationBarsPadding()
                    .padding(bottom = dimen_24)
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
                        cellWidth = dimen_46,
                        cellHeight = dimen_56,
                        cornerRadius = dimen_12,
                        gap = dimen_8,
                        onDone = {
                            if (otp.length == 6) {
                                onVerifyClick(otp)
                            }
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

                Spacer(modifier = Modifier.height(dimen_28))

                // Verify & Continue Gradient Button
                CommonGradientButton(
                    text = "Verify & Continue",
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Color.Black,
                    onClick = { onVerifyClick(otp) }
                )

                Spacer(modifier = Modifier.height(dimen_16))

                // Call for Emergency Red Gradient Button
                CommonGradientButton(
                    text = "Call for Emergency",
                    gradientColors = listOf(EmergencyRedStart, EmergencyRedCenter, EmergencyRedEnd),
                    textColor = White,
                    leftImage = R.drawable.ic_emergency,
                    leftIconTint = White,
                    onClick = { onEmergencyCallClick() }
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
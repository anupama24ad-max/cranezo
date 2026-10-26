package com.cranezo.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.LoginViewModel
import com.cranezo.R
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.LabelTextField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*

@Composable
fun LoginScreen(
    vm: LoginViewModel? = null) {
    val mobileNumber by vm?.mobileNumber
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val countryCode by vm?.countryCode
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("+91") }
    val countryFlag by vm?.countryFlag
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("🇮🇳") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Background Crane Image ---
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = "Crane Background",
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
                            Black.copy(alpha = 0.60f),
                            Transparent,
                            Transparent
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
            // Common Circular Back Button
            CommonBackButton(
                onClick = {
                },
            )

            Spacer(modifier = Modifier.height(dimen_30))

            // Header Texts
            Textview(
                text = "W E L C O M E   T O",
                size = font_12,
                color = Grey,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(
                text = "Cranezo",
                size = font_32,
                color = White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(
                text = "Book the right crane.\nGet the job moving.",
                size = font_16,
                color = Grey,
                fontWeight = FontWeight.Normal
            )

            // Spacious area to heroically display the crane
            Spacer(modifier = Modifier.weight(1f))

            // --- 4. Bottom Glassmorphic Card ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimen_26))
                    .background(Black.copy(0.50f))
                    .border(
                        BorderStroke(
                            dimen_1,
                            Brush.verticalGradient(
                                listOf(White.copy(alpha = 0.2f), Transparent)
                            )
                        ),
                        shape = RoundedCornerShape(dimen_26)
                    )
                    .padding(horizontal = dimen_20, vertical = dimen_24)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mobile Number Glass Field using LabelTextField
                    LabelTextField(
                        value = mobileNumber,
                        onValueChange = { vm?.onMobileChange(it) },
                        labelText = "Mobile Number",
                        subLabel = "We'll send you an OTP",
                        placeholder = "Enter your mobile number",
                        isGlassy = true,
                        isPhoneWithCountry = true,
                        countryCode = countryCode,
                        countryFlag = countryFlag
                    )

                    Spacer(modifier = Modifier.height(dimen_18))

                    // Get OTP Gradient Button
                    CommonGradientButton(
                        text = "Get OTP",
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
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CranezoTheme {
        LoginScreen()
    }
}

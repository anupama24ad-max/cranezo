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
import androidx.compose.ui.res.stringResource
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.LoginViewModel
import com.cranezo.R
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.LabelTextField
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.verticalScroll
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.rememberImeScrollState
import com.cranezo.ui.components.glassEffect
import com.cranezo.ui.components.Textview
import com.cranezo.ui.login.model.LoginIntent
import com.cranezo.ui.login.model.LoginUiState
import com.cranezo.ui.theme.*

@Composable
fun LoginScreen(
    vm: LoginViewModel? = null,
    onBack: () -> Unit = {}
) {
    val emergencyContext = androidx.compose.ui.platform.LocalContext.current
    val uiState = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: LoginUiState()

    val mobileNumber = uiState.mobileNumber
    val countryCode = uiState.countryCode
    val countryFlag = uiState.countryFlag

    ScreenBackground(darkImageRes = R.drawable.login_bg) {
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
            // Back to language selection
            CommonBackButton(onClick = onBack)

            Spacer(modifier = Modifier.height(dimen_30))

            // Header Texts
            Textview(
                text = stringResource(R.string.login_welcome),
                size = font_12,
                color = appColors.textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(
                text = stringResource(R.string.app_name),
                size = font_32,
                color = appColors.textPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(
                text = stringResource(R.string.login_tagline),
                size = font_16,
                color = appColors.textSecondary,
                fontWeight = FontWeight.Normal
            )

            // Spacious area to heroically display the crane
            Spacer(modifier = Modifier.weight(1f))

            // --- 4. Bottom Glassmorphic Card ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(RoundedCornerShape(dimen_26))
                    .padding(horizontal = dimen_20, vertical = dimen_24)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mobile Number Glass Field using LabelTextField
                    LabelTextField(
                        value = mobileNumber,
                        onValueChange = { vm?.onIntent(LoginIntent.MobileNumberChanged(it)) },
                        labelText = stringResource(R.string.login_mobile_label),
                        subLabel = stringResource(R.string.login_otp_hint),
                        placeholder = stringResource(R.string.login_mobile_placeholder),
                        isGlassy = true,
                        isPhoneWithCountry = true,
                        countryCode = countryCode,
                        countryFlag = countryFlag
                    )

                    Spacer(modifier = Modifier.height(dimen_18))

                    // Get OTP Gradient Button
                    CommonGradientButton(
                        text = stringResource(R.string.login_get_otp),
                        gradientColors = listOf(LightYellow, DarkYellow),
                        textColor = Black,
                        rightImage = R.drawable.ic_arrow_right,
                        rightIconTint = Black,
                        iconSize = dimen_16,
                        onClick = { vm?.onIntent(LoginIntent.GetOtp) }
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
                        onClick = {
                            runCatching { emergencyContext.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:112"))) }
                        }
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

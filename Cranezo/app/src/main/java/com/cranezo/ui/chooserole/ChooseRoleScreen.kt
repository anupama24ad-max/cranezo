package com.cranezo.ui.chooserole

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassRoleCard
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.TextviewWithGradient
import com.cranezo.ui.theme.*

@Composable
fun ChooseRoleScreen(
    vm: ChooseRoleViewModel? = null,
    onBackClick: () -> Unit = {},
    onContinueClick: (UserRole) -> Unit = {}
) {
    val selectedRole by vm?.selectedRole ?: remember {
        androidx.compose.runtime.mutableStateOf(
            UserRole.CRANE_OWNER
        )
    }

    ScreenBackground(darkImageRes = R.drawable.role_bg) {
        // --- 3. Screen Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimen_24, vertical = dimen_20)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // No back button: the user has just verified the OTP and must pick a role to continue
            Spacer(modifier = Modifier.height(dimen_30))

            // Header Texts
            Textview(
                text = stringResource(R.string.role_heading_1),
                size = font_32,
                color = appColors.textPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(dimen_2))
            TextviewWithGradient(
                text = stringResource(R.string.role_heading_2),
                size = font_32,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(dimen_10))

            // The brand name is passed into the translated sentence and made bold wherever it lands
            val brand = stringResource(R.string.app_name)
            val subtitleText = stringResource(R.string.role_subtitle, brand)
            val subtitle = buildAnnotatedString {
                append(subtitleText)
                val start = subtitleText.indexOf(brand)
                if (start >= 0) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + brand.length)
                }
            }

            Text(
                text = subtitle,
                color = appColors.textSecondary,
                fontSize = font_14,
                lineHeight = font_20
            )


            // Spacious area to show the crane hero
//            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(dimen_150))

            // --- 4. Role Selection Cards ---
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimen_16)
            ) {
                UserRole.values().forEach { role ->
                    GlassRoleCard(
                        title = stringResource(role.titleRes),
                        subtitle = stringResource(role.subtitleRes),
                        imageRes = role.imageRes,
                        isSelected = selectedRole == role,
                        onClick = {
                            vm?.selectRole(role)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimen_30))

            // --- 5. Continue Button ---

            CommonGradientButton(
                text = stringResource(R.string.common_continue),
                gradientColors = listOf(LightYellow, DarkYellow),
                textColor = Black,
                rightImage = R.drawable.ic_arrow_right,
                rightIconTint = Black,
                iconSize = dimen_16,
                onClick = { onContinueClick(selectedRole) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseRoleScreenPreview() {
    CranezoTheme {
        ChooseRoleScreen()
    }
}

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.components.CommonBackButton
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassRoleCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*

@Composable
fun ChooseRoleScreen(
    vm: ChooseRoleViewModel? = null,
    onBackClick: () -> Unit = {},
    onContinueClick: (UserRole) -> Unit = {}
) {
    val selectedRole by vm?.selectedRole ?: remember { androidx.compose.runtime.mutableStateOf(UserRole.CRANE_OWNER) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Background Image ---
        Image(
            painter = painterResource(id = R.drawable.role_bg),
            contentDescription = "Role Background",
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
                            Color.Black.copy(alpha = 0.60f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.90f)
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

            Spacer(modifier = Modifier.height(dimen_24))

            // Header Texts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20)
            ) {
                Textview(
                    text = "Choose Your",
                    size = font_32,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_2))
                Textview(
                    text = "Role",
                    size = font_32,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(dimen_10))

                val subtitle = buildAnnotatedString {
                    append("Select how you want to use\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = White)) {
                        append("Cranzo")
                    }
                    append(" app to continue.")
                }

                androidx.compose.material3.Text(
                    text = subtitle,
                    color = MutedSlate,
                    fontSize = font_14,
                    lineHeight = font_20
                )
            }

            // Spacious area to show the crane hero
            Spacer(modifier = Modifier.weight(1f))

            // --- 4. Role Selection Cards ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20),
                verticalArrangement = Arrangement.spacedBy(dimen_14)
            ) {
                UserRole.values().forEach { role ->
                    GlassRoleCard(
                        title = role.title,
                        subtitle = role.subtitle,
                        imageRes = role.imageRes,
                        isSelected = selectedRole == role,
                        onClick = {
                            vm?.selectRole(role)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimen_24))

            // --- 5. Continue Button ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20)
                    .navigationBarsPadding()
                    .padding(bottom = dimen_24)
            ) {
                CommonGradientButton(
                    text = "Continue",
                    gradientColors = listOf(AmberPrimary, AmberSecondary, AmberDark),
                    textColor = Color.Black,
                    rightImage = R.drawable.ic_arrow_right,
                    rightIconTint = Color.Black,
                    onClick = { onContinueClick(selectedRole) }
                )
            }
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

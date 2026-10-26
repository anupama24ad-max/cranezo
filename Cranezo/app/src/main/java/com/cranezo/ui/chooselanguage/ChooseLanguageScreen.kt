package com.cranezo.ui.chooselanguage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cranezo.R
import com.cranezo.ui.components.ContinueButton
import com.cranezo.ui.components.GlassBox
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.TextviewWithGradient
import com.cranezo.ui.theme.*

@Composable
fun ChooseLanguageScreen(
    vm: ChooseLanguageViewModel? = null,
) {
    val selectedLang by vm?.selectedLanguage
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("en") }

    val languages = vm?.languageList ?: listOf(
        LanguageItem(
            code = "en",
            badge = "Aa",
            name = "English",
            subtitle = "Continue in English"
        ),
        LanguageItem(
            code = "te",
            badge = "ఆ",
            name = "తెలుగు",
            subtitle = "తెలుగులో కొనసాగించండి"
        ),
        LanguageItem(
            code = "hi",
            badge = "आ",
            name = "हिन्दी",
            subtitle = "हिंदी में जारी रखें"
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Background Crane Image ---
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- 2. Dark Cinematic Overlay Gradient ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // --- 3. Screen Content Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Texts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_24, vertical = dimen_20)
            )
            {
                Textview(
                    text = "Choose Your",
                    size = font_32,
                    color = White,
                    fontWeight = FontWeight.ExtraBold
                )
                TextviewWithGradient(
                    text = "Language",
                    size = font_30,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(
                    text = "Select your preferred language\nto continue.",
                    size = font_16,
                    color = Grey,
                    fontWeight = FontWeight.Normal
                )


                // Spacious area to show the crane hero illustration
                Spacer(modifier = Modifier.weight(1f))

                // Glassmorphic Language Options
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimen_16)
                ) {
                    languages.forEach { item ->
                        val isSelected = selectedLang == item.code

                        GlassBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm?.onLanguageSelected(item.code)
                                },
                            shape = RoundedCornerShape(dimen_16),
                            backgroundColor = if (isSelected) LightYellow.copy(alpha = 0.10f) else White.copy(
                                alpha = 0.10f
                            ),
                            borderColor = if (isSelected) LightYellow else White.copy(alpha = 0.10f),
                            borderWidth = if (isSelected) dimen_1 * 1.5f else dimen_1
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dimen_16, vertical = dimen_14),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Badge with Language character
                                Box(
                                    modifier = Modifier
                                        .size(dimen_44)
                                        .clip(RoundedCornerShape(dimen_12))
                                        .background(White.copy(alpha = 0.10f))
                                        .border(
                                            width = dimen_1,
                                            color = White.copy(alpha = 0.10f),
                                            shape = RoundedCornerShape(dimen_12)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Textview(
                                        text = item.badge,
                                        size = font_18,
                                        color = if (isSelected) LightYellow else White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(dimen_14))

                                // Name and Subtitle
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Textview(
                                        text = item.name,
                                        size = font_16,
                                        color = White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(dimen_2))
                                    Textview(
                                        text = item.subtitle,
                                        size = font_12,
                                        color = LightGreyish
                                    )
                                }

                                // Selection Indicator
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(dimen_24)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    listOf(
                                                        LightYellow,
                                                        DarkYellow
                                                    )
                                                ), shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Black,
                                            modifier = Modifier.size(dimen_14)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(dimen_24)
                                            .border(
                                                width = dimen_1,
                                                color = DarkBlackish,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimen_30))

                // Bottom Continue Button
                ContinueButton(
                    text = "Continue",
                    onClick = {

                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChooseLanguageScreenPreview() {
    CranezoTheme {
        ChooseLanguageScreen()
    }
}

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cranezo.R
import com.cranezo.ui.components.ContinueButton
import com.cranezo.ui.components.GlassBox
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*

@Composable
fun ChooseLanguageScreen(
    vm: ChooseLanguageViewModel? = null,
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val selectedLang by vm?.selectedLanguage ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("en") }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with "Skip"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = dimen_20, vertical = dimen_12),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Textview(
                    text = "Skip",
                    size = font_16,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSkipClick() }
                )
            }

            // Header Texts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20, vertical = dimen_4)
            ) {
                Textview(
                    text = "Choose Your",
                    size = font_32,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Textview(
                    text = "Language",
                    size = font_32,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(
                    text = "Select your preferred language\nto continue.",
                    size = font_14,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Normal
                )
            }

            // Spacious area to show the crane hero illustration
            Spacer(modifier = Modifier.weight(1f))

            // Glassmorphic Language Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimen_20),
                verticalArrangement = Arrangement.spacedBy(dimen_12)
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
                        backgroundColor = if (isSelected) Color(0x66181C24) else Color(0x4014171E),
                        borderColor = if (isSelected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.12f),
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
                                    .background(Color(0x33FFFFFF))
                                    .border(
                                        width = dimen_1,
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(dimen_12)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Textview(
                                    text = item.badge,
                                    size = font_18,
                                    color = if (isSelected) Color(0xFFF59E0B) else Color.White,
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
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(dimen_2))
                                Textview(
                                    text = item.subtitle,
                                    size = font_12,
                                    color = if (isSelected) Color(0xFFCBD5E1) else Color(0xFF64748B)
                                )
                            }

                            // Selection Indicator
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(dimen_24)
                                        .background(Color(0xFFF59E0B), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF1E2128),
                                        modifier = Modifier.size(dimen_14)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(dimen_24)
                                        .border(
                                            width = dimen_1 * 1.5f,
                                            color = Color(0xFF475569),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimen_20))

            // Bottom Continue Button
            ContinueButton(
                text = "Continue",
                onClick = onContinueClick
            )
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

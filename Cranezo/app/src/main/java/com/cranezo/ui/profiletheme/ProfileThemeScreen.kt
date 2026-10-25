package com.cranezo.ui.profiletheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cranezo.ui.theme.AppThemeMode
import com.cranezo.ui.theme.AppThemeState
import com.cranezo.ui.theme.CranezoTheme
import com.cranezo.ui.theme.themeColor

@Composable
fun ProfileThemeScreen(
    vm: ProfileThemeViewModel? = null,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val selectedMode by vm?.selectedTheme ?: AppThemeState.currentThemeMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Content (Header + Theme Options) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp) // Leave space for Continue button
        ) {
            // --- Top Header Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        themeColor(
                            light = Color(0xFFF1F1F4),
                            dark = Color(0xFF1B1B1E)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Circular Back Button
                if (showBackButton) {
                    Surface(
                        shape = CircleShape,
                        color = themeColor(light = Color.White, dark = Color(0xFF2C2C2E)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Large Title: "Theme"
                Text(
                    text = "Theme",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Theme Options List ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val themeOptions = listOf(
                    AppThemeMode.LIGHT,
                    AppThemeMode.DARK,
                    AppThemeMode.SYSTEM
                )

                themeOptions.forEach { mode ->
                    val isSelected = selectedMode == mode

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm?.onThemeSelected(mode)
                            }
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.title,
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )

                        // Radio / Check Indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = Color(0xFF1976D2), // Blue accent circle
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = themeColor(
                                            light = Color(0xFFC7C7CD),
                                            dark = Color(0xFF555558)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.8.dp,
                        color = themeColor(
                            light = Color(0xFFEBEBF0),
                            dark = Color(0xFF28282B)
                        )
                    )
                }
            }
        }

        // --- Bottom Pinned Continue Button ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun ProfileThemeScreenLightPreview() {
    CranezoTheme(darkTheme = false) {
        ProfileThemeScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun ProfileThemeScreenDarkPreview() {
    CranezoTheme(darkTheme = true) {
        ProfileThemeScreen()
    }
}
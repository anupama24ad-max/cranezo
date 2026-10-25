package com.cranezo.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.cranezo.ui.components.LabelTextField
import com.cranezo.ui.components.ProfileAvatarPicker
import com.cranezo.ui.components.Textview
import com.cranezo.ui.components.VerifiedBadge
import com.cranezo.ui.theme.*

@Composable
fun ProfileScreen(
    vm: ProfileViewModel? = null,
    onBackClick: () -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val fullName by vm?.fullName ?: remember { androidx.compose.runtime.mutableStateOf("") }
    val mobileNumber by vm?.mobileNumber ?: remember { androidx.compose.runtime.mutableStateOf("+91 98765 43210") }
    val companyName by vm?.companyName ?: remember { androidx.compose.runtime.mutableStateOf("") }
    val roleName by vm?.roleName ?: remember { androidx.compose.runtime.mutableStateOf("crane owner") }
    val photoRes by vm?.photoRes ?: remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Background Image ---
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = "App Background",
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
                            Color.Black.copy(alpha = 0.50f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(dimen_24))

                // Header Texts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen_20)
                ) {
                    Textview(
                        text = "Complete",
                        size = font_32,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(dimen_2))
                    Textview(
                        text = "Your Profile",
                        size = font_32,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(dimen_10))

                    Textview(
                        text = "Share a few basic details to get started\nas a $roleName.",
                        size = font_14,
                        color = MutedSlate,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(dimen_26))

                // Profile Photo Section (Centered)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Textview(
                        text = "Profile Photo (Optional)",
                        size = font_14,
                        color = MutedSlate,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(dimen_14))

                    ProfileAvatarPicker(
                        onAddPhotoClick = onAddPhotoClick,
                        photoRes = photoRes
                    )
                }

                Spacer(modifier = Modifier.height(dimen_26))

                // Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen_20)
                ) {
                    // Full Name
                    LabelTextField(
                        value = fullName,
                        onValueChange = { vm?.onFullNameChange(it) },
                        labelText = "Full Name",
                        placeholder = "Enter your full name",
                        leftImage = R.drawable.ic_person,
                        isGlassy = true
                    )

                    Spacer(modifier = Modifier.height(dimen_18))

                    // Mobile Number with Verified badge
                    LabelTextField(
                        value = mobileNumber,
                        onValueChange = {},
                        labelText = "Mobile Number",
                        placeholder = "Enter mobile number",
                        leftImage = R.drawable.ic_phone,
                        isGlassy = true,
                        readOnly = true,
                        rightContent = {
                            VerifiedBadge()
                        }
                    )

                    Spacer(modifier = Modifier.height(dimen_18))

                    // Company Name
                    LabelTextField(
                        value = companyName,
                        onValueChange = { vm?.onCompanyNameChange(it) },
                        labelText = "Company Name",
                        placeholder = "Enter your company name",
                        leftImage = R.drawable.ic_building,
                        isGlassy = true
                    )

                    Spacer(modifier = Modifier.height(dimen_20))
                }
            }

            // --- 4. Continue Button ---
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
                    onClick = onContinueClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CranezoTheme {
        ProfileScreen()
    }
}

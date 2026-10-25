package com.cranezo.ui.components.dialogUtils

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cranezo.R
import com.cranezo.ui.components.GradientButton
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*


@Composable
fun SessionExpiredDialog(
    showDialog: Boolean,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    if (showDialog) {

     /*   Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false   // important
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),   // removes dim
                contentAlignment = Alignment.Center
            ) {

                Card(
                    shape = RoundedCornerShape(dimen_20),
                    colors = CardDefaults.cardColors(containerColor = WhiteGray),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimen_5),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)   // 90% width
                )
                {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimen_20),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Textview(
                            text = message,
                            size = font_12,
                            color = DarkBlueBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(
                                    start = dimen_30,
                                    end = dimen_30,
                                    top = dimen_10,
                                    bottom = dimen_20
                                )
                        )

                        Button(
                            onClick = {
                                onConfirm()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(dimen_12),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyBlue
                            )

                        ) {
                            Textview(
                                text = "Ok",
                                bold = true,
                                size = font_16,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }*/

    }
}


@Composable
fun NoInternetDialog(
    showDialog: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(dimen_16),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimen_24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
/*
                    Image(
                        painter = painterResource(R.drawable.no_internet_ic), // your image
                        contentDescription = null,
                        modifier = Modifier.size(dimen_100)
                    )

                    Spacer(Modifier.height(dimen_20))*/

                    Textview(
                        text = "No Internet",
                        size = font_22,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(dimen_10))

                    Textview(
                        text = "Please check your internet connection and try again.",
                        textAlign = TextAlign.Center,
                        color = Greyish
                    )

                    Spacer(Modifier.height(dimen_24))

                    GradientButton(
                        text = "Retry",
                        color1 = Green,
                        color2 = Green,
                        isCircle = false
                    ) {
                        onRetry()
                    }

                    Spacer(Modifier.height(dimen_12))

                    TextButton(onClick = onDismiss) {
                        Textview("Cancel")
                    }
                }
            }
        }
    }
}
package com.cranezo.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.theme.CranezoTheme
import kotlinx.coroutines.delay

/** The splash is dark only: it always shows the dark logo, whatever the app or phone theme is */
@Composable
fun SplashScreen(vm: SplashViewModel? = null) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(2000)
        (context as? SplashActivity)?.navigations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.cranezo_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(name = "Splash", showBackground = true)
@Composable
fun SplashScreenPreview() {
    CranezoTheme(darkTheme = true) {
        SplashScreen()
    }
}

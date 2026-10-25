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
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.theme.CranezoTheme
import com.cranezo.ui.theme.themePainter
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(vm: SplashViewModel? = null) {
    val context = LocalContext.current

    /* LaunchedEffect(Unit) {
         delay(2000)
         (context as? SplashActivity)?.navigations()
     }*/

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = themePainter(
                lightResId = R.drawable.cranezo_light_logo,
                darkResId = R.drawable.cranezo_logo
            ),
            contentDescription = "Cranezo Splash",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun SplashScreenLightPreview() {
    CranezoTheme(darkTheme = false) {
        SplashScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun SplashScreenDarkPreview() {
    CranezoTheme(darkTheme = true) {
        SplashScreen()
    }
}
package com.cranezo.ui.components

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.cranezo.R

/** One image loader that can play GIFs, shared by every loader on screen (the animation is decoded once) */
private object LoaderImageLoader {
    @Volatile private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader = instance ?: synchronized(this) {
        instance ?: ImageLoader.Builder(context.applicationContext)
            .components {
                // Android 9+ plays GIFs natively; older phones use Coil's own decoder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
            .also { instance = it }
    }
}

/**
 * The Cranezo logo animation (res/raw/cranezo_loader.gif), used instead of a spinner while a page loads.
 * [height] is the height of the logo; it is small on purpose (the picture is 3:4, so 72dp high is 54dp wide).
 */
@Composable
fun CranezoLoader(modifier: Modifier = Modifier, height: Dp = 72.dp) {
    val context = LocalContext.current
    val loader = remember(context) { LoaderImageLoader.get(context) }
    val request = remember(context) { ImageRequest.Builder(context).data(R.raw.cranezo_loader).build() }
    AsyncImage(
        model = request,
        imageLoader = loader,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(width = height * 0.75f, height = height)
    )
}

/** A page-sized loader: the logo animation in the middle of the available space */
@Composable
fun CranezoLoaderPage(modifier: Modifier = Modifier.fillMaxSize(), height: Dp = 72.dp) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) { CranezoLoader(height = height) }
}

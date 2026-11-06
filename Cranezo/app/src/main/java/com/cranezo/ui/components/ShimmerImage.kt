package com.cranezo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

/**
 * A network (or local) image that shows a shimmer until the picture has loaded.
 * Drop-in for Coil's AsyncImage: same model / contentDescription / contentScale / modifier / onError.
 * The modifier must give the image a size (as every AsyncImage in the app already does).
 * On an error the shimmer stops, so whatever is drawn behind (an icon or initial) shows instead.
 */
@Composable
fun ShimmerImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null
) {
    var loading by remember(model) { mutableStateOf(model != null) }
    Box(modifier = modifier) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize(),
            onLoading = { loading = true },
            onSuccess = { loading = false },
            onError = { e ->
                loading = false
                onError?.invoke(e)
            }
        )
        if (loading) Box(modifier = Modifier.matchParentSize().shimmerEffect())
    }
}

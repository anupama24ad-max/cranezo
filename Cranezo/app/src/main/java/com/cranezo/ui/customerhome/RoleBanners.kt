package com.cranezo.ui.customerhome

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.push.UnreadBadge
import com.cranezo.ui.customerhome.model.HomeBanner
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import dagger.hilt.android.EntryPointAccessors

/**
 * The same banner carousel the customer sees, for the owner and operator homes. The admin chooses
 * who sees each banner (customer, owner, operator or everyone) and the server returns only this role's banners.
 */
@Composable
fun RoleBanners(onProfile: () -> Unit = {}) {
    val context = LocalContext.current
    var banners by remember { mutableStateOf<List<HomeBanner>>(emptyList()) }
    LaunchedEffect(Unit) { banners = loadRoleBanners(context) }
    if (banners.isNotEmpty()) {
        BannerCarousel(banners.sortedBy { it.sort_order ?: 0 }) { runBannerAction(context, it, onProfile) }
    }
}

private suspend fun loadRoleBanners(context: Context): List<HomeBanner> {
    val sm = SessionManager(context.applicationContext)
    if (!sm.isUserLoggedIn()) return emptyList()
    val repo = runCatching {
        EntryPointAccessors.fromApplication(context.applicationContext, UnreadBadge.RepoEntryPoint::class.java).repository()
    }.getOrNull() ?: return emptyList()
    var out: List<HomeBanner> = emptyList()
    runCatching {
        repo.homeBanners(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(success = { r -> out = r?.data.orEmpty() })
    }
    return out
}

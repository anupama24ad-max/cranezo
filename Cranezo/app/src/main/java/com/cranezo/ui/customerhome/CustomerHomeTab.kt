package com.cranezo.ui.customerhome

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.booking.BookingActivity
import com.cranezo.ui.booking.BookingRoutes
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.model.MyJobCrane
import com.cranezo.ui.booking.model.MyJobItem
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.customerhome.model.BannerAction
import com.cranezo.ui.customerhome.model.CustomerHome
import com.cranezo.ui.customerhome.model.CustomerHomeState
import com.cranezo.ui.customerhome.model.HomeBanner
import com.cranezo.ui.customerhome.model.QuickAccessItem
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.support.SupportActivity
import com.cranezo.ui.support.SupportRoutes
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay

/** Opens an app screen by the key an admin chose; an unknown key does nothing. */
private fun openScreen(context: Context, key: String?, onProfile: () -> Unit) {
    when (key) {
        "my_jobs" -> context.startActivity(BookingActivity.intent(context, BookingRoutes.BOOKINGS))
        "track" -> context.startActivity(BookingActivity.intent(context, BookingRoutes.ACTIVE_TRACKING))
        "support" -> context.startActivity(SupportActivity.intent(context, SupportRoutes.HELP))
        "notifications" -> context.startActivity(SupportActivity.intent(context, SupportRoutes.NOTIFICATIONS))
        "profile" -> onProfile()
        else -> Unit
    }
}

private fun runBannerAction(context: Context, action: BannerAction?, onProfile: () -> Unit) {
    when (action?.type) {
        "book_crane" -> context.startActivity(BookingActivity.intent(context))
        "screen" -> openScreen(context, action.value, onProfile)
        // Only https links
        "url" -> action.value?.takeIf { it.startsWith("https://") }?.let {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
        }
        else -> Unit
    }
}

private fun quickIcon(icon: String?): ImageVector = when (icon) {
    "track" -> Icons.Default.LocationOn
    "jobs" -> Icons.Default.Description
    "help" -> Icons.Default.SupportAgent
    else -> Icons.Default.Circle
}

private val previewHome = CustomerHome(
    greeting_name = "Rahul", member_id = "CUST100001", unread_notifications = 2,
    banners = listOf(HomeBanner(1, "BOOK A CRANE", "Certified cranes for your construction needs.", "Book a Crane", null, BannerAction("book_crane"))),
    quick_access = listOf(
        QuickAccessItem("track_crane", "Track My Crane", "Live location & ETA", "track", "track", 1),
        QuickAccessItem("my_jobs", "My Jobs", "View all bookings", "jobs", "my_jobs", 3),
        QuickAccessItem("help", "Need Help?", "Get support", "help", "support", 0)
    ),
    recent_bookings = listOf(
        MyJobItem("1", "CRZ-260905-08421", 7, "Completed", "completed", "Completed", "completed", MyJobCrane(title = "50 Ton Mobile Crane"),
            "Construction Site, Kakinada", "2026-09-05T03:48:00.000Z", null, 32500.0)
    )
)

@Preview(showBackground = true, heightDp = 900)
@Composable
fun CustomerHomeTab(
    vm: CustomerHomeViewModel? = null,
    onProfile: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: CustomerHomeState(home = previewHome, loaded = true)
    val context = LocalContext.current
    // On opening and whenever the app comes back to the foreground
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm?.refresh() }
    val home = state.home

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        val name = home?.greeting_name?.takeIf { it.isNotBlank() }
        HomeHeader(
            name = when {
                name == null -> stringResource(R.string.ch_welcome)
                !home.member_id.isNullOrBlank() -> "$name, #${home.member_id}"
                else -> name
            },
            subtitle = stringResource(R.string.home_customer_subtitle),
            unread = home?.unread_notifications ?: 0
        )

        BannerCarousel(home?.banners.orEmpty().sortedBy { it.sort_order ?: 0 }) { runBannerAction(context, it, onProfile) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.home_recent_booking), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Textview(
                stringResource(R.string.common_view_all), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { openScreen(context, "my_jobs", onProfile) }
            )
        }
        val recent = home?.recent_bookings.orEmpty()
        if (state.loaded && recent.isEmpty() || home != null && recent.isEmpty()) {
            Textview(stringResource(R.string.ch_no_recent), size = font_13, color = appColors.textSecondary)
        }
        recent.forEach { booking ->
            GlassCard(contentPadding = dimen_10, minHeight = dimen_104, onClick = { booking.booking_id?.let { context.startActivity(BookingActivity.bookingIntent(context, it)) } }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = booking.crane?.primary_photo, modifier = Modifier.size(dimen_70))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(booking.booking_code.orEmpty(), size = font_10, color = appColors.textSecondary, modifier = Modifier.weight(1f), maxLines = 1)
                            BookingTag(booking.tab_label ?: booking.status_label.orEmpty(), when (booking.tab) { "cancelled" -> Red; "upcoming" -> AmberPrimary; else -> SuccessGreen })
                        }
                        Textview(booking.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                            Textview(booking.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                            Textview(" " + formatServerDate(booking.start_at), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                            Textview(rupees(booking.amount), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (booking.can_rate == true) BookingTag(stringResource(R.string.cj_rate), AmberPrimary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        // Need help and other links at the bottom, after the bookings
        // Quick links, minus the ones the bottom bar already has (Track, My Jobs). One full-width row each,
        // so long titles in Hindi or Telugu never squeeze into narrow columns.
        val quick = home?.quick_access.orEmpty().filterNot { it.screen == "track" || it.screen == "my_jobs" }
        quick.forEach { item ->
            GlassCard(contentPadding = dimen_14, minHeight = dimen_56, onClick = { openScreen(context, item.screen, onProfile) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = quickIcon(item.icon), accent = AmberPrimary, size = dimen_36)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(item.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Textview(item.subtitle.orEmpty(), size = font_12, color = appColors.textSecondary, maxLines = 1)
                    }
                    if ((item.badge ?: 0) > 0) BookingTag(item.badge.toString(), Red)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Banner carousel: count, text, order and action all come from the admin
// ---------------------------------------------------------------------------------------------

@Composable
private fun BannerCarousel(banners: List<HomeBanner>, onAction: (BannerAction?) -> Unit) {
    // A banner whose picture fails to load is skipped, not shown broken
    var failed by remember { mutableStateOf(setOf<Int?>()) }
    val visible = banners.filter { it.id !in failed && !it.image.isNullOrBlank() }
    if (visible.isEmpty()) return

    val pager = rememberPagerState(pageCount = { visible.size })
    val dragged by pager.interactionSource.collectIsDraggedAsState()
    var touching by remember { mutableStateOf(false) }

    // Auto-advance every 4.5 seconds, paused while the user is touching it
    LaunchedEffect(visible.size, dragged, touching) {
        while (visible.size > 1 && !dragged && !touching) {
            delay(4500)
            pager.animateScrollToPage((pager.currentPage + 1) % visible.size)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxWidth().height(dimen_175)
                .androidxPointerWatch { touching = it }
        ) { page ->
            val banner = visible[page]
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(dimen_20)).clickable { onAction(banner.action) }
            ) {
                ShimmerImage(
                    model = fileUrl(banner.image), contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    onError = { failed = failed + banner.id }
                )
                // With a title the picture is the artwork behind the text; without one it already has all the text
                if (!banner.title.isNullOrBlank()) {
                    Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent))))
                    Column(modifier = Modifier.align(Alignment.CenterStart).padding(dimen_16).fillMaxWidth(0.6f), verticalArrangement = Arrangement.spacedBy(dimen_6)) {
                        Textview(banner.title, size = font_24, color = Color.White, fontWeight = FontWeight.Bold)
                        if (!banner.subtitle.isNullOrBlank()) Textview(banner.subtitle, size = font_11, color = Color.White.copy(alpha = 0.85f))
                        if (!banner.button_text.isNullOrBlank()) {
                            CommonGradientButton(text = banner.button_text, onClick = { onAction(banner.action) }, height = dimen_40, textSize = font_12, modifier = Modifier.width(dimen_140))
                        }
                    }
                }
            }
        }
        // No dots for a single banner
        if (visible.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_6)) {
                repeat(visible.size) { i ->
                    Box(modifier = Modifier.size(if (i == pager.currentPage) dimen_8 else dimen_6).clip(CircleShape).background(if (i == pager.currentPage) AmberPrimary else appColors.subtleBorder))
                }
            }
        }
    }
}

/** Tells whether a finger is down on the carousel, to pause the auto-advance. */
private fun Modifier.androidxPointerWatch(onTouch: (Boolean) -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                onTouch(true)
                do {
                    val event = awaitPointerEvent()
                } while (event.changes.any { it.pressed })
                onTouch(false)
            }
        }
    )

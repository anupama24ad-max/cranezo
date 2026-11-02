package com.cranezo.ui.customerhome.model

import com.cranezo.ui.booking.model.MyJobItem

/** Response of POST /home/customer. Banners, quick access and bookings are all driven by the server. */

data class BannerAction(
    // none, book_crane, screen or url
    val type: String? = null,
    val value: String? = null
)

data class HomeBanner(
    val id: Int? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val button_text: String? = null,
    val image: String? = null,
    val action: BannerAction? = null,
    val sort_order: Int? = null
)

data class QuickAccessItem(
    val key: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    // track, jobs, help; anything else is a dot
    val icon: String? = null,
    // track, my_jobs, support
    val screen: String? = null,
    val badge: Int? = null
)

data class CustomerHome(
    val greeting_name: String? = null,
    val member_id: String? = null,
    val profile_image: String? = null,
    val unread_notifications: Int? = null,
    val banners: List<HomeBanner>? = null,
    val quick_access: List<QuickAccessItem>? = null,
    val recent_bookings: List<MyJobItem>? = null
)

data class CustomerHomeState(
    val home: CustomerHome? = null,
    val loaded: Boolean = false
)

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
    val recent_bookings: List<MyJobItem>? = null,
    // the order happening now (special card at the top), null when nothing is open
    val current_order: CurrentOrder? = null
)

/** "Your current order": where the open request / booking stands, in one line and a step bar */
data class CurrentOrder(
    // booking or request
    val kind: String? = null,
    val booking_id: String? = null,
    val request_id: String? = null,
    val code: String? = null,
    val crane_title: String? = null,
    val primary_photo: String? = null,
    val site: String? = null,
    val start_at: String? = null,
    val stage: String? = null,
    // 1 request sent, 2 pay advance, 3 confirmed / operator, 4 on the way, 5 working, 6 done
    val step: Int? = null,
    val steps_total: Int? = null,
    val message: String? = null,
    val operator_name: String? = null,
    val can_track: Boolean? = null,
    // view, view_request, pay_advance, pay_balance
    val action: String? = null
)

data class CustomerHomeState(
    val home: CustomerHome? = null,
    val loaded: Boolean = false
)

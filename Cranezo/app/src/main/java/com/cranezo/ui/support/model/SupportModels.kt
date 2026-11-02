package com.cranezo.ui.support.model

/** Response models of the Notifications and Help & Support APIs. All fields nullable. */

// ---- Notifications ----

data class NotificationTarget(val screen: String? = null, val booking_id: String? = null, val ticket_id: String? = null)

data class SupportNotification(
    val id: String? = null,
    val type: String? = null,
    val category: String? = null,
    val icon: String? = null,
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, Any?>? = null,
    val target: NotificationTarget? = null,
    val is_read: Boolean? = null,
    // "today" or "earlier"
    val group: String? = null,
    val created_at: String? = null
)

data class NotificationSummary(val unread: Int? = null, val today_unread: Int? = null)

data class MarkReadResult(val updated: Int? = null, val unread: Int? = null)

// ---- Help & Support home ----

data class EmergencyInfo(val title: String? = null, val subtitle: String? = null, val phone: String? = null, val available: Boolean? = null)

data class SupportTile(
    val key: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null,
    // call, chat, category, ticket, faq
    val action: String? = null,
    val category: String? = null
)

data class CallSupport(val phone: String? = null, val available: Boolean? = null, val hours: String? = null)

data class MyRequests(val title: String? = null, val subtitle: String? = null, val open_count: Int? = null, val unread_replies: Int? = null)

data class SupportTeam(val title: String? = null, val message: String? = null, val hours: String? = null)

data class SupportHome(
    val search_placeholder: String? = null,
    val emergency: EmergencyInfo? = null,
    val tiles: List<SupportTile>? = null,
    val call_support: CallSupport? = null,
    val my_requests: MyRequests? = null,
    val team: SupportTeam? = null,
    val support_email: String? = null
)

// ---- FAQs ----

data class FaqCategory(val id: Int? = null, val code: String? = null, val name: String? = null, val description: String? = null, val faq_count: Int? = null)

data class FaqCategoryRef(val id: Int? = null, val code: String? = null, val name: String? = null)

data class Faq(val id: Int? = null, val category: FaqCategoryRef? = null, val question: String? = null, val answer: String? = null)

// ---- Support requests ----

data class TicketItem(
    val ticket_id: String? = null,
    val ticket_code: String? = null,
    val category: String? = null,
    val category_label: String? = null,
    val subject: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val priority: String? = null,
    val booking_id: String? = null,
    val unread_replies: Int? = null,
    val last_message: String? = null,
    val last_message_from: String? = null,
    val last_message_at: String? = null,
    val created_at: String? = null,
    val resolved_at: String? = null
)

data class TicketMessage(
    val id: Int? = null,
    // user, admin or system
    val sender_type: String? = null,
    val is_mine: Boolean? = null,
    val sender_label: String? = null,
    val body: String? = null,
    val attachments: List<String>? = null,
    val created_at: String? = null
)

data class Ticket(
    val ticket_id: String? = null,
    val ticket_code: String? = null,
    val category: String? = null,
    val category_label: String? = null,
    val subject: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val priority: String? = null,
    val unread_replies: Int? = null,
    val is_chat: Boolean? = null,
    val can_reply: Boolean? = null,
    val messages: List<TicketMessage>? = null
)

data class EmergencyResult(val ticket: Ticket? = null, val emergency_phone: String? = null, val message: String? = null)

// ---------------------------------------------------------------------------------------------
// MVI
// ---------------------------------------------------------------------------------------------

data class SupportUiState(
    // Notifications
    val notifications: List<SupportNotification> = emptyList(),
    val summary: NotificationSummary? = null,
    val notificationsLoaded: Boolean = false,
    val notificationsHasMore: Boolean = false,
    val notificationsNextMin: Int = 0,
    val announcement: SupportNotification? = null,

    // Help & Support
    val home: SupportHome? = null,
    val homeLoaded: Boolean = false,

    // FAQs
    val faqCategories: List<FaqCategory> = emptyList(),
    val faqs: List<Faq> = emptyList(),
    val faqsLoaded: Boolean = false,
    val faqFeedback: Set<Int> = emptySet(),

    // Requests and the conversation
    val ticketTab: String = "open",
    val tickets: List<TicketItem> = emptyList(),
    val ticketsLoaded: Boolean = false,
    val ticketsHasMore: Boolean = false,
    val ticketsNextMin: Int = 0,
    val ticket: Ticket? = null,
    val ticketLoaded: Boolean = false,
    val created: Ticket? = null,

    // Photos uploaded for the message or request being written
    val attachments: List<String> = emptyList(),
    val isUploading: Boolean = false,
    val isSending: Boolean = false
)

sealed interface SupportIntent {
    data object LoadNotifications : SupportIntent
    data object LoadMoreNotifications : SupportIntent
    data object PollNotifications : SupportIntent
    data class OpenNotification(val item: SupportNotification) : SupportIntent
    data object MarkAllRead : SupportIntent

    data object LoadHome : SupportIntent

    data object LoadFaqCategories : SupportIntent
    data class LoadFaqs(val category: String?, val search: String) : SupportIntent
    data class FaqFeedback(val faqId: Int, val helpful: Boolean) : SupportIntent

    data class LoadTickets(val tab: String) : SupportIntent
    data object LoadMoreTickets : SupportIntent
    data class OpenTicket(val ticketId: String) : SupportIntent
    data object PollTicket : SupportIntent
    data object OpenChat : SupportIntent
    data class SendReply(val message: String) : SupportIntent
    data object CloseTicket : SupportIntent

    data class CreateTicket(val category: String, val subject: String, val description: String) : SupportIntent
    data class AddAttachment(val uri: String) : SupportIntent
    data class RemoveAttachment(val path: String) : SupportIntent
    data object ClearAttachments : SupportIntent

    data class Emergency(val bookingId: String?, val description: String) : SupportIntent
}

sealed interface SupportEffect {
    // Open a notification's screen: the activity knows the role and the other screens
    data class OpenTarget(val item: SupportNotification) : SupportEffect
    data class Dial(val number: String) : SupportEffect
    data class Go(val route: String, val popUpTo: String? = null) : SupportEffect
}

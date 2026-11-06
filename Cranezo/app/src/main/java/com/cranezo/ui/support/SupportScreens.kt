package com.cranezo.ui.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.SearchInput
import com.cranezo.ui.components.ScreenBackground
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneScreenHeader
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.support.model.CallSupport
import com.cranezo.ui.support.model.EmergencyInfo
import com.cranezo.ui.support.model.Faq
import com.cranezo.ui.support.model.MyRequests
import com.cranezo.ui.support.model.NotificationSummary
import com.cranezo.ui.support.model.SupportHome
import com.cranezo.ui.support.model.SupportIntent
import com.cranezo.ui.support.model.SupportNotification
import com.cranezo.ui.support.model.SupportTeam
import com.cranezo.ui.support.model.SupportTile
import com.cranezo.ui.support.model.SupportUiState
import com.cranezo.ui.support.model.Ticket
import com.cranezo.ui.support.model.TicketItem
import com.cranezo.ui.support.model.TicketMessage
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController
import com.cranezo.utils.serverMonthName
import com.cranezo.utils.serverYear
import kotlinx.coroutines.delay

fun dialNumber(context: Context, number: String?) {
    if (number.isNullOrBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${number.filter { it.isDigit() || it == '+' }}")))
}

/** Maps the server's icon key to an icon; anything unknown is a bell. */
private fun notificationIcon(icon: String?): ImageVector = when (icon) {
    "booking" -> Icons.Default.CalendarMonth
    "crane" -> Icons.Default.LocalShipping
    "clock" -> Icons.Default.AccessTime
    "check" -> Icons.Default.CheckCircle
    "job" -> Icons.Default.Work
    "time" -> Icons.Default.AccessTime
    "alert" -> Icons.Default.Warning
    "payment" -> Icons.Default.Payments
    "receipt" -> Icons.Default.Receipt
    "star" -> Icons.Default.Star
    "support" -> Icons.Default.SupportAgent
    else -> Icons.Default.Notifications
}

private fun tileIcon(icon: String?): ImageVector = when (icon) {
    "phone" -> Icons.Default.Phone
    "chat" -> Icons.Default.Chat
    "booking" -> Icons.Default.Book
    "payment" -> Icons.Default.CreditCard
    "report" -> Icons.Default.Description
    else -> Icons.Default.Help
}

@Composable
private fun rememberSupportState(vm: SupportViewModel?, preview: SupportUiState): SupportUiState =
    vm?.uiState?.collectAsStateWithLifecycle()?.value ?: preview

// ---------------------------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------------------------

private val previewNotification = SupportNotification(
    id = "1", type = "booking_confirmed", icon = "booking", title = "Booking Confirmed", body = "Your 50 Ton Mobile Crane booking has been confirmed.",
    is_read = false, group = "today", created_at = "2026-09-05T04:54:00.000Z"
)

@Preview(showBackground = true, heightDp = 800)
@Composable
fun NotificationsScreen(vm: SupportViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberSupportState(vm, SupportUiState(notifications = listOf(previewNotification), summary = NotificationSummary(1, 1), notificationsLoaded = true))
    LaunchedEffect(Unit) {
        vm?.onIntent(SupportIntent.LoadNotifications)
        // While open, the unread count is asked for every 10 seconds
        while (true) {
            delay(10_000)
            vm?.onIntent(SupportIntent.PollNotifications)
        }
    }

    val today = state.notifications.filter { it.group == "today" }
    val earlier = state.notifications.filter { it.group != "today" }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.common_notifications),
        action = {
            if ((state.summary?.unread ?: 0) > 0) {
                Textview(
                    stringResource(R.string.sp_mark_all_read), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { vm?.onIntent(SupportIntent.MarkAllRead) }
                )
            }
        }
    ) {
        if (state.notificationsLoaded && state.notifications.isEmpty()) {
            Textview(stringResource(R.string.sp_all_caught_up), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth().padding(top = dimen_60), textAlign = TextAlign.Center)
        }
        if (today.isNotEmpty()) {
            SectionHeader(stringResource(R.string.sp_today), stringResource(R.string.sp_n_unread, state.summary?.today_unread ?: 0))
            today.forEach { n -> NotificationRow(n, today = true) { vm?.onIntent(SupportIntent.OpenNotification(n)) } }
        }
        if (earlier.isNotEmpty()) {
            // "September & August 2026", from the dates in this section, in the order the server sent them
            val months = earlier.map { serverMonthName(it.created_at) }.filter { it.isNotBlank() }.distinct()
            SectionHeader(stringResource(R.string.sp_earlier), months.joinToString(" & ") + " " + serverYear(earlier.first().created_at))
            earlier.forEach { n -> NotificationRow(n, today = false) { vm?.onIntent(SupportIntent.OpenNotification(n)) } }
        }
        if (state.notificationsHasMore) LaunchedEffect(state.notifications.size) { vm?.onIntent(SupportIntent.LoadMoreNotifications) }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = dimen_4), verticalAlignment = Alignment.CenterVertically) {
        Textview(title.uppercase(), size = font_11, color = appColors.textSecondary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Textview(trailing, size = font_10, color = appColors.textHint)
    }
}

@Composable
private fun NotificationRow(n: SupportNotification, today: Boolean, onClick: () -> Unit) {
    val accent = if (n.icon == "alert") Red else AmberPrimary
    GlassCard(contentPadding = dimen_12, minHeight = dimen_88, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = notificationIcon(n.icon), accent = accent, size = dimen_36)
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(n.title.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                    Textview(if (today) formatServerTime(n.created_at) else formatServerDate(n.created_at), size = font_10, color = appColors.textSecondary)
                    if (n.is_read == false) {
                        Spacer(modifier = Modifier.width(dimen_6))
                        Box(modifier = Modifier.size(dimen_8).clip(CircleShape).background(AmberPrimary))
                    }
                }
                Textview(n.body.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 2)
            }
            // a notification that opens something (e.g. the payment breakdown after an owner accepts) shows an eye
            if (!n.target?.screen.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(dimen_8))
                Box(
                    modifier = Modifier.size(dimen_32).clip(CircleShape).background(AmberPrimary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.ia_view_details),
                        tint = AmberPrimary, modifier = Modifier.size(dimen_18)
                    )
                }
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
            }
        }
    }
}

@Composable
fun AnnouncementScreen(vm: SupportViewModel?, onBack: () -> Unit) {
    val state = rememberSupportState(vm, SupportUiState())
    val item = state.announcement
    BookingScaffold(onBack = onBack, title = stringResource(R.string.sp_announcement)) {
        if (item == null) return@BookingScaffold
        GlassCard(contentPadding = dimen_14) {
            Textview(item.title.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(formatServerDateTime(item.created_at), size = font_10, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(item.body.orEmpty(), size = font_13, color = appColors.textPrimary)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Help & Support
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun HelpSupportHomeScreen(
    vm: SupportViewModel? = null,
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onTile: (SupportTile) -> Unit = {},
    onMyRequests: () -> Unit = {}
) {
    val state = rememberSupportState(
        vm,
        SupportUiState(
            home = SupportHome(
                emergency = EmergencyInfo("Emergency Assistance", "Need immediate help? Contact our 24/7 emergency support.", "+91 80000 99999", true),
                tiles = listOf(SupportTile("call_support", "Call Support", "Talk to our support team.", "phone", "call"), SupportTile("faqs", "FAQs", "Quick answers.", "faq", "faq")),
                call_support = CallSupport("+91 80000 11111", true, "24/7"),
                my_requests = MyRequests("My Support Requests", "Track the status of your support tickets.", 2, 1),
                team = SupportTeam("Our Support Team is Available", "24/7 to assist you with any queries.", "24/7")
            ),
            homeLoaded = true
        )
    )
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm?.onIntent(SupportIntent.LoadHome) }
    var confirmEmergency by remember { mutableStateOf(false) }
    val home = state.home

    BookingScaffold(onBack = onBack, title = stringResource(R.string.mp_help_support), subtitle = stringResource(R.string.sp_here_to_help)) {
        // Search opens the FAQ search
        GlassCard(contentPadding = dimen_12, onClick = onSearch) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_20))
                Spacer(modifier = Modifier.width(dimen_10))
                Textview(home?.search_placeholder ?: stringResource(R.string.sp_search_help), size = font_13, color = appColors.textHint)
            }
        }

        home?.emergency?.let { e ->
            GlassCard(contentPadding = dimen_14, borderColor = Red, onClick = { confirmEmergency = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Warning, accent = Red, size = dimen_40)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(e.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(e.subtitle.orEmpty(), size = font_11, color = appColors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(dimen_8))
                // Without a number the team can still be alerted, but there is nothing to call
                CommonGradientButton(
                    text = stringResource(if (e.available == true) R.string.sp_call_now else R.string.sp_raise_emergency),
                    onClick = { confirmEmergency = true }, height = dimen_40, textSize = font_12,
                    gradientColors = listOf(Red.copy(alpha = 0.25f), Red.copy(alpha = 0.25f)), textColor = Red
                )
            }
            // asks once more right below the card (no popup)
            if (confirmEmergency) {
                com.cranezo.ui.components.InlineConfirm(
                    message = stringResource(R.string.sp_call_emergency_message),
                    confirmText = stringResource(R.string.sp_yes_call),
                    onConfirm = {
                        confirmEmergency = false
                        vm?.onIntent(SupportIntent.Emergency(null, ""))
                    },
                    onCancel = { confirmEmergency = false }
                )
            }
        }

        // The tiles are drawn as the server sent them: 6 for customers and owners, 4 for operators
        home?.tiles.orEmpty().chunked(2).forEach { row ->
            Row(modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                row.forEach { tile ->
                    GlassCard(modifier = Modifier.weight(1f).fillMaxHeight(), contentPadding = dimen_12, minHeight = dimen_112, onClick = {
                        if (tile.action == "call") {
                            if (home?.call_support?.available == true && !home.call_support.phone.isNullOrBlank()) dialNumber(context, home.call_support.phone)
                            else com.cranezo.ui.components.AppMessages.show(context.getString(R.string.sp_phone_unavailable))
                        } else onTile(tile)
                    }) {
                        IconBadge(icon = tileIcon(tile.icon), accent = AmberPrimary, size = dimen_32)
                        Spacer(modifier = Modifier.height(dimen_8))
                        Textview(tile.title.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 2)
                        Textview(tile.subtitle.orEmpty(), size = font_10, color = appColors.textSecondary, maxLines = 3)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        home?.my_requests?.let { m ->
            GlassCard(contentPadding = dimen_12, onClick = onMyRequests) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Description, accent = InfoBlue, size = dimen_36)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(m.title.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(m.subtitle.orEmpty(), size = font_10, color = appColors.textSecondary)
                    }
                    if ((m.unread_replies ?: 0) > 0) {
                        Box(modifier = Modifier.size(dimen_18).clip(CircleShape).background(AmberPrimary), contentAlignment = Alignment.Center) {
                            Textview(m.unread_replies.toString(), size = font_10, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        // ("Our Support Team is Available" card removed)
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// FAQs (also Booking Help and Payment Help: one category, with "still need help")
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun FaqScreen(
    vm: SupportViewModel? = null,
    category: String? = null,
    title: String = "",
    onBack: () -> Unit = {},
    onContact: () -> Unit = {}
) {
    val state = rememberSupportState(vm, SupportUiState(faqs = listOf(Faq(1, null, "Can I cancel a booking?", "Yes. From My Jobs."))))
    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(category) }
    var expanded by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { vm?.onIntent(SupportIntent.LoadFaqCategories) }
    // Search as the user types: after 300 ms, from 2 characters
    LaunchedEffect(query, selected) {
        delay(300)
        vm?.onIntent(SupportIntent.LoadFaqs(selected, query.trim()))
    }

    BookingScaffold(
        onBack = onBack,
        title = title.ifBlank { stringResource(R.string.sp_faqs) },
        bottom = { CommonGradientButton(text = stringResource(R.string.sp_still_need_help), onClick = onContact) }
    ) {
        SearchInput(value = query, onValueChange = { query = it }, placeholder = stringResource(R.string.sp_search_help))
        if (category == null && state.faqCategories.isNotEmpty()) {
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(dimen_8), verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                listOf<String?>(null).plus(state.faqCategories.map { it.code }).forEach { code ->
                    val label = state.faqCategories.firstOrNull { it.code == code }?.name ?: stringResource(R.string.op_tab_all)
                    val isSelected = selected == code
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(dimen_14))
                            .background(if (isSelected) AmberPrimary.copy(alpha = 0.2f) else appColors.subtleFill)
                            .clickable { selected = code }.padding(horizontal = dimen_10, vertical = dimen_6)
                    ) { Textview(label, size = font_11, color = if (isSelected) appColors.primaryText else appColors.textPrimary) }
                }
            }
        }
        if (state.faqsLoaded && state.faqs.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_24), horizontalAlignment = Alignment.CenterHorizontally) {
                Textview(stringResource(R.string.sp_no_results), size = font_14, color = appColors.textSecondary)
                Spacer(modifier = Modifier.height(dimen_12))
                CommonGradientButton(text = stringResource(R.string.sp_contact_support), onClick = onContact, modifier = Modifier.width(dimen_220))
            }
        }
        state.faqs.forEach { faq ->
            val open = expanded == faq.id
            GlassCard(contentPadding = dimen_12, onClick = { expanded = if (open) null else faq.id }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(faq.question.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Icon(if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = appColors.textSecondary)
                }
                if (open) {
                    Spacer(modifier = Modifier.height(dimen_8))
                    Textview(faq.answer.orEmpty(), size = font_12, color = appColors.textSecondary)
                    Spacer(modifier = Modifier.height(dimen_10))
                    val sent = faq.id in state.faqFeedback
                    if (sent) {
                        Textview(stringResource(R.string.sp_thanks_feedback), size = font_11, color = SuccessGreen)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(stringResource(R.string.sp_was_helpful), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                            Textview(stringResource(R.string.sp_yes), size = font_12, color = SuccessGreen, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { faq.id?.let { vm?.onIntent(SupportIntent.FaqFeedback(it, true)) } }.padding(dimen_6))
                            Textview(stringResource(R.string.sp_no), size = font_12, color = Red, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable {
                                faq.id?.let { vm?.onIntent(SupportIntent.FaqFeedback(it, false)) }
                                onContact()
                            }.padding(dimen_6))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Report an Issue / contact support
// ---------------------------------------------------------------------------------------------

@Composable
fun CreateTicketScreen(
    vm: SupportViewModel?,
    category: String,
    onBack: () -> Unit,
    onViewRequest: (String) -> Unit
) {
    val state = rememberSupportState(vm, SupportUiState())
    var subject by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val picker = rememberImagePickerController()
    LaunchedEffect(picker.imageUrl) {
        val uri = picker.imageUrl ?: return@LaunchedEffect
        vm?.onIntent(SupportIntent.AddAttachment(uri))
        picker.clear()
    }
    ImageSourceBottomSheet(picker)
    val created = state.created

    BookingScaffold(
        onBack = onBack,
        success = created != null,
        title = stringResource(if (category == "issue") R.string.sp_report_issue else R.string.sp_contact_support),
        bottom = {
            if (created == null) {
                CommonGradientButton(text = stringResource(R.string.sp_submit), enabled = !state.isUploading, onClick = { vm?.onIntent(SupportIntent.CreateTicket(category, subject, description)) })
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                    CommonGradientButton(
                        text = stringResource(R.string.sp_done), modifier = Modifier.weight(1f), onClick = {
                            vm?.onIntent(SupportIntent.ClearAttachments)
                            onBack()
                        },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                    CommonGradientButton(text = stringResource(R.string.sp_view_request), modifier = Modifier.weight(1f), onClick = { created.ticket_id?.let(onViewRequest) })
                }
            }
        }
    ) {
        if (created != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_24), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBadge(icon = Icons.Default.CheckCircle, accent = SuccessGreen, size = dimen_56, filled = true)
                Spacer(modifier = Modifier.height(dimen_10))
                Textview(stringResource(R.string.sp_request_sent), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(created.ticket_code.orEmpty(), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
            }
            return@BookingScaffold
        }
        GlassInputField(label = stringResource(R.string.sp_subject), value = subject, onValueChange = { subject = it.take(120) }, placeholder = stringResource(R.string.sp_subject_hint))
        GlassInputField(
            label = stringResource(R.string.sp_describe), value = description, onValueChange = { description = it.take(1000) },
            placeholder = stringResource(R.string.sp_describe_hint), singleLine = false, minHeight = dimen_120, isRequired = true
        )
        Textview(stringResource(R.string.sp_photos), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        AttachmentRow(state, picker::openChooser) { vm?.onIntent(SupportIntent.RemoveAttachment(it)) }
    }
}

@Composable
private fun AttachmentRow(state: SupportUiState, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
        state.attachments.forEach { path ->
            Box(modifier = Modifier.size(dimen_56).clip(RoundedCornerShape(dimen_10))) {
                ShimmerImage(model = fileUrl(path), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                Icon(
                    Icons.Default.Close, contentDescription = null, tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd).size(dimen_16).background(Color.Black.copy(alpha = 0.5f), CircleShape).clickable { onRemove(path) }
                )
            }
        }
        if (state.attachments.size < 5) {
            Box(
                modifier = Modifier.size(dimen_56).clip(RoundedCornerShape(dimen_10)).background(appColors.subtleFill).clickable(enabled = !state.isUploading) { onAdd() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = appColors.textSecondary) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// My Support Requests
// ---------------------------------------------------------------------------------------------

@Composable
private fun statusColor(label: String?): Color = when (label) {
    "Open" -> InfoBlue
    "In Progress" -> AmberPrimary
    "Resolved" -> SuccessGreen
    else -> appColors.textSecondary
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun TicketsScreen(vm: SupportViewModel? = null, onBack: () -> Unit = {}, onOpen: (String) -> Unit = {}) {
    val state = rememberSupportState(
        vm, SupportUiState(tickets = listOf(TicketItem("1", "TKT-102137", "issue", "Report an Issue", "App crashes on payment", 1, "In Progress", last_message = "Thanks, we are checking.", unread_replies = 1)), ticketsLoaded = true)
    )
    LaunchedEffect(Unit) { vm?.onIntent(SupportIntent.LoadTickets(state.ticketTab)) }

    BookingScaffold(onBack = onBack, title = stringResource(R.string.sp_my_requests)) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf("open" to R.string.sp_tab_open, "closed" to R.string.sp_tab_closed, "all" to R.string.op_tab_all).forEach { (key, label) ->
                val selected = key == state.ticketTab
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(dimen_14)).background(if (selected) AmberPrimary.copy(alpha = 0.2f) else appColors.subtleFill)
                        .clickable { vm?.onIntent(SupportIntent.LoadTickets(key)) }.padding(horizontal = dimen_14, vertical = dimen_6)
                ) { Textview(stringResource(label), size = font_12, color = if (selected) appColors.primaryText else appColors.textPrimary) }
            }
        }
        if (state.ticketsLoaded && state.tickets.isEmpty()) {
            Textview(stringResource(R.string.sp_no_requests), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth().padding(top = dimen_40), textAlign = TextAlign.Center)
        }
        state.tickets.forEach { t ->
            val unread = (t.unread_replies ?: 0) > 0
            GlassCard(contentPadding = dimen_12, minHeight = dimen_88, onClick = { t.ticket_id?.let(onOpen) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(t.ticket_code.orEmpty(), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                    BookingTag(t.status_label.orEmpty(), statusColor(t.status_label))
                }
                Textview(t.category_label.orEmpty(), size = font_10, color = appColors.textHint)
                Textview(t.subject.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(t.last_message.orEmpty(), size = font_11, color = if (unread) appColors.textPrimary else appColors.textSecondary, maxLines = 1, modifier = Modifier.weight(1f))
                    Textview(formatServerDate(t.last_message_at), size = font_10, color = appColors.textSecondary)
                    if (unread) {
                        Spacer(modifier = Modifier.width(dimen_6))
                        Box(modifier = Modifier.size(dimen_8).clip(CircleShape).background(AmberPrimary))
                    }
                }
            }
        }
        if (state.ticketsHasMore) LaunchedEffect(state.tickets.size) { vm?.onIntent(SupportIntent.LoadMoreTickets) }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// The conversation: a support request, or the chat with support
// ---------------------------------------------------------------------------------------------

@Composable
private fun MessageBubble(m: TicketMessage) {
    val context = LocalContext.current
    when {
        m.sender_type == "system" -> Textview(
            m.body.orEmpty(), size = font_11, color = appColors.textSecondary, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6, horizontal = dimen_24)
        )
        else -> {
            val mine = m.is_mine == true
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                Column(
                    modifier = Modifier.widthIn(max = dimen_280)
                        .clip(RoundedCornerShape(dimen_14))
                        .background(if (mine) AmberPrimary.copy(alpha = 0.85f) else appColors.subtleFill)
                        .padding(dimen_10)
                ) {
                    if (!mine) Textview(m.sender_label.orEmpty(), size = font_10, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
                    if (!m.body.isNullOrBlank()) Textview(m.body, size = font_13, color = if (mine) Color.Black else appColors.textPrimary)
                    m.attachments.orEmpty().forEach { path ->
                        ShimmerImage(
                            model = fileUrl(path), contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.padding(top = dimen_6).size(dimen_100).clip(RoundedCornerShape(dimen_8))
                                .clickable { com.cranezo.ui.cranes.openFile(context, path) }
                        )
                    }
                    Textview(formatServerTime(m.created_at), size = font_10, color = if (mine) Color.Black.copy(alpha = 0.6f) else appColors.textHint, modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun ConversationScreen(
    vm: SupportViewModel? = null,
    chat: Boolean = false,
    onBack: () -> Unit = {},
    onNewRequest: () -> Unit = {}
) {
    val state = rememberSupportState(
        vm, SupportUiState(ticket = Ticket("1", "TKT-1", "issue", "Report an Issue", "App crashes", 1, "In Progress", can_reply = true, messages = listOf(
            TicketMessage(1, "user", true, "You", "The app closes when I tap pay.", null, "2026-09-05T04:00:00.000Z"),
            TicketMessage(2, "admin", false, "Cranzo Support", "Thanks, we are checking this now.", null, "2026-09-05T04:05:00.000Z")
        )), ticketLoaded = true)
    )
    val ticket = state.ticket
    var text by rememberSaveable { mutableStateOf("") }
    var confirmClose by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val picker = rememberImagePickerController()
    LaunchedEffect(picker.imageUrl) {
        val uri = picker.imageUrl ?: return@LaunchedEffect
        vm?.onIntent(SupportIntent.AddAttachment(uri))
        picker.clear()
    }
    ImageSourceBottomSheet(picker)

    // Refreshed while open: every 5 seconds in the chat, 10 in a request
    LaunchedEffect(ticket?.ticket_id) {
        while (true) {
            delay(if (chat) 5_000 else 10_000)
            vm?.onIntent(SupportIntent.PollTicket)
        }
    }
    // The newest message is always in view
    val count = ticket?.messages?.size ?: 0
    LaunchedEffect(count) { if (count > 0) listState.animateScrollToItem(count - 1) }

    ScreenBackground(darkImageRes = R.drawable.app_bg) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            CraneScreenHeader(
                title = if (chat) stringResource(R.string.sp_chat_title) else ticket?.ticket_code ?: stringResource(R.string.sp_my_requests),
                subtitle = if (chat) stringResource(R.string.sp_chat_sub) else ticket?.subject,
                onBack = onBack,
                action = {
                    if (!chat && ticket != null && ticket.can_reply == true) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_22).clickable { confirmClose = true })
                    }
                }
            )
            if (ticket != null && !chat) {
                Row(modifier = Modifier.padding(horizontal = dimen_20), verticalAlignment = Alignment.CenterVertically) {
                    BookingTag(ticket.status_label.orEmpty(), statusColor(ticket.status_label))
                    Spacer(modifier = Modifier.width(dimen_8))
                    Textview(ticket.category_label.orEmpty(), size = font_11, color = appColors.textSecondary)
                }
            }
            // Close request asks once more under the header (no popup)
            if (confirmClose) {
                com.cranezo.ui.components.InlineConfirm(
                    message = stringResource(R.string.sp_close_message),
                    confirmText = stringResource(R.string.sp_close_request),
                    modifier = Modifier.padding(horizontal = dimen_20, vertical = dimen_8),
                    onConfirm = {
                        confirmClose = false
                        vm?.onIntent(SupportIntent.CloseTicket)
                    },
                    onCancel = { confirmClose = false }
                )
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = dimen_20),
                verticalArrangement = Arrangement.spacedBy(dimen_8)
            ) {
                item { Spacer(modifier = Modifier.height(dimen_8)) }
                items(ticket?.messages.orEmpty()) { MessageBubble(it) }
                item { Spacer(modifier = Modifier.height(dimen_8)) }
            }
            if (ticket != null && ticket.can_reply != true) {
                // Closed (or ended chat): nothing can be written, a new one can be started
                Column(modifier = Modifier.fillMaxWidth().padding(dimen_20), horizontalAlignment = Alignment.CenterHorizontally) {
                    Textview(stringResource(if (chat) R.string.sp_chat_ended else R.string.sp_request_closed), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(dimen_8))
                    CommonGradientButton(
                        text = stringResource(if (chat) R.string.sp_new_chat else R.string.sp_new_request),
                        onClick = { if (chat) vm?.onIntent(SupportIntent.OpenChat) else onNewRequest() }
                    )
                }
            } else if (ticket != null) {
                Column(modifier = Modifier.padding(horizontal = dimen_20, vertical = dimen_8)) {
                    if (state.attachments.isNotEmpty()) AttachmentRow(state, picker::openChooser) { vm?.onIntent(SupportIntent.RemoveAttachment(it)) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_24).clickable { picker.openChooser() })
                        Spacer(modifier = Modifier.width(dimen_8))
                        Box(modifier = Modifier.weight(1f)) {
                            GlassInputField(label = "", value = text, onValueChange = { text = it.take(1000) }, placeholder = stringResource(R.string.sp_type_message))
                        }
                        Spacer(modifier = Modifier.width(dimen_8))
                        Icon(
                            Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = AmberPrimary,
                            modifier = Modifier.size(dimen_28).clickable(enabled = !state.isSending) {
                                vm?.onIntent(SupportIntent.SendReply(text))
                                text = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

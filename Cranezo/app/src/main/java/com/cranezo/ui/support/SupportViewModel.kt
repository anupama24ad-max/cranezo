package com.cranezo.ui.support

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.support.model.SupportEffect
import com.cranezo.ui.support.model.SupportIntent
import com.cranezo.ui.support.model.SupportNotification
import com.cranezo.ui.support.model.SupportUiState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fetchCurrentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** Notifications and Help & Support: the same screens and APIs for every role. */
@HiltViewModel
class SupportViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SupportEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private companion object {
        const val PAGE = 20
    }

    private var chatMode = false

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    fun onIntent(intent: SupportIntent) {
        when (intent) {
            SupportIntent.LoadNotifications -> loadNotifications(reset = true, silent = false)
            SupportIntent.LoadMoreNotifications -> loadNotifications(reset = false, silent = true)
            SupportIntent.PollNotifications -> pollNotifications()
            is SupportIntent.OpenNotification -> openNotification(intent.item)
            SupportIntent.MarkAllRead -> markRead(JSONObject().put("all", true))

            SupportIntent.LoadHome -> loadHome()

            SupportIntent.LoadFaqCategories -> runWhenOnline {
                repo.supFaqCategories(headers()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(faqCategories = r?.data.orEmpty()) } },
                    failure = { _, _, m -> fail(m) }
                )
            }
            is SupportIntent.LoadFaqs -> loadFaqs(intent.category, intent.search)
            is SupportIntent.FaqFeedback -> faqFeedback(intent.faqId, intent.helpful)

            is SupportIntent.LoadTickets -> loadTickets(intent.tab, reset = true)
            SupportIntent.LoadMoreTickets -> loadTickets(_uiState.value.ticketTab, reset = false)
            is SupportIntent.OpenTicket -> openTicket(intent.ticketId, chat = false)
            SupportIntent.OpenChat -> openTicket(null, chat = true)
            SupportIntent.PollTicket -> pollTicket()
            is SupportIntent.SendReply -> sendReply(intent.message)
            SupportIntent.CloseTicket -> closeTicket()

            is SupportIntent.CreateTicket -> createTicket(intent.category, intent.subject, intent.description)
            is SupportIntent.AddAttachment -> addAttachment(intent.uri)
            is SupportIntent.RemoveAttachment -> _uiState.update { it.copy(attachments = it.attachments - intent.path) }
            SupportIntent.ClearAttachments -> _uiState.update { it.copy(attachments = emptyList(), created = null) }

            is SupportIntent.Emergency -> emergency(intent.bookingId, intent.description)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Notifications
    // ---------------------------------------------------------------------------------------

    private fun loadNotifications(reset: Boolean, silent: Boolean) {
        val s = _uiState.value
        if (!reset && !s.notificationsHasMore) return
        val min = if (reset) 0 else s.notificationsNextMin
        runWhenOnline {
            if (reset) {
                repo.supSummary(headers()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(summary = r?.data) } }
                )
            }
            val body = JSONObject().put("min", min).put("max", min + PAGE)
            repo.supNotifications(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
                        val known = if (reset) emptyList() else it.notifications
                        it.copy(
                            notifications = known + page.filter { n -> known.none { k -> k.id == n.id } },
                            notificationsLoaded = true,
                            notificationsHasMore = r?.pagination?.has_more == true,
                            notificationsNextMin = r?.pagination?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(notificationsLoaded = true) }
                    if (!silent) fail(m)
                }
            )
        }
    }

    /** Every 10 seconds while the screen is open: the list is fetched again when the unread count grows. */
    private fun pollNotifications() {
        viewModelScope.launch {
            var unread: Int? = null
            repo.supSummary(headers()).isRequestCallSuspendSuccess(success = { r ->
                unread = r?.data?.unread
                _uiState.update { it.copy(summary = r?.data) }
            })
            val known = _uiState.value.notifications.count { it.is_read == false }
            if ((unread ?: 0) > known) loadNotifications(reset = true, silent = true)
        }
    }

    /** Marked read first, then its screen opens. */
    private fun openNotification(item: SupportNotification) {
        if (item.is_read != true) item.id?.let { id -> markRead(JSONObject().put("ids", JSONArray().put(id))) }
        viewModelScope.launch {
            if (item.target?.screen == "announcement") _uiState.update { it.copy(announcement = item) }
            _effect.send(SupportEffect.OpenTarget(item))
        }
    }

    private fun markRead(body: JSONObject) {
        viewModelScope.launch {
            repo.supMarkRead(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val all = body.optBoolean("all", false)
                    val ids = body.optJSONArray("ids")
                    val idSet = (0 until (ids?.length() ?: 0)).map { ids!!.getString(it) }.toSet()
                    _uiState.update { s ->
                        s.copy(
                            notifications = s.notifications.map { n -> if (all || n.id in idSet) n.copy(is_read = true) else n },
                            summary = s.summary?.copy(
                                unread = r?.data?.unread ?: s.summary.unread,
                                today_unread = if (all) 0 else s.summary.today_unread
                            )
                        )
                    }
                }
            )
        }
    }

    // ---------------------------------------------------------------------------------------
    // Help & Support home and FAQs
    // ---------------------------------------------------------------------------------------

    private fun loadHome() {
        runWhenOnline {
            repo.supHome(headers()).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(home = r?.data, homeLoaded = true) } },
                failure = { _, _, m ->
                    _uiState.update { it.copy(homeLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    private fun loadFaqs(category: String?, search: String) {
        viewModelScope.launch {
            val body = JSONObject().put("min", 0).put("max", PAGE)
            category?.let { body.put("category_code", it) }
            if (search.length >= 2) body.put("search", search)
            repo.supFaqs(headers(), body).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(faqs = r?.data.orEmpty(), faqsLoaded = true) } },
                failure = { _, _, m ->
                    _uiState.update { it.copy(faqsLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    /** Once per FAQ per session. */
    private fun faqFeedback(faqId: Int, helpful: Boolean) {
        if (faqId in _uiState.value.faqFeedback) return
        _uiState.update { it.copy(faqFeedback = it.faqFeedback + faqId) }
        viewModelScope.launch {
            repo.supFaqFeedback(headers(), JSONObject().put("faq_id", faqId).put("helpful", helpful)).isRequestCallSuspendSuccess()
        }
    }

    // ---------------------------------------------------------------------------------------
    // Support requests and the conversation
    // ---------------------------------------------------------------------------------------

    private fun loadTickets(tab: String, reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.ticketsHasMore) return
        val min = if (reset) 0 else s.ticketsNextMin
        if (reset) _uiState.update { it.copy(ticketTab = tab, tickets = if (tab != it.ticketTab) emptyList() else it.tickets, ticketsLoaded = false) }
        runWhenOnline {
            val body = JSONObject().put("min", min).put("max", min + PAGE)
            if (tab != "all") body.put("status", tab)
            repo.supTickets(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
                        if (it.ticketTab != tab) return@update it
                        val known = if (reset) emptyList() else it.tickets
                        it.copy(
                            tickets = known + page.filter { t -> known.none { k -> k.ticket_id == t.ticket_id } },
                            ticketsLoaded = true,
                            ticketsHasMore = r?.pagination?.has_more == true,
                            ticketsNextMin = r?.pagination?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(ticketsLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    /** tickets/get, or chat (opens the running chat or starts a new one). Opening marks the replies read. */
    private fun openTicket(ticketId: String?, chat: Boolean) {
        chatMode = chat
        _uiState.update { it.copy(ticket = null, ticketLoaded = false, attachments = emptyList()) }
        runWhenOnline {
            setIsLoading(true)
            fetchTicket(ticketId)
            setIsLoading(false)
        }
    }

    private suspend fun fetchTicket(ticketId: String?) {
        val path = if (chatMode) "chat" else "tickets/get"
        val body = JSONObject().also { b -> ticketId?.let { b.put("ticket_id", it) } }
        repo.supTicket(headers(), path, body).isRequestCallSuspendSuccess(
            success = { r -> _uiState.update { it.copy(ticket = r?.data, ticketLoaded = true) } },
            failure = { _, _, m ->
                _uiState.update { it.copy(ticketLoaded = true) }
                fail(m)
            }
        )
    }

    private fun pollTicket() {
        val id = _uiState.value.ticket?.ticket_id
        if (!chatMode && id == null) return
        viewModelScope.launch {
            val path = if (chatMode) "chat" else "tickets/get"
            val body = JSONObject().also { b -> id?.let { b.put("ticket_id", it) } }
            repo.supTicket(headers(), path, body).isRequestCallSuspendSuccess(
                success = { r -> r?.data?.let { t -> _uiState.update { it.copy(ticket = t) } } }
            )
        }
    }

    private fun sendReply(message: String) {
        val s = _uiState.value
        val id = s.ticket?.ticket_id ?: return
        if (message.isBlank() && s.attachments.isEmpty()) return
        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val body = JSONObject().put("ticket_id", id).put("message", message)
            if (s.attachments.isNotEmpty()) body.put("attachments", JSONArray(s.attachments))
            repo.supReply(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    r?.data?.let { m ->
                        _uiState.update { it.copy(ticket = it.ticket?.copy(messages = it.ticket.messages.orEmpty() + m), attachments = emptyList(), isSending = false) }
                    }
                    // A resolved request opens again when answered: the status is read again
                    pollTicket()
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(isSending = false) }
                    fail(m)
                    pollTicket()
                }
            )
        }
    }

    private fun closeTicket() {
        val id = _uiState.value.ticket?.ticket_id ?: return
        runWhenOnline {
            setIsLoading(true)
            repo.supCloseTicket(headers(), JSONObject().put("ticket_id", id)).isRequestCallSuspendSuccess(
                success = { showSuccess(string(R.string.sp_msg_closed), autoHideMs = 2000L) },
                failure = { _, _, m -> fail(m) }
            )
            fetchTicket(id)
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // New request, photos
    // ---------------------------------------------------------------------------------------

    private fun createTicket(category: String, subject: String, description: String) {
        if (description.trim().length < 5) {
            showError(string(R.string.sp_msg_describe))
            return
        }
        val s = _uiState.value
        runWhenOnline {
            setIsLoading(true)
            val body = JSONObject().put("category", category).put("description", description.trim())
            if (subject.isNotBlank()) body.put("subject", subject.trim())
            if (s.attachments.isNotEmpty()) body.put("photos", JSONArray(s.attachments))
            repo.supTicket(headers(), "tickets/create", body).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(created = r?.data, attachments = emptyList()) } },
                failure = { _, _, m -> fail(m) }
            )
            setIsLoading(false)
        }
    }

    private fun addAttachment(uri: String) {
        if (_uiState.value.attachments.size >= 5) return
        _uiState.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            val part = withContext(Dispatchers.IO) {
                val file = AppMethods.getFileFromUri(app, Uri.parse(uri)) ?: return@withContext null
                val rb = AppMethods.createRequestBody(file, app, Uri.parse(uri)) ?: return@withContext null
                MultipartBody.Part.createFormData(AppStrings.InputData.file, file.name, rb)
            }
            if (part == null) {
                _uiState.update { it.copy(isUploading = false) }
                showError(string(R.string.msg_file_unreadable))
                return@launch
            }
            runWhenOnline {
                repo.uploadApi(headers(), part).isRequestCallSuspendSuccess(
                    success = { r ->
                        r?.data?.file_url?.let { path -> _uiState.update { it.copy(attachments = it.attachments + path) } }
                    },
                    failure = { _, _, m -> fail(m) }
                )
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Emergency: the request first, the call second
    // ---------------------------------------------------------------------------------------

    private fun emergency(bookingId: String?, description: String) {
        viewModelScope.launch {
            val body = JSONObject()
            bookingId?.let { body.put("booking_id", it) }
            if (description.isNotBlank()) body.put("description", description)
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(app)?.let { body.put("latitude", it.latitude).put("longitude", it.longitude) }
            }
            runWhenOnline {
                setIsLoading(true)
                repo.supEmergency(headers(), body).isRequestCallSuspendSuccess(
                    success = { r ->
                        r?.data?.message?.let { showSuccess(it, autoHideMs = 3500L) }
                        // The team knows now, so the call can be made even if it drops
                        r?.data?.emergency_phone?.takeIf { it.isNotBlank() }?.let { _effect.send(SupportEffect.Dial(it)) }
                    },
                    failure = { _, _, m -> fail(m) }
                )
                setIsLoading(false)
            }
        }
    }
}

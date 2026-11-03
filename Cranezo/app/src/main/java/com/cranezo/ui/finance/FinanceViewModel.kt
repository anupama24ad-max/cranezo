package com.cranezo.ui.finance

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.finance.model.AvailableJob
import com.cranezo.ui.finance.model.BillingSettings
import com.cranezo.ui.finance.model.BookingPick
import com.cranezo.ui.finance.model.Charge
import com.cranezo.ui.finance.model.ComplaintCategory
import com.cranezo.ui.finance.model.ComplaintItem
import com.cranezo.ui.finance.model.ComplaintOptions
import com.cranezo.ui.finance.model.JobRequestItem
import com.cranezo.ui.finance.model.OperatorDues
import com.cranezo.ui.finance.model.OperatorEarnings
import com.cranezo.ui.finance.model.OwnerJobMoney
import com.cranezo.ui.finance.model.OwnerMoneySummary
import com.cranezo.ui.finance.model.PendingJobs
import com.cranezo.ui.finance.model.PlatformBalance
import com.cranezo.ui.finance.model.SettlementItem
import com.cranezo.ui.finance.model.TransactionItem
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fetchCurrentLocation
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject

data class FinanceUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    // inline result shown on the page itself (no popups)
    val notice: String? = null,
    // owner
    val billing: BillingSettings? = null,
    val jobMoney: OwnerJobMoney? = null,
    val dues: OperatorDues? = null,
    val dueJobs: PendingJobs? = null,
    val platform: PendingJobs? = null,
    val platformBalance: PlatformBalance? = null,
    val settlements: List<SettlementItem> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val requests: List<JobRequestItem> = emptyList(),
    val complaints: List<ComplaintItem> = emptyList(),
    val complaint: ComplaintItem? = null,
    // operator
    val available: List<AvailableJob> = emptyList(),
    val myRequests: List<JobRequestItem> = emptyList(),
    val opEarnings: OperatorEarnings? = null,
    // customer
    val categories: List<ComplaintCategory> = emptyList(),
    val bookings: List<BookingPick> = emptyList(),
    val createdComplaintId: String? = null,
    // which pages finished their first load (so "empty" text is not shown while loading)
    val loaded: Set<String> = emptySet()
)

/** One view model for every finance page of every role. Each page loads what it needs. */
@HiltViewModel
class FinanceViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _ui = MutableStateFlow(FinanceUiState())
    val ui: StateFlow<FinanceUiState> = _ui.asStateFlow()

    private val gson = Gson()
    private fun headers() = AppMethods.getToken(sm)
    val role: String get() = sm.getUserRole()

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    private fun <T> parse(data: JsonElement?, type: Type): T? =
        if (data == null || data.isJsonNull) null else runCatching { gson.fromJson<T>(data, type) }.getOrNull()

    private inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type

    private fun markLoaded(key: String) = _ui.update { it.copy(loaded = it.loaded + key, loading = false) }

    /** POST [path] and hand the parsed data to [onData]. Errors show the server's message. */
    private fun <T> call(
        path: String,
        body: JSONObject,
        type: Type,
        page: String? = null,
        saving: Boolean = false,
        onFail: () -> Unit = {},
        onData: (T?) -> Unit
    ) {
        _ui.update { if (saving) it.copy(saving = true, notice = null) else it.copy(loading = true) }
        runWhenOnline {
            repo.financePost(path, headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    onData(parse<T>(r?.data, type))
                    if (saving) {
                        _ui.update { it.copy(saving = false, notice = r?.message) }
                    }
                    if (page != null) markLoaded(page) else _ui.update { it.copy(loading = false) }
                },
                failure = { _, _, m ->
                    _ui.update { it.copy(saving = false, loading = false) }
                    if (page != null) markLoaded(page)
                    onFail()
                    fail(m)
                }
            )
        }
    }

    private fun page(min: Int = 0, max: Int = 50) = JSONObject().put("min", min).put("max", max)

    fun clearNotice() = _ui.update { it.copy(notice = null) }

    /** The new complaint page navigates once, then forgets the id */
    fun consumeCreatedComplaint(): String? {
        val id = _ui.value.createdComplaintId
        _ui.update { it.copy(createdComplaintId = null) }
        return id
    }

    // ======================================================================== owner

    fun loadBilling() = call<BillingSettings>("owner/billing/settings", JSONObject(), typeOf<BillingSettings>(), page = "billing") { d ->
        _ui.update { it.copy(billing = d) }
    }

    fun saveBilling(gst: String, gstNumber: String, charges: List<Charge>, commissionType: String, commissionValue: String) {
        val list = JSONArray()
        charges.filter { !it.name.isNullOrBlank() }.forEach { c ->
            list.put(JSONObject().put("name", c.name).put("type", c.type ?: "fixed").put("value", c.value ?: 0.0))
        }
        val body = JSONObject()
            .put("gst_percent", gst.toDoubleOrNull() ?: 0.0)
            .put("gst_number", gstNumber.trim())
            .put("charges", list)
            .put("default_commission_type", commissionType)
            .put("default_commission_value", commissionValue.toDoubleOrNull() ?: 0.0)
        call<BillingSettings>("owner/billing/settings/save", body, typeOf<BillingSettings>(), saving = true) { d ->
            _ui.update { it.copy(billing = it.billing?.copy(
                gst_percent = d?.gst_percent, gst_number = d?.gst_number, charges = d?.charges,
                default_commission_type = d?.default_commission_type, default_commission_value = d?.default_commission_value
            ) ?: d) }
        }
    }

    fun loadJobMoney(bookingId: String) =
        call<OwnerJobMoney>("owner/jobs/get", JSONObject().put("booking_id", bookingId), typeOf<OwnerJobMoney>(), page = "job_money") { d ->
            _ui.update { it.copy(jobMoney = d) }
        }

    fun saveCommission(bookingId: String, type: String, value: String) {
        val amount = value.toDoubleOrNull()
        if (amount == null) {
            fail(string(R.string.fin_enter_amount))
            return
        }
        val body = JSONObject().put("booking_id", bookingId).put("type", type).put("value", amount)
        call<OwnerJobMoney>("owner/jobs/commission", body, typeOf<OwnerJobMoney>(), saving = true) { d ->
            _ui.update { it.copy(jobMoney = d) }
        }
    }

    fun loadDues() = call<OperatorDues>("owner/operator-payments", JSONObject(), typeOf<OperatorDues>(), page = "dues") { d ->
        _ui.update { it.copy(dues = d) }
    }

    fun loadDueJobs(operatorId: String) =
        call<PendingJobs>("owner/operator-payments/jobs", JSONObject().put("operator_id", operatorId), typeOf<PendingJobs>(), page = "due_jobs") { d ->
            _ui.update { it.copy(dueJobs = d) }
        }

    fun payOperator(operatorId: String, bookingIds: List<String>, method: String, reference: String, onDone: () -> Unit) {
        if (bookingIds.isEmpty()) {
            fail(string(R.string.fin_select_jobs))
            return
        }
        val body = JSONObject()
            .put("operator_id", operatorId)
            .put("booking_ids", JSONArray(bookingIds))
            .put("method", method)
            .put("reference", reference.trim())
        call<SettlementItem>("owner/operator-payments/pay", body, typeOf<SettlementItem>(), saving = true) {
            loadDues()
            onDone()
        }
    }

    fun loadPlatform() {
        call<PendingJobs>("owner/settlements/platform", JSONObject(), typeOf<PendingJobs>(), page = "platform") { d ->
            _ui.update { it.copy(platform = d) }
        }
        call<OwnerMoneySummary>("owner/earnings/summary", JSONObject().put("period", "all"), typeOf<OwnerMoneySummary>()) { d ->
            _ui.update { it.copy(platformBalance = d?.platform_balance) }
        }
        loadSettlements("owner/settlements")
    }

    fun loadSettlements(path: String) = call<List<SettlementItem>>(path, page(), typeOf<List<SettlementItem>>(), page = "settlements") { d ->
        _ui.update { it.copy(settlements = d.orEmpty()) }
    }

    fun loadTransactions() {
        val path = if (role == "CRANE_OWNER") "owner/transactions" else "customer/transactions"
        call<List<TransactionItem>>(path, page(), typeOf<List<TransactionItem>>(), page = "transactions") { d ->
            _ui.update { it.copy(transactions = d.orEmpty()) }
        }
    }

    fun loadRequests() = call<List<JobRequestItem>>("owner/job-requests", page(), typeOf<List<JobRequestItem>>(), page = "requests") { d ->
        _ui.update { it.copy(requests = d.orEmpty()) }
    }

    fun respondRequest(requestId: String, accept: Boolean, reason: String = "") {
        val body = JSONObject().put("request_id", requestId).put("action", if (accept) "accept" else "reject")
        if (reason.isNotBlank()) body.put("reason", reason.trim())
        call<JobRequestItem>("owner/job-requests/respond", body, typeOf<JobRequestItem>(), saving = true) { loadRequests() }
    }

    // ======================================================================== complaints (owner and customer)

    private val complaintBase get() = if (role == "CRANE_OWNER") "owner/complaints" else "customer/complaints"

    fun loadComplaints() = call<List<ComplaintItem>>(complaintBase, page(), typeOf<List<ComplaintItem>>(), page = "complaints") { d ->
        _ui.update { it.copy(complaints = d.orEmpty()) }
    }

    fun openComplaint(id: String) {
        _ui.update { it.copy(complaint = null, notice = null) }
        call<ComplaintItem>("$complaintBase/get", JSONObject().put("complaint_id", id), typeOf<ComplaintItem>(), page = "complaint") { d ->
            _ui.update { it.copy(complaint = d) }
        }
    }

    /** Owner: message and/or status (in_review, resolved). Customer: message only. */
    fun replyComplaint(id: String, message: String, status: String? = null) {
        if (message.isBlank() && status == null) {
            fail(string(R.string.fin_write_message))
            return
        }
        val body = JSONObject().put("complaint_id", id)
        if (message.isNotBlank()) body.put("message", message.trim())
        status?.let { body.put("status", it) }
        val path = if (role == "CRANE_OWNER") "owner/complaints/update" else "customer/complaints/reply"
        call<ComplaintItem>(path, body, typeOf<ComplaintItem>(), saving = true) { d ->
            _ui.update { it.copy(complaint = d) }
        }
    }

    fun loadComplaintForm() {
        _ui.update { it.copy(createdComplaintId = null) }
        call<ComplaintOptions>("customer/complaints/options", JSONObject(), typeOf<ComplaintOptions>(), page = "complaint_form") { d ->
            _ui.update { it.copy(categories = d?.categories.orEmpty()) }
        }
        val body = page(0, 30).put("tab", "completed")
        call<List<BookingPick>>("customer/booking/list", body, typeOf<List<BookingPick>>()) { d ->
            _ui.update { it.copy(bookings = d.orEmpty()) }
        }
    }

    fun createComplaint(bookingId: String?, category: String, description: String) {
        if (category.isBlank()) {
            fail(string(R.string.fin_choose_problem))
            return
        }
        if (description.trim().length < 10) {
            fail(string(R.string.fin_describe_problem))
            return
        }
        val body = JSONObject().put("category", category).put("description", description.trim())
        bookingId?.let { body.put("booking_id", it) }
        call<ComplaintItem>("customer/complaints/create", body, typeOf<ComplaintItem>(), saving = true) { d ->
            _ui.update { it.copy(complaint = d, createdComplaintId = d?.id) }
        }
    }

    // ======================================================================== operator

    fun loadAvailable() {
        runWhenOnline {
            val body = page()
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(app)?.let { body.put("latitude", it.latitude).put("longitude", it.longitude) }
            }
            call<List<AvailableJob>>("operator/available-jobs", body, typeOf<List<AvailableJob>>(), page = "available") { d ->
                _ui.update { it.copy(available = d.orEmpty()) }
            }
        }
    }

    fun requestJob(bookingId: String, message: String) {
        val body = JSONObject().put("booking_id", bookingId)
        if (message.isNotBlank()) body.put("message", message.trim())
        call<JobRequestItem>("operator/available-jobs/request", body, typeOf<JobRequestItem>(), saving = true) { loadAvailable() }
    }

    fun loadMyRequests() = call<List<JobRequestItem>>("operator/job-requests", page(), typeOf<List<JobRequestItem>>(), page = "my_requests") { d ->
        _ui.update { it.copy(myRequests = d.orEmpty()) }
    }

    fun withdrawRequest(requestId: String) =
        call<JobRequestItem>("operator/job-requests/withdraw", JSONObject().put("request_id", requestId), typeOf<JobRequestItem>(), saving = true) {
            loadMyRequests()
        }

    fun loadOperatorPayments() {
        call<OperatorEarnings>("operator/earnings/summary", JSONObject(), typeOf<OperatorEarnings>()) { d ->
            _ui.update { it.copy(opEarnings = d) }
        }
        loadSettlements("operator/settlements")
    }
}

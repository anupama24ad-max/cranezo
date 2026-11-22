package com.cranezo.ui.owneroperators

import android.app.Application
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import javax.inject.Inject

data class OperatorTeamState(
    val team: List<TeamOperator> = emptyList(),
    val sent: List<TeamInvite> = emptyList(),
    val loaded: Boolean = false,
    val busy: Boolean = false,
    /** The operator found by a search (null until one is found) */
    val found: OperatorBrief? = null,
    /** Set when an add / request / terms save went through: the screen goes back to the list */
    val doneTick: Int = 0,
    /** The operator whose mobile number is being verified with an OTP (null = no OTP screen) */
    val verifying: TeamOperator? = null,
    /** The fixed test code, when the server is in test mode (it is never a real SMS code) */
    val testOtp: String? = null,
    // operator side: the requests received
    val received: List<TeamInvite> = emptyList(),
    val waiting: Int = 0
)

/** The owner's operator team (list, add, find, request, pay terms) and, for an operator, the requests received */
@HiltViewModel
class OperatorTeamViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _state = MutableStateFlow(OperatorTeamState())
    val state: StateFlow<OperatorTeamState> = _state.asStateFlow()
    private val gson = Gson()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    private inline fun <reified T> post(path: String, body: JSONObject, crossinline onOk: (T?) -> Unit, noinline onFail: (() -> Unit)? = null) {
        _state.update { it.copy(busy = true) }
        runWhenOnline {
            repo.financePost(path, headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val data = r?.data?.takeIf { !it.isJsonNull }?.let { gson.fromJson<T>(it, object : TypeToken<T>() {}.type) }
                    _state.update { it.copy(busy = false) }
                    onOk(data)
                },
                failure = { _, _, m ->
                    _state.update { it.copy(busy = false) }
                    fail(m)
                    onFail?.invoke()
                }
            )
        }
    }

    private fun termsJson(t: PayTerms, body: JSONObject = JSONObject()): JSONObject = body
        .put("salary_type", t.salary_type)
        .put("monthly_salary", t.monthly_salary ?: 0.0)
        .put("daily_wage", t.daily_wage ?: 0.0)
        .put("batta", t.batta ?: 0.0)

    // ---------------------------------------------------------------- owner

    fun load() {
        post<List<TeamOperator>>("owner/operators", JSONObject().put("min", 0).put("max", 100), { list ->
            _state.update { it.copy(team = list.orEmpty(), loaded = true) }
        })
        post<List<TeamInvite>>("owner/operators/invites", JSONObject(), { list ->
            _state.update { it.copy(sent = list.orEmpty()) }
        })
    }

    fun search(query: String) {
        _state.update { it.copy(found = null) }
        post<OperatorBrief>("owner/operators/search", JSONObject().put("query", query.trim()), { found ->
            _state.update { it.copy(found = found) }
        })
    }

    fun clearFound() = _state.update { it.copy(found = null) }

    /** Ask a registered operator to join: they accept or decline */
    fun sendRequest(operatorId: String, terms: PayTerms, message: String) {
        post<TeamInvite>("owner/operators/invite", termsJson(terms, JSONObject().put("operator_id", operatorId).put("message", message)), {
            showSuccess(string(R.string.ot_request_sent))
            _state.update { it.copy(found = null, doneTick = it.doneTick + 1) }
            load()
        })
    }

    fun cancelRequest(inviteId: String) {
        post<TeamInvite>("owner/operators/invites/cancel", JSONObject().put("invite_id", inviteId), { load() })
    }

    /** A brand-new operator the owner types in (they sign in later with this mobile number) */
    fun addNew(name: String, mobile: String, email: String, terms: PayTerms) {
        val body = termsJson(terms, JSONObject().put("full_name", name.trim()).put("mobile_number", mobile.trim()))
        if (email.isNotBlank()) body.put("user_email", email.trim())
        post<TeamOperator>("owner/operators/add", body, { op ->
            showSuccess(string(R.string.ot_operator_added))
            // an OTP was sent to the operator: the owner types it next
            _state.update { it.copy(verifying = op, testOtp = op?.otp) }
            load()
        })
    }

    /** Verify an operator already in the team: the OTP is sent again, then typed */
    fun startVerify(op: TeamOperator) {
        post<TeamOperator>("owner/operators/resend-otp", JSONObject().put("operator_id", op.id.orEmpty()), { r ->
            _state.update { it.copy(verifying = op, testOtp = r?.otp) }
        })
    }

    fun resendOtp(operatorId: String) {
        post<TeamOperator>("owner/operators/resend-otp", JSONObject().put("operator_id", operatorId), { r ->
            showSuccess(string(R.string.ot_otp_sent))
            _state.update { it.copy(testOtp = r?.otp) }
        })
    }

    fun verifyOtp(operatorId: String, code: String) {
        post<TeamOperator>("owner/operators/verify-otp", JSONObject().put("operator_id", operatorId).put("otp_number", code.trim()), {
            showSuccess(string(R.string.ot_verified))
            _state.update { it.copy(verifying = null, testOtp = null, doneTick = it.doneTick + 1) }
            load()
        })
    }

    fun clearVerify() = _state.update { it.copy(verifying = null, testOtp = null) }

    fun saveTerms(operatorId: String, terms: PayTerms) {
        post<TeamOperator>("owner/operators/salary", termsJson(terms, JSONObject().put("operator_id", operatorId)), {
            showSuccess(string(R.string.ot_terms_saved))
            _state.update { it.copy(doneTick = it.doneTick + 1) }
            load()
        })
    }

    // ---------------------------------------------------------------- operator

    fun loadReceived() {
        post<ReceivedInvites>("operator/invites", JSONObject(), { r ->
            _state.update { it.copy(received = r?.items.orEmpty(), waiting = r?.waiting ?: 0, loaded = true) }
        })
    }

    fun respond(inviteId: String, accept: Boolean) {
        post<TeamInvite>("operator/invites/respond", JSONObject().put("invite_id", inviteId).put("accept", accept), {
            showSuccess(string(if (accept) R.string.ot_joined else R.string.ot_declined))
            loadReceived()
        }, onFail = { loadReceived() })
    }
}

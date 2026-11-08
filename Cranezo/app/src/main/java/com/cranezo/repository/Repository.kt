package com.cranezo.repository

import com.cranezo.api.ApiResult
import com.cranezo.api.Resource
import com.cranezo.api.RestApi
import com.cranezo.api.UploadApiResponse
import com.cranezo.api.safeApiCall
import com.cranezo.ui.chooselanguage.model.ChooseLanguageApiResponse
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.cranes.model.CraneFormOptionsResponse
import com.cranezo.ui.cranes.model.FormOption
import com.cranezo.ui.cranes.model.Operator
import com.cranezo.ui.login.model.RegisterOrLoginApiResponse
import com.cranezo.ui.myprofile.model.AppearanceOption
import com.cranezo.ui.myprofile.model.LanguageOption
import com.cranezo.ui.myprofile.model.LegalContent
import com.cranezo.ui.myprofile.model.NotificationGroup
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.profile.model.UpdateProfileApiResponse
import com.cranezo.ui.verifyotp.model.VerifyOtpApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import com.cranezo.ui.booking.model.*

class Repository @Inject constructor(val api: RestApi) {

    private val TAG = "Repository"

    private companion object {
        // /api/upload: 1 for single upload, 2 for multiple upload
        const val SINGLE_UPLOAD = 1
    }

    suspend fun loginApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<Any>?> {

        return safeApiCall {
            api.loginApi(
                headers, jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
        }
    }

    suspend fun dropDownApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<ChooseLanguageApiResponse>?> {

        return safeApiCall {
            api.dropDownApi(
                headers, jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
        }
    }

    suspend fun registerOrLoginApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<RegisterOrLoginApiResponse>?> {

        return safeApiCall {
            api.registerOrLoginApi(
                headers, jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
        }
    }

    suspend fun verifyOtpApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<VerifyOtpApiResponse>?> {

        return safeApiCall {
            api.verifyOtpApi(
                headers, jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
        }
    }

    /** Uploads a single file (upload type 1). [file] is the multipart part named "file". */
    suspend fun uploadApi(
        headers: Map<String, String>, file: MultipartBody.Part,
    ): Resource<ApiResult<UploadApiResponse>?> {

        return safeApiCall {
            api.uploadApi(
                headers,
                SINGLE_UPLOAD.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                file
            )
        }
    }

    suspend fun updateProfileApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<UpdateProfileApiResponse>?> {

        return safeApiCall {
            api.updateProfileApi(
                headers, jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
        }
    }


    // ---- Owner cranes. A 409 here is a real answer (duplicate registration / mobile), not a logout ----

    private fun json(jsonObj: JSONObject) =
        jsonObj.toString().toRequestBody("application/json".toMediaTypeOrNull())

    suspend fun craneFormOptionsApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<CraneFormOptionsResponse>?> =
        safeApiCall(conflictAsError = true) { api.craneFormOptionsApi(headers, json(jsonObj)) }

    suspend fun craneFormOptionsByTypeApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<FormOption>>?> =
        safeApiCall(conflictAsError = true) { api.craneFormOptionsByTypeApi(headers, json(jsonObj)) }

    suspend fun getCranesApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<Crane>>?> =
        safeApiCall(conflictAsError = true) { api.getCranesApi(headers, json(jsonObj)) }

    suspend fun getCraneDetailsApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<Crane>?> =
        safeApiCall(conflictAsError = true) { api.getCraneDetailsApi(headers, json(jsonObj)) }

    suspend fun addCraneApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<Crane>?> =
        safeApiCall(conflictAsError = true) { api.addCraneApi(headers, json(jsonObj)) }

    suspend fun editCraneApi(
        headers: Map<String, String>, craneId: String, jsonObj: JSONObject,
    ): Resource<ApiResult<Crane>?> =
        safeApiCall(conflictAsError = true) { api.editCraneApi(headers, craneId, json(jsonObj)) }

    suspend fun craneDocumentsApi(
        headers: Map<String, String>, craneId: String, jsonObj: JSONObject,
    ): Resource<ApiResult<Crane>?> =
        safeApiCall(conflictAsError = true) { api.craneDocumentsApi(headers, craneId, json(jsonObj)) }

    suspend fun ownerOperatorsApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<Operator>>?> =
        safeApiCall(conflictAsError = true) { api.ownerOperatorsApi(headers, json(jsonObj)) }

    suspend fun addOperatorApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<Operator>?> =
        safeApiCall(conflictAsError = true) { api.addOperatorApi(headers, json(jsonObj)) }


    // ---- My Profile ----

    suspend fun profileMeApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<ProfileData>?> =
        safeApiCall(conflictAsError = true) { api.profileMeApi(headers, json(jsonObj)) }

    suspend fun profileUpdateApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<ProfileData>?> =
        safeApiCall(conflictAsError = true) { api.profileUpdateApi(headers, json(jsonObj)) }

    suspend fun profileLanguagesApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<List<LanguageOption>>?> =
        safeApiCall(conflictAsError = true) { api.profileLanguagesApi(headers, json(jsonObj)) }

    suspend fun profileLanguageSaveApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<LanguageOption>>?> =
        safeApiCall(conflictAsError = true) { api.profileLanguageSaveApi(headers, json(jsonObj)) }

    suspend fun profileAppearanceApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<List<AppearanceOption>>?> =
        safeApiCall(conflictAsError = true) { api.profileAppearanceApi(headers, json(jsonObj)) }

    suspend fun profileAppearanceSaveApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<AppearanceOption>>?> =
        safeApiCall(conflictAsError = true) { api.profileAppearanceSaveApi(headers, json(jsonObj)) }

    suspend fun profileNotificationsApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<List<NotificationGroup>>?> =
        safeApiCall(conflictAsError = true) { api.profileNotificationsApi(headers, json(jsonObj)) }

    suspend fun profileNotificationsSaveApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<List<NotificationGroup>>?> =
        safeApiCall(conflictAsError = true) { api.profileNotificationsSaveApi(headers, json(jsonObj)) }

    suspend fun legalApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<LegalContent>?> =
        safeApiCall(conflictAsError = true) { api.legalApi(headers, json(jsonObj)) }

    suspend fun assignedCraneDetailsApi(
        headers: Map<String, String>, jsonObj: JSONObject,
    ): Resource<ApiResult<Crane>?> =
        safeApiCall(conflictAsError = true) { api.assignedCraneDetailsApi(headers, json(jsonObj)) }

    suspend fun logoutApi(
        headers: Map<String, String>, jsonObj: JSONObject = JSONObject(),
    ): Resource<ApiResult<Any>?> =
        safeApiCall(conflictAsError = true) { api.logoutApi(headers, json(jsonObj)) }

    // ---- Booking, Crane Requests, Notifications ----

    suspend fun bookingOptionsApi(h: Map<String, String>): Resource<ApiResult<BookingOptions>?> =
        safeApiCall(conflictAsError = true) { api.bookingOptionsApi("customer/booking/options", h, json(JSONObject())) }

    suspend fun searchCranesApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<List<AvailableCrane>>?> =
        safeApiCall(conflictAsError = true) { api.availableCranesApi("customer/booking/search", h, json(body)) }

    suspend fun craneForBookingApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<AvailableCrane>?> =
        safeApiCall(conflictAsError = true) { api.craneForBookingApi("customer/booking/crane-details", h, json(body)) }

    /** [action] is create, get or cancel. */
    suspend fun bookingActionApi(h: Map<String, String>, action: String, body: JSONObject): Resource<ApiResult<Booking>?> =
        safeApiCall(conflictAsError = true) { api.bookingApi("customer/booking/$action", h, json(body)) }

    suspend fun bookingListApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<List<Booking>>?> =
        safeApiCall(conflictAsError = true) { api.bookingListApi("customer/booking/list", h, json(body)) }

    suspend fun trackApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<TrackInfo>?> =
        safeApiCall(conflictAsError = true) { api.trackApi("customer/booking/track", h, json(body)) }

    /** [action] is get, accept or decline. */
    suspend fun ownerRequestApi(h: Map<String, String>, action: String, body: JSONObject): Resource<ApiResult<OwnerRequest>?> =
        safeApiCall(conflictAsError = true) { api.ownerRequestApi("owner/requests/$action", h, json(body)) }

    suspend fun notificationsApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<List<AppNotification>>?> =
        safeApiCall(conflictAsError = true) { api.notificationsApi("notifications/list", h, json(body)) }

    suspend fun appVersionApi(body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.appVersionApi("app/version", json(body)) }

    suspend fun placesSearchApi(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.placesSearchApi("places/search", h, json(body)) }

    suspend fun placesRouteApi(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.placesRouteApi("places/route", h, json(body)) }

    suspend fun placesReverseApi(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.placesReverseApi("places/reverse", h, json(body)) }

    suspend fun unreadCountApi(h: Map<String, String>): Resource<ApiResult<UnreadCount>?> =
        safeApiCall(conflictAsError = true) { api.unreadCountApi("notifications/unread-count", h, json(JSONObject())) }

    suspend fun markReadApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<Any>?> =
        safeApiCall(conflictAsError = true) { api.plainPostApi("notifications/mark-read", h, json(body)) }

    /** Saves this phone's Firebase push token for the signed-in user. */
    suspend fun registerDeviceApi(h: Map<String, String>, body: JSONObject): Resource<ApiResult<Any>?> =
        safeApiCall(conflictAsError = true) { api.plainPostApi("notifications/register-device", h, json(body)) }

    // ---- Operator jobs and earnings ----

    private val O = "operator/"

    suspend fun opHome(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.opHomeApi(O + "home", h, json(JSONObject())) }

    /** [path] is e.g. "jobs/get", "jobs/accept", "jobs/start-journey"; every one answers with the job. */
    suspend fun opJob(h: Map<String, String>, path: String, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opJobApi(O + path, h, json(body)) }

    suspend fun opJobList(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opJobListApi(O + "jobs/list", h, json(body)) }

    /** jobs/checklist and jobs/checklist/save. */
    suspend fun opChecklist(h: Map<String, String>, path: String, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opChecklistApi(O + path, h, json(body)) }

    suspend fun opWaiting(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opTimersApi(O + "jobs/waiting", h, json(body)) }

    /** jobs/breakdown, jobs/issue or jobs/emergency. */
    suspend fun opIssue(h: Map<String, String>, path: String, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opIssueApi(O + path, h, json(body)) }

    suspend fun opRequestConfirmation(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opOtpApi(O + "jobs/request-confirmation", h, json(body)) }

    suspend fun opDecline(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.plainPostApi(O + "jobs/decline", h, json(body)) }

    suspend fun opStatus(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opStatusApi(O + "status", h, json(body)) }

    suspend fun opLocation(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opLocationApi(O + "location", h, json(body)) }

    suspend fun opEarningsSummary(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.opEarningsSummaryApi(O + "earnings/summary", h, json(JSONObject())) }

    suspend fun opEarningsList(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.opEarningsListApi(O + "earnings/list", h, json(body)) }

    // ---- Owner jobs and earnings ----

    suspend fun ownerJobCounts(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.ownerCountsApi("owner/jobs/summary", h, json(JSONObject())) }

    suspend fun ownerJobList(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerJobListApi("owner/jobs/list", h, json(body)) }

    suspend fun ownerJobGet(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerJobApi("owner/jobs/get", h, json(body)) }

    suspend fun ownerJobTrack(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.trackApi("owner/jobs/track", h, json(body)) }

    suspend fun ownerJobEvidence(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerEvidenceApi("owner/jobs/evidence", h, json(body)) }

    suspend fun ownerAssignOperator(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.plainPostApi("owner/requests/assign-operator", h, json(body)) }

    suspend fun ownerEarningsSummary(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerEarningsSummaryApi("owner/earnings/summary", h, json(body)) }

    suspend fun ownerEarningsTrend(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerTrendApi("owner/earnings/trend", h, json(body)) }

    suspend fun ownerEarningJobs(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.ownerEarningJobsApi("owner/earnings/jobs", h, json(body)) }

    // ---- Customer: after the booking is confirmed ----

    /** [action] is extension/request or extension/cancel. */
    suspend fun extensionApi(h: Map<String, String>, action: String, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.extensionResultApi("customer/booking/$action", h, json(body)) }

    suspend fun jobCountsApi(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.jobCountsApi("customer/booking/summary", h, json(JSONObject())) }

    suspend fun myJobsApi(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.myJobsApi("customer/booking/list", h, json(body)) }

    suspend fun activeTrackingApi(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.activeTrackingApi("customer/booking/active-tracking", h, json(JSONObject())) }

    // ---- Notifications and Help & Support ----

    suspend fun supNotifications(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supNotificationsApi("notifications/list", h, json(body)) }

    suspend fun supSummary(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.supSummaryApi("notifications/summary", h, json(JSONObject())) }

    suspend fun supMarkRead(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supMarkReadApi("notifications/mark-read", h, json(body)) }

    suspend fun supHome(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.supHomeApi("support/home", h, json(JSONObject())) }

    suspend fun supFaqCategories(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.supFaqCategoriesApi("support/faq/categories", h, json(JSONObject())) }

    suspend fun supFaqs(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supFaqsApi("support/faq/list", h, json(body)) }

    suspend fun supFaqFeedback(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.plainPostApi("support/faq/feedback", h, json(body)) }

    suspend fun supTickets(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supTicketsApi("support/tickets/list", h, json(body)) }

    /** tickets/create, tickets/get or chat: all answer with the request and its messages. */
    suspend fun supTicket(h: Map<String, String>, path: String, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supTicketApi("support/$path", h, json(body)) }

    suspend fun supReply(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supMessageApi("support/tickets/reply", h, json(body)) }

    suspend fun supCloseTicket(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.plainPostApi("support/tickets/close", h, json(body)) }

    suspend fun supEmergency(h: Map<String, String>, body: JSONObject) =
        safeApiCall(conflictAsError = true) { api.supEmergencyApi("support/emergency", h, json(body)) }

    suspend fun customerHome(h: Map<String, String>) =
        safeApiCall(conflictAsError = true) { api.customerHomeApi("home/customer", h, json(JSONObject())) }

    /** Payments, commission, settlements, job requests and complaints. [path] is relative, e.g. "owner/billing/settings". */
    suspend fun financePost(path: String, h: Map<String, String>, body: JSONObject = JSONObject()) =
        safeApiCall(conflictAsError = true) { api.financeApi(path, h, json(body)) }

}

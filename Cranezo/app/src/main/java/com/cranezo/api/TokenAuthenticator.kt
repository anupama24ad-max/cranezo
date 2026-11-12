package com.cranezo.api

import com.cranezo.BuildConfig
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import com.cranezo.utils.objects.AppDialogType
import com.cranezo.utils.objects.SessionManagerEvent
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

/**
 * On HTTP 401 calls POST /auth/refreshToken { refresh_token }, stores the new tokens and retries
 * the request once with the new access token. If the refresh does not work the session is over.
 *
 * [refreshClient] must be a client without this authenticator.
 */
class TokenAuthenticator(
    private val sm: SessionManager,
    private val refreshClient: OkHttpClient
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val sentAuth = request.header(AppStrings.Constants.authorization) ?: return null

        // Not an owner/user call: the default key (login APIs) or the refresh call itself
        if (sentAuth == AppStrings.Constants.defaultToken ||
            request.url.encodedPath.endsWith(RESTURLS.refreshToken)
        ) return null

        // Only one retry per request
        if (response.priorResponse != null) return null

        synchronized(this) {
            val current = bearer(sm.getData(AppStrings.SessionValues.accessToken, ""))
            // Another request already refreshed the token while this one waited
            if (current.isNotBlank() && !current.equals(sentAuth, ignoreCase = true)) {
                return request.newBuilder().header(AppStrings.Constants.authorization, current).build()
            }

            return when (val result = refresh()) {
                is Refresh.Done -> request.newBuilder().header(AppStrings.Constants.authorization, result.bearer).build()
                // the server said the sign-in is no longer valid: the session is over
                is Refresh.Rejected -> {
                    // "signed in on another device": say so (the dialog shows the server's words), otherwise the usual text
                    SessionManagerEvent.show(result.reason, AppDialogType.SESSION_EXPIRED)
                    null
                }
                // no network / server unreachable right now: this call fails, the user stays signed in
                Refresh.Unreachable -> null
            }
        }
    }

    private sealed interface Refresh {
        data class Done(val bearer: String) : Refresh
        data class Rejected(val reason: String = "") : Refresh
        data object Unreachable : Refresh
    }

    /** A new "Bearer ..." value; Rejected only when the server refused the refresh token. */
    private fun refresh(): Refresh {
        val refreshToken = sm.getData(AppStrings.SessionValues.refreshToken, "")
        if (refreshToken.isBlank()) return Refresh.Rejected()

        val body = JSONObject().put(AppStrings.InputData.refresh_token, refreshToken).toString()
            .toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BuildConfig.SERVER_URL + RESTURLS.refreshToken)
            .header("Accept", "application/json")
            .post(body)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { res ->
                // 401 / 403 / 400: the refresh token is expired or the account is gone
                if (res.code in 400..499) return Refresh.Rejected(otherDeviceMessage(res.body?.string().orEmpty()))
                // 5xx: the server is having a problem, not the user
                if (!res.isSuccessful) return Refresh.Unreachable
                val data = JSONObject(res.body?.string().orEmpty()).optJSONObject("data") ?: return Refresh.Unreachable
                val access = data.optString("access_token").ifBlank { data.optString("token") }
                if (access.isBlank()) return Refresh.Unreachable
                sm.setData(AppStrings.SessionValues.accessToken, access)
                data.optString("refresh_token").takeIf { it.isNotBlank() }
                    ?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
                Refresh.Done(bearer(access))
            }
        } catch (e: Exception) {
            // no network, timeout, DNS: try again later, do not sign the user out
            Refresh.Unreachable
        }
    }

    /** The server's own words, only when the sign-in ended because the account was opened on another device */
    private fun otherDeviceMessage(body: String): String =
        runCatching { JSONObject(body).optString("message") }.getOrDefault("")
            .takeIf { it.contains("another device", ignoreCase = true) } ?: ""

    private fun bearer(token: String) =
        if (token.isBlank() || token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
}

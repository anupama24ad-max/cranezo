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

            val refreshed = refresh()
            if (refreshed == null) {
                SessionManagerEvent.show(
                    "Session expired. Please log in again.",
                    AppDialogType.SESSION_EXPIRED
                )
                return null
            }
            return request.newBuilder().header(AppStrings.Constants.authorization, refreshed).build()
        }
    }

    /** Returns the new "Bearer ..." header value, or null when the refresh failed. */
    private fun refresh(): String? {
        val refreshToken = sm.getData(AppStrings.SessionValues.refreshToken, "")
        if (refreshToken.isBlank()) return null

        val body = JSONObject().put(AppStrings.InputData.refresh_token, refreshToken).toString()
            .toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BuildConfig.SERVER_URL + RESTURLS.refreshToken)
            .header("Accept", "application/json")
            .post(body)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { res ->
                if (!res.isSuccessful) return null
                val data = JSONObject(res.body?.string().orEmpty()).optJSONObject("data") ?: return null
                val access = data.optString("access_token").ifBlank { data.optString("token") }
                if (access.isBlank()) return null
                sm.setData(AppStrings.SessionValues.accessToken, access)
                data.optString("refresh_token").takeIf { it.isNotBlank() }
                    ?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
                bearer(access)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun bearer(token: String) =
        if (token.isBlank() || token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
}

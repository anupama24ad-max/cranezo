package com.cranezo.api

import com.cranezo.BuildConfig
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Renews the sign-in when the app starts (POST /auth/refreshToken), so someone who uses the app keeps a fresh
 * access token and a refresh token that runs 30 days from the last use, instead of being signed out 30 days
 * after signing in. A failure here never signs anyone out: the next call that gets a 401 goes through
 * [TokenAuthenticator], which decides that.
 */
object TokenRefresher {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /** At most once every few minutes (Home can open several times in a row) */
    private const val MIN_GAP_MS = 5 * 60 * 1000L
    @Volatile private var lastRun = 0L

    /** Call off the main thread. True when new tokens were stored. */
    @Synchronized
    fun refreshNow(sm: SessionManager): Boolean {
        if (!sm.isUserLoggedIn()) return false
        val now = System.currentTimeMillis()
        if (now - lastRun < MIN_GAP_MS) return false
        lastRun = now
        val refreshToken = sm.getData(AppStrings.SessionValues.refreshToken, "")
        if (refreshToken.isBlank()) return false
        val body = JSONObject().put(AppStrings.InputData.refresh_token, refreshToken).toString()
            .toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BuildConfig.SERVER_URL + RESTURLS.refreshToken)
            .header("Accept", "application/json")
            .post(body)
            .build()
        return try {
            client.newCall(request).execute().use { res ->
                if (!res.isSuccessful) return false
                val data = JSONObject(res.body?.string().orEmpty()).optJSONObject("data") ?: return false
                val access = data.optString("access_token").ifBlank { data.optString("token") }
                if (access.isBlank()) return false
                sm.setData(AppStrings.SessionValues.accessToken, access)
                data.optString("refresh_token").takeIf { it.isNotBlank() }
                    ?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

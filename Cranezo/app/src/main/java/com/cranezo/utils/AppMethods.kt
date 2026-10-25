package com.cranezo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.cranezo.BuildConfig
import com.cranezo.api.RestApi
import com.cranezo.utils.objects.AppDialogType
import com.cranezo.utils.objects.SessionManagerEvent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


private val TAG = "AppMethods"

class AppMethods {
    companion object {
        fun getAndroidId(context: Context): String? {
            return try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: SecurityException) {
                Log.e(TAG, "getAndroidId: SecurityException ")
                // Handle SecurityException if the app doesn't have the necessary permissions (rare)
                e.printStackTrace()
                null
            }
        }

        fun getDeviceDetails(): String {
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            val androidVersion = Build.VERSION.RELEASE
            return "Device Name: $deviceName, Android Version: $androidVersion"
        }

        fun getToken(
            sm: SessionManager? = null, defaultToken: Boolean = false
        ): HashMap<String, String> {
            val headers = HashMap<String, String>()

            if (defaultToken) {
                headers[AppStrings.Constants.authorization] = AppStrings.Constants.defaultToken
            } else {
                val token = sm?.getData<String>(AppStrings.SessionValues.accessToken, "").toString()
                headers[AppStrings.Constants.authorization] = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
            }
            headers["Accept"] = "application/json"
            return headers

        }

        fun handleResponse(response: String): String {
            try {
                val jsonObject = JSONObject(response)
                // val status = jsonObject.getInt("status")
                val message = jsonObject.getString("message")
                return message
            } catch (e: Exception) {

            }
            return ""
        }

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        }
        fun isValidPassword(password: String): Boolean {
            val passwordRegex = Regex(
                "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"
            )
            return passwordRegex.matches(password)
        }


/*
        suspend fun refreshAccessToken(activity: Activity? = null, callback: (Boolean) -> Unit) {

            var sm = SessionManager(applicationContext!!)

            Log.e(TAG, "refreshTokenApi: @@@@@@@@@@")
            Log.e(TAG, "refreshTokenApi: sm =${sm.getData(AppStrings.SessionValues.userId, "")}")
            Log.e(
                TAG, "refreshTokenApi refresh toke: sm =${sm.getData(
                    AppStrings.SessionValues.refreshToken,
                        ""
                    )
                }"
            )

            getRetrofitInstance()

            val restApi = getRetrofitInstance().create(RestApi::class.java)
            var headers = HashMap<String, String>()
            headers[AppStrings.Constants.authorization] =
                sm.getData(
                    AppStrings.SessionValues.refreshToken,
                    sm.getData(AppStrings.SessionValues.refreshToken, "")
                )
            var jsonObject = JSONObject()
            jsonObject.put(
                AppStrings.InputData.refresh_token,
                sm.getData(AppStrings.SessionValues.refreshToken, "")
            )
            var response = restApi.refreshTokenApi(
                headers, jsonObject.toString().toRequestBody(
                    "application/json".toMediaTypeOrNull()
                )
            )
            response.enqueue(object : Callback<Any?> {
                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                    if (response.code() == 200 || response.code() == 201 || response.code() == 202 || response.code() == 204) {
                        response.body()?.let {
                            Log.e(TAG, "onResponse: response :$response")
                            val rawData = Gson().toJsonTree(response.body()).asJsonObject.toString()
                            val jsonObject = JSONObject(rawData)
                            Log.e(TAG, "onResponse: refresh token -> ${jsonObject}")
                            var data = jsonObject.getJSONObject(AppStrings.ResponseData.data)
                            sm.setData(
                                AppStrings.SessionValues.accessToken,
                                data.getString(AppStrings.SessionValues.accessToken)
                            )
                            sm.setData(
                                AppStrings.SessionValues.refreshToken,
                                data.getString(AppStrings.SessionValues.refreshToken)
                            )
                            // Notify callback that token has been refreshed
                            callback(true)
                        }
                    } else if (response.code() == 401) {
                        Log.e(TAG, "onResponse: errorcode:${response.errorBody().toString()}")
                        SessionManagerEvent.show(
                            handleResponse(
                                response.errorBody()?.string() ?: "Some Exception Occurred"
                            ), AppDialogType.SESSION_EXPIRED
                        )
                    } else {
                        Log.e(TAG, "onResponse: errorcode:${response.code()}")
                    }

                }

                override fun onFailure(call: Call<Any?>, t: Throwable) {
                    Log.e(TAG, "onFailure: @@@@")
                    // Notify callback that token refresh failed
                    callback(false)

                }
            })

        }
*/


        lateinit var retrofit: Retrofit
        private fun getRetrofitInstance(): Retrofit {
            val gson = GsonBuilder().setLenient().create()
            retrofit = Retrofit.Builder().baseUrl(BuildConfig.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).client(http())
                .build()
            return retrofit
        }


        fun http(): OkHttpClient {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return OkHttpClient.Builder().addInterceptor(loggingInterceptor)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                        val requestBuilder: Request.Builder = chain.request().newBuilder()
                        return chain.proceed(requestBuilder.build())
                    }

                }).connectTimeout(3, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { hostname, session -> true }.build()
        }

        fun decryptLaravel(cipherText: String, appKey: ByteArray): String {
            // 1. Decode the base64 input
            val json = String(Base64.decode(cipherText, Base64.DEFAULT))

            // 2. Parse JSON
            val jsonObj = JSONObject(json)
            val iv = Base64.decode(jsonObj.getString("iv"), Base64.DEFAULT)
            val value = Base64.decode(jsonObj.getString("value"), Base64.DEFAULT)

            // 3. Decrypt AES-256-CBC
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(appKey, "AES")
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decrypted = cipher.doFinal(value)

            return String(decrypted, Charsets.UTF_8)
        }

        fun createTempImageUri(context: Context, prefix: String = "img_"): Uri {
            val dir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File.createTempFile(prefix, ".jpg", dir)
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        fun getFileFromUri(context: Context, uri: Uri): File? {
            return try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    // Create a temporary file in cache directory
                    val file = File.createTempFile(
                        "upload_${System.currentTimeMillis()}",
                        getFileExtension(context, uri),
                        context.cacheDir
                    )

                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    file
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun getFileExtension(context: Context, uri: Uri): String {
            val mimeType = context.contentResolver.getType(uri)
            return when {
                mimeType == null -> ""
                mimeType.contains("image/jpeg") -> ".jpg"
                mimeType.contains("image/jpg") -> ".jpg"
                mimeType.contains("image/png") -> ".png"
                mimeType.contains("application/pdf") -> ".pdf"
                else -> ".tmp"
            }
        }

        fun createRequestBody(file: File, context: Context, uri: Uri): RequestBody? {
            return try {
                val mimeType = context.contentResolver.getType(uri) ?: "*/*"
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }




    }
}

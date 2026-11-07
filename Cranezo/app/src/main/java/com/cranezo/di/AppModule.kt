package com.cranezo.di

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.cranezo.BuildConfig
import com.cranezo.api.RestApi
import com.cranezo.utils.SessionManager


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.cranezo.api.TokenAuthenticator
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient

import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) = SessionManager(context)

    @Provides
    fun provideUserServicesApis(retrofit: Retrofit): RestApi =
        retrofit.create(RestApi::class.java)


    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, sm: SessionManager): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(http(TokenAuthenticator(sm, http())))
        .build()

    /**
     * One shared connection pool, so the API client and the token-refresh client reuse connections.
     * Idle connections are dropped after 30 seconds: the mobile network silently kills idle sockets
     * (overnight, or when the phone sleeps), and reusing such a dead socket made the first calls of
     * the morning hang.
     */
    private val pool = okhttp3.ConnectionPool(5, 30, TimeUnit.SECONDS)

    fun http(authenticator: Authenticator? = null): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        // Full request logs only in debug builds: release builds must not print tokens or personal data
        loggingInterceptor.setLevel(if (com.cranezo.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectionPool(pool)
            // a call fails in seconds instead of hanging for minutes; a stale connection is retried on a new one
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        // Normal HTTPS certificate checks (the server has a valid certificate); the old
        // trust-all setup let anyone on the network read or change the traffic.
        authenticator?.let { builder.authenticator(it) }
        return builder.build()
    }

    private fun httpAuthKey(): OkHttpClient? {
        try {
            return OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    val authorisedReq = chain.request().newBuilder()
                        .build()
                    chain.proceed(authorisedReq)
                }
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()

        } catch (e: SocketTimeoutException) {
            Log.e("TAG", "httpAuthKey:SocketTimeoutException ")
            return null
        }
    }


}
package com.cranezo.api

import com.cranezo.ui.chooselanguage.model.ChooseLanguageApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface RestApi {


    @POST(RESTURLS.refreshToken)
    suspend fun refreshTokenApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<Any>>


    @POST(RESTURLS.dropDown)
    suspend fun dropDownApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<ChooseLanguageApiResponse>>

    @POST(RESTURLS.login)
    suspend fun loginApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<Any>>



}

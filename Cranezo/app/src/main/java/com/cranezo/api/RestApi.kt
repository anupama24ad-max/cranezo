package com.cranezo.api

import com.cranezo.ui.chooselanguage.model.ChooseLanguageApiResponse
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.cranes.model.CraneFormOptionsResponse
import com.cranezo.ui.cranes.model.FormOption
import com.cranezo.ui.cranes.model.Operator
import com.cranezo.ui.login.model.RegisterOrLoginApiResponse
import com.cranezo.ui.profile.model.UpdateProfileApiResponse
import com.cranezo.ui.verifyotp.model.VerifyOtpApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @POST(RESTURLS.registerOrLogin)
    suspend fun registerOrLoginApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<RegisterOrLoginApiResponse>>

    @POST(RESTURLS.verifyOtp)
    suspend fun verifyOtpApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<VerifyOtpApiResponse>>

    @POST(RESTURLS.updateProfile)
    suspend fun updateProfileApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody?=null
    ): Response<ApiResult<UpdateProfileApiResponse>>

    // multipart/form-data: type = 1 (single) + file
    @Multipart
    @POST(RESTURLS.upload)
    suspend fun uploadApi(
        @HeaderMap headers: Map<String, String>,
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResult<UploadApiResponse>>


    // ---- Owner cranes (JSON bodies, bearer token) ----

    @POST(RESTURLS.craneFormOptions)
    suspend fun craneFormOptionsApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<CraneFormOptionsResponse>>

    // { type: N } returns one plain list
    @POST(RESTURLS.craneFormOptions)
    suspend fun craneFormOptionsByTypeApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<List<FormOption>>>

    // List body { min, max }: data is an array
    @POST(RESTURLS.getCranes)
    suspend fun getCranesApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<List<Crane>>>

    // Details body { crane_id }: data is one crane
    @POST(RESTURLS.getCranes)
    suspend fun getCraneDetailsApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<Crane>>

    @POST(RESTURLS.addCrane)
    suspend fun addCraneApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<Crane>>

    @PUT(RESTURLS.editCrane)
    suspend fun editCraneApi(
        @HeaderMap headers: Map<String, String>,
        @Path("id") craneId: String,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<Crane>>

    @PUT(RESTURLS.craneDocuments)
    suspend fun craneDocumentsApi(
        @HeaderMap headers: Map<String, String>,
        @Path("id") craneId: String,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<Crane>>

    @POST(RESTURLS.ownerOperators)
    suspend fun ownerOperatorsApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<List<Operator>>>

    @POST(RESTURLS.addOperator)
    suspend fun addOperatorApi(
        @HeaderMap headers: Map<String, String>,
        @Body jsonObject: RequestBody? = null
    ): Response<ApiResult<Operator>>

}

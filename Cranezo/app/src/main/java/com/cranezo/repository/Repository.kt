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
import com.cranezo.ui.profile.model.UpdateProfileApiResponse
import com.cranezo.ui.verifyotp.model.VerifyOtpApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

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

}

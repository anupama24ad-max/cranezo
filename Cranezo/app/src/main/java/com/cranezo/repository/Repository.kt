package com.cranezo.repository

import com.cranezo.api.ApiResult
import com.cranezo.api.Resource
import com.cranezo.api.RestApi
import com.cranezo.api.UploadApiResponse
import com.cranezo.api.safeApiCall
import com.cranezo.ui.chooselanguage.model.ChooseLanguageApiResponse
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

}
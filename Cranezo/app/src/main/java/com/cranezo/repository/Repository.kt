package com.cranezo.repository

import com.cranezo.api.ApiResult
import com.cranezo.api.Resource
import com.cranezo.api.RestApi
import com.cranezo.api.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class Repository @Inject constructor(val api: RestApi) {

    private val TAG = "Repository"



}
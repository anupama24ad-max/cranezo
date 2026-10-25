package com.cranezo.api

sealed class Resource<T>(val data: T? = null, val errorType : ErrorType?= null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(data: T? = null, errorType : ErrorType?= null, message: String = "") : Resource<T>(data, errorType, message)
    class UnAuthorised<T>(data: T? = null, errorType : ErrorType?= null, message: String = "") : Resource<T>(data, errorType, message)
    class Logout<T>(data: T? = null, errorType : ErrorType?= null, message: String = "") : Resource<T>(data, errorType, message)
    class BlockOrDelete<T>(data: T? = null, errorType : ErrorType?= null, message: String = "") : Resource<T>(data, errorType, message)
}
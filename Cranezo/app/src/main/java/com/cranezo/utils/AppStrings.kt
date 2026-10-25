package com.cranezo.utils

import com.cranezo.BuildConfig


class AppStrings {
    class SessionValues {
        companion object {
            var userId = "userId"
            var accessToken = "accessToken"
            var refreshToken = "refreshToken"
            var userName = "userName"
            var name = "name"
            var email = "email"
            var contactNumber = "contactNumber"
            var customerId = "customerId"
            var accountStatus = "accountStatus"
            var Id = "Id"
            var city = "city"

        }
    }

    class Constants {
        companion object {

            var authorization = "Authorization"
            var defaultToken = BuildConfig.API_KEY

            val android = "1"

            val memberType = 2
            val deviceId = "dNDZvU4NQSi2w2sNXPgrEt:APA91bFEnTSViQCaK5y9UyrShyQ4p8-oB_LiKPzGfL-yRAVhvouPHgtCpelZ424SiGVBhrSn71qOebj82BhPLzjG6EQl-jfNkwb_5c_gBhGx1Mrm5mUMTYo"


        }
    }

    class InputData{
        companion object{
            var user_code = "user_code"
            var company_password = "company_password"
            var device_id = "device_id"
            var device_unique_id = "device_unique_id"
            var platform_type = "platform_type"
            var device_details = "device_details"
            var password = "password"
            var name = "name"
            var contact_number = "contact_number"
            var password_confirmation = "password_confirmation"
            var user_email = "user_email"
            var type = "type"
            var contact = "contact"
            var otp = "otp"
            var reset_password = "reset_password"
            var reset_password_confirmation = "reset_password_confirmation"
            var user_id = "user_id"
            var min = "min"
            var max = "max"
            var search = "search"
            var refresh_token = "refresh_token"
            var work_id = "work_id"
            var assigned_id = "assigned_id"
            var id = "id"
            var category_id = "category_id"
            var user_name = "user_name"
            var phone_number = "phone_number"
            var service = "service"
            var message = "message"
            var page_number = "page_number"
            var page_size = "page_size"
            var other_user_id = "other_user_id"
            var old_password = "old_password"
            var new_password = "new_password"
            var topic_ids = "topic_ids"
            var subscriber_id = "subscriber_id"
            var group_id = "group_id"
            var phone = "phone"
            var email = "email"
            var full_name = "full_name"
            var categoryId = "categoryId"
            var login = "login"
            var category_uuid = "category_uuid"
            var subcategory_uuid = "subcategory_uuid"
            var quantity = "quantity"
            var items = "items"
            var pickup_location = "pickup_location"
            var pickup_date = "pickup_date"
            var pickup_time = "pickup_time"
            var notes = "notes"
            var images = "images"
            var order_id = "order_id"
            var new_password_confirmation = "new_password_confirmation"
            var mobile = "mobile"
            var vehicleType = "vehicle_type"
            var vehicleNumber = "vehicle_number"
            var vehicleBrand = "vehicle_brand"
            var vehicleModel = "vehicle_model"
            var remark = "remark"
            var photos = "photos"
            var coins = "coins"
            var razorpay_payment_id = "razorpay_payment_id"
            var razorpay_order_id = "razorpay_order_id"
            var razorpay_signature = "razorpay_signature"
        }
    }
    class AccountStatus{
        companion object{
            var approved = 0
            var pending = 1
            var resetPassword = 2
            var registration = 4
        }
    }
    class IntentData {
        companion object{
            var userId = "userId"
            var emailOrPhoneNumber = "emailOrPhoneNumber"
            var type = "type"
            var from = "from"
            var webUrl = "webUrl"
            var webTitle = "webTitle"
            var workId = "workId"
            var assignedId = "assignedId"
            var categoryId = "categoryId"
            var categoryImg = "categoryImg"
            var categoryName = "categoryName"
            var serviceId = "serviceId"
            var city = "city"
            var state = "state"
            var latitude = "latitude"
            var longitude = "longitude"
            var zipcode = "zipcode"
            var particulars = "particulars"
            var selectedCategory = "selectedCategory"
            var phoneNumber = "phoneNumber"
            var fullName = "fullName"
            var orderId = "orderId"
            var totalAmount = "totalAmount"
        }
    }
    class Type{
        companion object{
            var email = 1
            var phoneNumber = 2
        }
    }
    class FromAcivity{
        companion object{
            var registrationScreen  = "registrationScreen"
            var logoutScreen  = "logoutScreen"
            var deleteScreen  = "deleteScreen"
            var forgotPasswordScreen = "forgotPasswordScreen"
        }
    }

    class ResponseData {
        companion object {
            val access_token = "access_token"
            val refresh_token = "refresh_token"
            val data = "data"

        }
    }
    class CustomerStatus{
        companion object{
            val pending = 0
            val completed = 1
            val ongoing = 2
        }
    }
    class VerificationStatus{
        companion object{
            val notVerified = 0
            val isVerified = 1
        }
    }

    class TermsPrivacy{
        companion object{
            val termsAndConditions = "terms-and-conditions"
            val privacyPolicy = "privacy-policy"
        }
    }

    class PaymentStatus{
        companion object{
            val pending = "pending"
            val completed = "completed"
        }
    }


}
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
            var languageId = "languageId"
            var profileImage = "profileImage"
            var countryCode = "countryCode"
            var memberId = "memberId"
            var roleId = "roleId"
            var companyName = "companyName"

        }
    }

    class Constants {
        companion object {

            var authorization = "Authorization"
            var defaultToken = BuildConfig.API_KEY

            val android = "1"

            val memberType = 2
            val platformAndroid = "ANDROID"
            val defaultCountryCode = "+91"
            val defaultLanguageId = 1
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
            var country_code = "country_code"
            var language_id = "language_id"
            var otp_number = "otp_number"
            var role_id = "role_id"
            var file = "file"
            var profile_image = "profile_image"
            var customer_type = "customer_type"
            var constitution = "constitution"
            var nature_of_business = "nature_of_business"
            var company_date_establishment = "company_date_establishment"
            var gst_number = "gst_number"
            var directors_details = "directors_details"
            var finance_head_details = "finance_head_details"
            var branch_offices_details = "branch_offices_details"
            var aadhar_number = "aadhar_number"
            var pan_number = "pan_number"
            var state_id = "state_id"
            var operator_license_number = "operator_license_number"
            var license_expiry_date = "license_expiry_date"
            var operator_type = "operator_type"
            var owner_id = "owner_id"
            var owner_type = "owner_type"
            var number_of_business_partners = "number_of_business_partners"
            var pincode = "pincode"
            var business_category = "business_category"
            var company_name = "company_name"
            var business_address = "business_address"
            var business_ownership_type = "business_ownership_type"
            var years_of_experience = "years_of_experience"
            var city = "city"
            var state = "state"
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
    class DropDown{
        companion object{
            var language = 1
            var owner = 2
            var yearsOfExperience = 3
            var partners = 4
            var ownership = 5
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
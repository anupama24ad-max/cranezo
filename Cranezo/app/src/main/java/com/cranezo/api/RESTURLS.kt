package com.cranezo.api

class RESTURLS {
    companion object {
        const val refreshToken = "auth/refreshToken"
        const val login = "login"
        const val dropDown = "dropdowns"
        const val registerOrLogin = "auth/registerOrLogin"
        const val verifyOtp = "auth/verifyOtp"
        const val updateProfile = "auth/updateProfile"
        const val upload = "upload"

        // My Profile
        const val profileMe = "profile/me"
        const val profileUpdate = "profile/update"
        const val profileLanguages = "profile/languages"
        const val profileLanguageSave = "profile/language/save"
        const val profileAppearance = "profile/appearance"
        const val profileAppearanceSave = "profile/appearance/save"
        const val profileNotifications = "profile/notifications"
        const val profileNotificationsSave = "profile/notifications/save"
        const val contentLegal = "content/legal"
        const val assignedCranes = "operator/getAssignedCranes"
        const val logout = "auth/logout"

        // Owner cranes
        const val craneFormOptions = "owner/cranes/form-options"
        const val getCranes = "owner/getCranes"
        const val addCrane = "owner/cranes"
        const val editCrane = "owner/cranes/{id}"
        const val craneDocuments = "owner/cranes/{id}/documents"
        const val ownerOperators = "owner/operators"
        const val addOperator = "owner/operators/add"
     }

}

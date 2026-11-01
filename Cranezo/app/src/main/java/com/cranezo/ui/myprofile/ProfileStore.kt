package com.cranezo.ui.myprofile

import android.content.Context
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.theme.AppThemeMode
import com.cranezo.ui.theme.AppThemeState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The signed-in user's profile, shared by the Profile tab and every My Profile screen. It is replaced
 * by the answer of POST /profile/me and by every POST /profile/update.
 */
@Singleton
class ProfileStore @Inject constructor() {

    private val _profile = MutableStateFlow<ProfileData?>(null)
    val profile: StateFlow<ProfileData?> = _profile.asStateFlow()

    /**
     * Keeps [data] as the current profile, mirrors the basics into the session and brings the app
     * theme in line with the server. Returns true when the language on the server differs from the
     * one on this device (it was changed elsewhere) and has been applied, so the screens must restart.
     */
    fun accept(data: ProfileData, sm: SessionManager, context: Context): Boolean {
        _profile.value = data

        data.full_name?.let { sm.setData(AppStrings.SessionValues.name, it) }
        data.company_name?.let { sm.setData(AppStrings.SessionValues.companyName, it) }
        data.member_id?.let { sm.setData(AppStrings.SessionValues.memberId, it) }
        data.profile_image?.let { sm.setData(AppStrings.SessionValues.profileImage, it) }
        data.role_id?.let { id ->
            UserRole.fromId(id)?.let { sm.setUserRole(it.name) }
        }

        // The server value wins over this device's cache (it may have been changed on another phone)
        val serverMode = AppThemeMode.fromKey(data.appearance)
        if (data.appearance != null && serverMode != AppThemeMode.fromKey(sm.getAppTheme())) {
            AppThemeState.setThemeMode(serverMode, context)
        }

        val serverLanguage = data.language?.id ?: return false
        val localLanguage = sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
        if (serverLanguage == localLanguage) return false
        sm.setData(AppStrings.SessionValues.languageId, serverLanguage)
        return true
    }

    /**
     * POST /profile/me. Returns true when the server's language differs from this device's and was
     * applied (see [accept]). [onError] gets the server's message when the call fails.
     */
    suspend fun refresh(
        repo: Repository,
        sm: SessionManager,
        context: Context,
        onError: (String?) -> Unit = {}
    ): Boolean {
        var languageChanged = false
        repo.profileMeApi(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(
            success = { response ->
                response?.data?.let { languageChanged = accept(it, sm, context) }
            },
            failure = { _, _, message -> onError(message) }
        )
        return languageChanged
    }

    fun clear() {
        _profile.value = null
    }
}

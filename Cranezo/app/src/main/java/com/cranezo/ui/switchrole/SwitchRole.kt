package com.cranezo.ui.switchrole

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.MainHomeActivity
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.myprofile.ProfileStore
import com.cranezo.ui.profile.ProfileActivity
import com.cranezo.ui.theme.*
import com.cranezo.ui.verifyotp.model.VerifyOtpApiResponse
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * One phone number can have an owner, an operator and a customer account. This sheet lists them, opens the one
 * that is picked (no new OTP: the number is signed in already) and adds a role the number does not have yet.
 */

/** One row of POST /auth/roles */
data class RoleAccount(
    val role_id: Int? = null,
    val role_name: String? = null,
    val role_code: String? = null,
    val registered: Boolean? = null,
    val profile_complete: Boolean? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val blocked: Boolean? = null,
    val is_current: Boolean? = null
)

data class SwitchRoleState(
    val roles: List<RoleAccount> = emptyList(),
    val loading: Boolean = true,
    // the role being opened right now
    val switching: Int? = null,
    val error: String? = null
)

@HiltViewModel
class SwitchRoleViewModel @Inject constructor(
    app: Application,
    private val repo: Repository,
    private val sm: SessionManager,
    private val store: ProfileStore
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(SwitchRoleState())
    val state: StateFlow<SwitchRoleState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repo.rolesApi(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(
                success = { r -> _state.update { it.copy(roles = r?.data.orEmpty(), loading = false) } },
                failure = { _, _, message -> _state.update { it.copy(loading = false, error = message?.toString()) } }
            )
        }
    }

    /** [onDone] gets true when the role has no profile yet (the app then asks for it) */
    fun switchTo(roleId: Int, onDone: (needsProfile: Boolean) -> Unit) {
        if (_state.value.switching != null) return
        _state.update { it.copy(switching = roleId, error = null) }
        viewModelScope.launch {
            repo.switchRoleApi(AppMethods.getToken(sm), JSONObject().put("role_id", roleId)).isRequestCallSuspendSuccess(
                success = { r ->
                    val data = r?.data
                    if (data == null) {
                        _state.update { it.copy(switching = null) }
                    } else {
                        save(data)
                        _state.update { it.copy(switching = null) }
                        onDone(data.needs_profile == true || data.full_name.isNullOrBlank())
                    }
                },
                failure = { _, _, message -> _state.update { it.copy(switching = null, error = message?.toString()) } }
            )
        }
    }

    /** The same fields the sign-in with OTP stores, so every screen then shows the new role */
    private fun save(data: VerifyOtpApiResponse) {
        data.user_id?.let { sm.setUserId(it) }
        (data.access_token ?: data.token)?.let { sm.setData(AppStrings.SessionValues.accessToken, it) }
        data.refresh_token?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
        sm.setData(AppStrings.SessionValues.memberId, data.member_id.orEmpty())
        sm.setData(AppStrings.SessionValues.name, data.full_name.orEmpty())
        sm.setData(AppStrings.SessionValues.companyName, data.company_name.orEmpty())
        UserRole.fromId(data.role_id)?.let { role ->
            sm.setUserRole(role.name)
            sm.setData(AppStrings.SessionValues.roleId, role.id)
        }
        // nothing of the other role's profile may show in this one
        store.clear()
        // a role with no profile yet goes through the profile screen first, like a new sign-up
        sm.setUserLoggedIn(!(data.needs_profile == true || data.full_name.isNullOrBlank()))
    }

    companion object {
        fun of(context: Context): SwitchRoleViewModel? =
            (context as? androidx.activity.ComponentActivity)?.let { ViewModelProvider(it)[SwitchRoleViewModel::class.java] }
    }
}

/** Bottom sheet: the three roles of this phone number */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchRoleSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { SwitchRoleViewModel.of(context) } ?: return
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }

    val open: (Int) -> Unit = { roleId ->
        vm.switchTo(roleId) { needsProfile ->
            // the whole app restarts as the new role: every screen, tab and cache starts clean
            context.startActivity(
                Intent(context, if (needsProfile) ProfileActivity::class.java else MainHomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? android.app.Activity)?.finish()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = dimen_20)
                .padding(bottom = dimen_16)
        ) {
            Textview(stringResource(R.string.sw_title), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(stringResource(R.string.sw_subtitle), size = font_13, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_16))

            when {
                state.loading -> Box(modifier = Modifier.fillMaxWidth().height(dimen_100), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_3)
                }
                else -> state.roles.forEach { acc ->
                    RoleRow(acc, busy = state.switching == acc.role_id, enabled = state.switching == null, onClick = { acc.role_id?.let(open) })
                    Spacer(modifier = Modifier.height(dimen_10))
                }
            }
            state.error?.let {
                Spacer(modifier = Modifier.height(dimen_4))
                Textview(it, size = font_13, color = Color(0xFFEF4444), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RoleRow(acc: RoleAccount, busy: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val role = UserRole.fromId(acc.role_id) ?: return
    val current = acc.is_current == true
    val blocked = acc.blocked == true
    val shape = RoundedCornerShape(dimen_16)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (current) AmberPrimary.copy(alpha = 0.10f) else appColors.subtleFill)
            .border(if (current) dimen_1 else 0.dp, if (current) AmberPrimary else Color.Transparent, shape)
            .clickable(enabled = enabled && !current && !blocked, onClick = onClick)
            .padding(dimen_12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(role.imageRes),
            contentDescription = null,
            modifier = Modifier.size(dimen_48).clip(RoundedCornerShape(dimen_12))
        )
        Spacer(modifier = Modifier.width(dimen_12))
        Column(modifier = Modifier.weight(1f)) {
            Textview(stringResource(role.titleRes), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            val sub = when {
                blocked -> stringResource(R.string.sw_blocked)
                acc.registered != true -> stringResource(R.string.sw_not_added)
                acc.profile_complete != true -> stringResource(R.string.sw_finish_profile)
                else -> acc.full_name.orEmpty() + (acc.member_id?.let { " · $it" } ?: "")
            }
            Textview(sub, size = font_12, color = appColors.textSecondary, maxLines = 1)
        }
        Spacer(modifier = Modifier.width(dimen_8))
        when {
            busy -> CircularProgressIndicator(modifier = Modifier.size(dimen_20), color = AmberPrimary, strokeWidth = dimen_2)
            current -> RoleChip(stringResource(R.string.sw_current), SuccessGreen)
            blocked -> Unit
            acc.registered != true -> RoleChip(stringResource(R.string.sw_add), AmberPrimary)
            else -> RoleChip(stringResource(R.string.sw_switch), InfoBlue)
        }
    }
}

@Composable
private fun RoleChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = dimen_12, vertical = dimen_6),
        contentAlignment = Alignment.Center
    ) {
        Textview(text, size = font_12, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

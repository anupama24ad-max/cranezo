package com.cranezo.ui.chooserole

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.cranezo.R
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// id matches the backend role_id (1: Crane Owner, 2: Operator, 3: Customer)
enum class UserRole(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageRes: Int
) {
    CUSTOMER(
        id = 3,
        title = "Customer",
        subtitle = "Book cranes for your\nprojects easily.",
        imageRes = R.drawable.role_customer
    ),
    CRANE_OWNER(
        id = 1,
        title = "Crane Owner",
        subtitle = "List your cranes and\nget more business",
        imageRes = R.drawable.role_owner
    ),
    OPERATOR(
        id = 2,
        title = "Operation/Captain",
        subtitle = "Get jobs and start\nearning.",
        imageRes = R.drawable.role_operator
    );

    companion object {
        fun fromId(id: Int?): UserRole? = entries.firstOrNull { it.id == id }
    }
}

@HiltViewModel
class ChooseRoleViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    val selectedRole = mutableStateOf(
        try {
            UserRole.valueOf(sm.getUserRole())
        } catch (_: Exception) {
            UserRole.CRANE_OWNER
        }
    )

    fun selectRole(role: UserRole) {
        selectedRole.value = role
        sm.setUserRole(role.name)
    }
}

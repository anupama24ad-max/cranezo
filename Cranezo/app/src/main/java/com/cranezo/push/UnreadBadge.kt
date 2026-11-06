package com.cranezo.push

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Unread notifications for the bell on every home screen. The bell asks the server itself
 * (on open, every 30 seconds and whenever a push arrives), so the badge is right on every role's
 * home and is hidden when there is nothing unread.
 */
object UnreadBadge {
    /** null until the first answer from the server */
    val count = mutableStateOf<Int?>(null)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RepoEntryPoint {
        fun repository(): Repository
    }

    suspend fun refresh(context: Context) {
        val sm = SessionManager(context.applicationContext)
        if (!sm.isUserLoggedIn()) {
            count.value = 0
            return
        }
        val repo = runCatching {
            EntryPointAccessors.fromApplication(context.applicationContext, RepoEntryPoint::class.java).repository()
        }.getOrNull() ?: return
        runCatching {
            repo.supSummary(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(success = { r ->
                count.value = r?.data?.unread ?: 0
            })
        }
    }
}

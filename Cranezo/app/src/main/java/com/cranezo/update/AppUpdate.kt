package com.cranezo.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.cranezo.BuildConfig
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import org.json.JSONObject

/** What the server says about this app version (POST /app/version, or the body of a 426 answer) */
data class UpdateInfo(
    val update_required: Boolean? = null,
    val update_available: Boolean? = null,
    val latest_version: String? = null,
    val min_supported_version: String? = null,
    val store_url: String? = null,
    val message: String? = null
)

/**
 * Force update. [state] is read by every screen (see UpdateHost): when an update is required the screen
 * is blocked by a dialog that cannot be closed; an optional update can be skipped once per start of the app.
 */
object AppUpdate {
    var state by mutableStateOf<UpdateInfo?>(null)
        private set
    private var skipped = false

    /**
     * Asked on every launch and every time the app comes back from the background (the answer is never kept).
     * When the call fails nothing happens: a failing check never blocks the app.
     */
    suspend fun check(repo: Repository) {
        var answer: UpdateInfo? = null
        repo.appVersionApi(JSONObject().put("platform", "android").put("current_version", BuildConfig.VERSION_NAME))
            .isRequestCallSuspendSuccess(success = { r -> answer = r?.data })
        val info = answer ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { show(info) }
    }

    /** Also called when any call is answered with 426 (the server refuses this old version) */
    fun show(info: UpdateInfo) {
        state = when {
            info.update_required == true -> info.copy(update_required = true)
            info.update_available == true && !skipped -> info
            else -> null // up to date, or the admin lowered the minimum: the dialog goes away
        }
    }

    fun skip() {
        skipped = true
        state = null
    }

    fun open(context: Context, info: UpdateInfo) {
        val url = info.store_url ?: "https://play.google.com/store/apps/details?id=${context.packageName}"
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}

/** The update dialog. Required: no way to close it (back and outside taps do nothing). Optional: "Later". */
@Composable
fun UpdateHost() {
    val info = AppUpdate.state ?: return
    val context = LocalContext.current
    val required = info.update_required == true
    AlertDialog(
        onDismissRequest = { if (!required) AppUpdate.skip() },
        properties = DialogProperties(dismissOnBackPress = !required, dismissOnClickOutside = !required),
        title = { Text(stringResource(if (required) R.string.update_required_title else R.string.update_available_title)) },
        text = {
            Column {
                Text(info.message?.takeIf { it.isNotBlank() } ?: stringResource(R.string.update_default_message))
                info.latest_version?.takeIf { it.isNotBlank() }?.let { Text(stringResource(R.string.update_version, it)) }
            }
        },
        confirmButton = { TextButton(onClick = { AppUpdate.open(context, info) }) { Text(stringResource(R.string.update_now)) } },
        dismissButton = if (required) null else ({ TextButton(onClick = { AppUpdate.skip() }) { Text(stringResource(R.string.update_later)) } })
    )
}

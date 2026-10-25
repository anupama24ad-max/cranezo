package com.cranezo.utils.networkCheck

// NetNotice.kt
sealed class NetNoticeType { object Offline : NetNoticeType(); object Slow : NetNoticeType();object Online  : NetNoticeType() }

data class NetNotice(
    val type: NetNoticeType,
    val message: String,
    val sticky: Boolean = true, // offline/slow usually sticky until dismiss
    val autoHideMs: Long? = null       // NEW: auto-dismiss duration

)

    sealed class NetNoticeEvent {
        data class Show(val notice: NetNotice) : NetNoticeEvent()
        object Dismiss : NetNoticeEvent()
    }

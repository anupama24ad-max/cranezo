package com.cranezo.interfaces


sealed interface BannerEvent {
    data class Show(val message: String, val status: SnackbarStatus, val autoHideMs: Long = 1000L): BannerEvent
    data object Dismiss : BannerEvent
}

enum class SnackbarStatus { success, informative, delete, warning, error }


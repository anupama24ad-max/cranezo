package com.cranezo.ui.components.uievents

// One place for all clicks / one-shot effects
sealed interface UiClickEvent {
    data class ShowToast(val msg: String) : UiClickEvent
    data object OpenImagePicker : UiClickEvent
    data object OpenFiltersSheet : UiClickEvent
    data object NavigateForgotPassword : UiClickEvent
    data class NavigateToPlaceOrder(val productId: String) : UiClickEvent
    data object NavigateToAddNewAddress : UiClickEvent
    data class NavigateToEditAddress(val addressId: String) : UiClickEvent

    data object NavigateToHomeScreen : UiClickEvent
    data object NavigateToCheckOutDeliveryOrder : UiClickEvent
    data object NavigateToPlaceSelfPickUpOrder : UiClickEvent

}
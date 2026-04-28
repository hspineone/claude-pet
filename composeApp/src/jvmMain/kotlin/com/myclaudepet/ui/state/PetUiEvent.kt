package com.myclaudepet.ui.state

import com.myclaudepet.domain.model.Accessory
import com.myclaudepet.domain.model.PetPosition

sealed interface PetUiEvent {
    data object Clicked : PetUiEvent
    data object DoubleClicked : PetUiEvent
    data object Feed : PetUiEvent
    data object Jump : PetUiEvent
    data class Moved(val position: PetPosition) : PetUiEvent
    data object SpeechExpired : PetUiEvent
    data object OpenAccessibilitySettings : PetUiEvent
    data object DismissPermissionDialog : PetUiEvent
    data object RequestReset : PetUiEvent
    data object ConfirmReset : PetUiEvent
    data object CancelReset : PetUiEvent
    data object OpenUpdateLink : PetUiEvent
    data object DismissUpdate : PetUiEvent
    data class SetAccessory(val value: Accessory?) : PetUiEvent
}

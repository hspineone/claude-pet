package com.myclaudepet

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.myclaudepet.data.platform.ScreenDefaults
import com.myclaudepet.di.appModule
import com.myclaudepet.domain.model.PetPosition
import com.myclaudepet.domain.repository.PetRepository
import com.myclaudepet.ui.pet.PermissionDialog
import com.myclaudepet.ui.pet.PetScreen
import com.myclaudepet.ui.pet.ResetDialog
import com.myclaudepet.ui.pet.UpdateDialog
import com.myclaudepet.ui.state.PetStateHolder
import com.myclaudepet.ui.state.PetUiEvent
import com.myclaudepet.ui.theme.PetDimens
import com.myclaudepet.ui.theme.PetStrings
import com.myclaudepet.ui.theme.PetTheme
import com.myclaudepet.ui.tray.PetTrayIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import kotlin.system.exitProcess

/**
 * 접근성 권한 부여 후 JNativeHook 을 재초기화하려면 JVM 자체 재시작이 필요.
 * jpackage 로 만든 번들이면 `compose.application.resources.dir` 이
 * `/Applications/ClaudePet.app/Contents/app/resources` 형태 → `.app` 경로 추출 후
 * `open` 으로 재실행. 개발 환경(./gradlew run)에선 단순 종료만.
 */
private fun restartAppAndExit() {
    val resourcesDir = System.getProperty("compose.application.resources.dir")
    if (resourcesDir != null) {
        val idx = resourcesDir.indexOf(".app/")
        if (idx >= 0) {
            val appPath = resourcesDir.substring(0, idx + 4)
            runCatching { ProcessBuilder("open", "-n", appPath).start() }
        }
    }
    exitProcess(0)
}

fun main() {
    // 해상도/모니터 전환 시 창 크기 자동 rescale 방지.
    // Windows/Linux 의 OS DPI 스케일을 1.0 으로 고정. macOS 는 자체 처리.
    System.setProperty("sun.java2d.uiScale", "1")

    startKoin { modules(appModule) }
    val koin = GlobalContext.get()
    val stateHolder: PetStateHolder = koin.get()
    val initialPet = runBlocking { koin.get<PetRepository>().current() }

    stateHolder.start()

    application {
        val uiState by stateHolder.state.collectAsState()
        var windowVisible by remember { mutableStateOf(true) }

        // 저장된 위치가 현재 화면 밖이면(외장 모니터 해제·해상도 변경 등) 기본 위치로 폴백.
        // 그렇지 않으면 사용자가 창을 찾지 못해 "안 뜬다" 로 인식하게 된다.
        val safePosition = remember {
            if (ScreenDefaults.isOnScreen(initialPet.position)) initialPet.position
            else ScreenDefaults.initialPosition()
        }

        val windowState = rememberWindowState(
            size = DpSize(PetDimens.WindowWidth, PetDimens.WindowHeight),
            position = WindowPosition.Absolute(
                x = safePosition.x.dp,
                y = safePosition.y.dp,
            ),
        )

        LaunchedEffect(windowState) {
            snapshotFlow { windowState.position }
                .filterIsInstance<WindowPosition.Absolute>()
                .distinctUntilChanged()
                .drop(1)
                .collect { pos ->
                    stateHolder.onEvent(
                        PetUiEvent.Moved(
                            PetPosition(pos.x.value.toInt(), pos.y.value.toInt()),
                        ),
                    )
                }
        }

        val walkOffsetX by stateHolder.petOffsetX.collectAsState()
        val walkOffsetY by stateHolder.petOffsetY.collectAsState()
        val facingRight by stateHolder.petFacingRight.collectAsState()

        Window(
            onCloseRequest = {
                stateHolder.stop()
                exitApplication()
            },
            state = windowState,
            title = PetStrings.AppName,
            undecorated = true,
            transparent = true,
            alwaysOnTop = true,
            resizable = false,
            visible = windowVisible,
        ) {
            PetTheme {
                uiState?.let { snapshot ->
                    WindowDraggableArea {
                        PetScreen(
                            state = snapshot,
                            onEvent = stateHolder::onEvent,
                            walkOffsetX = walkOffsetX,
                            walkOffsetY = walkOffsetY,
                            facingRight = facingRight,
                            windowVisible = windowVisible,
                            onToggleWindowVisible = { windowVisible = !windowVisible },
                            onExit = {
                                stateHolder.stop()
                                exitApplication()
                            },
                        )
                    }
                }
            }
        }

        PermissionDialog(
            visible = uiState?.permissionRequired == true,
            onOpenSettings = { stateHolder.onEvent(PetUiEvent.OpenAccessibilitySettings) },
            onRestart = {
                stateHolder.stop()
                restartAppAndExit()
            },
            onDismiss = { stateHolder.onEvent(PetUiEvent.DismissPermissionDialog) },
        )

        ResetDialog(
            visible = uiState?.resetConfirmVisible == true,
            onConfirm = { stateHolder.onEvent(PetUiEvent.ConfirmReset) },
            onCancel = { stateHolder.onEvent(PetUiEvent.CancelReset) },
        )

        UpdateDialog(
            update = uiState?.availableUpdate,
            onOpenLink = { stateHolder.onEvent(PetUiEvent.OpenUpdateLink) },
            onDismiss = { stateHolder.onEvent(PetUiEvent.DismissUpdate) },
        )

        Tray(
            icon = PetTrayIcon.load(),
            tooltip = PetStrings.AppName,
            menu = {
                Item(
                    if (windowVisible) PetStrings.TrayHide else PetStrings.TrayShow,
                    onClick = { windowVisible = !windowVisible },
                )
                Item(PetStrings.TrayFeed, onClick = { stateHolder.onEvent(PetUiEvent.Feed) })
                Item(
                    PetStrings.TrayQuit,
                    onClick = {
                        stateHolder.stop()
                        exitApplication()
                    },
                )
            },
        )
    }
}

package com.myclaudepet.ui.pet

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.myclaudepet.domain.model.Accessory
import com.myclaudepet.ui.state.PetUiEvent
import com.myclaudepet.ui.state.PetUiState
import com.myclaudepet.ui.theme.PetDimens
import com.myclaudepet.ui.theme.PetStrings

@Composable
fun PetScreen(
    state: PetUiState,
    onEvent: (PetUiEvent) -> Unit,
    walkOffsetX: Float = 0f,
    walkOffsetY: Float = 0f,
    facingRight: Boolean = true,
    windowVisible: Boolean = true,
    onToggleWindowVisible: () -> Unit = {},
    onExit: () -> Unit = {},
    /**
     * Windows 는 시스템 트레이 노출이 환경에 따라 불안정해 우클릭 메뉴에 트레이 기능 전체
     * (옷장 포함) 를 통합한다. macOS 는 메뉴바 트레이가 안정적이라 우클릭은 기본 액션만.
     */
    isWindows: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 펫·말풍선 영역. Stats 영역(고정 높이)을 침범하지 않도록 bottom padding 으로 격리.
        // 말풍선이 길어 column 합이 윈도우 높이를 초과해도 Stats 는 별도 자식이라 영향 없음.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = PetDimens.StatsAreaHeight + 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            SpeechBubble(
                // 일회성 대사(speech, 2.5초) 가 있으면 우선. 없으면 지속 상태 라벨.
                text = state.speech ?: state.stateLabel,
                modifier = Modifier.padding(bottom = 6.dp),
            )

            ContextMenuArea(
                items = {
                    buildPetContextMenu(
                        isWindows = isWindows,
                        windowVisible = windowVisible,
                        equipped = state.pet.equippedAccessory,
                        onToggleWindowVisible = onToggleWindowVisible,
                        onEvent = onEvent,
                        onExit = onExit,
                    )
                },
            ) {
                PetCharacter(
                    state = state.pet.animationState,
                    mood = state.pet.mood,
                    walkOffsetX = walkOffsetX,
                    walkOffsetY = walkOffsetY,
                    facingRight = facingRight,
                    accessory = state.pet.equippedAccessory,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onEvent(PetUiEvent.Clicked) },
                                onDoubleTap = { onEvent(PetUiEvent.DoubleClicked) },
                            )
                        },
                )
            }
        }

        // 포만감/친밀도 게이지는 항상 하단에 고정. 말풍선 길이/펫 애니메이션과 독립.
        StatsOverlay(
            pet = state.pet,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .width(PetDimens.PetSize),
        )
    }
}

/**
 * Windows: 트레이 미사용을 가정하고 옷장(없음+5종) 까지 평면 노출 + Hide/Show 는 제거
 * (트레이 없으면 다시 띄울 길이 사라지므로 종료만 허용).
 * macOS: 메뉴바 트레이가 안정적이라 우클릭은 기본 4 항목(Feed / Hide·Show / Reset / Quit) 만.
 */
private fun buildPetContextMenu(
    isWindows: Boolean,
    windowVisible: Boolean,
    equipped: Accessory?,
    onToggleWindowVisible: () -> Unit,
    onEvent: (PetUiEvent) -> Unit,
    onExit: () -> Unit,
): List<ContextMenuItem> = buildList {
    add(ContextMenuItem(PetStrings.TrayFeed) { onEvent(PetUiEvent.Feed) })
    if (isWindows) {
        add(ContextMenuItem(PetStrings.wardrobeContextItem(null, equipped)) {
            onEvent(PetUiEvent.SetAccessory(null))
        })
        Accessory.ALL.forEach { acc ->
            add(ContextMenuItem(PetStrings.wardrobeContextItem(acc, equipped)) {
                onEvent(PetUiEvent.SetAccessory(acc))
            })
        }
    } else {
        add(
            ContextMenuItem(
                if (windowVisible) PetStrings.TrayHide else PetStrings.TrayShow,
            ) { onToggleWindowVisible() },
        )
    }
    add(ContextMenuItem(PetStrings.MenuReset) { onEvent(PetUiEvent.RequestReset) })
    add(ContextMenuItem(PetStrings.TrayQuit) { onExit() })
}

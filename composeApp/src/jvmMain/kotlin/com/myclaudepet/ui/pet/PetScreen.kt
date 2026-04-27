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
                    listOf(
                        ContextMenuItem(PetStrings.TrayFeed) {
                            onEvent(PetUiEvent.Feed)
                        },
                        ContextMenuItem(
                            if (windowVisible) PetStrings.TrayHide else PetStrings.TrayShow,
                        ) {
                            onToggleWindowVisible()
                        },
                        ContextMenuItem(PetStrings.MenuReset) {
                            onEvent(PetUiEvent.RequestReset)
                        },
                        ContextMenuItem(PetStrings.TrayQuit) {
                            onExit()
                        },
                    )
                },
            ) {
                PetCharacter(
                    state = state.pet.animationState,
                    mood = state.pet.mood,
                    walkOffsetX = walkOffsetX,
                    walkOffsetY = walkOffsetY,
                    facingRight = facingRight,
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

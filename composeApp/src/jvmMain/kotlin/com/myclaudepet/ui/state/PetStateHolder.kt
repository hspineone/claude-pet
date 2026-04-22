package com.myclaudepet.ui.state

import com.myclaudepet.domain.model.DialogueCatalog
import com.myclaudepet.domain.model.DialogueTier
import com.myclaudepet.domain.model.DialogueTrigger
import com.myclaudepet.domain.model.PetAnimationState
import com.myclaudepet.domain.model.dialogueTrigger
import com.myclaudepet.domain.platform.WorkAppWhitelist
import com.myclaudepet.domain.repository.InputEventSource
import com.myclaudepet.domain.repository.InputEventSource.StartResult
import com.myclaudepet.domain.repository.PetRepository
import com.myclaudepet.domain.repository.PlatformBridge
import com.myclaudepet.domain.usecase.CheckForUpdateUseCase
import com.myclaudepet.domain.usecase.FeedOnKeystrokeUseCase
import com.myclaudepet.domain.usecase.FeedPetUseCase
import com.myclaudepet.domain.usecase.InteractWithPetUseCase
import com.myclaudepet.domain.usecase.MovePetUseCase
import com.myclaudepet.domain.usecase.ObservePetUseCase
import com.myclaudepet.domain.usecase.ResetPetUseCase
import com.myclaudepet.domain.usecase.TickSatiationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PetStateHolder(
    private val scope: CoroutineScope,
    private val observePet: ObservePetUseCase,
    private val interact: InteractWithPetUseCase,
    private val feed: FeedOnKeystrokeUseCase,
    private val tick: TickSatiationUseCase,
    private val move: MovePetUseCase,
    private val reset: ResetPetUseCase,
    private val feedPet: FeedPetUseCase,
    private val checkForUpdate: CheckForUpdateUseCase,
    private val repository: PetRepository,
    private val inputSource: InputEventSource,
    private val platformBridge: PlatformBridge,
) {
    private val _state = MutableStateFlow<PetUiState?>(null)
    val state: StateFlow<PetUiState?> = _state.asStateFlow()

    /**
     * 창 내부 캐릭터의 가로 오프셋(dp). 걷기 애니메이션이 갱신.
     * 창 자체는 이동시키지 않는다.
     */
    private val _petOffsetX = MutableStateFlow(0f)
    val petOffsetX: StateFlow<Float> = _petOffsetX.asStateFlow()

    /**
     * 창 내부 캐릭터의 세로 오프셋(dp). 걸음걸이 bob + 점프 애니메이션은 PetCharacter
     * 에서 관리하지만, 걷기 bob 은 여기서 계산해 emit.
     */
    private val _petOffsetY = MutableStateFlow(0f)
    val petOffsetY: StateFlow<Float> = _petOffsetY.asStateFlow()

    /**
     * 걷고 있는 방향. true=오른쪽, false=왼쪽. PetCharacter 가 이 값으로
     * scaleX(±1) 를 적용해 캐릭터 좌우 반전 렌더링.
     */
    private val _petFacingRight = MutableStateFlow(true)
    val petFacingRight: StateFlow<Boolean> = _petFacingRight.asStateFlow()

    // ── 내부 전이 감지용 마지막 값 캐시 (UI 상태와 무관, 직렬화 대상 아님).
    //    observePetFlow 의 "이전 값 ↔ 현재 값" 비교에만 쓰이며 외부에 노출되지 않는다.
    private var currentTier: DialogueTier? = null
    private var currentAnimationState: PetAnimationState? = null
    private var permissionRequiredAtStart: Boolean = false

    // ── 취소 가능한 단발 코루틴 핸들. 새 이벤트 발생 시 이전 작업을 cancel 한다.
    //    StateFlow 로 노출할 의미가 없고 내부 취소 대상이라 private var 로 유지.
    private var speechClearJob: Job? = null
    private var transientStateJob: Job? = null

    // ── 행동 루프가 여러 코루틴에서 쓰는 "마지막 상호작용 시각".
    //    단순 epoch ms 스칼라라 StateFlow 오버헤드 불필요. 여러 스레드 접근 대비 @Volatile.
    @Volatile
    private var lastInteractionMillis: Long = System.currentTimeMillis()

    // 작업 화이트리스트 판정 시 OS 별 규칙을 선택. 1회 감지 후 캐싱.
    private val platform: WorkAppWhitelist.Platform by lazy {
        val os = System.getProperty("os.name", "").lowercase()
        when {
            os.contains("mac") -> WorkAppWhitelist.Platform.MAC
            os.contains("win") -> WorkAppWhitelist.Platform.WINDOWS
            else -> WorkAppWhitelist.Platform.OTHER
        }
    }

    fun start() {
        permissionRequiredAtStart = inputSource.start() == StartResult.PermissionDenied
        observePetFlow()
        observeKeystrokes()
        launchTick()
        launchBehaviorLoop()
        launchForegroundProbe()
        launchUpdateCheck()
    }

    /** 앱 시작 시 1회 GitHub Releases 조회. 네트워크 실패면 조용히 무시. */
    private fun launchUpdateCheck() {
        scope.launch {
            val update = runCatching { checkForUpdate() }.getOrNull() ?: return@launch
            _state.value = _state.value?.copy(availableUpdate = update)
        }
    }

    fun stop() {
        inputSource.stop()
    }

    fun onEvent(event: PetUiEvent): Unit = when (event) {
        PetUiEvent.Clicked -> onClick()
        PetUiEvent.DoubleClicked -> onDoubleClick()
        PetUiEvent.Feed -> onFeed()
        PetUiEvent.Jump -> { triggerJump(); lastInteractionMillis = System.currentTimeMillis() }
        is PetUiEvent.Moved -> onMoved(event)
        PetUiEvent.SpeechExpired -> clearSpeech()
        PetUiEvent.OpenAccessibilitySettings -> openAccessibilitySettings()
        PetUiEvent.DismissPermissionDialog -> dismissPermissionDialog()
        PetUiEvent.RequestReset -> onRequestReset()
        PetUiEvent.ConfirmReset -> onConfirmReset()
        PetUiEvent.CancelReset -> onCancelReset()
        PetUiEvent.OpenUpdateLink -> onOpenUpdateLink()
        PetUiEvent.DismissUpdate -> onDismissUpdate()
    }

    private fun onOpenUpdateLink() {
        val url = _state.value?.availableUpdate?.releaseUrl ?: return
        platformBridge.openUrl(url)
        _state.value = _state.value?.copy(availableUpdate = null)
    }

    private fun onDismissUpdate() {
        _state.value = _state.value?.copy(availableUpdate = null)
    }

    private fun observePetFlow() {
        scope.launch {
            observePet()
                .onEach { pet ->
                    val previousTier = currentTier
                    val previousAnimation = currentAnimationState
                    currentTier = pet.tier
                    currentAnimationState = pet.animationState
                    val label = pet.animationState.persistentLabel()
                    val previous = _state.value
                    val merged = previous?.copy(pet = pet, stateLabel = label)
                        ?: PetUiState(
                            pet = pet,
                            stateLabel = label,
                            permissionRequired = permissionRequiredAtStart,
                        )
                    _state.value = merged
                    if (previousTier != null && pet.tier != previousTier) {
                        speak(pet.tier, DialogueTrigger.LevelUp)
                    }
                    if (previousAnimation != null && pet.animationState != previousAnimation) {
                        speak(pet.tier, pet.animationState.dialogueTrigger)
                    }
                }
                .collect {}
        }
    }

    private fun observeKeystrokes() {
        scope.launch {
            inputSource.keystrokes.collect {
                feed()
                lastInteractionMillis = System.currentTimeMillis()
            }
        }
    }

    private fun launchTick() {
        scope.launch {
            while (isActive) {
                delay(TICK_INTERVAL)
                tick()
                syncAnimationStateFromSatiation()
                emitIdleSpeechIfNeeded()
            }
        }
    }

    /**
     * 자동이동·점프·boring 전이를 담당하는 행동 루프.
     * 10~30초 간격으로 주사위 굴려 하나의 행동을 수행.
     */
    private fun launchBehaviorLoop() {
        scope.launch {
            while (isActive) {
                delay(BEHAVIOR_INTERVAL)
                tryBehaviorTick()
            }
        }
    }

    private suspend fun tryBehaviorTick() {
        val pet = _state.value?.pet ?: return
        // 일시적 상태(Jumping/Touch/Fed)나 작업 상태 중이면 행동 루프 개입 안 함
        if (pet.animationState in TRANSIENT_STATES || pet.animationState in WORKING_STATES) return
        // Hungry 는 배고픔 해제 전까지 유지
        if (pet.animationState == PetAnimationState.Hungry) return

        val idleMillis = System.currentTimeMillis() - lastInteractionMillis
        when {
            Random.nextInt(JUMP_ODDS) == 0 -> triggerJump()
            idleMillis >= BORING_IDLE_MILLIS && pet.animationState == PetAnimationState.Default ->
                enterBoring()
            else -> triggerMove()
        }
    }

    private fun triggerJump() {
        transientStateJob?.cancel()
        transientStateJob = scope.launch {
            repository.updateAnimationState(PetAnimationState.Jumping)
            delay(JUMP_DURATION)
            repository.updateAnimationState(PetAnimationState.Default)
        }
    }

    /**
     * 창 내부 왕복 걷기. 중앙(0) → 한쪽(±WALK_RANGE_DP) → 중앙 복귀. 끝나면 Default.
     * 걷는 동안 상태=Walking 이라 `rememberPetSprite` 가 idle_walk_01/02 교차 렌더.
     */
    private fun triggerMove() {
        transientStateJob?.cancel()
        transientStateJob = scope.launch {
            repository.updateAnimationState(PetAnimationState.Walking)
            val direction = if (Random.nextBoolean()) 1f else -1f
            val amplitude = WALK_RANGE_DP * direction

            // 가는 길: 방향 세트 후 한쪽 끝까지
            _petFacingRight.value = direction > 0f
            animateOffset(startX = _petOffsetX.value, endX = amplitude, durationMs = WALK_LEG_MS)
            // 돌아오는 길: 반대 방향으로 돌아서 중앙으로
            _petFacingRight.value = direction < 0f
            animateOffset(startX = amplitude, endX = 0f, durationMs = WALK_LEG_MS)
            _petOffsetY.value = 0f
            _petFacingRight.value = true // 기본값 복귀 (대기 상태 오른쪽 바라봄)

            val now = _state.value?.pet?.animationState ?: return@launch
            if (now == PetAnimationState.Walking) {
                repository.updateAnimationState(PetAnimationState.Default)
            }
        }
    }

    private suspend fun animateOffset(startX: Float, endX: Float, durationMs: Long) {
        val steps = 45
        val frameMs = durationMs / steps
        repeat(steps) { i ->
            val t = (i + 1f) / steps
            val eased = easeInOutCubic(t)
            _petOffsetX.value = startX + (endX - startX) * eased
            // 걸음걸이 bob — 한 leg 당 2걸음, 위아래로 ±3.5dp 진동
            _petOffsetY.value = (sin(t * Math.PI.toFloat() * 4f) * 3.5f)
            delay(frameMs)
        }
    }

    private fun easeInOutCubic(t: Float): Float =
        if (t < 0.5f) 4f * t * t * t else 1f - Math.pow(-2.0 * t + 2.0, 3.0).toFloat() / 2f

    private suspend fun enterBoring() {
        repository.updateAnimationState(PetAnimationState.Boring)
    }

    /**
     * macOS 포그라운드 앱을 주기적으로 조회해 IDE/에디터/터미널 화이트리스트에 들어오면
     * `WorkingPrepare → Working` 전이, 이탈 시 `WorkingEnd → Default` 전이.
     */
    private fun launchForegroundProbe() {
        scope.launch {
            var lastWasWorkContext = false
            while (isActive) {
                delay(WORK_PROBE_INTERVAL)
                val front = platformBridge.foregroundAppName()
                // 자기 자신(ClaudePet) 포커스는 감지 건너뜀. "Claude" 부분일치 오탐 방지.
                if (front != null && front.contains("ClaudePet", ignoreCase = true)) continue

                val isWorkApp = front != null && WorkAppWhitelist.isWorkApp(front, platform)
                // claude CLI 감지는 "터미널이 포커스일 때" 만 Working 으로 승격.
                // (CLI 가 상시 실행되어 영구 Working 고정되는 문제 회피)
                val isFrontTerminal = front != null && WorkAppWhitelist.isTerminal(front, platform)
                val isClaudeCli = platformBridge.isClaudeCliRunning()
                val isWorkContext = isWorkApp || (isClaudeCli && isFrontTerminal)

                val pet = _state.value?.pet ?: continue
                if (isWorkContext && !lastWasWorkContext) {
                    lastWasWorkContext = true
                    if (pet.animationState !in TRANSIENT_STATES &&
                        pet.animationState !in WORKING_STATES) {
                        repository.updateAnimationState(PetAnimationState.WorkingPrepare)
                        delay(WORK_PREPARE_DURATION)
                        val now = _state.value?.pet?.animationState ?: continue
                        if (now == PetAnimationState.WorkingPrepare) {
                            repository.updateAnimationState(PetAnimationState.Working)
                        }
                    }
                } else if (!isWorkContext && lastWasWorkContext) {
                    lastWasWorkContext = false
                    if (pet.animationState in WORKING_STATES) {
                        repository.updateAnimationState(PetAnimationState.WorkingEnd)
                        delay(WORK_END_DURATION)
                        val now = _state.value?.pet?.animationState ?: continue
                        if (now == PetAnimationState.WorkingEnd) {
                            repository.updateAnimationState(PetAnimationState.Default)
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncAnimationStateFromSatiation() {
        val pet = _state.value?.pet ?: return
        if (pet.animationState in TRANSIENT_STATES) return
        if (pet.animationState in WORKING_STATES) return  // Working 중엔 포만도 Hungry 덮어쓰기 금지
        val target = when {
            pet.satiation.percent <= HUNGRY_BELOW -> PetAnimationState.Hungry
            pet.animationState == PetAnimationState.Hungry -> PetAnimationState.Default
            else -> return
        }
        if (pet.animationState == target) return
        repository.updateAnimationState(target)
    }

    private fun onClick() {
        lastInteractionMillis = System.currentTimeMillis()
        scope.launch {
            interact()
            val snapshot = _state.value ?: return@launch
            // Boring 에서 빠져나오기
            if (snapshot.pet.animationState == PetAnimationState.Boring) {
                repository.updateAnimationState(PetAnimationState.Smile)
            } else if (snapshot.pet.animationState == PetAnimationState.Default) {
                // Default → Smile 순간 전이 (짧게)
                transientStateJob?.cancel()
                transientStateJob = scope.launch {
                    repository.updateAnimationState(PetAnimationState.Smile)
                    delay(SMILE_DURATION)
                    // 그 사이 다른 상태(Hungry 등)로 바뀌었으면 건드리지 않음
                    val now = _state.value?.pet?.animationState ?: return@launch
                    if (now == PetAnimationState.Smile) {
                        repository.updateAnimationState(PetAnimationState.Default)
                    }
                }
            }
            speak(snapshot.pet.tier, DialogueTrigger.Click)
        }
    }

    private fun onDoubleClick() {
        lastInteractionMillis = System.currentTimeMillis()
        triggerJump()
    }

    private fun onMoved(event: PetUiEvent.Moved) {
        lastInteractionMillis = System.currentTimeMillis()
        scope.launch { move(event.position) }
    }

    private fun onFeed() {
        lastInteractionMillis = System.currentTimeMillis()
        transientStateJob?.cancel()
        transientStateJob = scope.launch {
            feedPet()                    // satiation=Full, affinity+5, animationState=Fed
            delay(FED_DURATION)
            val now = _state.value?.pet?.animationState ?: return@launch
            if (now == PetAnimationState.Fed) {
                repository.updateAnimationState(PetAnimationState.Default)
            }
        }
    }

    private fun clearSpeech() {
        speechClearJob?.cancel()
        speechClearJob = null
        _state.value = _state.value?.copy(speech = null)
    }

    private fun openAccessibilitySettings() {
        platformBridge.openAccessibilitySettings()
    }

    private fun dismissPermissionDialog() {
        _state.value = _state.value?.copy(permissionRequired = false)
    }

    private fun onRequestReset() {
        _state.value = _state.value?.copy(resetConfirmVisible = true)
    }

    private fun onCancelReset() {
        _state.value = _state.value?.copy(resetConfirmVisible = false)
    }

    private fun onConfirmReset() {
        scope.launch {
            reset()
            lastInteractionMillis = System.currentTimeMillis()
            _state.value = _state.value?.copy(resetConfirmVisible = false)
        }
    }

    private fun emitIdleSpeechIfNeeded() {
        val current = _state.value ?: return
        if (current.speech != null) return
        if (Random.nextInt(IDLE_SPEECH_ODDS) != 0) return
        val trigger = current.pet.animationState.dialogueTrigger
        speak(current.pet.tier, trigger)
    }

    private fun speak(tier: DialogueTier, trigger: DialogueTrigger) {
        val line = DialogueCatalog.pick(tier, trigger) ?: return
        speechClearJob?.cancel()
        _state.value = _state.value?.copy(speech = line.text)
        speechClearJob = scope.launch {
            delay(SPEECH_DURATION)
            _state.value = _state.value?.copy(speech = null)
            speechClearJob = null
        }
    }

    private companion object {
        val TICK_INTERVAL = 30.seconds
        val SPEECH_DURATION = 2.5.seconds
        val JUMP_DURATION = 500.milliseconds
        val TOUCH_DURATION = 500.milliseconds
        val SMILE_DURATION = 1_500.milliseconds
        val FED_DURATION = 3_000.milliseconds
        val BEHAVIOR_INTERVAL = 10.seconds
        val WORK_PROBE_INTERVAL = 5.seconds
        val WORK_PREPARE_DURATION = 2_000.milliseconds
        val WORK_END_DURATION = 2_000.milliseconds
        const val WALK_LEG_MS = 1_800L

        // 작업 화이트리스트는 `domain/platform/WorkAppWhitelist` 로 분리되었다.

        const val HUNGRY_BELOW = 20
        const val IDLE_SPEECH_ODDS = 3
        /** 행동 루프에서 1/N 확률로 점프. 나머지는 걷기 또는 boring. */
        const val JUMP_ODDS = 6
        /** 상호작용 없이 지난 시간 임계치. 이후 Default 상태면 Boring 전이. */
        const val BORING_IDLE_MILLIS = 5 * 60 * 1000L

        /** 창 내부 좌우 걷기 진폭 (dp). WindowWidth 260 - PetSize 140 = 여유 120dp, 각 60dp. */
        const val WALK_RANGE_DP = 55f

        val TRANSIENT_STATES = setOf(
            PetAnimationState.Jumping,
            PetAnimationState.Touch,
            PetAnimationState.Fed,
            PetAnimationState.Smile,
            PetAnimationState.Walking,
        )
        val WORKING_STATES = setOf(
            PetAnimationState.WorkingPrepare,
            PetAnimationState.Working,
            PetAnimationState.WorkingEnd,
        )
    }
}

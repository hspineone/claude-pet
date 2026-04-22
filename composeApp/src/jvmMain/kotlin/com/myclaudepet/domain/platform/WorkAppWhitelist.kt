package com.myclaudepet.domain.platform

/**
 * 포그라운드 앱이 "작업 컨텍스트" 에 속하는지 / "터미널 그룹" 에 속하는지 판정.
 *
 * OS 감지는 호출자 책임이다 (이 도메인 모듈은 Pure Kotlin 을 유지). 호출자가
 * [Platform] 값을 넘기면 각 OS 에 맞는 매칭 규칙이 적용된다.
 *
 * ## 매칭 규칙
 *
 * - **macOS**: `osascript` 가 반환하는 프로세스 이름의 부분 문자열(contains, 대소문자 무시).
 * - **Windows**: `QueryFullProcessImageName` basename 의 완전 일치(equals, 대소문자 무시).
 *   ConEmu 처럼 여러 exe 가 한 그룹을 이루는 경우 prefix 매칭을 허용한다.
 */
object WorkAppWhitelist {

    enum class Platform { MAC, WINDOWS, OTHER }

    // macOS — osascript 반환값(예: "idea", "Code") 의 부분 문자열.
    private val MAC_WORK_TOKENS = listOf(
        "idea",          // IntelliJ IDEA
        "pycharm",
        "webstorm",
        "goland",
        "rustrover",
        "clion",
        "studio",        // Android Studio
        "Code",          // VS Code
        "Cursor",
        "Windsurf",
        "Zed",
        "Xcode",
        "Terminal",
        "iTerm2",
        "Sublime Text",
        "Nova",
        "Fleet",
        "Claude",        // Claude Desktop
        "ChatGPT",       // ChatGPT Desktop
    )
    private val MAC_TERMINAL_TOKENS = listOf("Terminal", "iTerm")

    // Windows — 실행 파일 basename 의 완전 일치.
    private val WINDOWS_WORK_FILES = setOf(
        // IDE / 에디터
        "Code.exe", "Cursor.exe", "Windsurf.exe", "Zed.exe",
        "idea64.exe", "pycharm64.exe", "webstorm64.exe",
        "studio64.exe", "goland64.exe", "rubymine64.exe",
        "clion64.exe", "rider64.exe",
        "fleet.exe", "devenv.exe", "sublime_text.exe",
        // 터미널
        "WindowsTerminal.exe", "wt.exe",
        "powershell.exe", "pwsh.exe", "cmd.exe",
        "mintty.exe",           // Git Bash
        "WezTerm.exe", "alacritty.exe", "kitty.exe", "Hyper.exe",
        // AI 채팅 데스크탑
        "Claude.exe", "ChatGPT.exe",
    )
    private val WINDOWS_TERMINAL_FILES = setOf(
        "WindowsTerminal.exe", "wt.exe",
        "powershell.exe", "pwsh.exe", "cmd.exe",
        "mintty.exe",
        "WezTerm.exe", "alacritty.exe", "kitty.exe", "Hyper.exe",
    )
    // ConEmu64.exe / ConEmuC.exe 등 한 도구의 여러 바이너리 커버.
    private val WINDOWS_PREFIXES = listOf("ConEmu")

    fun isWorkApp(frontName: String, platform: Platform): Boolean = when (platform) {
        Platform.MAC ->
            MAC_WORK_TOKENS.any { frontName.contains(it, ignoreCase = true) }
        Platform.WINDOWS ->
            WINDOWS_WORK_FILES.any { it.equals(frontName, ignoreCase = true) } ||
                WINDOWS_PREFIXES.any { frontName.startsWith(it, ignoreCase = true) }
        Platform.OTHER -> false
    }

    fun isTerminal(frontName: String, platform: Platform): Boolean = when (platform) {
        Platform.MAC ->
            MAC_TERMINAL_TOKENS.any { frontName.contains(it, ignoreCase = true) }
        Platform.WINDOWS ->
            WINDOWS_TERMINAL_FILES.any { it.equals(frontName, ignoreCase = true) } ||
                WINDOWS_PREFIXES.any { frontName.startsWith(it, ignoreCase = true) }
        Platform.OTHER -> false
    }
}

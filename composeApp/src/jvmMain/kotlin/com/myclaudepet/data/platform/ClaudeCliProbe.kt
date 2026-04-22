package com.myclaudepet.data.platform

import java.util.Locale

/**
 * Claude Code CLI (`claude` 프로세스) 가 실행 중인지 감지한다.
 *
 * 설치 방식(전용 바이너리 / npm global / 쉘 래퍼) 에 독립적이고,
 * OS 무관하게 동일한 결과를 내기 위해 `java.lang.ProcessHandle` 을 사용한다.
 *
 * ## 판정 기준 (아래 중 하나라도 맞으면 running)
 *
 * 1. 프로세스 이미지 이름(basename) 이 `claude` / `claude.exe`
 * 2. Node.js 런처(`node` / `node.exe`) 가 npm global 설치된 Claude Code 스크립트를 실행 중
 *    (command line 에 `@anthropic-ai/claude-code` 또는 `/.bin/claude` 토큰, 경로 경계 매치)
 * 3. Windows 쉘(`cmd.exe` / `powershell.exe` / `pwsh.exe`) 이 `claude.cmd` / `claude.ps1`
 *    래퍼를 실행 중 (full path 또는 PATH 기반 파일명 호출 모두 허용)
 *
 * basename 을 먼저 검사해 **타 프로세스(grep, 에디터 등) 가 args 에 Claude 문자열을
 * 포함했을 때 발생하는 false positive 를 차단** 한다.
 *
 * ## 설계상 감지되지 않는 시나리오 (허용 범위 밖)
 *
 * - 다른 사용자(sudo 등) 소유 claude 프로세스 — `ProcessHandle.allProcesses()` visibility 밖
 * - macOS SIP-protected 프로세스 — `info().command()` / `commandLine()` empty
 * - Docker 컨테이너 내부 Claude — host 프로세스 테이블에 보이지 않음
 * - SSH 로 원격 서버에서 실행되는 Claude — 로컬 프로세스 아님
 * - Claude API 를 직접 호출하는 SDK 코드(Python/Node) — 의도적으로 CLI 만 감지
 * - Git Bash / MSYS2 의 `mintty.exe`·`bash.exe` 가 claude shell script 를 직접 spawn —
 *   쉘 화이트리스트 밖 (대부분 basename=claude 로 잡히므로 실제 영향 낮음)
 * - `SecurityManager` 가 `RuntimePermission("manageProcess")` 를 거부하는 환경 — fail-safe 로 false 반환
 *
 * ## 불확정 (실환경 검증 전까지 확정 불가)
 *
 * - `ProcessHandle.Info.commandLine()` 의 OS 별 full-argv 반환 여부
 * - commandLine truncation 발생 빈도
 * - `allProcesses()` 순회 비용의 실측 CPU/배터리 영향
 */
object ClaudeCliProbe {

    private val CLAUDE_IMAGE_NAMES = setOf("claude", "claude.exe")
    private val NODE_IMAGE_NAMES = setOf("node", "node.exe")
    private val WINDOWS_SHELL_NAMES = setOf("cmd.exe", "powershell.exe", "pwsh.exe")

    // Node.js 런처 전용 — npm 패키지 경로 또는 npm global bin 래퍼. 경로 경계 매치.
    private val NODE_LAUNCHED_CLAUDE_PATTERN = Regex(
        "@anthropic-ai/claude-code(?:[/\\s]|$)" +
            "|[/\\\\]\\.bin[/\\\\]claude(?:\\s|$)",
        RegexOption.IGNORE_CASE,
    )

    // Windows 쉘 전용 — claude.cmd / claude.ps1 래퍼.
    // 앞 경계: 문자열 시작 / 경로 구분자 / 공백 중 하나. PATH 기반 파일명 호출도 매치.
    // 뒤 경계: 공백 / 문자열 끝. claude.cmdfoo 같은 bogus 방지.
    private val WINDOWS_SHELL_CLAUDE_PATTERN = Regex(
        "(?:^|[/\\\\\\s])claude\\.(?:cmd|ps1)(?:\\s|$)",
        RegexOption.IGNORE_CASE,
    )

    fun isRunning(): Boolean = runCatching {
        ProcessHandle.allProcesses().anyMatch(::matchesClaudeCli)
    }.getOrDefault(false)

    private fun matchesClaudeCli(process: ProcessHandle): Boolean {
        val info = process.info()
        val basename = info.command().orElse(null)?.let(::basenameLower) ?: return false

        if (basename in CLAUDE_IMAGE_NAMES) return true
        if (basename in NODE_IMAGE_NAMES) return matchCommandLine(info, NODE_LAUNCHED_CLAUDE_PATTERN)
        if (basename in WINDOWS_SHELL_NAMES) return matchCommandLine(info, WINDOWS_SHELL_CLAUDE_PATTERN)
        return false
    }

    private fun matchCommandLine(info: ProcessHandle.Info, pattern: Regex): Boolean {
        val commandLine = info.commandLine().orElse(null)?.lowercase(Locale.ROOT) ?: return false
        return pattern.containsMatchIn(commandLine)
    }

    /**
     * `/a/b/claude.exe` 또는 `C:\a\b\claude.exe` 에서 `claude.exe` 를 추출.
     * 두 경로 구분자(`/`, `\`) 를 모두 처리한 뒤 locale-independent 로 소문자화.
     * `command()` 가 full path 가 아닌 bare name 만 반환해도(`claude`) 안전하게 동작.
     */
    private fun basenameLower(path: String): String =
        path.substringAfterLast('/').substringAfterLast('\\').lowercase(Locale.ROOT)
}

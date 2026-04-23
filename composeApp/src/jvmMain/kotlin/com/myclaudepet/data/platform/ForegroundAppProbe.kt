package com.myclaudepet.data.platform

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 현재 포그라운드 앱 이름 조회.
 *
 * - **macOS**: `osascript` 의 System Events 로 frontmost 프로세스 이름.
 * - **Windows**: JNA 기반 Win32 API
 *   (`GetForegroundWindow` → `GetWindowThreadProcessId`
 *    → `OpenProcess` + `QueryFullProcessImageName`) 로 실행 파일 basename.
 * - **기타 OS**: null.
 *
 * 실패 시 조용히 null 을 반환하고 `java.util.logging` 의 FINE 레벨로만 기록한다.
 * 호출자(펫 상태 전이 로직) 는 null 을 "감지 불가" 로 취급해 무동작 폴백한다.
 */
object ForegroundAppProbe {

    private val logger: Logger by lazy {
        Logger.getLogger(ForegroundAppProbe::class.java.name)
    }

    private val osFamily: OsFamily by lazy {
        val os = System.getProperty("os.name", "").lowercase()
        when {
            os.contains("mac") -> OsFamily.MAC
            os.contains("win") -> OsFamily.WINDOWS
            else -> OsFamily.OTHER
        }
    }

    fun current(): String? = when (osFamily) {
        OsFamily.MAC -> currentMac()
        OsFamily.WINDOWS -> currentWindows()
        OsFamily.OTHER -> null
    }

    private fun currentMac(): String? = runCatching {
        val process = ProcessBuilder(
            "osascript",
            "-e",
            "tell application \"System Events\" to " +
                "name of first application process whose frontmost is true",
        ).redirectErrorStream(true).start()
        val finished = process.waitFor(1500, TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            return@runCatching null
        }
        process.inputStream.bufferedReader().readText().trim()
            .takeIf { it.isNotBlank() }
    }.onFailure { logger.log(Level.FINE, "mac foreground probe failed", it) }
        .getOrNull()

    private fun currentWindows(): String? = runCatching {
        val hwnd = User32.INSTANCE.GetForegroundWindow() ?: return@runCatching null

        val pidRef = IntByReference()
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef)
        val pid = pidRef.value
        if (pid == 0) return@runCatching null

        val handle = Kernel32.INSTANCE.OpenProcess(
            WinNT.PROCESS_QUERY_LIMITED_INFORMATION,
            false,
            pid,
        ) ?: return@runCatching null

        try {
            val buf = CharArray(1024)
            val size = IntByReference(buf.size)
            val ok = Kernel32.INSTANCE.QueryFullProcessImageName(handle, 0, buf, size)
            if (!ok) return@runCatching null
            val fullPath = String(buf, 0, size.value)
            fullPath.substringAfterLast('\\').substringAfterLast('/')
                .takeIf { it.isNotBlank() }
        } finally {
            Kernel32.INSTANCE.CloseHandle(handle)
        }
    }.onFailure { logger.log(Level.FINE, "windows foreground probe failed", it) }
        .getOrNull()

    private enum class OsFamily { MAC, WINDOWS, OTHER }
}

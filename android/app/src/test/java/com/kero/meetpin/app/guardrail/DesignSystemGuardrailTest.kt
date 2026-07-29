package com.kero.meetpin.app.guardrail

import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * 디자인 시스템 이탈 방지 가드레일.
 *
 * 규칙(feature 모듈의 main 소스 대상):
 *  1. 색을 하드코딩하지 않는다 — `Color(0xFF...)` 금지. 토큰(MaterialTheme/MeetPinTheme)만 사용.
 *  2. Material3 원자 컴포넌트(Button/OutlinedButton/TextButton/(Outlined)TextField/Card)를
 *     직접 쓰지 않는다 — `:core:designsystem`의 대응 컴포넌트를 쓴다.
 *     (Scaffold/Surface/CircularProgressIndicator 같은 구조·프리미티브는 허용.)
 *
 * CI가 아직 없으므로 `./gradlew check`(app 유닛 테스트)로 로컬·향후 CI에서 강제된다.
 * Konsist로의 교체는 추후 과제. 새 외부 의존성 없이 파일 스캔으로 구현했다.
 */
class DesignSystemGuardrailTest {

    private val bannedAtomImports = listOf(
        "androidx.compose.material3.Button",
        "androidx.compose.material3.OutlinedButton",
        "androidx.compose.material3.TextButton",
        "androidx.compose.material3.OutlinedTextField",
        "androidx.compose.material3.TextField",
        "androidx.compose.material3.Card",
        "androidx.compose.material3.CardDefaults",
    )

    /**
     * TODO(DS 이관): LiveTracking 화면의 WIP(디버그 모의 코드) 정리 후 DS 컴포넌트로 이관하고
     * 이 예외를 제거한다. 예외가 비면 규칙 2가 tracking 화면에도 완전 적용된다.
     */
    private val pendingMigration = setOf(
        "LiveTrackingScreen.kt",
        "ParticipantStatusSheet.kt",
    )

    @Test
    fun `feature 모듈은 색을 하드코딩하지 않는다`() {
        val offenders = featureMainKotlinFiles()
            .filter { it.readText().contains("Color(0xFF") }
            .map { it.name }
            .sorted()

        if (offenders.isNotEmpty()) {
            fail(
                "feature 모듈에서 하드코딩 색상 Color(0xFF...)이 발견되었습니다: $offenders\n" +
                    "→ MaterialTheme.colorScheme 또는 MeetPinTheme.extendedColors 토큰을 사용하세요.",
            )
        }
    }

    @Test
    fun `feature 모듈은 M3 원자 컴포넌트를 직접 쓰지 않는다`() {
        val offenders = mutableListOf<String>()

        for (file in featureMainKotlinFiles()) {
            if (file.name in pendingMigration) continue
            val importLines = file.readLines().map { it.trim() }
            for (banned in bannedAtomImports) {
                if (importLines.any { it == "import $banned" }) {
                    offenders += "${file.name} → $banned"
                }
            }
        }

        if (offenders.isNotEmpty()) {
            fail(
                "feature 모듈에서 Material3 원자 컴포넌트를 직접 사용했습니다:\n" +
                    offenders.joinToString("\n") { "  - $it" } +
                    "\n→ :core:designsystem의 MeetPinButton/MeetPinSecondaryButton/" +
                    "MeetPinGhostButton/MeetPinCard 등을 사용하세요.",
            )
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private fun featureMainKotlinFiles(): List<File> {
        val featureDir = File(findAndroidRoot(), "feature")
        require(featureDir.isDirectory) { "feature 디렉터리를 찾지 못했습니다: $featureDir" }
        return featureDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.path.replace(File.separatorChar, '/').contains("/src/main/") }
            .toList()
    }

    /** app/feature/settings.gradle.kts를 모두 가진 android 루트를 상위로 탐색한다. */
    private fun findAndroidRoot(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(8) {
            val d = dir ?: return@repeat
            val looksLikeRoot = File(d, "feature").isDirectory &&
                File(d, "app").isDirectory &&
                File(d, "settings.gradle.kts").exists()
            if (looksLikeRoot) return d
            dir = d.parentFile
        }
        error("android 루트를 찾지 못했습니다 (feature/ + app/ + settings.gradle.kts).")
    }
}

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.File
import java.util.UUID

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
}

// 버전 / 배포 대상 GitHub 리포 메타 — gradle.properties 에서 읽어 모든 곳에서 공유.
val projectVersionProp: String = (project.findProperty("projectVersion") as String?) ?: "1.0.0"
val githubOwnerProp: String = (project.findProperty("githubOwner") as String?) ?: "YOUR_GITHUB_USERNAME"
val githubRepoProp: String = (project.findProperty("githubRepo") as String?) ?: "claude-pet"
version = projectVersionProp

// Kotlin 2.0+ merged kotlin-stdlib-jdk7/jdk8 into kotlin-stdlib.
// Transitive deps (Koin, Compose 등) still pull 1.9.24 legacy jars whose older
// kotlin.text.HexExtensionsKt shadows the 2.1.0 class, breaking kotlin.uuid.Uuid.
configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.sqldelight.coroutines.ext)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.jnativehook)
            // Windows 포그라운드 앱 감지를 위한 Win32 API (User32, Kernel32) 바인딩.
            // macOS/Linux 에서는 사용되지 않지만 jar 번들에는 항상 포함 (런타임 OS 감지 후 사용).
            implementation(libs.jna.platform)
            implementation(libs.slf4j.simple)
        }
    }
}

// ────────────────────────────── SQLDelight ──────────────────────────────
sqldelight {
    databases {
        create("PetDatabase") {
            packageName.set("com.myclaudepet.db")
            srcDirs.setFrom(project.file("src/commonMain/sqldelight"))
        }
    }
}

// ──────────────────────── Build-time Resource Injection ────────────────────────
// install_id.txt 는 배포 jar 에 classpath 리소스로 담겨, 앱 부팅 시 DB 에 저장된
// 직전 install_id 와 비교된다. 불일치 시 진행 데이터가 초기화된다.
//
// - 매 빌드마다 새 UUID 가 생성되도록 outputs.upToDateWhen(false).
// - 개발 `run` 은 jvmProcessResources 의존으로 어쩔 수 없이 install_id 가 주입되지만,
//   run 태스크에 `claudepet.dev=true` 시스템 프로퍼티를 넣어 InstallId 가 null 을
//   반환하도록 해 리셋을 스킵한다 (개발 편의).
val installIdDir = layout.buildDirectory.dir("generated/installId/resources")
val generateInstallId = tasks.register("generateInstallId") {
    outputs.dir(installIdDir)
    outputs.upToDateWhen { false }
    val targetFileProvider = installIdDir.map { it.file("install_id.txt").asFile }
    doLast {
        val f = targetFileProvider.get()
        f.parentFile.mkdirs()
        f.writeText(UUID.randomUUID().toString())
    }
}
kotlin.sourceSets.getByName("jvmMain").resources.srcDir(
    generateInstallId.map { installIdDir.get() }
)

// 앱 메타 (버전, GitHub owner/repo) 를 런타임이 읽을 수 있도록 classpath 리소스로 주입.
// 자동 업데이트 체커가 이 값으로 GitHub Releases API 호출.
val appMetaDir = layout.buildDirectory.dir("generated/appMeta/resources")
val generateAppMeta = tasks.register("generateAppMeta") {
    outputs.dir(appMetaDir)
    outputs.upToDateWhen { false }
    val targetDirProvider = appMetaDir
    val version = projectVersionProp
    val owner = githubOwnerProp
    val repo = githubRepoProp
    doLast {
        val dir = targetDirProvider.get().asFile.apply { mkdirs() }
        File(dir, "app_version.txt").writeText(version)
        File(dir, "github_owner.txt").writeText(owner)
        File(dir, "github_repo.txt").writeText(repo)
    }
}
kotlin.sourceSets.getByName("jvmMain").resources.srcDir(
    generateAppMeta.map { appMetaDir.get() }
)
tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
    systemProperty("claudepet.dev", "true")
}

// ──────────────────────── Native Distribution Pipeline ────────────────────────
// 배포 산출물을 프로젝트 루트 `dist/` 로 모은다.
// `./gradlew packageDmg` 등 실행 후 자동으로 `dist/ClaudePet-1.0.0.dmg` 에 사본 생성.
val distDir = rootProject.layout.projectDirectory.dir("dist")
val collectDistributables by tasks.registering(Copy::class) {
    description = "배포 산출물을 dist/ 로 복사"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(layout.buildDirectory.dir("compose/binaries/main/dmg")) { include("*.dmg") }
    from(layout.buildDirectory.dir("compose/binaries/main/msi")) { include("*.msi") }
    from(layout.buildDirectory.dir("compose/binaries/main/exe")) { include("*.exe") }
    from(layout.buildDirectory.dir("compose/binaries/main/deb")) { include("*.deb") }
    into(distDir)
}
tasks.matching {
    it.name in setOf("packageDmg", "packageMsi", "packageExe", "packageDeb")
}.configureEach {
    finalizedBy(collectDistributables)
}

// ──────────────────────── macOS DMG Icon Customization ────────────────────────
// jpackage 는 dmg 볼륨 내부 앱 아이콘만 처리할 뿐, dmg 파일 자체 / 마운트 볼륨의
// Finder 아이콘에는 Duke fallback 이 노출된다. 아래 두 태스크가 각각 보정.
//
//  • injectVolumeIcon : 볼륨 루트에 `.VolumeIcon.icns` + HasCustomIcon FinderInfo flag
//  • dmgIconize       : dmg 파일 자체 리소스 포크에 `fileicon` 으로 custom icon 주입
val injectVolumeIcon = tasks.register("injectVolumeIcon") {
    mustRunAfter(collectDistributables)
    val dmgPath = rootProject.file("dist/ClaudePet-1.0.0.dmg").absolutePath
    val iconPath = file("icons/app-icon.icns").absolutePath
    val scriptPath = rootProject.file("scripts/inject-volume-icon.sh").absolutePath
    doLast {
        val script = File(scriptPath)
        val dmg = File(dmgPath)
        val icon = File(iconPath)
        if (!script.canExecute() || !dmg.exists() || !icon.exists()) {
            println("injectVolumeIcon: script/dmg/icon missing — skip")
            return@doLast
        }
        val proc = ProcessBuilder("bash", scriptPath, dmgPath, iconPath)
            .redirectErrorStream(true)
            .start()
        proc.inputStream.bufferedReader().lineSequence().forEach { println(it) }
        proc.waitFor()
        println("injectVolumeIcon: exit=${proc.exitValue()}")
    }
}

// 파일 자체 Finder 아이콘. 볼륨 아이콘 주입이 dmg 를 재압축하므로 반드시 그 뒤에 실행.
val dmgIconize = tasks.register("dmgIconize") {
    mustRunAfter(injectVolumeIcon)
    val dmgPath = rootProject.file("dist/ClaudePet-1.0.0.dmg").absolutePath
    val iconPath = file("icons/app-icon.icns").absolutePath
    doLast {
        val dmg = File(dmgPath)
        val icon = File(iconPath)
        if (!dmg.exists() || !icon.exists()) {
            println("dmgIconize: dmg 또는 icns 없음, skip")
            return@doLast
        }
        val fileiconPath = listOf(
            "/opt/homebrew/bin/fileicon",
            "/usr/local/bin/fileicon",
        ).firstOrNull { File(it).canExecute() }
        if (fileiconPath == null) {
            println("dmgIconize: fileicon 미설치. `brew install fileicon` 후 다시 빌드.")
            return@doLast
        }
        val process = ProcessBuilder(fileiconPath, "set", dmgPath, iconPath)
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        println("dmgIconize: fileicon exit=${process.exitValue()}")
    }
}

tasks.matching { it.name == "packageDmg" }.configureEach {
    finalizedBy(collectDistributables, injectVolumeIcon, dmgIconize)
}

// SPEC-033 — LaunchServices 아이콘 DB 리빌드 + Dock/Finder 재시작.
val resetIconCaches = tasks.register<Exec>("resetIconCaches") {
    description = "macOS LaunchServices 아이콘 캐시 초기화 + Dock/Finder 재시작"
    commandLine("bash", rootProject.file("scripts/reset-icon-caches.sh").absolutePath)
    isIgnoreExitValue = true
}

compose.desktop {
    application {
        mainClass = "com.myclaudepet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ClaudePet"
            packageVersion = projectVersionProp
            description = "Cross-platform desktop Claude pet"
            vendor = "me"

            // jlink 가 만드는 최소 JRE 에 포함시킬 JDK 모듈.
            // `:composeApp:suggestRuntimeModules` 가 추천한 값.
            modules("java.instrument", "java.sql", "jdk.unsupported")

            macOS {
                bundleID = "com.myclaudepet.desktop"
                iconFile.set(project.file("icons/app-icon.icns"))
                // LSUIElement: 도크 숨김 (메뉴바/트레이로만 접근)
                infoPlist {
                    extraKeysRawXml = "<key>LSUIElement</key><true/>"
                }
            }
            windows {
                menuGroup = "ClaudePet"
                upgradeUuid = "6fdd1f0c-3f26-4d76-9a7f-8a4c3b9a24cc"
                // iconFile.set(project.file("icons/app-icon.ico"))
                // ↑ Windows .ico 는 ImageMagick 필요. Windows 빌드 시점에 별도 생성.
            }
            linux {
                iconFile.set(project.file("icons/app-icon.png"))
            }
        }
    }
}

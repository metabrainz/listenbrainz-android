import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

// BuildKonfig has no concept of Android build types; it generates a single config per build,
// selected by the `buildkonfig.flavor` Gradle property (read lazily in afterEvaluate). We pick the
// "release" flavor whenever a release task is requested unless a flavor was already supplied via
// -P / gradle.properties. so release builds get DEBUG=false and the keys from keystore.properties,
// while everything else falls back to the default config (local.properties, DEBUG=true).
// Note: a single invocation that builds both debug and release (e.g. `./gradlew build`) will apply
// the release flavor to both, since the flavor is global per build.
if (!project.hasProperty("buildkonfig.flavor")) {
    val buildingRelease = gradle.startParameter.taskNames.any {
        it.contains("Release", ignoreCase = true)
    }
    if (buildingRelease) {
        project.extensions.extraProperties.set("buildkonfig.flavor", "release")
    }
}

buildkonfig {
    packageName = "org.listenbrainz.shared"

    fun TargetConfigDsl.common(properties: Properties) {
        buildConfigField(
            FieldSpec.Type.STRING,
            "YOUTUBE_API_KEY",
            properties.getProperty("youtubeApiKey").orEmpty()
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "SPOTIFY_CLIENT_ID",
            properties.getProperty("spotifyClientId").orEmpty()
        )
    }

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
        common(localProperties)
    }

    defaultConfigs("release") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
        common(keystoreProperties)
    }
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "org.listenbrainz.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        // Required for Compose Multiplatform resources on Android
        androidResources {
            enable = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        optimization {
            minify = true
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(project.file("proguard-rules.pro"))
        }
    }

    // Set JVM target for Android
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    // Linking the framework needs the iOS SDK from a full Xcode install, so it is only declared
    // when the build can link: when Xcode drives it, or when xcode-select points at Xcode.app.
    // Otherwise the iOS targets still compile to klibs and `gradlew build` stays green.
    val invokedByXcode = providers.environmentVariable("SDK_NAME").isPresent
    val xcodeSelected = runCatching {
        providers.exec {
            commandLine("xcode-select", "-p")
            isIgnoreExitValue = true
        }.standardOutput.asText.get().contains("Xcode.app")
    }.getOrDefault(false)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        if (invokedByXcode || xcodeSelected) {
            target.binaries.framework {
                baseName = "sharedKit"
                export(libs.kmpnotifier.core)
                export(libs.kmpnotifier.local)
            }
        }
    }

    ktorfit {
        compilerPluginVersion.set(libs.versions.ktorfit.compiler)
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines.core)

                // Compose Multiplatform UI
                implementation(libs.compose.ui)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                api(libs.compose.resources)
                implementation(libs.compose.ui.tooling.preview)
                // datastore
                implementation(libs.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.json)
                // lifecycle
                api(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime)
                // navigation
                api(libs.androidx.navigation3.runtime)
                implementation(libs.compose.savedstate)
                // koin
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                // Room Multiplatform
                implementation(libs.androidx.room.runtime)
                api(libs.androidx.sqlite.bundled)
                // Kermit Logger
                implementation(libs.kermit)
                // Ktor & Ktorfit
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktorfit.lib)
                implementation(libs.kmp.socketio)
                implementation(libs.kotlinx.datetime)
                implementation(libs.coil.compose)
                // KMPalette
                implementation(libs.kmpalette.core)
                implementation(libs.kmpalette.androidx.palette)
                // Paging
                implementation(libs.androidx.paging.common)
                // KMP Workmanager ( Brewkits )
                api(libs.kmpworker.core)
                api(libs.kmpworker.annotations)
                api(libs.kmpnotifier.core)
                api(libs.kmpnotifier.local)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.core)
                implementation(libs.androidx.palette.ktx)
                implementation(libs.ktor.client.okhttp)
                implementation(
                    fileTree(
                        mapOf(
                            "dir" to "${rootProject.projectDir}/spotify-app-remote",
                            "include" to listOf("*.aar")
                        )
                    )
                )
                if (project.findProperty("buildkonfig.flavor") == "release") {
                    implementation(libs.chucker.noop)
                } else {
                    implementation(libs.chucker)
                }
                implementation(libs.androidx.paging.runtime)
                implementation(libs.androidx.paging.compose)
                implementation(libs.androidx.startup.runtime)
                // Android UI, until it moves to commonMain
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.ui.graphics)
                implementation(libs.androidx.compose.ui.util)
                implementation(libs.androidx.compose.material)
                implementation(libs.androidx.compose.material.icons.extended)
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.compose.material3.window.size)
                implementation(libs.androidx.compose.animation)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.browser)
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.material)
                implementation(libs.lottie.compose)
                implementation(libs.share.android)
                implementation(libs.reorderable)
                implementation(libs.google.accompanist.permissions)
                implementation(libs.app.update.ktx)
                implementation(libs.jsoup)
                implementation(libs.vico.compose)
                implementation(libs.compose.shimmer)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.test.ext.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.listenbrainz.shared.resources"
}

// Room schema configuration
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP configuration for Room compiler
dependencies {
    // Runtime-only so Android Studio can render @Preview in this module (ComposeViewAdapter)
    add("androidRuntimeClasspath", libs.compose.ui.tooling)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)

    add("kspAndroid", libs.kmpworker.ksp)
    add("kspIosArm64", libs.kmpworker.ksp)
    add("kspIosSimulatorArm64", libs.kmpworker.ksp)
}

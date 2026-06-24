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
    androidLibrary {
        namespace = "org.listenbrainz.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

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

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
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
                // datastore
                implementation(libs.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.json)
                // lifecycle
                api(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime)
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
                implementation(libs.androidx.paging.common)
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

// Room schema configuration
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP configuration for Room compiler
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

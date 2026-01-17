import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktorfit)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val localPropertiesFile = rootProject.file("local.properties")

android {
    val major = "major"
    val minor = "minor"
    val patch = "patch"
    val build = "build"

    val versionMap = mapOf(
        major to 2,
        minor to 13,
        patch to 0,
        build to 0
    )
    fun versionCode() = versionMap[major]!! * 10000 + versionMap[minor]!! * 100 + versionMap[patch]!! * 10 + versionMap[build]!! * 1
    fun versionName() = "${versionMap[major]}.${versionMap[minor]}.${versionMap[patch]}.${versionMap[build]}"

    namespace = "org.listenbrainz.android"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.listenbrainz.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = versionCode()
        versionName = versionName()
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            val localProperties = Properties()
                .takeIf { localPropertiesFile.exists() }
                ?.apply { load(FileInputStream(localPropertiesFile)) }

            fun addStringRes(name: String) =
                resValue("string", name, localProperties?.getProperty(name).orEmpty())

            addStringRes("youtubeApiKey")
            addStringRes("spotifyClientId")

            resValue("string", "environment", "debug")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }

        release {
            val keystoreProperties = Properties()
                .takeIf { keystorePropertiesFile.exists() }
                ?.apply { load(FileInputStream(keystorePropertiesFile)) }

            fun addStringRes(name: String) =
                resValue("string", name, keystoreProperties?.getProperty(name).orEmpty())

            addStringRes("youtubeApiKey")
            addStringRes("spotifyClientId")

            resValue("string", "environment", "production")

            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("playstore") {
            dimension = "version"
        }

        create("github") {
            dimension = "version"
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeCompiler {
        version = libs.versions.compose.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    ktorfit {
        compilerPluginVersion.set(libs.versions.ktorfit.compiler)
    }

    lint {
        abortOnError = false
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

dependencies {
    // AndroidX libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.palette.ktx)

    //Room DB
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Networking
    implementation(libs.kotlinx.serialization.json)

    // Ktor & Ktorfit
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktorfit.lib)

    // Image loading and processing
    implementation(libs.coil.compose)
    implementation(libs.coil.ktor)
    implementation(libs.coil.svg)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection - Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.androidx.startup.runtime)

    // UI Components
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    implementation(libs.onboarding)
    implementation(libs.share.android)
    implementation(libs.compose.ratingbar)
    implementation(libs.reorderable)

    // Accompanist
    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.accompanist.systemuicontroller)

    // Media playback
    implementation(libs.google.exoplayer.core)
    implementation(libs.google.exoplayer.ui)
    implementation(libs.google.exoplayer.mediasession)

    // Google Play Core for in-app updates
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // Spotify SDK
    api(project(":spotify-app-remote"))
    // TODO: Remove when we've migrated spotify-app-remote to androidMain of shared module.
    implementation(libs.gson)

    // Networking and parsing
    implementation(libs.jsoup)
    implementation(libs.socket.io) {
        exclude(group = "org.json", module = "json")
    }

    // Date time
    implementation(libs.kotlinx.datetime)

    // Logging
    implementation(libs.logger.android)

    // Charts
    implementation(libs.vico.compose)

    // Testing - Kotlin Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(project(":sharedTest"))

    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation(project(":sharedTest"))

    debugImplementation(libs.androidx.test.monitor)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.test.ext.junit.ktx)
    implementation(libs.turbine)

    // Chucker
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    //Navigation 3 API
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.compose.shimmer)
}

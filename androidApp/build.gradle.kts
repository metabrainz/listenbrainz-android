import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
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
        minor to 15,
        patch to 3,
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
            resValue("string", "environment", "debug")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }

        release {
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
        resValues = true
    }

    composeCompiler {
        version = libs.versions.compose.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    ktorfit {
        compilerPluginVersion.set(libs.versions.ktorfit.compiler)
    }

    lint {
        abortOnError = false
        ignoreTestSources = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

dependencies {
    // Shared KMP module
    implementation(project(":shared"))

    // AndroidX libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.core.splashscreen)

    // Networking
    implementation(libs.kotlinx.serialization.json)

    // Ktor & Ktorfit
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktorfit.lib)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.compose)

    // Dependency Injection - Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // UI Components
    implementation(libs.material)

    // Google Play Core for in-app updates
    implementation(libs.app.update)

    // Testing - Kotlin Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    // Chucker
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)
}

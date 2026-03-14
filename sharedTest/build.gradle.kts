import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.listenbrainz.sharedtest"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    lint {
        targetSdk = libs.versions.compileSdk.get().toInt()
    }
}

dependencies {
    // AndroidX and UI
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Serialization
    implementation(libs.ktor.serialization.kotlinx.json)

    // Spotify SDK for mocking remotePlaybackHandler
    api(project(":spotify-app-remote"))

    // Testing - Kotlin Test
    implementation(libs.kotlin.test)
    implementation(libs.kotlin.test.junit)
    implementation(libs.androidx.arch.core.testing)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.espresso.intents)

    // Shared module dependency for DataStorePreference
    implementation(project(":shared"))

    // App module dependency
    implementation(project(":app"))
}
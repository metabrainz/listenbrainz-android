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

    // Networking (OkHttp kept for mockwebserver)
    implementation(libs.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)

    //Spotify SDK for mocking remotePlaybackHandler
    api(project(":spotify-app-remote"))

    // Testing
    implementation(libs.junit)
    implementation(libs.mockwebserver)
    implementation(libs.androidx.arch.core.testing)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.espresso.intents)

    // App module dependency
    implementation(project(":app"))
}
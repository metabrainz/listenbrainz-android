plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        targetSdk = libs.versions.compileSdk.get().toInt()
    }
}

dependencies {
    // AndroidX and UI
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Networking
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.gson)

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
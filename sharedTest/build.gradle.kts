plugins {
    id("com.android.library")
    id("kotlin-android")
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        targetSdk = libs.versions.compileSdk.get().toInt()
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Web Service Setup
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.gson)

    // Test Setup
    implementation(libs.junit)
    implementation(libs.mockwebserver)
    implementation(libs.androidx.arch.core.testing)
    implementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.espresso.intents)

    implementation(project(":app"))
}
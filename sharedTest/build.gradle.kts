plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "org.listenbrainz.sharedtest"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    //Web Service Setup
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Test Setup
    implementation("junit:junit:4.13.2")
    implementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.12")
    implementation("androidx.arch.core:core-testing:2.2.0'")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation("androidx.room:room-testing:2.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")

    implementation("androidx.test:runner:1.5.2")
    implementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.arch.core:core-testing:2.2.0")
    implementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.test.espresso:espresso-intents:3.5.1")
    implementation("androidx.compose.ui:ui-test-junit4:$compose_version")

    implementation project(path: ":app")
}
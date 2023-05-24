import java.io.FileInputStream
import java.util.Properties

plugins {
    
    id("io.sentry.android.gradle") version "3.4.2"
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    kotlin("android")
    kotlin("kapt")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val localPropertiesFile: File = rootProject.file("local.properties")

android {
    namespace = App.namespace.appNamespace
    compileSdk = App.compileSdk
    
    signingConfigs {
        register("release") {
            if (keystorePropertiesFile.exists()) {
                
                val keystoreProperties = Properties().apply {
                    load(FileInputStream(keystorePropertiesFile))
                }
    
                with(keystoreProperties) {
                    storeFile = file(this["storeFile"] as String)
                    storePassword = this["storePassword"] as String
                    keyPassword = this["keyPassword"] as String
                    keyAlias = this["keyAlias"] as String
                }
            }
        }
    }
    defaultConfig {
        applicationId = App.namespace.appNamespace
        minSdk = App.minSdk
        targetSdk = App.targetSdk
        versionCode = App.versionCode
        versionName = App.versionName

        multiDexEnabled = true
        testInstrumentationRunner = App.testInstrumentationRunner
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            if (localPropertiesFile.exists()) {
                val localProperties = Properties().apply{
                    load(FileInputStream(localPropertiesFile))
                }

                if(localProperties.getProperty("youtubeApiKey") != null && !localProperties.getProperty("youtubeApiKey").isEmpty()){
                    resValue("string", "youtubeApiKey", localProperties["youtubeApiKey"] as String)
                }
                else{
                    resValue("string", "youtubeApiKey", "test")
                }

                if  ( localProperties.getProperty("spotifyClientId") != null && !localProperties.getProperty("spotifyClientId").isEmpty() ) {
                    resValue("string", "spotifyClientId", localProperties["spotifyClientId"] as String)
                }
                else{
                    resValue("string", "spotifyClientId", "test")
                }
            }
            else{
                resValue("string", "youtubeApiKey", "test")
                resValue("string", "spotifyClientId", "test")
            }
            resValue("string", "sentryDsn", "")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }
        getByName("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties().apply {
                    load(FileInputStream(keystorePropertiesFile))
                }
                
                with(keystoreProperties){
                    resValue("string", "youtubeApiKey", this["youtubeApiKey"] as String)
                    resValue("string", "spotifyClientId", this["spotifyClientId"] as String)
                    resValue("string", "sentryDsn", this["sentryDsn"] as String)
                }
                
                signingConfig = signingConfigs.getByName("release")
                
            } else {
                resValue("string", "youtubeApiKey", "")
                resValue("string", "spotifyClientId", "")
                resValue("string", "sentryDsn", "")
            }
            isMinifyEnabled = false
            // shrinkResources = true
            // proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = App.composeCompiler
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

dependencies {

    //AndroidX
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.core:core-ktx:${Versions.androidx.core}")
    implementation ("androidx.browser:browser:1.5.0")
    implementation ("androidx.preference:preference-ktx:1.2.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")

    //Web Service Setup
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation ("androidx.paging:paging-runtime-ktx:3.1.1")

    //Image downloading and Caching library
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.bumptech.glide:compose:1.0.0-alpha.1")
    implementation ("io.coil-kt:coil-compose:2.3.0")
    implementation ("com.caverock:androidsvg-aar:1.4")
    kapt ("com.github.bumptech.glide:compiler:4.15.1")

    //Permissions
    implementation ("com.google.accompanist:accompanist-permissions:${Versions.accompanist}")

    //Fragment Setup For Kotlin
    implementation ("androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}")
    implementation ("androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}")
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    //Design Setup
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.airbnb.android:lottie:6.0.0")
    implementation ("com.github.akshaaatt:Onboarding:1.0.5")
    implementation ("com.github.akshaaatt:Share-Android:1.0.0")

    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Versions.hilt}")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    //Jetpack Compose
    implementation ("androidx.compose.ui:ui:${Versions.compose.compose_version}")
    implementation ("androidx.compose.ui:ui-tooling:${Versions.compose.compose_version}")
    implementation ("androidx.compose.ui:ui-util:${Versions.compose.compose_version}")
    implementation ("androidx.compose.material:material:${Versions.compose.compose_version}")
    implementation ("androidx.compose.material:material-icons-extended:${Versions.compose.compose_version}")
    implementation ("androidx.compose.material3:material3:1.1.0")
    implementation ("androidx.compose.material3:material3-window-size-class:1.1.0")
    implementation ("androidx.compose.animation:animation:${Versions.compose.compose_version}")
    implementation ("androidx.compose.ui:ui-tooling-preview:${Versions.compose.compose_version}")
    implementation ("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation ("com.airbnb.android:lottie-compose:6.0.0")
    implementation ("androidx.activity:activity-compose:1.7.1")

    // Compose Navigation
    implementation ("androidx.navigation:navigation-compose:2.6.0-rc01")     // Stable one
    implementation ("com.google.accompanist:accompanist-navigation-animation:${Versions.accompanist}")     // Experimental but has animations

    //Spotify
    implementation (files("./lib/spotify-app-remote-release-0.7.2.aar"))

    //Test Setup
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.11")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    debugImplementation ("androidx.test:monitor:1.6.1")       // Solves "class PlatformTestStorageRegistery not found" error for ui tests.
    debugImplementation ("androidx.compose.ui:ui-test-manifest:${Versions.compose.compose_version}")

    kaptAndroidTest ("com.google.dagger:hilt-android-compiler:${Versions.hilt}")

    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:${Versions.compose.compose_version}")
    androidTestImplementation ("com.google.dagger:hilt-android-testing:${Versions.hilt}")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation ("app.cash.turbine:turbine:0.12.3")
    androidTestImplementation ("tools.fastlane:screengrab:2.1.1")     // Fastlane ScreenGrab

    testImplementation (project(":sharedTest"))
    androidTestImplementation (project(":sharedTest"))

    //ViewPager
    implementation ("com.google.accompanist:accompanist-pager:${Versions.accompanist}")

    //Exoplayer
    api ("com.google.android.exoplayer:exoplayer-core:${Versions.exoplayer}")
    api ("com.google.android.exoplayer:exoplayer-ui:${Versions.exoplayer}")
    api ("com.google.android.exoplayer:extension-mediasession:${Versions.exoplayer}")

    //Room db
    implementation ("androidx.room:room-runtime:${Versions.room}")
    kapt ("androidx.room:room-compiler:${Versions.room}")
    implementation ("androidx.room:room-ktx:${Versions.room}")
    testImplementation ("androidx.room:room-testing:${Versions.room}")

    //Jetpack Compose accompanists (https://github.com/google/accompanist)
    implementation ("com.google.accompanist:accompanist-systemuicontroller:${Versions.accompanist}")

    // Util
    implementation ("com.github.dariobrux:Timer:1.1.0")
}
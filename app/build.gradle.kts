import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    id("io.sentry.android.gradle") version "4.7.0"
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val localPropertiesFile = rootProject.file("local.properties")

android {
    namespace = "org.listenbrainz.android"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.listenbrainz.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 51
        versionName = "2.6.1"
        multiDexEnabled = true
        testInstrumentationRunner = "org.listenbrainz.android.di.CustomTestRunner"
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
            if (localPropertiesFile.exists()) {
                val localProperties = Properties()
                localProperties.load(FileInputStream(localPropertiesFile))

                if (localProperties.getProperty("youtubeApiKey") != null && localProperties.getProperty("youtubeApiKey").isNotEmpty()) {
                    resValue("string", "youtubeApiKey", localProperties.getProperty("youtubeApiKey"))
                } else {
                    resValue("string", "youtubeApiKey", "test")
                }

                if (localProperties.getProperty("spotifyClientId") != null && localProperties.getProperty("spotifyClientId").isNotEmpty()) {
                    resValue("string", "spotifyClientId", localProperties.getProperty("spotifyClientId"))
                } else {
                    resValue("string", "spotifyClientId", "test")
                }
            } else {
                resValue("string", "youtubeApiKey", "test")
                resValue("string", "spotifyClientId", "test")
            }
            resValue("string", "sentryDsn", "")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }

        release {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                resValue("string", "youtubeApiKey", keystoreProperties.getProperty("youtubeApiKey"))
                resValue("string", "spotifyClientId", keystoreProperties.getProperty("spotifyClientId"))
                resValue("string", "sentryDsn", keystoreProperties.getProperty("sentryDsn"))

                signingConfig = signingConfigs.getByName("release")
            } else {
                resValue("string", "youtubeApiKey", "")
                resValue("string", "spotifyClientId", "")
                resValue("string", "sentryDsn", "")
            }
            isMinifyEnabled = false
            // isShrinkResources = true
            // proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.glide)
    implementation(libs.glide.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidsvg)
    ksp(libs.glide.compiler)

    implementation(libs.google.accompanist.permissions)

    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.onboarding)
    implementation(libs.share.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.lottie.compose)

    // Dependency Injection with Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.startup.runtime)
    androidTestImplementation(libs.hilt.android)

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

    //Spotify
    implementation(files("./lib/spotify-app-remote-release-0.7.2.aar"))

    implementation(libs.jsoup)

    implementation(libs.socket.io) {
        exclude(group = "org.json", module = "json")
    }

    implementation(libs.androidx.test.ext.junit.ktx)
    implementation(libs.turbine)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    debugImplementation(libs.androidx.test.monitor)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)

    testImplementation(project(":sharedTest"))
    androidTestImplementation(project(":sharedTest"))

    implementation(libs.google.exoplayer.core)
    implementation(libs.google.exoplayer.ui)
    implementation(libs.google.exoplayer.mediasession)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.google.accompanist.systemuicontroller)

    implementation(libs.compose.ratingbar)
    implementation(libs.logger.android)

    implementation(libs.vico.compose)
}
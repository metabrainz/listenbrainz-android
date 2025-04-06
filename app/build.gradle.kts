import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sentry)
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
        versionCode = 60
        versionName = "2.8.3"
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
            val localProperties = Properties()
                .takeIf { localPropertiesFile.exists() }
                ?.apply { load(FileInputStream(localPropertiesFile)) }

            fun addStringRes(name: String) =
                resValue("string", name, localProperties?.getProperty(name)?.toString().toString())

            addStringRes("youtubeApiKey")
            addStringRes("spotifyClientId")
            addStringRes("sentryDsn")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }

        release {
            val keystoreProperties = Properties()
                .takeIf { keystorePropertiesFile.exists() }
                ?.apply { load(FileInputStream(keystorePropertiesFile)) }

            fun addStringRes(name: String) =
                resValue("string", name, keystoreProperties?.getProperty(name)?.toString().toString())

            addStringRes("youtubeApiKey")
            addStringRes("spotifyClientId")
            addStringRes("sentryDsn")

            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
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

sentry {
    org.set("metabrainz")
    projectName.set("android")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
    // TODO: Enable when server upload body max size is increased.
    autoUploadProguardMapping.set(false)
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
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Image loading and processing
    implementation(libs.glide)
    implementation(libs.glide.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidsvg)
    ksp(libs.glide.compiler)

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

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.startup.runtime)

    // UI Components
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    implementation(libs.onboarding)
    implementation(libs.share.android)
    implementation(libs.compose.ratingbar)

    // Accompanist
    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.accompanist.systemuicontroller)

    // Media playback
    implementation(libs.google.exoplayer.core)
    implementation(libs.google.exoplayer.ui)
    implementation(libs.google.exoplayer.mediasession)

    // Spotify SDK
    api(project(":spotify-app-remote"))

    // Networking and parsing
    implementation(libs.jsoup)
    implementation(libs.socket.io) {
        exclude(group = "org.json", module = "json")
    }

    // Logging
    implementation(libs.logger.android)

    // Charts
    implementation(libs.vico.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(project(":sharedTest"))

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.hilt.android)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(project(":sharedTest"))

    kspAndroidTest(libs.hilt.android.compiler)

    debugImplementation(libs.androidx.test.monitor)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.test.ext.junit.ktx)
    implementation(libs.turbine)

    // Chucker
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
    id("dagger.hilt.android.plugin")
    id("io.sentry.android.gradle") version "4.2.0"
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val localPropertiesFile = rootProject.file("local.properties")

android {
    namespace = "org.listenbrainz.android"
    compileSdkVersion(34)

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                storeFile = file(keystoreProperties["storeFile"])
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "org.listenbrainz.android"
        minSdkVersion(21)
        targetSdkVersion(34)
        versionCode = 48
        versionName = "2.5.4"
        multiDexEnabled = true
        testInstrumentationRunner = "org.listenbrainz.android.di.CustomTestRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            if (localPropertiesFile.exists()) {
                val localProperties = Properties()
                localProperties.load(FileInputStream(localPropertiesFile))

                val youtubeApiKey = localProperties.getProperty("youtubeApiKey") ?: "test"
                val spotifyClientId = localProperties.getProperty("spotifyClientId") ?: "test"

                buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")
                buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClientId\"")
            }

            buildConfigField("String", "SENTRY_DSN", "\"\"")

            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }
        getByName("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                buildConfigField("String", "YOUTUBE_API_KEY", "\"${keystoreProperties["youtubeApiKey"]}\"")
                buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${keystoreProperties["spotifyClientId"]}\"")
                buildConfigField("String", "SENTRY_DSN", "\"${keystoreProperties["sentryDsn"]}\"")

                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lintOptions {
        isAbortOnError = false
    }

    kaptOptions {
        correctErrorTypes = true
    }

    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}
dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Web Service Setup
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.12")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // Image downloading and Caching library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.caverock:androidsvg-aar:1.4")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")

    // Design Setup
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.akshaaatt:Onboarding:1.1.3")
    implementation("com.github.akshaaatt:Share-Android:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Dagger-Hilt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-android-compiler:$hilt_version")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.startup:startup-runtime:1.1.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hilt_version")

    // Jetpack Compose
    implementation("androidx.compose:compose-bom:2023.10.01")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:$navigationVersion")

    // Spotify
    implementation(files("./lib/spotify-app-remote-release-0.7.2.aar"))

    // HTML Parser for retrieving token
    implementation("org.jsoup:jsoup:1.17.2")

    // Socket IO
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // Test Setup
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Mockito framework
    testImplementation("org.mockito:mockito-core:5.9.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    debugImplementation("androidx.test:monitor:1.6.1") // Solves "class PlatformTestStorageRegistery not found" error for ui tests.
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    androidTestImplementation("androidx.work:work-testing:$work_version")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    // androidTestImplementation 'tools.fastlane:screengrab:2.1.1' // Fastlane ScreenGrab

    testImplementation(project(path = ":sharedTest"))
    androidTestImplementation(project(path = ":sharedTest"))

    // ViewPager
    implementation("com.google.accompanist:accompanist-pager:$accompanist_version")

    // Exoplayer
    api("com.google.android.exoplayer:exoplayer-core:$exoplayer_version")
    api("com.google.android.exoplayer:exoplayer-ui:$exoplayer_version")
    api("com.google.android.exoplayer:extension-mediasession:$exoplayer_version")

    // Room db
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")
    implementation("androidx.paging:paging-compose:$paging_version")
    testImplementation("androidx.paging:paging-common-ktx:$paging_version")

    // Jetpack Compose accompanists (https://github.com/google/accompanist)
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")

    // Third party libraries
    implementation("com.github.a914-gowtham:compose-ratingbar:1.3.4")
    implementation("com.github.akshaaatt:Logger-Android:1.0.0")

    // Charting Library (Vico)
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
}

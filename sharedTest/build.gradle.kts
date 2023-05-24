plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}
android {

    namespace = App.namespace.sharedTestNamespace
    compileSdk = App.compileSdk

    defaultConfig {
        minSdk = App.minSdk
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
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

    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")

    //Web Service Setup
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation ("androidx.paging:paging-runtime-ktx:3.1.1")

    //Test Setup
    implementation ("junit:junit:4.13.2")
    implementation ("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.11")
    implementation ("androidx.arch.core:core-testing:2.2.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    implementation ("androidx.room:room-testing:2.5.1")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:${Versions.compose.compose_version}")

    implementation ("androidx.test:runner:1.5.2")
    implementation ("androidx.test.ext:junit:1.1.5")
    implementation ("androidx.arch.core:core-testing:2.2.0")
    implementation ("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.test.espresso:espresso-intents:3.5.1")
    implementation ("androidx.compose.ui:ui-test-junit4:${Versions.compose.compose_version}")
    implementation ("app.cash.turbine:turbine:0.12.3")
    
    implementation(project(":app"))
}
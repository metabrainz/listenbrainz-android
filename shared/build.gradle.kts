import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "org.listenbrainz.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    // Set JVM target for Android
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    // iOS targets for future use
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.datastore.preferences.core)
            api(libs.kotlinx.coroutines.core)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

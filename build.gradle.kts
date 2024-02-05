import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
    id("dagger.hilt.android.plugin") version "2.50"
}

val kotlinVersion by extra("1.9.22")
val navigationVersion by extra("2.7.6")
val hiltVersion by extra("2.50")
val composeVersion by extra("1.5.4")
val roomVersion by extra("2.6.1")
val accompanistVersion by extra("0.32.0")
val workVersion by extra("2.9.0")
val exoplayerVersion by extra("2.19.1")
val pagingVersion by extra("3.2.1")

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

subprojects {
    tasks.withType(KotlinCompile::class) {
        configureEach {
            kotlinOptions {
                if (project.findProperty("composeCompilerReports") == "true") {
                    freeCompilerArgs += listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                                project.buildDir.absolutePath + "/compose_compiler"
                    )
                }
                if (project.findProperty("composeCompilerMetrics") == "true") {
                    freeCompilerArgs += listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                                project.buildDir.absolutePath + "/compose_compiler"
                    )
                }
            }
        }
    }
}

android {
    compileSdkVersion(31)

    defaultConfig {
        applicationId = "your.package.name"
        minSdkVersion(21)
        targetSdkVersion(31)
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
}

// This block of code is used to apply various plugins to the project.
plugins {
    // This applies the Android Application plugin to the project.
    // This plugin extends the 'com.android.application' plugin and allows you to build an Android app.
    alias(libs.plugins.androidApplication)

    // This applies the JetBrains Kotlin Android plugin to the project.
    // This plugin allows you to write Android apps using the Kotlin language.
    alias(libs.plugins.jetbrainsKotlinAndroid)

    // This applies the Kotlin Symbol Processing (KSP) plugin to the project.
    // KSP is a fast, lightweight compiler plugin for processing Kotlin code.
    alias(libs.plugins.kotlinSymbolProcessing)

    // This applies the Dagger Hilt Android plugin to the project.
    // Hilt is a dependency injection library for Android that reduces the boilerplate of doing manual dependency injection in your project.
    alias(libs.plugins.daggerHiltAndroid)

    // This applies the Kotlin Annotation Processing (kapt) plugin to the project.
    // kapt is a compiler plugin for Kotlin that allows you to use annotation processors, which can create boilerplate code for you.
    kotlin("kapt")

    // This applies the Kotlin Serialization plugin to the project.
    // This plugin allows you to convert data classes to and from JSON.
    kotlin("plugin.serialization") version "1.8.0" // Use the appropriate version
}

android {
    namespace = "com.combros.vendingmachine"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.combros.vendingmachine"
        minSdk = 22
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11")
            }
        }
    }

//    lintOptions {
//        checkReleaseBuilds false
//        abortOnError false
//    }

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "1234567890"

//            storeFile = file("C:\\leduytuanvu\\develop\\application\\kotlin\\vendingmachine\\keystore\\keystore2.jks")
//            storeFile = file("C:\\AVF\\vendingmachine\\keystore\\keystore2.jks")
            storeFile = file("/Users/admin/Documents/develop/vendingmachine/keystore/keystore2.jks")
            storePassword = "1234567890"
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "API_URL", "\"https://dev-api.avf.vn\"")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_URL", "\"https://api.avf.vn\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
        languageVersion = "1.9"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/java/com/combros/vendingmachine/core/native/CMakeLists.txt")
        }
    }
    buildToolsVersion = "35.0.0"
    ndkVersion = "25.1.8937393"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Arrow
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // Coil
    implementation(libs.coil.compose)

    // Android Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Dagger Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    // ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    // Datetime
    implementation(libs.threetenabp)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    coreLibraryDesugaring (libs.desugar.jdk.libs)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // Interceptor
    implementation(libs.logging.interceptor)

    // Runtime
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle
    implementation(libs.core)

    // Location
    implementation(libs.play.services.location)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
}

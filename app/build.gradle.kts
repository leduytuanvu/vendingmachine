plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinSymbolProcessing)
    alias(libs.plugins.daggerHiltAndroid)
}

android {
    namespace = "com.leduytuanvu.vendingmachine"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.leduytuanvu.vendingmachine"
        minSdk = 24
        targetSdk = 34
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

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "1234567890"
//            storeFile = file("C:\\Users\\leduytuanvu\\Develop\\Kotlin\\vendingmachine\\keystore\\keystore.jks")
            storePassword = "1234567890"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

//    buildTypes {
////        release {
////            isMinifyEnabled = false
////            proguardFiles(
////                getDefaultProguardFile("proguard-android-optimize.txt"),
////                "proguard-rules.pro"
////            )
////        }
//        release {
//            signingConfig signingConfigs.release
//        }
//    }



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
            path("src/main/java/com/leduytuanvu/vendingmachine/core/native/CMakeLists.txt")
        }
    }
    buildToolsVersion = "34.0.0"
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
    ksp(libs.dagger.hilt.android.compiler)
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

    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("com.google.zxing:core:3.4.1")

    implementation("com.google.android.gms:play-services-location:21.0.1")


}

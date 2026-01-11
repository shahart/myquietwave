
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.shahartal.myquietchannel"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.shahartal.myquietchannel"
        minSdk = 26
        targetSdk = 36

        versionCode = 34
        versionName = "1.23"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        ndk {
//            debugSymbolLevel = "FULL"
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
//        debug {
//            ndk {
//                debugSymbolLevel = "FULL"
//            }
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(11)
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.measurement.api)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)

//    androidTestImplementation("com.android.support.test:rules:1.0.2")
    androidTestImplementation("androidx.test:rules:1.6.1")

//    implementation("com.google.android.gms:play-services-location:21.3.0")

//    implementation("com.google.firebase:firebase-analytics:34.0.0")

    implementation(libs.firebase.analytics)

    implementation(platform(libs.firebase.bom))

//    implementation(libs.androidx.leanback)
    // implementation("com.google.android.play:review:2.0.2")
    // implementation("com.google.android.play:review-ktx:2.0.2")

}

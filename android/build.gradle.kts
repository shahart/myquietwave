// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("dev.detekt") version "2.0.0-alpha.6" apply false
    id("com.google.firebase.crashlytics") version "3.0.8" apply false
}

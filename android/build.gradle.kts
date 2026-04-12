// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("io.gitlab.arturbosch.detekt") version "1.23.5" // Use the latest version
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}
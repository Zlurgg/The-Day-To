// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsKotlinKsp) apply false
    alias(libs.plugins.hiltPlugin) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}
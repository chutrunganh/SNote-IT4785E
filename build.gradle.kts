// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
    }

    dependencies {
        val navVersion = "2.7.5"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
        classpath("com.google.gms:google-services:4.4.2")
    }
}


plugins {
    id ("com.android.application") version "8.7.2" apply false
    id ("com.android.library") version "8.7.2" apply false
    id ("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false

}
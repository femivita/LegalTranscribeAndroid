plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        androidMain {
            kotlin.srcDirs("src/main/kotlin")
        }
        androidMain.dependencies {
            implementation("com.legal.transcriber:shared")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation("com.google.firebase:firebase-bom:33.5.1")
            implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
            implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
            implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
            implementation("androidx.datastore:datastore-preferences:1.1.1")
        }
    }
}

android {
    namespace = "com.legal.transcriber"
    compileSdk = 35

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            res.srcDirs("src/main/res")
        }
    }

    defaultConfig {
        applicationId = "com.legal.transcriber"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = "legaltranscriber2024"
            keyAlias = "legal-transcriber"
            keyPassword = "legaltranscriber2024"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

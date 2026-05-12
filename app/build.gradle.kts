plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // 🔥 Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nammahomestay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nammahomestay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // 🔹 Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 🔹 Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // 🔹 Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 🔥 Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 🔥 Firebase Auth (login system)
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")

    // 🔥 Firebase Firestore (database)
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")

    // 🔥 Firebase Storage (image upload)
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")

    // 🔥 Image loading (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 🧪 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

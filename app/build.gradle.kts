plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.points"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.points"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Firebase BOM (te asegura usar versiones compatibles)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Firebase
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    
    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Coil para Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Lottie para animaciones
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    
    // Shimmer para efectos de carga
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    
    // Environment variables
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    
    // Accompanist para efectos adicionales
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
    
    // Material 3 Extended
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
    
    // ConstraintLayout para Compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    
    // Navigation Animation
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")

    // Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Extended icons (Event, Report, NotificationImportant, etc.)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.text)
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.8.0")
}

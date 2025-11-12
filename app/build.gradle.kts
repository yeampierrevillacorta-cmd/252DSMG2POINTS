plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
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
        buildConfig = true
    }
}

dependencies {
    // --- Firebase (BOM) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    
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
    
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    
    // OkHttp para interceptores y logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

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
    //Viemodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.text)
    
    //Dashboards
    implementation(libs.tehras.charts)
    
    // Room para almacenamiento local
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // SharedPreferences
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // DataStore (alternativa moderna a SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Gson para serialización JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
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

// ========== Tarea para copiar .env a assets ==========
// Esta tarea copia el archivo .env de la raíz del proyecto a app/src/main/assets/
// para que esté disponible en la aplicación Android
tasks.register<Copy>("copyEnvToAssets") {
    val envFile = file("../.env")
    val assetsDir = file("src/main/assets")
    
    from(envFile)
    into(assetsDir)
    onlyIf { envFile.exists() }
    
    doFirst {
        assetsDir.mkdirs()
    }
}

// Ejecutar copia de .env antes de procesar recursos (preBuild)
tasks.named("preBuild") {
    dependsOn("copyEnvToAssets")
}

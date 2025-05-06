plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.photoplant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.photoplant"
        minSdk = 24
        targetSdk = 35
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

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Dependência fazer as chamadas HTTP à API REST do Gemini
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Dependências de autenticação do Google Cloud
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.34.0")
    implementation ("com.google.auth:google-auth-library-credentials:1.34.0")

    // Dependência para processar as respostas JSON da API Gemini
    implementation ("com.google.code.gson:gson:2.13.1")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // No necesitamos un plugin específico para Compose, solo habilitarlo en buildFeatures
}

android {
    namespace = "com.tudominio.checklistapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tudominio.checklistapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Comentamos el resto de dependencias para agregarlas conforme avancemos
    // Descomentar según necesitemos implementar nuevas funcionalidades

    // Para iconos extendidos
    implementation(libs.androidx.compose.material.icons.extended)

    // Para guardar datos
    // implementation(libs.androidx.datastore.preferences)
    // implementation(libs.gson)

    // Para imágenes y cámara
    // implementation(libs.coil.compose)
    // implementation(libs.accompanist.permissions)
    // implementation(libs.androidx.camera.camera2)
    // implementation(libs.androidx.camera.lifecycle)
    // implementation(libs.androidx.camera.view)

    // Para generación de PDF
    // implementation(libs.itextpdf)
}
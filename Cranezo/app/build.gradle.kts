import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

val defaultToken = project.findProperty("defaultToken") ?: "default_value_if_not_found"

// Google Maps key (Maps SDK for Android) from local.properties: MAPS_API_KEY=...  Never committed.
val mapsApiKey: String = run {
    val f = rootProject.file("local.properties")
    if (!f.exists()) "" else java.util.Properties().apply { f.inputStream().use { load(it) } }.getProperty("MAPS_API_KEY", "")
}

android {
    namespace = "com.cranezo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cranezo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // the map screens show a plain location card until a key is set
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("Boolean", "HAS_MAPS_KEY", if (mapsApiKey.isNotBlank()) "true" else "false")
    }

    buildTypes {
        release {
            // Smaller, faster release APK: unused code and resources are removed
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "SERVER_URL",
                value = "\"https://cranezo.com/api/\""
            )
            buildConfigField("String", "API_KEY", "$defaultToken")
        }
        debug {
            buildConfigField(
                "String",
                "SERVER_URL",
                value = "\"https://cranezo.com/api/\""
            )

            buildConfigField("String", "API_KEY", "$defaultToken")

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.runtime:runtime-livedata")

    //dagger:hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)


    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

    //Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)



    implementation(libs.logging.interceptor)

    implementation("androidx.core:core-splashscreen:1.0.1")

    // icons
    implementation("androidx.compose.material:material-icons-extended")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Maps in the app (Rapido-style booking screens)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:4.4.1")

    // Razorpay Checkout (customer online payments: advance and final balance)
    implementation("com.razorpay:checkout:1.6.33")

    // Firebase Cloud Messaging (push notifications)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")


    implementation(libs.coil.compose)
}
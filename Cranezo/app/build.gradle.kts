import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

val defaultToken = project.findProperty("defaultToken") ?: "default_value_if_not_found"

android {
    namespace = "com.cranezo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cranezo"
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
            buildConfigField(
                "String",
                "SERVER_URL",
                value = "\"https://chits.frelenz.tech/api/\""
            )
            buildConfigField("String", "API_KEY", "$defaultToken")
        }
        debug {
            buildConfigField(
                "String",
                "SERVER_URL",
                value = "\"https://scrapdaddy.in/api/customer/\""
            )

            buildConfigField(
                "String",
                "IMAGE_URL",
                value = "\"https://scrapdaddy.in/\""
            )

            buildConfigField(
                "String",
                "PRIVACY_POLICY_URL",
                value = "\"https://nscompaniesgrouptraining.website/privacyPolicy\""
            );

            buildConfigField(
                "String",
                "TERMS",
                value = "\"https://nscompaniesgrouptraining.website/termsofservice\""
            );

            buildConfigField(
                "String",
                "GUIDANCE_VIDEO_LINK",
                value = "\"https://www.youtube.com/@nstaxconsultancy7845\""
            )
            buildConfigField(
                "String",
                "SERVICE_LINK",
                value = "\"https://www.nscompaniesgrouptraining.website/auth/workservices/\""
            )
            buildConfigField(
                "String",
                "RATE_US_YES",
                value = "\"https://www.nscompaniesgrouptraining.website/rateUs\""
            )
            buildConfigField(
                "String",
                "RATE_US_NO",
                value = "\"https://www.nscompaniesgrouptraining.website/grievenceForm\""
            )
            buildConfigField(
                "String",
                "GIVE_TESTIMONIAL",
                value = "\"https://www.nscompaniesgrouptraining.website/nstaxconsultancy/giveTestimonial\""
            )
            buildConfigField(
                "String",
                "YOU_TUBE",
                value = "\"https://www.nscompaniesgrouptraining.website/nstaxconsultancy/videos/011d2040-ae23-4b90-ab5c-1120f1fe7bc7?flag=011d2040-ae23-4b90-ab5c-1120f1fe7bc7\""
            )
            buildConfigField(
                "String",
                "IS0",
                value = "\"https://nscompaniesgrouptraining.website/images/Iso%20Certificate.pdf\""
            )
            buildConfigField(
                "String",
                "BRANCHES",
                value = "\"https://nscompaniesgrouptraining.website/branches\""
            )
            buildConfigField(
                "String",
                "CALL_US",
                value = "\"https://nscompaniesgrouptraining.website/callUs\""
            )
            buildConfigField(
                "String",
                "WHATS_UP",
                value = "\"https://nscompaniesgrouptraining.website/whatsappUpUser\""
            )
            buildConfigField(
                "String",
                "LATEST_UPDATES",
                value = "\"https://www.nscompaniesgrouptraining.website/getLatestUpdates\""
            )
            buildConfigField(
                "String",
                "YOUTUBE_CHANNEL",
                value = "\"https://www.youtube.com/@nstaxconsultancy7845\""
            )
            buildConfigField(
                "String",
                "CALL",
                value = "\"https://www.nscompaniesgrouptraining.website/callUs\""
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
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
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
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.extensions)

    // optional - helpers for implementing LifecycleOwner in a Service
    implementation(libs.androidx.lifecycle.service)

    //Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.okhttp.v493)
    implementation(libs.logging.interceptor)

    implementation("androidx.core:core-splashscreen:1.0.1")

    // icons
    implementation("androidx.compose.material:material-icons-extended")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Razorpay
    implementation("com.razorpay:checkout:1.6.33")

    implementation (libs.lottie.compose)
    implementation (libs.coil.compose)
}
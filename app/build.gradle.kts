import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    val kotlin_version = "2.0.21"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    kotlin("plugin.serialization") version "$kotlin_version"

}

android {
    namespace = "com.thenewkenya.ingrediet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thenewkenya.ingrediet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val keystoreFile = project.rootProject.file("apikeys.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY") ?: ""

                buildConfigField(
                    type = "String",
                    name = "SUPABASE_ANON_KEY",
                    value = "\"$supabaseAnonKey\""
                )

        val supabaseUrl = properties.getProperty("SUPABASE_URL") ?: ""

                buildConfigField(
                    type = "String",
                    name = "SUPABASE_URL",
                    value = "\"$supabaseUrl\""
                )

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
        buildConfig = true
        compose = true
    }
}

dependencies {

    // Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.auth.kt)
    implementation(libs.storage.kt)

    // Ktor
    implementation(libs.ktor.client.okhttp)

    //implementation(libs.androidx.material.icons.extended)
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

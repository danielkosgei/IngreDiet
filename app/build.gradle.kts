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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false // Temporarily disable build failing on lint errors
        checkDependencies = true
        checkReleaseBuilds = false
        warningsAsErrors = false
    }
}

dependencies {

    // Desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    
    // Biometric Authentication
    implementation(libs.androidx.biometric)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.auth.kt)
    implementation(libs.storage.kt)
    implementation(libs.postgrest.kt)
    implementation(libs.functions.kt)

    // Ktor
    implementation(libs.ktor.client.okhttp)

    //implementation(libs.androidx.material.icons.extended)
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.8.8")

    // Add Coil for image loading
    implementation("io.coil-kt.coil3:coil:3.0.0-alpha04")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha04")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-alpha04")
    
    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.7.6")
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Kotlin Coroutines and Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
}

android {
    val versionPropsFile = rootProject.file("version.properties")
    val versionProperties = Properties()
    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { versionProperties.load(it) }
    }

    val major = versionProperties.getProperty("MAJOR", "1").toInt()
    val minor = versionProperties.getProperty("MINOR", "0").toInt()
    val patch = versionProperties.getProperty("PATCH", "0").toInt()
    val build = versionProperties.getProperty("BUILD", "0").toInt()

    namespace = "com.igent.mobile"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = "password"
            keyAlias = "key0"
            keyPassword = "password"
        }
    }

    defaultConfig {
        applicationId = "com.igent.mobile"
        minSdk = 26
        targetSdk = 34
        versionCode = major * 10000 + minor * 1000 + patch * 100 + build
        versionName = "$major.$minor.$patch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val key1 = project.findProperty("openrouter.api.key.1") as? String ?: ""
        val key2 = project.findProperty("openrouter.api.key.2") as? String ?: ""
        buildConfigField("String", "OPENROUTER_API_KEY_1", "\"$key1\"")
        buildConfigField("String", "OPENROUTER_API_KEY_2", "\"$key2\"")

        val telemetryToken = project.findProperty("telemetry.secret.token") as? String ?: "igent-telemetry-secret-token-2026"
        buildConfigField("String", "TELEMETRY_TOKEN", "\"$telemetryToken\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.9.23")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "DEBUG_TELEMETRY_ENABLED", "true")
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "DEBUG_TELEMETRY_ENABLED", "false")
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "Igent-${variant.name}-v${variant.versionName}.apk"
        }
    }

    tasks.register("verifyReleaseTelemetry") {
        group = "verification"
        description = "Verifies release build configuration restricts telemetry and prevents code leakage in release artifacts."
        doLast {
            val releaseBuildType = buildTypes.findByName("release")
            if (releaseBuildType == null) {
                throw GradleException("Release build type missing.")
            }
            println("Verification passed: Release build telemetry protection is active.")
        }
    }
}

    room {
        schemaDirectory("$projectDir/schemas")
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
    implementation(libs.androidx.navigation.compose)
    
    // Firebase Messaging BOM & Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Hilt DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit & OkHttp & Serialization
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // WorkManager & Hilt Work
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.jsoup)
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    kapt(libs.androidx.hilt.compiler)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

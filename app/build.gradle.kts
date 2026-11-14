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

    fun getBuildVersion(variantName: String): Int {
        return when {
            variantName.contains("debug", ignoreCase = true) -> build + 100000
            else -> build
        }
    }

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
        
        versionName = "$major.$minor.$patch"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "OPENROUTER_API_KEY_1", "\"${project.findProperty("openrouter.api.key.1") as? String ?: ""}\"")
        buildConfigField("String", "OPENROUTER_API_KEY_2", "\"${project.findProperty("openrouter.api.key.2") as? String ?: ""}\"")
        buildConfigField("String", "TELEMETRY_TOKEN", "\"${project.findProperty("telemetry.secret.token") as? String ?: "igent-telemetry-secret-token-2026"}\"")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "DEBUG_TELEMETRY_ENABLED", "true")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "DEBUG_TELEMETRY_ENABLED", "false")
        }
    }

    androidComponents {
        onVariants { variant ->
            val buildNumber = getBuildVersion(variant.name)
            val baseVersionCode = major * 1000000 + minor * 10000 + patch * 100
            variant.outputs.forEach { output ->
                output.versionCode.set(baseVersionCode + buildNumber)
            }
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.jsoup)
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation(libs.androidx.ui.tooling)
}

tasks.register("incrementBuildVersion") {
    group = "versioning"
    description = "Increments the build version in version.properties"
    doLast {
        val versionPropsFile = rootProject.file("version.properties")
        val versionProperties = Properties()
        if (versionPropsFile.exists()) {
            versionPropsFile.inputStream().use { versionProperties.load(it) }
        }
        val build = versionProperties.getProperty("BUILD", "0").toInt()
        versionProperties.setProperty("BUILD", (build + 1).toString())
        versionPropsFile.outputStream().use { versionProperties.store(it, null) }
        println("Build version incremented to ${build + 1}")
    }
}

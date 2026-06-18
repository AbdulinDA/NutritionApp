plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

fun Project.stringConfig(
    gradleName: String,
    envName: String,
    defaultValue: String
): String {
    return providers.gradleProperty(gradleName)
        .orElse(providers.environmentVariable(envName))
        .orElse(defaultValue)
        .get()
}

fun Project.isReleaseBuildRequested(): Boolean {
    return gradle.startParameter.taskNames.any { taskName ->
        taskName.contains("Release", ignoreCase = true)
    }
}

android {
    namespace = "com.abdulin.nutritionapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.abdulin.nutritionapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val debugBaseUrl = project.stringConfig(
                gradleName = "nutritionApiBaseUrlDebug",
                envName = "NUTRITION_API_BASE_URL_DEBUG",
                defaultValue = "https://adminpannel.online/api/"
            )
            val debugDomain = project.stringConfig(
                gradleName = "nutritionApiDomainDebug",
                envName = "NUTRITION_API_DOMAIN_DEBUG",
                defaultValue = "adminpannel.online"
            )
            val debugCertPin = project.stringConfig(
                gradleName = "nutritionApiCertPinDebug",
                envName = "NUTRITION_API_CERT_PIN_DEBUG",
                defaultValue = ""
            )

            buildConfigField("String", "API_BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("String", "API_DOMAIN", "\"$debugDomain\"")
            buildConfigField("String", "API_CERT_PIN", "\"$debugCertPin\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "false")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            val releaseBaseUrl = project.stringConfig(
                gradleName = "nutritionApiBaseUrlRelease",
                envName = "NUTRITION_API_BASE_URL_RELEASE",
                defaultValue = "https://adminpannel.online/api/"
            )
            val releaseDomain = project.stringConfig(
                gradleName = "nutritionApiDomainRelease",
                envName = "NUTRITION_API_DOMAIN_RELEASE",
                defaultValue = "adminpannel.online"
            )
            val releaseCertPin = project.stringConfig(
                gradleName = "nutritionApiCertPinRelease",
                envName = "NUTRITION_API_CERT_PIN_RELEASE",
                defaultValue = ""
            )

            if (project.isReleaseBuildRequested() && releaseCertPin.isBlank()) {
                throw GradleException("Release build requires NUTRITION_API_CERT_PIN_RELEASE (sha256/... pin).")
            }

            buildConfigField("String", "API_BASE_URL", "\"$releaseBaseUrl\"")
            buildConfigField("String", "API_DOMAIN", "\"$releaseDomain\"")
            buildConfigField("String", "API_CERT_PIN", "\"$releaseCertPin\"")
            buildConfigField("boolean", "ENABLE_NETWORK_LOGS", "false")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.runtime.livedata)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.coroutines.android)
    implementation(libs.datastore)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.timber)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.splashscreen)
    
    // Room
    implementation(libs.androidx.room.runtime.v284)
    implementation(libs.androidx.room.ktx.v284)
    ksp(libs.androidx.room.compiler.v284)

    // Health & ML
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.mlkit.barcode.scanning)
    implementation(libs.androidx.health.connect.client)

    // Security & UI
    implementation(libs.androidx.security.crypto)
    implementation(libs.sqlite.ktx)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}

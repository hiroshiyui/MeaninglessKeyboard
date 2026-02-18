plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.miyabi_hiroshi.app.meaninglesskeyboard"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.miyabi_hiroshi.app.meaninglesskeyboard"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "0.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

base {
    archivesName = "${android.defaultConfig.applicationId}-${android.defaultConfig.versionName}"
}

tasks.register("bumpPatchVersion") {
    description = "Increment versionCode and versionName patch level"
    doLast {
        val buildFile = file("build.gradle.kts")
        var text = buildFile.readText()

        val codeRegex = Regex("""versionCode\s*=\s*(\d+)""")
        val nameRegex = Regex("""versionName\s*=\s*"(\d+)\.(\d+)\.(\d+)"""")

        val codeMatch = codeRegex.find(text) ?: error("versionCode not found")
        val nameMatch = nameRegex.find(text) ?: error("versionName not found")

        val newCode = codeMatch.groupValues[1].toInt() + 1
        val major = nameMatch.groupValues[1]
        val minor = nameMatch.groupValues[2]
        val newPatch = nameMatch.groupValues[3].toInt() + 1
        val newName = "$major.$minor.$newPatch"

        text = text.replace(codeMatch.value, "versionCode = $newCode")
        text = text.replace(nameMatch.value, """versionName = "$newName"""")
        buildFile.writeText(text)

        println("Version bumped: versionCode=$newCode, versionName=$newName")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
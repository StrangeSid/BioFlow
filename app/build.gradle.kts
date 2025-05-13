import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.konan.properties.Properties

configurations.all {
    resolutionStrategy {
        // Handle javax.activation / jakarta.activation conflict
        eachDependency {
            if (requested.group == "com.sun.activation" && requested.name == "javax.activation") {
                // Explicitly use the newer Jakarta version when the older javax version is requested
                useTarget("jakarta.activation:jakarta.activation-api:1.2.1")
                because("Duplicate javax.activation classes conflict with jakarta.activation-api")
            }
        }

        // Keep the exclude rule for the old com.intellij annotations (this seems to be working)
        exclude(group = "com.intellij", module = "annotations")

        // Remove the previous 'force("jakarta.activation:jakarta.activation-api:1.2.1")' line
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
}


android {
    namespace = "com.sid.bioflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sid.bioflow"
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
    packaging {
        resources {
            // Exclude the duplicate META-INF/AL2.0 file
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "xsd/catalog.xml"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            // You might also need to exclude other META-INF files sometimes,
            // like META-INF/LGPL2.1, META-INF/licenses/... etc. if more duplicate file errors appear.
        }
    }
}


dependencies {
    implementation("com.android.tools.build:gradle:8.9.2")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.appcompat:appcompat:1.6.1") {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.browser:browser:1.5.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.3.0") {
        exclude(group = "com.sun.activation", module = "javax.activation")
    }
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.health.connect:connect-client:1.1.0-rc01")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation("androidx.compose.foundation:foundation:1.6.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.8.0")

    implementation ("io.github.ehsannarmani:compose-charts:0.1.7")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.datastore:datastore-preferences:1.1.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-compose:1.8.6")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
}

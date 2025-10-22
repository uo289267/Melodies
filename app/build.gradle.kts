plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "tfg.uniovi.melodies"
    compileSdk = 35

    defaultConfig {
        applicationId = "tfg.uniovi.melodies"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            viewBinding = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            pickFirsts.addAll(
                listOf(
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE",
                    "META-INF/NOTICE.md",
                    "META-INF/NOTICE",
                    "META-INF/LICENSE-notice.md"
                )
            )
        }
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.core.ktx)

    // --- Firebase (usar BoM para evitar conflictos de versión) ---
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")

    // --- Fuerza la versión correcta de protobuf ---
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.25.1")

    // --- Librerías de terceros ---
    implementation(files("libs/TarsosDSP-Android-latest-bin.jar"))
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("com.caverock:androidsvg:1.4")
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib){
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestUtil("androidx.test:orchestrator:1.6.1")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.5")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    testImplementation(kotlin("test"))
}


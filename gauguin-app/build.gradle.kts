
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.takahirom.roborazzi")
}

val keystoreProperties = Properties()
val keystoreExists = rootProject.file("keystore.properties").exists()

if (keystoreExists) {
    // Create a variable called keystorePropertiesFile, and initialize it to your
    // keystore.properties file, in the rootProject folder.
    val keystorePropertiesFile = rootProject.file("keystore.properties")

    // Load your keystore.properties file into the keystoreProperties object.
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    compileSdkVersion = "android-35"
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "org.piepmeyer.gauguin"
        minSdk = 24
        targetSdk = 34
    }

    if (keystoreExists) {
        signingConfigs {
            register("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    applicationVariants.all {
        this.resValue("string", "versionName", this.versionName)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true

        unitTests.all {
            it.useJUnitPlatform {
                if (!project.hasProperty("screenshot")) {
                    excludeTags("org.piepmeyer.gauguin.ScreenshotTest")
                }
            }

            // Do not run out of memory when running Roborazzi tests for different api levels
            it.jvmArgs = listOf("-Xmx2g")

            // Enable running tests in parallel
            if (project.hasProperty("parallel")) {
                it.maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
            }

            // Enable hardware rendering to display shadows and elevation. Still experimental
            // Supported only on API 31+
            it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
        }
    }

    buildTypes {
        release {
            if (keystoreExists) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.txt",
            )
            resValue("bool", "debuggable", "false")
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("bool", "debuggable", "true")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    lint {
        disable += listOf("ExpiredTargetSdkVersion", "MissingTranslation")
    }
    namespace = "org.piepmeyer.gauguin"

    androidResources {
        generateLocaleConfig = true
    }
}

repositories {
    google()
    mavenLocal()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

kotlin {
    jvmToolchain(11)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

roborazzi {
    outputDir.set(File("src/test/resources/screenshots"))
}

dependencies {
    implementation(project(":gauguin-core"))

    implementation(libs.logging.logback.android)

    implementation(libs.koin.android)

    implementation(libs.android.material)

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.transition)
    implementation(libs.androidx.window)
    implementation(libs.androidx.window.core)

    implementation(libs.thirdparty.konfetti)
    implementation(libs.thirdparty.ferriswheel)
    implementation(libs.thirdparty.navigationdrawer)
    implementation(libs.thirdparty.balloon)
    implementation(libs.thirdparty.vico)
    implementation(libs.thirdparty.androidplot)

    implementation(libs.bundles.koin)

    // debugImplementation(libs.thirdparty.leakcanary)

    implementation(libs.bundles.zxing)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.koin.test)
    testImplementation(libs.test.mockk)
    testImplementation(libs.bundles.screenshotTests)
    testImplementation(libs.bundles.androidx.test)

    testImplementation(testFixtures(project(":gauguin-core")))

    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(testFixtures(project(":gauguin-core")))
}

sonarqube {
    properties {
        property("sonar.androidLint.reportPaths", "$projectDir/build/reports/lint-results-debug.xml")
    }
}

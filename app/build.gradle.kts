import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.doro.music"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.doro.music"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val localProps = Properties()
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) {
                localProps.load(localPropsFile.inputStream())
            }
            storeFile = file(System.getenv("DORO_KEYSTORE_PATH") ?: localProps.getProperty("keystore.path") ?: "doro.jks")
            storePassword = System.getenv("DORO_KEYSTORE_PASSWORD") ?: localProps.getProperty("keystore.password")
            keyAlias = System.getenv("DORO_KEY_ALIAS") ?: localProps.getProperty("keystore.alias") ?: "doro"
            keyPassword = System.getenv("DORO_KEY_PASSWORD") ?: localProps.getProperty("keystore.alias.password")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
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

    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xexplicit-backing-fields")
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.paging)
    implementation(libs.coil.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui.compose.material3)
    implementation(libs.datastore.preferences)
    implementation(libs.constraintlayout.compose)
    implementation(libs.androidx.splashscreen)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "androidx/**/*.*",
        "**/*\$Lambda*.*",
        "**/*Companion*.*",
        "**/*\$Serializer*.*",
        "**/di/**",
        "**/Hilt_**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        // UI layer (requires instrumented tests)
        "**/ui/**",
        // Android framework classes
        "**/player/service/**",
        "**/player/controller/**",
        "**/player/PlayerSession*",
        // Room DAOs (requires instrumented tests)
        "**/data/db/dao/**",
        "**/data/db/AppDataBase*",
        // DataStore (requires Android context)
        "**/data/datastore/**",
        // MusicScanner (requires Android ContentResolver)
        "**/player/util/MusicScanner*",
        // Android Context extensions (requires Android framework)
        "**/ext/ContextExtKt*",
        // Compose Modifier extensions (requires Compose runtime)
        "**/ext/ModifierExtKt*",
        // Navigation extensions (requires Android framework)
        "**/ext/NavExtKt*",
        // Application class (requires Android framework)
        "**/App*",
        // QueueSong toMediaItem (requires Media3)
        "**/player/model/QueueSongKt*",
        // Paging map transform lambdas (generated by Paging library)
        "**/*\$\$inlined\$map*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("jacoco/testDebugUnitTest.exec")
    })
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "androidx/**/*.*",
        "**/*\$Lambda*.*",
        "**/*Companion*.*",
        "**/*\$Serializer*.*",
        "**/di/**",
        "**/Hilt_**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        // UI layer (requires instrumented tests)
        "**/ui/**",
        // Android framework classes
        "**/player/service/**",
        "**/player/controller/**",
        "**/player/PlayerSession*",
        // Room DAOs (requires instrumented tests)
        "**/data/db/dao/**",
        "**/data/db/AppDataBase*",
        // DataStore (requires Android context)
        "**/data/datastore/**",
        // MusicScanner (requires Android ContentResolver)
        "**/player/util/MusicScanner*",
        // Android Context extensions (requires Android framework)
        "**/ext/ContextExtKt*",
        // Compose Modifier extensions (requires Compose runtime)
        "**/ext/ModifierExtKt*",
        // Navigation extensions (requires Android framework)
        "**/ext/NavExtKt*",
        // Application class (requires Android framework)
        "**/App*",
        // QueueSong toMediaItem (requires Media3)
        "**/player/model/QueueSongKt*",
        // Paging map transform lambdas (generated by Paging library)
        "**/*\$\$inlined\$map*.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("jacoco/testDebugUnitTest.exec")
    })

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

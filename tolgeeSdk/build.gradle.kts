import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    `maven-publish`
}

android {
    namespace = "com.github.huglx.tolgeesdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    buildTypes {
        release {
            // Optimize resources
            isShrinkResources = false  // Set to true if this is an application module
        }
        
        debug {
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
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose UI - Use api for necessary UI components
    api(libs.androidx.material3)
    api(libs.androidx.ui.tooling.preview)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui.text)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    /*
    DataStore Preferences - Implementation detail, not exposed to consumers
     */
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    
    /*
    Lifecycle - Implementation detail, not exposed to consumers
    */
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    
    /*
    Koin dependency injection - Implementation detail, not exposed to consumers
     */
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    /*
        Retrofit - Implementation detail, not exposed to consumers
    */
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor) {
        // Only enable logging in debug builds
        exclude(group = "org.json", module = "json")
    }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    /*
        Room Database - Implementation detail, not exposed to consumers
    */
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Development tools - only included in debug builds
    implementation("androidx.compose.ui:ui-tooling:1.7.8")
    implementation("com.squareup.radiography:radiography:2.7")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:runner:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")

    // Android Testing
    androidTestImplementation("io.insert-koin:koin-test:3.4.0")
    androidTestImplementation("io.insert-koin:koin-test-junit4:3.4.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("io.mockk:mockk-android:1.13.5")
}

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.huglx"
            artifactId = "tolgeesdk"
            version = "0.0.2"

            afterEvaluate {
                from(components["release"])
            }

            // Add POM information
            pom {
                name.set("TolgeeSdk")
                description.set("Translation SDK for Android applications")
                url.set("https://github.com/huglx/tolgee-sdk-android-compose")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("huglx")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/huglx/tolgee-sdk-android-compose")
            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = localProperties.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
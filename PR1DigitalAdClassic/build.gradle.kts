import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//plugins {
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.jetbrains.kotlin.android)
//}

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.purered.pr1digitaladclassic"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

publishing{
    publications{
        create<MavenPublication>("release"){

            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
            }

            afterEvaluate {
                from(components.findByName("release"))
                groupId = "com.purered.pr1digitaladclassic"
                artifactId = "pr1digitaladclassic"
                version = "0.0.12"

                //com.purered.pr1digitaladclassic:pr1digitaladclassic:0.0.4
            }
        }
    }
}

dependencies {

    implementation (libs.androidx.core.ktx)
    implementation (platform(libs.androidx.compose.bom))
    implementation (libs.androidx.compose.ui)
    implementation (libs.androidx.compose.ui.tooling.preview)
    debugImplementation (libs.androidx.compose.ui.tooling)
    implementation (libs.androidx.activity.compose)
    implementation (libs.androidx.appcompat)
    implementation (libs.material)
    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit)
    androidTestImplementation (libs.androidx.espresso.core)
    implementation (libs.coil.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.hilt.android)
    implementation(libs.kotlinx.serialization.json)
}
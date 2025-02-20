//plugins {
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.jetbrains.kotlin.android)
//}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.purered.pr1digitaladclassic"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
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
                version = "0.0.7"

                //com.purered.pr1digitaladclassic:pr1digitaladclassic:0.0.4
            }
        }
    }
}

dependencies {

    implementation ("androidx.core:core-ktx:1.8.21")
    implementation (platform("androidx.compose:compose-bom:2023.05.01"))
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    implementation ("androidx.activity:activity-compose:1.5.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.12.0")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.dagger:hilt-android:2.45")
}
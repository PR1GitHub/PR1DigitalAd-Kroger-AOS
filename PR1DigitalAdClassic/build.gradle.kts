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

// Single source of truth for the published version. JitPack invokes the build with
// -Pversion=<git tag>; the fallback is only used for local builds. Both the Maven
// publication and BuildConfig.LIB_VERSION read from here so they cannot drift apart.
val libVersion: String = (project.findProperty("version") as? String)
    ?.takeIf { it.isNotBlank() && it != "unspecified" }
    ?: "0.0.18"

android {
    namespace = "com.purered.pr1digitaladclassic"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "LIB_VERSION", "\"$libVersion\"")

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
        buildConfig = true
    }

    // Registers the AGP "release" software component. Without this, the
    // components.findByName("release") below resolves to null and the Maven publication
    // ships a bare POM with no .aar attached.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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
                version = libVersion

                //com.purered.pr1digitaladclassic:pr1digitaladclassic:0.0.4
            }
        }
    }
}

dependencies {

    implementation (platform(libs.androidx.compose.bom))
    implementation (libs.androidx.compose.ui)
    implementation (libs.androidx.compose.ui.tooling.preview)
    debugImplementation (libs.androidx.compose.ui.tooling)
    implementation (libs.coil.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // Used directly by ApiKeyInterceptor, so declared rather than relied on transitively.
    implementation(libs.okhttp)

    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit)
    androidTestImplementation (libs.androidx.espresso.core)
}
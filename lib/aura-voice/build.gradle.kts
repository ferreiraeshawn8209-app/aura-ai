plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aura.voice"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    lint {
        abortOnError = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm()

    androidTarget(MyTargetConfigurations.Android.config())

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.serialization.json)

            // Logging
            api(libs.logging.kermit)
        }
    }
}

android {
    MyTargetConfigurations.Android.sharedConfig()()
}

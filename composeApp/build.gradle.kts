plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop")

    androidTarget(MyTargetConfigurations.Android.config())

//    js("web") {
//        moduleName = "composeApp"
//        browser {}
//        binaries.executable()
//    }

    sourceSets {
        val desktopMain by getting
//        val webMain by getting

        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // ktor client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)

            implementation(projects.shared)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // ktor engine
            implementation(libs.ktor.client.engine.cio)

            // logging
            implementation(libs.logback)

            // gamepad input
            implementation(libs.libsdl4j)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // ktor engine
            implementation(libs.ktor.client.engine.cio)

            // logging
            //implementation(libs.slf4j.android)

            // gamepad input can be read natively on Android, no dependency required
        }
//        webMain.dependencies {
        // ktor engine
//            implementation(libs.ktor.client.engine.js)
//        }
    }
}

compose.desktop {
    MyTargetConfigurations.Desktop.composeConfig()()
}

android {
    MyTargetConfigurations.Android.composeConfig()()

    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

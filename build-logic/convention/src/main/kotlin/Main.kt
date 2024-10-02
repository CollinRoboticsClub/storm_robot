import com.android.build.api.dsl.CompileOptions
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.precompile.PrecompiledScriptDependenciesResolver.EnvironmentProperties.projectRoot
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

class MyTargetConfigurations : Plugin<Project> {
    override fun apply(target: Project) {}

    object Android {
        private const val MIN_SDK = 28
        private const val TARGET_SDK = 34
        private const val COMPILE_SDK = 34

        private val JVM_VERSION = JvmTarget.JVM_17
        private val JDK_VERSION = JavaVersion.VERSION_17

        fun config(): (KotlinAndroidTarget).() -> Unit = {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                jvmTarget.set(JVM_VERSION)
            }
        }

        private val commonCompileOptions: (CompileOptions).() -> Unit = {
            sourceCompatibility = JDK_VERSION
            targetCompatibility = JDK_VERSION
        }

        fun sharedConfig(): (LibraryExtension).() -> Unit = {
            namespace = "$projectRoot.shared"
            compileSdk = COMPILE_SDK
            compileOptions {
                commonCompileOptions()
            }
            defaultConfig {
                minSdk = MIN_SDK
            }
        }

        fun composeConfig(): (BaseAppModuleExtension).() -> Unit = {
            namespace = MyConstants.ROOT_PACKAGE
            compileSdk = COMPILE_SDK

//            with(sourceSets["main"]) {
//                manifest.srcFile("src/androidMain/AndroidManifest.xml")
//                res.srcDirs("src/androidMain/res")
//                resources.srcDirs("src/commonMain/resources")
//            }

            defaultConfig {
                applicationId = MyConstants.ROOT_PACKAGE
                minSdk = MIN_SDK
                targetSdk = TARGET_SDK
                versionCode = 1
                versionName = "1.0"
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST}"
                }
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            compileOptions {
                commonCompileOptions()
            }
            buildFeatures {
                compose = true
            }
        }
    }

    object Desktop {
        fun composeConfig(): (DesktopExtension).() -> Unit = {
            application {
                mainClass = "${MyConstants.ROOT_PACKAGE}.MainKt"

                nativeDistributions {
                    targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.AppImage)
                    packageName = MyConstants.ROOT_PACKAGE
                    packageVersion = "1.0.0"
                }
            }
        }
    }

    object Server {
        const val VERSION = "1.0.0"

        fun Project.serverConfig(): (JavaApplication).() -> Unit = {
            mainClass.set("${MyConstants.ROOT_PACKAGE}.ApplicationKt")
            applicationDefaultJvmArgs =
                listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
        }
    }
}

object MyConstants {
    private const val ROOT_DOMAIN = "me.arianb"
    private const val ROOT_PROJECT_NAME = "storm_robot"
    
    const val ROOT_PACKAGE = "$ROOT_DOMAIN.$ROOT_PROJECT_NAME"
}

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("myBuildLogic") {
            id = "me.arianb.my.build.logic"
            implementationClass = "MyTargetConfigurations"
        }
    }
}

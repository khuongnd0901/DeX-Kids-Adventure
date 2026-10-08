
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val natives by configurations.creating
val nativeOutputDir = layout.buildDirectory.dir("generated/nativeJniLibs")
val copyAndroidNatives by tasks.registering {
    inputs.files(natives)
    outputs.dir(nativeOutputDir)
    doLast {
        natives.files.forEach { archive ->
            val abi = when {
                archive.name.contains("arm64-v8a") -> "arm64-v8a"
                archive.name.contains("armeabi-v7a") -> "armeabi-v7a"
                archive.name.contains("x86_64") -> "x86_64"
                else -> error("Unknown libGDX native ABI: " + archive.name)
            }
            copy {
                from(zipTree(archive))
                include("*.so")
                into(nativeOutputDir.get().dir(abi))
            }
        }
    }
}
android {
    namespace = "com.khuongnd.dexkids"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.khuongnd.dexkids"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    sourceSets.getByName("main").apply {
        assets.srcDir("../assets")
        jniLibs.srcDir(nativeOutputDir)
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}
tasks.matching { it.name.startsWith("merge") && it.name.endsWith("JniLibFolders") }
    .configureEach { dependsOn(copyAndroidNatives) }
dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.14.2")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-x86_64")
}

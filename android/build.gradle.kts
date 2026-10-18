plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
// AndroidX Core 1.17+ requires compileSdk 36; project contract pins 35.
configurations.configureEach {
    resolutionStrategy.force("androidx.core:core:1.16.0", "androidx.core:core-ktx:1.16.0")
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
val memoryOutputDir = layout.buildDirectory.dir("generated/memoryJniLibs")
val compileMemoryTrim by tasks.registering(Exec::class) {
    inputs.files("src/main/cpp/native_heap_trim.c", "../tools/build_native_heap_trim.py")
    outputs.dir(memoryOutputDir)
    workingDir(rootProject.projectDir)
    commandLine("python3", "tools/build_native_heap_trim.py", "--out", memoryOutputDir.get().asFile.absolutePath)
}
// Runtime assets exclude source galleries and the unused all-art M10 atlas.
// Source atlas remains available to local art validation and contact sheets.
val runtimeAssets = tasks.register<Sync>("prepareRuntimeAssets") {
    dependsOn(rootProject.tasks.named("generateKidsArt"))
    from("../assets")
    exclude("generated/sprites/**", "generated/m10-gallery/**", "generated/m10/m10.atlas",
        "generated/m10/m10-*.png", "generated/m10/manifest.json")
    into(layout.buildDirectory.dir("generated/runtimeAssets"))
}
val iconOutputDir = layout.buildDirectory.dir("generated/launcherIconRes")
val launcherIcons = tasks.register<Exec>("generateLauncherIcons") {
    inputs.files("../art/assets-source/app-icon-v1/capybara-icon.png", "../tools/build_launcher_icons.py")
    outputs.dir(iconOutputDir)
    workingDir(rootProject.projectDir)
    commandLine("python3", "tools/build_launcher_icons.py", "--out", iconOutputDir.get().asFile.absolutePath)
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
        testInstrumentationRunner = "com.khuongnd.dexkids.qa.LifecycleValidationRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    sourceSets.getByName("main").apply {
        assets.srcDir(runtimeAssets)
        res.srcDir(iconOutputDir)
        jniLibs.srcDir(nativeOutputDir)
        jniLibs.srcDir(memoryOutputDir)
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}
tasks.named("preBuild") { dependsOn(launcherIcons) }
tasks.matching { it.name.startsWith("merge") && it.name.endsWith("JniLibFolders") }
    .configureEach { dependsOn(copyAndroidNatives, compileMemoryTrim) }
dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.14.2")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-x86_64")
}

// Android asset-merge must see atlas before packaging the APK.
tasks.matching { it.name.matches(Regex("merge.*Assets")) }.configureEach {
    dependsOn(runtimeAssets)
}

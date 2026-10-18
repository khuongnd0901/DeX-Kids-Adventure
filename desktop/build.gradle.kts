plugins {
    java
    application
}
java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }
dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.14.2")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-desktop")
}
application { mainClass.set("com.khuongnd.dexkids.desktop.DesktopLauncher") }
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    dependsOn(rootProject.tasks.named("generateKidsArt"))
}

// Actual GL capture through the production world renderer; writes only QA/gallery artifacts.
tasks.register<JavaExec>("storyWorldGallery") {
    dependsOn(tasks.classes, rootProject.tasks.named("generateKidsArt"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.game.StoryWorldGallery")
    workingDir = rootProject.projectDir
    args(providers.gradleProperty("storyWorldOutput").getOrElse("build/story-world-gallery"))
}

tasks.register<JavaExec>("storyWorldGameSmoke") {
    dependsOn(tasks.classes, rootProject.tasks.named("generateKidsArt"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.game.StoryWorldGameSmoke")
    workingDir = rootProject.projectDir
}

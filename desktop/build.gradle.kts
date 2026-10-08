
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
    workingDir = rootProject.file("assets")
}

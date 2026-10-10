plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
}
allprojects {
    group = "com.khuongnd.dexkids"
    version = "0.1.0-SNAPSHOT"
}


// Reproducible source SVG -> transparent PNG -> packed LibGDX atlas.
val generateKidsArt by tasks.registering(Exec::class) {
    group = "assets"
    description = "Rasterize original SVG artwork into a deterministic texture atlas"
    workingDir = rootProject.projectDir
    inputs.dir(layout.projectDirectory.dir("art/assets-source"))
    inputs.file(layout.projectDirectory.file("tools/build_sprites.py"))
    inputs.file(layout.projectDirectory.file("tools/build_backdrops.py"))
    outputs.dir(layout.projectDirectory.dir("assets/generated"))
    outputs.file(layout.projectDirectory.file("assets/characters/sau-costumes.png"))
    commandLine("python3", "tools/build_sprites.py")
}

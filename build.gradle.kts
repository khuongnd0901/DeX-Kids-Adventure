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
    inputs.files("assets/art-style.txt", "assets/art-mode.txt", "tools/storybook_art.py")
    inputs.file(layout.projectDirectory.file("tools/build_sprites.py"))
    inputs.file(layout.projectDirectory.file("tools/build_runtime_children.py"))
    inputs.file(layout.projectDirectory.file("tools/build_child_outfits.py"))
    inputs.file(layout.projectDirectory.file("tools/build_ong_complete_outfits.py"))
    inputs.file(layout.projectDirectory.file("tools/child_outfit_geometry.py"))
    inputs.file(layout.projectDirectory.file("tools/build_backdrops.py"))
    inputs.file(layout.projectDirectory.file("tools/build_extras.py"))
    inputs.file(layout.projectDirectory.file("tools/build_story_support.py"))
    inputs.file(layout.projectDirectory.file("tools/build_story_world.py"))
    inputs.file(layout.projectDirectory.file("tools/build_whole_characters.py"))
    inputs.file(layout.projectDirectory.file("tools/build_t048_atlas.py"))
    outputs.dir(layout.projectDirectory.dir("assets/generated"))
    outputs.dir(layout.projectDirectory.dir("assets/characters"))
    commandLine("python3", "tools/build_sprites.py")
}

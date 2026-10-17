
plugins { `java-library` }
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
dependencies {
    api("com.badlogicgames.gdx:gdx:1.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test {
    useJUnitPlatform()
    // The public-pack playback acceptance test must rerun after content-only edits.
    inputs.file(rootProject.file("docs/story-source/stories.example.json"))
    inputs.file(rootProject.file("docs/story-source/stories.world.example.json"))
    inputs.file(rootProject.file("docs/learning-source/learning.example.tsv"))
    inputs.file(rootProject.file("docs/learning-source/learning.5-7.400.tsv"))
}

// Run with -PstoryPack=/absolute/path/to/stories.json; no network or source rewriting.
tasks.register<JavaExec>("checkStoryPack") {
    workingDir = rootProject.projectDir
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    args(providers.gradleProperty("storyPack").getOrElse(rootProject.file("docs/story-source/stories.example.json").absolutePath))
}
tasks.register<JavaExec>("writeStoryPackSample") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    // Keep hand-authored public stories intact when regenerating the minimal fixture.
    val sample = rootProject.layout.buildDirectory.file("story-samples/stories.example.json")
    doFirst { sample.get().asFile.parentFile.mkdirs() }
    args("--sample", sample.get().asFile.absolutePath)
}

tasks.register<JavaExec>("writeExpandedStoryPackSample") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    args("--expanded-sample", rootProject.file("docs/story-source/stories.expanded.example.json").absolutePath)
}

tasks.register<JavaExec>("writeWorldStoryPackSample") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    val sample = rootProject.layout.buildDirectory.file("story-samples/stories.world.example.json")
    doFirst { sample.get().asFile.parentFile.mkdirs() }
    args("--world-sample", sample.get().asFile.absolutePath)
}

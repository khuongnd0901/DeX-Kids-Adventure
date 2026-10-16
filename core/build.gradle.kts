
plugins { `java-library` }
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
dependencies {
    api("com.badlogicgames.gdx:gdx:1.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }

// Run with -PstoryPack=/absolute/path/to/stories.json; no network or source rewriting.
tasks.register<JavaExec>("checkStoryPack") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    args(providers.gradleProperty("storyPack").getOrElse(rootProject.file("docs/story-source/stories.example.json").absolutePath))
}
tasks.register<JavaExec>("writeStoryPackSample") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.khuongnd.dexkids.story.adventure.StoryPackTool")
    args("--sample", rootProject.file("docs/story-source/stories.example.json").absolutePath)
}

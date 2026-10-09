
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

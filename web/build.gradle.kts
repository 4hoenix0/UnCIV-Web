// The :web module. It is injected into the (patched) Unciv checkout by gradle/web-init.gradle.kts.
plugins {
    id("kotlin")
    id("com.github.xpenatan.gdx-teavm") version "1.6.1"
}

// core targets Java 8 bytecode; TeaVM is fine with that. The launcher itself needs 11+.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
}

dependencies {
    implementation(project(":core"))
    implementation(rootProject.libs.gdx)
}

// Unciv keeps its game data in android/assets (jsons, atlases, sounds, fonts, translations...)
val unciv = rootProject.file("android/assets")
val webAssets = layout.buildDirectory.dir("webAssets")
val stageAssets by tasks.registering(Sync::class) {
    from(unciv) {
        exclude("**/*.md", "**/.git*", "**/*.psd")
    }
    into(webAssets)
}

gdxTeaVM {
    assets.from(webAssets)

    webDefaults {
        mainClass = "com.unciv.web.WebLauncher"
        htmlTitle = "Unciv"
        // Unciv's translation + ruleset JSON is huge; give the TeaVM compiler room.
        processMemory = 4096
        // Unciv loads many model classes through libGDX Json (reflection). Anything not listed here
        // may be dropped by TeaVM's dead-code elimination - add classes here when JSON loading fails at runtime.
        preservedClasses.addAll(
            "com.unciv.models.ruleset.Ruleset",
            "com.unciv.models.metadata.GameSettings",
            "com.unciv.logic.GameInfo"
        )
    }
    js {}
}

tasks.matching { it.name.startsWith("gdx_teavm_") }.configureEach { dependsOn(stageAssets) }

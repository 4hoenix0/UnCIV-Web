// The :web module. It is injected into the (patched) Unciv checkout by gradle/web-init.gradle.kts.
plugins {
    id("kotlin")
    id("com.github.xpenatan.gdx-teavm") version "1.6.1"
}

// core targets Java 8 bytecode; TeaVM is fine with that. The launcher itself needs 17+.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

    // EXPERIMENT: no Unciv classes reflectable at all (libGDX's own defaults stay on). Json loading of rulesets/saves
    // will NOT work at runtime, but this shows whether the TeaVM analysis can finish without the reflection fan-out.

    js {
        mainClass.set("com.unciv.web.WebLauncher")
        htmlTitle.set("Unciv")
        outOfProcess.set(false)
        fastGlobalAnalysis.set(true)
    }
}

tasks.matching { it.name.startsWith("gdx_teavm_") }.configureEach { dependsOn(stageAssets) }

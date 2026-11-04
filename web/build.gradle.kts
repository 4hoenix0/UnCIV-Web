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

    // Only data-model packages: gdx-teavm makes EVERY member of every matching class reflectively reachable,
    // so "com.unciv.**" (whole game incl. UI) ran TeaVM's analysis out of memory even at 12 GB.
    reflection(
        "com.unciv.models.**",
        "com.unciv.json.**",
        "com.unciv.logic.civilization.**",
        "com.unciv.logic.city.**",
        "com.unciv.logic.map.**",
        "com.unciv.logic.trade.**",
        "com.unciv.logic.battle.**",
        "com.unciv.logic.automation.civilization.**",
        "com.unciv.logic.GameInfo**",
        "com.unciv.logic.Versioning**",
        "com.unciv.ui.components.MapArrowType**",
        "com.unciv.ui.components.fonts.FontFamilyData**"
    )
    // Prints which classes were selected, so the next log shows what the patterns matched
    reflectionDebug.set(true)

    js {
        mainClass.set("com.unciv.web.WebLauncher")
        htmlTitle.set("Unciv")
        // Unciv's code base is big. Run the compiler inside the Gradle JVM, whose heap scripts/build.sh sets to 12 GB
        // (a separate 9 GB compiler process also ran out of memory - this tells us which JVM was the problem).
        outOfProcess.set(false)
        processMemory.set(9216)
        // Less precise but much lighter whole-program analysis
        fastGlobalAnalysis.set(true)
    }
}

tasks.matching { it.name.startsWith("gdx_teavm_") }.configureEach { dependsOn(stageAssets) }

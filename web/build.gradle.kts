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

    // Unciv (de)serializes its whole data model through libGDX Json, which uses reflection.
    // gdx-teavm 1.6.1 generates reflection metadata for these patterns (`**` = whole package tree).
    reflection("com.unciv.**")

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

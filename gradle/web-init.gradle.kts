// Gradle init script: bolts our `:web` module onto an UNMODIFIED Unciv checkout.
// Used as: ./gradlew -I gradle/web-init.gradle.kts ...   (scripts/build.sh does this for you)
// Because nothing in the upstream tree is edited by this, upstream updates can't conflict with it.
//
// NOTE: init scripts also run for Unciv's nested `buildSrc` build, so everything here must be harmless there.

val webDir = System.getenv("UNCIV_WEB_MODULE_DIR") ?: error("UNCIV_WEB_MODULE_DIR not set")

beforeSettings {
    // gdx-teavm's Gradle plugin lives on Maven Central. Declaring any repository replaces Gradle's default
    // plugin portal, so list the portal explicitly too (buildSrc needs it for kotlin-dsl).
    pluginManagement.repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

settingsEvaluated {
    // Only the real Unciv build gets the :web module, not buildSrc
    if (settingsDir.name != "buildSrc") {
        include(":web")
        project(":web").projectDir = file(webDir)
    }
}

// Gradle init script: bolts our `:web` module onto an UNMODIFIED Unciv checkout.
// Used as: ./gradlew -I gradle/web-init.gradle.kts ...   (scripts/build.sh does this for you)
// Because nothing in the upstream tree is edited by this, upstream updates can't conflict with it.

val webDir = System.getenv("UNCIV_WEB_MODULE_DIR") ?: error("UNCIV_WEB_MODULE_DIR not set")

beforeSettings {
    // gdx-teavm's Gradle plugin lives on Maven Central (upstream only lists the plugin portal)
    pluginManagement.repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

settingsEvaluated {
    include(":web")
    project(":web").projectDir = file(webDir)
}

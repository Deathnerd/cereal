plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
    // Apply Detekt plugin from `gradle/libs.versions.toml` for Kotlin linting
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

// Configure Detekt for Kotlin code linting
detekt {
    config.setFrom(rootProject.files(".github/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
}

dependencies {
    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    implementation(libs.bundles.kotlinxEcosystem)

    // JUnit 5
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
}
plugins {
    // Apply the shared build logic from a convention plugin.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application

    // Apply kotlinx.serialization plugin for XML parsing
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
    // XML serialization support
    implementation(libs.kotlinxSerializationXml)

    // CLI argument parsing
    implementation(libs.clikt)

    // Kotlin poet for code generation
    implementation(libs.kotlinPoet)

    // JUnit 5
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

application {
    // Define the Fully Qualified Name for the application main class
    mainClass = "com.deathnerd.doctrineToKotlin.cli.MainKt"
}

tasks.named<dev.detekt.gradle.Detekt>("detekt").configure {
    reports {
        sarif.required.set(true)
    }
}

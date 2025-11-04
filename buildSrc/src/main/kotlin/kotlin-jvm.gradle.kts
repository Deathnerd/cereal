// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
    // Apply JaCoCo for code coverage reporting
    jacoco
}

kotlin {
    // Use a specific Java version to make it easier to work in different environments.
    jvmToolchain(24)
}

tasks.withType<Test>().configureEach {
    // Configure all test Gradle tasks to use JUnitPlatform.
    useJUnitPlatform()

    // Log information about all test results, not only the failed ones.
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}

// Add coverage report generation tasks
afterEvaluate {
    val testTask = tasks.findByName("test")
    if (testTask != null) {
        tasks.register<JacocoReport>("jacocoReport") {
            dependsOn(testTask)

            // Configure source and class directories for coverage
            sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
            classDirectories.setFrom(files("${layout.buildDirectory.get()}/classes/kotlin/main"))

            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }
        }
    }
}

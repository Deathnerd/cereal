plugins {
    // Apply Detekt plugin from `gradle/libs.versions.toml` for Kotlin linting
    alias(libs.plugins.detekt) apply false
}

val reportMerge by tasks.registering(dev.detekt.gradle.report.ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
    // Apply Detekt plugin to all subprojects
    apply(plugin = "io.gitlab.arturbosch.detekt")

    // Configure Detekt for Kotlin code linting
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files(".github/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    // Configure Detekt task to enable SARIF reports for report merging
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            sarif.required.set(true)
        }
    }

    reportMerge {
        val detektTasks = tasks.withType<dev.detekt.gradle.Detekt>()
        input.from(detektTasks.map { it.reports.sarif.outputLocation.get() })
        dependsOn(detektTasks)
    }
}
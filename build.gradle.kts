plugins {
    // Apply Detekt plugin from `gradle/libs.versions.toml` for Kotlin linting
    alias(libs.plugins.detekt) apply false
}

val reportMerge by tasks.registering(dev.detekt.gradle.report.ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
    reportMerge {
        val detektTasks = tasks.withType<dev.detekt.gradle.Detekt>()
        input.from(detektTasks.map { it.reports.sarif.outputLocation.get() })
        dependsOn(detektTasks)
    }
}
/**
 * Annotation to mark code that should be excluded from JaCoCo code coverage analysis.
 *
 * This annotation is used to exclude specific code elements from code coverage reports.
 * Common use cases include:
 * - Boilerplate code (e.g., auto-generated equals/hashCode methods)
 * - Synthetic or compiler-generated code
 * - Code that doesn't benefit from coverage measurement
 * - Trivial accessors or delegating methods
 *
 * The annotation can be applied to functions, properties, and classes, and is retained
 * at runtime for tooling and documentation purposes.
 *
 * Example usage:
 * ```
 * @ExcludeFromCoverage(reason = "Boilerplate equals implementation")
 * override fun equals(other: Any?): Boolean { ... }
 * ```
 *
 * @property reason A descriptive reason explaining why this code is excluded from coverage.
 *                  This helps maintainers understand the rationale for the exclusion.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeFromCoverage(
    /**
     * The reason for excluding this code from coverage analysis.
     *
     * Should provide a clear explanation of why this code element doesn't need coverage,
     * making it easier for code reviewers and future maintainers to understand the decision.
     *
     * Default: "Excluded from coverage"
     */
    val reason: String = "Excluded from coverage"
)
/**
 * Annotation to mark code that should be excluded from JaCoCo code coverage analysis.
 *
 * Used for boilerplate code (like auto-generated equals/hashCode), synthetic code,
 * or other code that doesn't benefit from coverage measurement.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcludeFromCoverage(
    val reason: String = "Excluded from coverage"
)
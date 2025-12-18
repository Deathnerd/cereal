package com.deathnerd.doctrineToKotlin.cli

import com.deathnerd.doctrineToKotlin.converter.ConverterRegistry
import com.deathnerd.doctrineToKotlin.xml.XmlParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import kotlin.system.exitProcess

/**
 * Main CLI application for converting Doctrine XML mappings to Kotlin entities
 */
class DoctrineToKotlinCommand : CliktCommand(
    name = "doctrineToKotlin",
    help = """
        Convert Doctrine ORM XML entity mappings to Kotlin entity classes.

        Supports multiple target formats:
        - hibernate: Generates JPA/Hibernate annotated entities
        - exposed: Generates Exposed DSL table definitions

        The tool validates XML files against the Doctrine ORM schema by default.
    """.trimIndent()
) {
    private val inputFiles by argument(
        name = "INPUT_FILES",
        help = "One or more Doctrine XML mapping files to convert"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .multiple(required = true)

    private val format by option(
        "-f", "--format",
        help = "Target format for generated entities (${ConverterRegistry.availableNames().joinToString(", ")})"
    ).choice(
        choices = ConverterRegistry.availableNames().associateWith { it },
        ignoreCase = true
    ).required()

    private val outputDir by option(
        "-o", "--output",
        help = "Output directory for generated Kotlin files"
    ).file(canBeFile = false)
        .required()

    private val packageName by option(
        "-p", "--package",
        help = "Package name for generated classes"
    ).required()

    private val skipValidation by option(
        "--skip-validation",
        help = "Skip XML schema validation (not recommended)"
    ).flag(default = false)

    private val verbose by option(
        "-v", "--verbose",
        help = "Enable verbose output"
    ).flag(default = false)

    override fun help(context: Context): String = """
        Doctrine to Kotlin Entity Converter

        This tool converts Doctrine ORM XML entity mapping files to Kotlin entity classes
        for various ORM frameworks. The generated code is ready to use in your Kotlin projects.

        Examples:
          # Convert to Hibernate entities
          doctrineToKotlin -f hibernate -o src/main/kotlin -p com.example.entities User.orm.xml Product.orm.xml

          # Convert to Exposed tables
          doctrineToKotlin --format exposed --output src/main/kotlin --package com.example.tables *.orm.xml

          # Skip validation (faster but not recommended)
          doctrineToKotlin -f hibernate -o output -p com.example --skip-validation mapping.xml
    """.trimIndent()

    override fun run() {
        try {
            validateInputs()

            if (verbose) {
                echo("Doctrine to Kotlin Converter")
                echo("============================")
                echo("Input files: ${inputFiles.size}")
                inputFiles.forEach { echo("  - ${it.name}") }
                echo("Format: $format")
                echo("Output directory: ${outputDir.absolutePath}")
                echo("Package: $packageName")
                echo("Validation: ${if (skipValidation) "disabled" else "enabled"}")
                echo()
            }

            // Parse XML files
            if (verbose) echo("Parsing XML files...")
            val parser = XmlParser()
            val mappings = try {
                parser.parseAll(inputFiles, validateSchema = !skipValidation)
            } catch (e: IllegalArgumentException) {
                throw UsageError("Failed to parse XML files: ${e.message}", statusCode = 1)
            }

            if (verbose) echo("Parsed ${mappings.size} mapping file(s)")

            // Get converter
            val converter = try {
                ConverterRegistry.get(format)
            } catch (e: IllegalArgumentException) {
                throw UsageError(e.message ?: "Unknown converter", statusCode = 1)
            }

            // Create output directory if it doesn't exist
            if (!outputDir.exists()) {
                if (verbose) echo("Creating output directory: ${outputDir.absolutePath}")
                outputDir.mkdirs()
            }

            // Convert all entities
            var totalEntities = 0
            mappings.forEach { mapping ->
                if (verbose) echo("Converting ${mapping.entities.size} entities from mapping...")
                totalEntities += converter.convertAndWrite(mapping, packageName, outputDir)
            }

            // Success message
            echo("✓ Successfully converted $totalEntities entities to $format format")
            echo("  Output directory: ${outputDir.absolutePath}")

        } catch (e: UsageError) {
            throw e
        } catch (e: Exception) {
            echo("Error: ${e.message}", err = true)
            if (verbose) {
                e.printStackTrace()
            }
            exitProcess(1)
        }
    }

    private fun validateInputs() {
        // Validate input files
        val invalidFiles = inputFiles.filter { !it.name.endsWith(".xml") }
        if (invalidFiles.isNotEmpty()) {
            throw UsageError(
                "The following files do not appear to be XML files: ${invalidFiles.joinToString { it.name }}",
                statusCode = 1
            )
        }

        // Validate package name
        if (!packageName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$"))) {
            throw UsageError(
                "Invalid package name: $packageName. Package names must be lowercase and follow Java naming conventions.",
                statusCode = 1
            )
        }
    }
}

/**
 * Application entry point
 */
fun main(args: Array<String>) {
    DoctrineToKotlinCommand().main(args)
}

package com.deathnerd.doctrineToKotlin.converter

import com.deathnerd.doctrineToKotlin.model.DoctrineMapping
import com.deathnerd.doctrineToKotlin.model.Entity
import com.squareup.kotlinpoet.FileSpec
import java.io.File

/**
 * Base interface for all entity converters.
 * Implement this interface to add support for new target frameworks.
 */
interface EntityConverter {
    /**
     * The name of this converter (e.g., "Hibernate", "Exposed")
     */
    val name: String

    /**
     * Converts a single Doctrine entity to the target framework format
     * @param entity The Doctrine entity to convert
     * @param packageName The package name for the generated class
     * @return A FileSpec representing the generated Kotlin class
     */
    fun convertEntity(entity: Entity, packageName: String): FileSpec

    /**
     * Converts all entities in a DoctrineMapping to the target framework format
     * @param mapping The DoctrineMapping containing entities
     * @param packageName The package name for the generated classes
     * @return List of FileSpecs representing the generated Kotlin classes
     */
    fun convertAll(mapping: DoctrineMapping, packageName: String): List<FileSpec> {
        return mapping.entities.map { convertEntity(it, packageName) }
    }

    /**
     * Writes all generated files to the output directory
     * @param fileSpecs List of FileSpecs to write
     * @param outputDir The output directory
     */
    fun writeFiles(fileSpecs: List<FileSpec>, outputDir: File) {
        fileSpecs.forEach { fileSpec ->
            fileSpec.writeTo(outputDir)
        }
    }

    /**
     * Converts and writes all entities from a DoctrineMapping
     * @param mapping The DoctrineMapping containing entities
     * @param packageName The package name for the generated classes
     * @param outputDir The output directory
     * @return Number of files written
     */
    fun convertAndWrite(mapping: DoctrineMapping, packageName: String, outputDir: File): Int {
        val fileSpecs = convertAll(mapping, packageName)
        writeFiles(fileSpecs, outputDir)
        return fileSpecs.size
    }
}

/**
 * Registry for managing available converters
 */
object ConverterRegistry {
    private val converters = mutableMapOf<String, EntityConverter>()

    init {
        // Register built-in converters
        register(HibernateConverter())
        register(ExposedConverter())
    }

    /**
     * Registers a converter
     */
    fun register(converter: EntityConverter) {
        converters[converter.name.lowercase()] = converter
    }

    /**
     * Gets a converter by name
     * @throws IllegalArgumentException if converter not found
     */
    fun get(name: String): EntityConverter {
        return converters[name.lowercase()]
            ?: throw IllegalArgumentException("Unknown converter: $name. Available converters: ${availableNames()}")
    }

    /**
     * Returns list of available converter names
     */
    fun availableNames(): List<String> = converters.keys.sorted()

    /**
     * Checks if a converter is available
     */
    fun has(name: String): Boolean = converters.containsKey(name.lowercase())
}

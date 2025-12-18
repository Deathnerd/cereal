package com.deathnerd.doctrineToKotlin.converter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

/**
 * Maps Doctrine field types to Kotlin types
 */
object TypeMapper {
    /**
     * Maps a Doctrine type to a Kotlin type, handling nullability
     */
    fun mapToKotlinType(doctrineType: String, nullable: Boolean): TypeName {
        val baseType = when (doctrineType.lowercase()) {
            "string", "text" -> String::class.asClassName()
            "integer", "smallint" -> Int::class.asClassName()
            "bigint" -> Long::class.asClassName()
            "boolean" -> Boolean::class.asClassName()
            "decimal", "float" -> Double::class.asClassName()
            "datetime", "datetimetz", "date", "time" -> ClassName("java.time", "LocalDateTime")
            "json", "json_array" -> ClassName("kotlin.collections", "Map")
                .parameterizedBy(String::class.asClassName(), ClassName("kotlin", "Any").copy(nullable = true))
            "array", "simple_array" -> ClassName("kotlin.collections", "List")
                .parameterizedBy(String::class.asClassName())
            "blob", "binary" -> ClassName("kotlin", "ByteArray")
            "guid", "uuid" -> ClassName("java.util", "UUID")
            else -> String::class.asClassName() // Default to String for unknown types
        }

        return baseType.copy(nullable = nullable)
    }

    /**
     * Maps a Doctrine type to a SQL/JPA column type
     */
    fun mapToJpaColumnType(doctrineType: String): String? {
        return when (doctrineType.lowercase()) {
            "string" -> "VARCHAR"
            "text" -> "TEXT"
            "integer", "smallint" -> "INTEGER"
            "bigint" -> "BIGINT"
            "boolean" -> "BOOLEAN"
            "decimal" -> "DECIMAL"
            "float" -> "DOUBLE"
            "datetime", "datetimetz" -> "TIMESTAMP"
            "date" -> "DATE"
            "time" -> "TIME"
            "json", "json_array" -> "JSON"
            "blob", "binary" -> "BLOB"
            "guid", "uuid" -> "UUID"
            else -> null
        }
    }

    /**
     * Converts a Doctrine class name to a Kotlin class name
     */
    fun toKotlinClassName(doctrineClassName: String): String {
        // Handle namespace separators (backslash in PHP)
        return doctrineClassName.split("\\").last()
    }

    /**
     * Converts a field name to property name (camelCase)
     */
    fun toPropertyName(fieldName: String): String {
        return fieldName
    }

    /**
     * Converts a table name to class name (PascalCase)
     */
    fun toClassName(tableName: String): String {
        return tableName.split("_")
            .joinToString("") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    }
}

package com.deathnerd.doctrineToKotlin.converter

import com.deathnerd.doctrineToKotlin.model.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Converts Doctrine entities to Exposed table definitions
 */
class ExposedConverter : EntityConverter {
    override val name: String = "Exposed"

    override fun convertEntity(entity: Entity, packageName: String): FileSpec {
        val className = TypeMapper.toKotlinClassName(entity.name)
        val tableName = entity.table ?: className.lowercase()

        // Create the Table object
        val tableBuilder = TypeSpec.objectBuilder("${className}Table")
            .superclass(ClassName("org.jetbrains.exposed.sql", "Table"))
            .addSuperclassConstructorParameter("%S", tableName)

        // Add ID columns
        entity.id.forEach { id ->
            addIdColumn(tableBuilder, id)
        }

        // Add regular field columns
        entity.field.forEach { field ->
            addFieldColumn(tableBuilder, field)
        }

        // Add reference columns for associations
        entity.manyToOne.forEach { association ->
            addManyToOneColumn(tableBuilder, association)
        }

        // Add primary key if there are ID fields
        if (entity.id.isNotEmpty()) {
            val primaryKeyColumns = entity.id.joinToString(", ") { TypeMapper.toPropertyName(it.name) }
            tableBuilder.addFunction(
                FunSpec.builder("primaryKey")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(ClassName("org.jetbrains.exposed.sql", "Table").nestedClass("PrimaryKey"))
                    .addStatement("return PrimaryKey($primaryKeyColumns)")
                    .build()
            )
        }

        val fileBuilder = FileSpec.builder(packageName, "${className}Table")
            .addType(tableBuilder.build())
            .addImport("org.jetbrains.exposed.sql", "*")

        // Create the data class for the entity
        val dataClassBuilder = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)

        val constructorBuilder = FunSpec.constructorBuilder()

        // Add ID properties
        entity.id.forEach { id ->
            addDataClassProperty(dataClassBuilder, constructorBuilder, id.name, id.type ?: "integer", false)
        }

        // Add field properties
        entity.field.forEach { field ->
            addDataClassProperty(dataClassBuilder, constructorBuilder, field.name, field.type, field.nullable)
        }

        // Add association properties
        entity.manyToOne.forEach { association ->
            val targetType = association.targetEntity?.let { TypeMapper.toKotlinClassName(it) } ?: "Long"
            val propertyName = "${TypeMapper.toPropertyName(association.field)}Id"
            val kotlinType = ClassName("", targetType).copy(nullable = true)
            val property = PropertySpec.builder(propertyName, kotlinType)
                .initializer(propertyName)
                .build()
            dataClassBuilder.addProperty(property)
            constructorBuilder.addParameter(
                ParameterSpec.builder(propertyName, kotlinType)
                    .defaultValue("null")
                    .build()
            )
        }

        dataClassBuilder.primaryConstructor(constructorBuilder.build())
        fileBuilder.addType(dataClassBuilder.build())

        // Create DAO-like extension functions
        fileBuilder.addFunction(createToEntityFunction(className, entity))
        fileBuilder.addFunction(createInsertFunction(className, entity))

        return fileBuilder.build()
    }

    private fun addIdColumn(tableBuilder: TypeSpec.Builder, id: Id) {
        val columnName = id.column ?: id.name
        val propertyName = TypeMapper.toPropertyName(id.name)

        val columnType = when (id.type?.lowercase()) {
            "bigint", "long" -> "long"
            else -> "integer"
        }

        val initializer = when {
            id.generator?.strategy == GeneratorStrategy.IDENTITY -> "$columnType(%S).autoIncrement()"
            else -> "$columnType(%S)"
        }

        tableBuilder.addProperty(
            PropertySpec.builder(propertyName, ClassName("org.jetbrains.exposed.sql", "Column")
                .parameterizedBy(ClassName("kotlin", columnType.replaceFirstChar { it.uppercase() })))
                .initializer(initializer, columnName)
                .build()
        )
    }

    private fun addFieldColumn(tableBuilder: TypeSpec.Builder, field: Field) {
        val columnName = field.column ?: field.name
        val propertyName = TypeMapper.toPropertyName(field.name)

        val columnFunction = getExposedColumnType(field.type, field.length, field.precision, field.scale)
        val nullable = if (field.nullable) ".nullable()" else ""
        val unique = if (field.unique) ".uniqueIndex()" else ""
        val default = if (!field.nullable && field.nullable) ".default(\"\")" else ""

        val initializer = "$columnFunction(%S)$nullable$unique"

        val kotlinType = when (field.type.lowercase()) {
            "string", "text" -> String::class.asClassName()
            "integer", "smallint" -> Int::class.asClassName()
            "bigint" -> Long::class.asClassName()
            "boolean" -> Boolean::class.asClassName()
            "decimal", "float" -> Double::class.asClassName()
            else -> String::class.asClassName()
        }

        tableBuilder.addProperty(
            PropertySpec.builder(
                propertyName,
                ClassName("org.jetbrains.exposed.sql", "Column")
                    .parameterizedBy(kotlinType.copy(nullable = field.nullable))
            )
                .initializer(initializer, columnName)
                .build()
        )
    }

    private fun addManyToOneColumn(tableBuilder: TypeSpec.Builder, association: ManyToOne) {
        val propertyName = "${TypeMapper.toPropertyName(association.field)}Id"
        val columnName = association.joinColumn?.name ?: "${association.field}_id"
        val referencedTable = association.targetEntity?.let { "${TypeMapper.toKotlinClassName(it)}Table" } ?: "Table"

        tableBuilder.addProperty(
            PropertySpec.builder(
                propertyName,
                ClassName("org.jetbrains.exposed.sql", "Column")
                    .parameterizedBy(Long::class.asClassName().copy(nullable = true))
            )
                .initializer("long(%S).nullable().references($referencedTable.id)", columnName)
                .build()
        )
    }

    private fun addDataClassProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        fieldName: String,
        fieldType: String,
        nullable: Boolean
    ) {
        val propertyName = TypeMapper.toPropertyName(fieldName)
        val kotlinType = TypeMapper.mapToKotlinType(fieldType, nullable)

        val property = PropertySpec.builder(propertyName, kotlinType)
            .initializer(propertyName)
            .build()

        classBuilder.addProperty(property)

        if (nullable) {
            constructorBuilder.addParameter(
                ParameterSpec.builder(propertyName, kotlinType)
                    .defaultValue("null")
                    .build()
            )
        } else {
            constructorBuilder.addParameter(propertyName, kotlinType)
        }
    }

    private fun getExposedColumnType(
        doctrineType: String,
        length: Int?,
        precision: Int?,
        scale: Int?
    ): String {
        return when (doctrineType.lowercase()) {
            "string" -> if (length != null) "varchar($length)" else "varchar(255)"
            "text" -> "text"
            "integer", "smallint" -> "integer"
            "bigint" -> "long"
            "boolean" -> "bool"
            "decimal" -> if (precision != null && scale != null) {
                "decimal($precision, $scale)"
            } else {
                "decimal(10, 2)"
            }
            "float" -> "double"
            "datetime", "datetimetz" -> "datetime"
            "date" -> "date"
            "time" -> "time"
            "json", "json_array" -> "text"
            "blob", "binary" -> "blob"
            "uuid", "guid" -> "uuid"
            else -> "varchar(255)"
        }
    }

    private fun createToEntityFunction(className: String, entity: Entity): FunSpec {
        val funBuilder = FunSpec.builder("toEntity")
            .receiver(ClassName("org.jetbrains.exposed.sql", "ResultRow"))
            .returns(ClassName("", className))

        val parameters = mutableListOf<String>()

        // Add ID fields
        entity.id.forEach { id ->
            val propertyName = TypeMapper.toPropertyName(id.name)
            parameters.add("$propertyName = this[${className}Table.$propertyName]")
        }

        // Add regular fields
        entity.field.forEach { field ->
            val propertyName = TypeMapper.toPropertyName(field.name)
            parameters.add("$propertyName = this[${className}Table.$propertyName]")
        }

        // Add association IDs
        entity.manyToOne.forEach { association ->
            val propertyName = "${TypeMapper.toPropertyName(association.field)}Id"
            parameters.add("$propertyName = this[${className}Table.$propertyName]")
        }

        funBuilder.addStatement("return $className(\n    ${parameters.joinToString(",\n    ")}\n)")

        return funBuilder.build()
    }

    private fun createInsertFunction(className: String, entity: Entity): FunSpec {
        val funBuilder = FunSpec.builder("insert")
            .receiver(ClassName("", "${className}Table"))
            .addParameter("entity", ClassName("", className))
            .returns(Int::class)

        val assignments = mutableListOf<String>()

        // Add ID assignments (excluding auto-increment)
        entity.id.forEach { id ->
            if (id.generator?.strategy != GeneratorStrategy.IDENTITY) {
                val propertyName = TypeMapper.toPropertyName(id.name)
                assignments.add("it[$propertyName] = entity.$propertyName")
            }
        }

        // Add field assignments
        entity.field.forEach { field ->
            val propertyName = TypeMapper.toPropertyName(field.name)
            assignments.add("it[$propertyName] = entity.$propertyName")
        }

        // Add association assignments
        entity.manyToOne.forEach { association ->
            val propertyName = "${TypeMapper.toPropertyName(association.field)}Id"
            assignments.add("it[$propertyName] = entity.$propertyName")
        }

        funBuilder.addCode(
            """
            return ${className}Table.insert {
                ${assignments.joinToString("\n    ")}
            } get ${className}Table.${TypeMapper.toPropertyName(entity.id.firstOrNull()?.name ?: "id")}
            """.trimIndent()
        )

        return funBuilder.build()
    }
}

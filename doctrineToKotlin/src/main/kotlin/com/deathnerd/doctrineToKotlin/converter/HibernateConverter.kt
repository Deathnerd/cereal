package com.deathnerd.doctrineToKotlin.converter

import com.deathnerd.doctrineToKotlin.model.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Converts Doctrine entities to Hibernate/JPA entities
 */
class HibernateConverter : EntityConverter {
    override val name: String = "Hibernate"

    override fun convertEntity(entity: Entity, packageName: String): FileSpec {
        val className = TypeMapper.toKotlinClassName(entity.name)

        val classBuilder = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)
            .addAnnotation(
                AnnotationSpec.builder(ClassName("jakarta.persistence", "Entity"))
                    .build()
            )

        // Add @Table annotation
        val tableName = entity.table ?: className.lowercase()
        classBuilder.addAnnotation(
            AnnotationSpec.builder(ClassName("jakarta.persistence", "Table"))
                .addMember("name = %S", tableName)
                .apply {
                    entity.schema?.let { addMember("schema = %S", it) }
                }
                .build()
        )

        // Add inheritance strategy if specified
        entity.inheritanceType?.let { inheritanceType ->
            val strategy = when (inheritanceType) {
                InheritanceType.SINGLE_TABLE -> "InheritanceType.SINGLE_TABLE"
                InheritanceType.JOINED -> "InheritanceType.JOINED"
            }
            classBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName("jakarta.persistence", "Inheritance"))
                    .addMember("strategy = %L", strategy)
                    .build()
            )
        }

        // Add discriminator column if specified
        entity.discriminatorColumn?.let { discriminator ->
            classBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName("jakarta.persistence", "DiscriminatorColumn"))
                    .addMember("name = %S", discriminator.name)
                    .apply {
                        discriminator.type?.let { addMember("discriminatorType = DiscriminatorType.STRING") }
                    }
                    .build()
            )
        }

        val constructorBuilder = FunSpec.constructorBuilder()

        // Add ID fields
        entity.id.forEach { id ->
            addIdProperty(classBuilder, constructorBuilder, id)
        }

        // Add regular fields
        entity.field.forEach { field ->
            addFieldProperty(classBuilder, constructorBuilder, field)
        }

        // Add associations
        entity.oneToOne.forEach { association ->
            addOneToOneProperty(classBuilder, constructorBuilder, association)
        }

        entity.manyToOne.forEach { association ->
            addManyToOneProperty(classBuilder, constructorBuilder, association)
        }

        entity.oneToMany.forEach { association ->
            addOneToManyProperty(classBuilder, constructorBuilder, association)
        }

        entity.manyToMany.forEach { association ->
            addManyToManyProperty(classBuilder, constructorBuilder, association)
        }

        classBuilder.primaryConstructor(constructorBuilder.build())

        return FileSpec.builder(packageName, className)
            .addType(classBuilder.build())
            .addImport("jakarta.persistence", "*")
            .build()
    }

    private fun addIdProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        id: Id
    ) {
        val propertyName = TypeMapper.toPropertyName(id.name)
        val kotlinType = TypeMapper.mapToKotlinType(id.type ?: "integer", false)

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer(propertyName)
            .addAnnotation(ClassName("jakarta.persistence", "Id"))

        // Add generation strategy
        id.generator?.let { generator ->
            val strategy = when (generator.strategy) {
                GeneratorStrategy.AUTO -> "GenerationType.AUTO"
                GeneratorStrategy.IDENTITY -> "GenerationType.IDENTITY"
                GeneratorStrategy.SEQUENCE -> "GenerationType.SEQUENCE"
                GeneratorStrategy.NONE -> null
                GeneratorStrategy.CUSTOM -> null
            }

            strategy?.let {
                propertyBuilder.addAnnotation(
                    AnnotationSpec.builder(ClassName("jakarta.persistence", "GeneratedValue"))
                        .addMember("strategy = %L", it)
                        .build()
                )
            }
        }

        // Add @Column annotation
        val columnName = id.column ?: propertyName
        val columnAnnotation = AnnotationSpec.builder(ClassName("jakarta.persistence", "Column"))
            .addMember("name = %S", columnName)
            .apply {
                id.length?.let { addMember("length = %L", it) }
            }
            .build()
        propertyBuilder.addAnnotation(columnAnnotation)

        classBuilder.addProperty(propertyBuilder.build())
        constructorBuilder.addParameter(propertyName, kotlinType)
    }

    private fun addFieldProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        field: Field
    ) {
        val propertyName = TypeMapper.toPropertyName(field.name)
        val kotlinType = TypeMapper.mapToKotlinType(field.type, field.nullable)

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer(propertyName)

        // Add @Column annotation
        val columnName = field.column ?: propertyName
        val columnAnnotation = AnnotationSpec.builder(ClassName("jakarta.persistence", "Column"))
            .addMember("name = %S", columnName)
            .apply {
                field.length?.let { addMember("length = %L", it) }
                if (!field.nullable) addMember("nullable = false")
                if (field.unique) addMember("unique = true")
                if (!field.updatable) addMember("updatable = false")
                if (!field.insertable) addMember("insertable = false")
                field.columnDefinition?.let { addMember("columnDefinition = %S", it) }
                field.precision?.let { addMember("precision = %L", it) }
                field.scale?.let { addMember("scale = %L", it) }
            }
            .build()
        propertyBuilder.addAnnotation(columnAnnotation)

        classBuilder.addProperty(propertyBuilder.build())
        constructorBuilder.addParameter(propertyName, kotlinType)
    }

    private fun addOneToOneProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        association: OneToOne
    ) {
        val propertyName = TypeMapper.toPropertyName(association.field)
        val targetClass = association.targetEntity?.let { TypeMapper.toKotlinClassName(it) } ?: "Any"
        val kotlinType = ClassName("", targetClass).copy(nullable = true)

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer(propertyName)
            .mutable(true)

        val annotationBuilder = AnnotationSpec.builder(ClassName("jakarta.persistence", "OneToOne"))
            .addMember("fetch = FetchType.%L", association.fetch.name)

        association.mappedBy?.let {
            annotationBuilder.addMember("mappedBy = %S", it)
        }

        if (association.orphanRemoval) {
            annotationBuilder.addMember("orphanRemoval = true")
        }

        propertyBuilder.addAnnotation(annotationBuilder.build())

        // Add cascade if specified
        association.cascade?.let { cascade ->
            addCascadeAnnotation(propertyBuilder, cascade)
        }

        // Add join column if specified and not mapped by
        if (association.mappedBy == null) {
            association.joinColumn?.let { joinColumn ->
                addJoinColumnAnnotation(propertyBuilder, joinColumn)
            }
        }

        classBuilder.addProperty(propertyBuilder.build())
        constructorBuilder.addParameter(
            ParameterSpec.builder(propertyName, kotlinType)
                .defaultValue("null")
                .build()
        )
    }

    private fun addManyToOneProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        association: ManyToOne
    ) {
        val propertyName = TypeMapper.toPropertyName(association.field)
        val targetClass = association.targetEntity?.let { TypeMapper.toKotlinClassName(it) } ?: "Any"
        val kotlinType = ClassName("", targetClass).copy(nullable = true)

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer(propertyName)
            .mutable(true)

        val annotationBuilder = AnnotationSpec.builder(ClassName("jakarta.persistence", "ManyToOne"))
            .addMember("fetch = FetchType.%L", association.fetch.name)

        propertyBuilder.addAnnotation(annotationBuilder.build())

        // Add cascade if specified
        association.cascade?.let { cascade ->
            addCascadeAnnotation(propertyBuilder, cascade)
        }

        // Add join column if specified
        association.joinColumn?.let { joinColumn ->
            addJoinColumnAnnotation(propertyBuilder, joinColumn)
        }

        classBuilder.addProperty(propertyBuilder.build())
        constructorBuilder.addParameter(
            ParameterSpec.builder(propertyName, kotlinType)
                .defaultValue("null")
                .build()
        )
    }

    private fun addOneToManyProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        association: OneToMany
    ) {
        val propertyName = TypeMapper.toPropertyName(association.field)
        val targetClass = TypeMapper.toKotlinClassName(association.targetEntity)
        val kotlinType = ClassName("kotlin.collections", "MutableSet")
            .parameterizedBy(ClassName("", targetClass))

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer("mutableSetOf()")
            .mutable(true)

        val annotationBuilder = AnnotationSpec.builder(ClassName("jakarta.persistence", "OneToMany"))
            .addMember("mappedBy = %S", association.mappedBy)
            .addMember("fetch = FetchType.%L", association.fetch.name)

        if (association.orphanRemoval) {
            annotationBuilder.addMember("orphanRemoval = true")
        }

        propertyBuilder.addAnnotation(annotationBuilder.build())

        // Add cascade if specified
        association.cascade?.let { cascade ->
            addCascadeAnnotation(propertyBuilder, cascade)
        }

        classBuilder.addProperty(propertyBuilder.build())
    }

    private fun addManyToManyProperty(
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        association: ManyToMany
    ) {
        val propertyName = TypeMapper.toPropertyName(association.field)
        val targetClass = TypeMapper.toKotlinClassName(association.targetEntity)
        val kotlinType = ClassName("kotlin.collections", "MutableSet")
            .parameterizedBy(ClassName("", targetClass))

        val propertyBuilder = PropertySpec.builder(propertyName, kotlinType)
            .initializer("mutableSetOf()")
            .mutable(true)

        val annotationBuilder = AnnotationSpec.builder(ClassName("jakarta.persistence", "ManyToMany"))
            .addMember("fetch = FetchType.%L", association.fetch.name)

        association.mappedBy?.let {
            annotationBuilder.addMember("mappedBy = %S", it)
        }

        propertyBuilder.addAnnotation(annotationBuilder.build())

        // Add cascade if specified
        association.cascade?.let { cascade ->
            addCascadeAnnotation(propertyBuilder, cascade)
        }

        // Add join table if specified and not mapped by
        if (association.mappedBy == null) {
            association.joinTable?.let { joinTable ->
                val joinTableAnnotation = AnnotationSpec.builder(ClassName("jakarta.persistence", "JoinTable"))
                    .addMember("name = %S", joinTable.name)
                    .build()
                propertyBuilder.addAnnotation(joinTableAnnotation)
            }
        }

        classBuilder.addProperty(propertyBuilder.build())
    }

    private fun addCascadeAnnotation(propertyBuilder: PropertySpec.Builder, cascade: Cascade) {
        val cascadeTypes = mutableListOf<String>()
        if (cascade.cascadeAll) cascadeTypes.add("CascadeType.ALL")
        if (cascade.cascadePersist) cascadeTypes.add("CascadeType.PERSIST")
        if (cascade.cascadeRemove) cascadeTypes.add("CascadeType.REMOVE")
        if (cascade.cascadeRefresh) cascadeTypes.add("CascadeType.REFRESH")
        if (cascade.cascadeDetach) cascadeTypes.add("CascadeType.DETACH")

        if (cascadeTypes.isNotEmpty()) {
            propertyBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName("jakarta.persistence", "Cascade"))
                    .addMember("value = [%L]", cascadeTypes.joinToString(", "))
                    .build()
            )
        }
    }

    private fun addJoinColumnAnnotation(propertyBuilder: PropertySpec.Builder, joinColumn: JoinColumn) {
        val joinColumnAnnotation = AnnotationSpec.builder(ClassName("jakarta.persistence", "JoinColumn"))
            .apply {
                joinColumn.name?.let { addMember("name = %S", it) }
                addMember("referencedColumnName = %S", joinColumn.referencedColumnName)
                if (!joinColumn.nullable) addMember("nullable = false")
                if (joinColumn.unique) addMember("unique = true")
            }
            .build()
        propertyBuilder.addAnnotation(joinColumnAnnotation)
    }
}

package com.deathnerd.doctrineToKotlin.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@XmlSerialName("field", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Field(
    val name: String,
    val type: String = "string",
    val column: String? = null,
    val length: Int? = null,
    val unique: Boolean = false,
    val nullable: Boolean = false,
    val index: Boolean = false,
    val insertable: Boolean = true,
    val updatable: Boolean = true,
    val generated: GeneratedType = GeneratedType.NEVER,
    @XmlSerialName("enum-type")
    val enumType: String? = null,
    val version: Boolean? = null,
    @XmlSerialName("column-definition")
    val columnDefinition: String? = null,
    val precision: Int? = null,
    val scale: Int? = null,
    @XmlElement(true)
    val options: Options? = null
)

@Serializable
@XmlSerialName("id", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Id(
    val name: String,
    val type: String? = null,
    val column: String? = null,
    val length: Int? = null,
    @XmlSerialName("association-key")
    val associationKey: Boolean = false,
    @XmlSerialName("column-definition")
    val columnDefinition: String? = null,
    @XmlElement(true)
    val generator: Generator? = null,
    @XmlElement(true)
    @XmlSerialName("sequence-generator")
    val sequenceGenerator: SequenceGenerator? = null,
    @XmlElement(true)
    @XmlSerialName("custom-id-generator")
    val customIdGenerator: CustomIdGenerator? = null,
    @XmlElement(true)
    val options: Options? = null
)

@Serializable
@XmlSerialName("generator", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Generator(
    val strategy: GeneratorStrategy = GeneratorStrategy.AUTO
)

@Serializable
enum class GeneratorStrategy {
    @XmlSerialName("NONE")
    NONE,
    @XmlSerialName("SEQUENCE")
    SEQUENCE,
    @XmlSerialName("IDENTITY")
    IDENTITY,
    @XmlSerialName("AUTO")
    AUTO,
    @XmlSerialName("CUSTOM")
    CUSTOM
}

@Serializable
enum class GeneratedType {
    @XmlSerialName("NEVER")
    NEVER,
    @XmlSerialName("INSERT")
    INSERT,
    @XmlSerialName("ALWAYS")
    ALWAYS
}

@Serializable
@XmlSerialName("sequence-generator", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class SequenceGenerator(
    @XmlSerialName("sequence-name")
    val sequenceName: String,
    @XmlSerialName("allocation-size")
    val allocationSize: Int = 1,
    @XmlSerialName("initial-value")
    val initialValue: Int = 1
)

@Serializable
@XmlSerialName("custom-id-generator", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class CustomIdGenerator(
    @XmlSerialName("class")
    val className: String
)

@Serializable
@XmlSerialName("embedded", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Embedded(
    val name: String,
    @XmlSerialName("class")
    val className: String? = null,
    @XmlSerialName("column-prefix")
    val columnPrefix: String? = null,
    @XmlSerialName("use-column-prefix")
    val useColumnPrefix: Boolean = true
)

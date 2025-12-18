package com.deathnerd.doctrineToKotlin.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("cache", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Cache(
    val usage: CacheUsageType? = null,
    val region: String? = null
)

@Serializable
enum class CacheUsageType {
    @XmlSerialName("READ_ONLY")
    READ_ONLY,
    @XmlSerialName("READ_WRITE")
    READ_WRITE,
    @XmlSerialName("NONSTRICT_READ_WRITE")
    NONSTRICT_READ_WRITE
}

@Serializable
@XmlSerialName("options", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Options(
    @XmlElement(true)
    val option: List<Option> = emptyList()
)

@Serializable
@XmlSerialName("option", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Option(
    val name: String,
    @XmlValue(true)
    val value: String = ""
)

@Serializable
@XmlSerialName("indexes", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Indexes(
    @XmlElement(true)
    val index: List<Index> = emptyList()
)

@Serializable
@XmlSerialName("index", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Index(
    val name: String? = null,
    val columns: String? = null,
    val fields: String? = null,
    val flags: String? = null,
    @XmlElement(true)
    val options: Options? = null
)

@Serializable
@XmlSerialName("unique-constraints", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class UniqueConstraints(
    @XmlElement(true)
    @XmlSerialName("unique-constraint")
    val uniqueConstraint: List<UniqueConstraint> = emptyList()
)

@Serializable
@XmlSerialName("unique-constraint", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class UniqueConstraint(
    val name: String? = null,
    val columns: String? = null,
    val fields: String? = null,
    @XmlElement(true)
    val options: Options? = null
)

@Serializable
@XmlSerialName("discriminator-column", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class DiscriminatorColumn(
    val name: String,
    val type: String? = null,
    @XmlSerialName("field-name")
    val fieldName: String? = null,
    val length: Int? = null,
    @XmlSerialName("column-definition")
    val columnDefinition: String? = null,
    @XmlSerialName("enum-type")
    val enumType: String? = null,
    @XmlElement(true)
    val options: Options? = null
)

@Serializable
@XmlSerialName("discriminator-map", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class DiscriminatorMap(
    @XmlElement(true)
    @XmlSerialName("discriminator-mapping")
    val discriminatorMapping: List<DiscriminatorMapping> = emptyList()
)

@Serializable
@XmlSerialName("discriminator-mapping", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class DiscriminatorMapping(
    val value: String,
    @XmlSerialName("class")
    val className: String
)

@Serializable
@XmlSerialName("lifecycle-callbacks", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class LifecycleCallbacks(
    @XmlElement(true)
    @XmlSerialName("lifecycle-callback")
    val lifecycleCallback: List<LifecycleCallback> = emptyList()
)

@Serializable
@XmlSerialName("lifecycle-callback", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class LifecycleCallback(
    val type: LifecycleCallbackType,
    val method: String
)

@Serializable
enum class LifecycleCallbackType {
    @XmlSerialName("prePersist")
    PRE_PERSIST,
    @XmlSerialName("postPersist")
    POST_PERSIST,
    @XmlSerialName("preUpdate")
    PRE_UPDATE,
    @XmlSerialName("postUpdate")
    POST_UPDATE,
    @XmlSerialName("preRemove")
    PRE_REMOVE,
    @XmlSerialName("postRemove")
    POST_REMOVE,
    @XmlSerialName("postLoad")
    POST_LOAD,
    @XmlSerialName("preFlush")
    PRE_FLUSH
}

@Serializable
@XmlSerialName("entity-listeners", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class EntityListeners(
    @XmlElement(true)
    @XmlSerialName("entity-listener")
    val entityListener: List<EntityListener> = emptyList()
)

@Serializable
@XmlSerialName("entity-listener", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class EntityListener(
    @XmlSerialName("class")
    val className: String,
    @XmlElement(true)
    @XmlSerialName("lifecycle-callback")
    val lifecycleCallback: List<LifecycleCallback> = emptyList()
)

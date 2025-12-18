package com.deathnerd.doctrineToKotlin.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@XmlSerialName("doctrine-mapping", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class DoctrineMapping(
    @XmlElement(true)
    val entities: List<Entity> = emptyList(),
    @XmlElement(true)
    val mappedSuperclasses: List<MappedSuperclass> = emptyList(),
    @XmlElement(true)
    val embeddables: List<Embeddable> = emptyList()
)

@Serializable
@XmlSerialName("entity", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Entity(
    val name: String,
    val table: String? = null,
    val schema: String? = null,
    @XmlSerialName("repository-class")
    val repositoryClass: String? = null,
    @XmlSerialName("inheritance-type")
    val inheritanceType: InheritanceType? = null,
    @XmlSerialName("change-tracking-policy")
    val changeTrackingPolicy: ChangeTrackingPolicy? = null,
    @XmlSerialName("read-only")
    val readOnly: Boolean = false,
    @XmlElement(true)
    val cache: Cache? = null,
    @XmlElement(true)
    val options: Options? = null,
    @XmlElement(true)
    val indexes: Indexes? = null,
    @XmlElement(true)
    @XmlSerialName("unique-constraints")
    val uniqueConstraints: UniqueConstraints? = null,
    @XmlElement(true)
    @XmlSerialName("discriminator-column")
    val discriminatorColumn: DiscriminatorColumn? = null,
    @XmlElement(true)
    @XmlSerialName("discriminator-map")
    val discriminatorMap: DiscriminatorMap? = null,
    @XmlElement(true)
    @XmlSerialName("lifecycle-callbacks")
    val lifecycleCallbacks: LifecycleCallbacks? = null,
    @XmlElement(true)
    @XmlSerialName("entity-listeners")
    val entityListeners: EntityListeners? = null,
    @XmlElement(true)
    val id: List<Id> = emptyList(),
    @XmlElement(true)
    val field: List<Field> = emptyList(),
    @XmlElement(true)
    val embedded: List<Embedded> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("one-to-one")
    val oneToOne: List<OneToOne> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("one-to-many")
    val oneToMany: List<OneToMany> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("many-to-one")
    val manyToOne: List<ManyToOne> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("many-to-many")
    val manyToMany: List<ManyToMany> = emptyList()
)

@Serializable
@XmlSerialName("mapped-superclass", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class MappedSuperclass(
    val name: String,
    @XmlElement(true)
    val id: List<Id> = emptyList(),
    @XmlElement(true)
    val field: List<Field> = emptyList()
)

@Serializable
@XmlSerialName("embeddable", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Embeddable(
    val name: String,
    @XmlElement(true)
    val field: List<Field> = emptyList()
)

@Serializable
enum class InheritanceType {
    @XmlSerialName("SINGLE_TABLE")
    SINGLE_TABLE,
    @XmlSerialName("JOINED")
    JOINED
}

@Serializable
enum class ChangeTrackingPolicy {
    @XmlSerialName("DEFERRED_IMPLICIT")
    DEFERRED_IMPLICIT,
    @XmlSerialName("DEFERRED_EXPLICIT")
    DEFERRED_EXPLICIT
}

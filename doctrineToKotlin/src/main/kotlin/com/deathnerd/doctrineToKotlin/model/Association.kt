package com.deathnerd.doctrineToKotlin.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@XmlSerialName("one-to-one", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class OneToOne(
    val field: String,
    @XmlSerialName("target-entity")
    val targetEntity: String? = null,
    @XmlSerialName("mapped-by")
    val mappedBy: String? = null,
    @XmlSerialName("inversed-by")
    val inversedBy: String? = null,
    val fetch: FetchType = FetchType.LAZY,
    @XmlSerialName("orphan-removal")
    val orphanRemoval: Boolean = false,
    @XmlElement(true)
    val cache: Cache? = null,
    @XmlElement(true)
    val cascade: Cascade? = null,
    @XmlElement(true)
    @XmlSerialName("join-column")
    val joinColumn: JoinColumn? = null,
    @XmlElement(true)
    @XmlSerialName("join-columns")
    val joinColumns: JoinColumns? = null
)

@Serializable
@XmlSerialName("one-to-many", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class OneToMany(
    val field: String,
    @XmlSerialName("target-entity")
    val targetEntity: String,
    @XmlSerialName("mapped-by")
    val mappedBy: String,
    @XmlSerialName("index-by")
    val indexBy: String? = null,
    val fetch: FetchType = FetchType.LAZY,
    @XmlSerialName("orphan-removal")
    val orphanRemoval: Boolean = false,
    @XmlElement(true)
    val cache: Cache? = null,
    @XmlElement(true)
    val cascade: Cascade? = null,
    @XmlElement(true)
    @XmlSerialName("order-by")
    val orderBy: OrderBy? = null
)

@Serializable
@XmlSerialName("many-to-one", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class ManyToOne(
    val field: String,
    @XmlSerialName("target-entity")
    val targetEntity: String? = null,
    @XmlSerialName("inversed-by")
    val inversedBy: String? = null,
    val fetch: FetchType = FetchType.LAZY,
    @XmlElement(true)
    val cache: Cache? = null,
    @XmlElement(true)
    val cascade: Cascade? = null,
    @XmlElement(true)
    @XmlSerialName("join-column")
    val joinColumn: JoinColumn? = null,
    @XmlElement(true)
    @XmlSerialName("join-columns")
    val joinColumns: JoinColumns? = null
)

@Serializable
@XmlSerialName("many-to-many", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class ManyToMany(
    val field: String,
    @XmlSerialName("target-entity")
    val targetEntity: String,
    @XmlSerialName("mapped-by")
    val mappedBy: String? = null,
    @XmlSerialName("inversed-by")
    val inversedBy: String? = null,
    @XmlSerialName("index-by")
    val indexBy: String? = null,
    val fetch: FetchType = FetchType.LAZY,
    @XmlSerialName("orphan-removal")
    val orphanRemoval: Boolean = false,
    @XmlElement(true)
    val cache: Cache? = null,
    @XmlElement(true)
    val cascade: Cascade? = null,
    @XmlElement(true)
    @XmlSerialName("join-table")
    val joinTable: JoinTable? = null,
    @XmlElement(true)
    @XmlSerialName("order-by")
    val orderBy: OrderBy? = null
)

@Serializable
enum class FetchType {
    @XmlSerialName("EAGER")
    EAGER,
    @XmlSerialName("LAZY")
    LAZY,
    @XmlSerialName("EXTRA_LAZY")
    EXTRA_LAZY
}

@Serializable
@XmlSerialName("cascade", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class Cascade(
    @XmlElement(true)
    @XmlSerialName("cascade-all")
    val cascadeAll: Boolean = false,
    @XmlElement(true)
    @XmlSerialName("cascade-persist")
    val cascadePersist: Boolean = false,
    @XmlElement(true)
    @XmlSerialName("cascade-remove")
    val cascadeRemove: Boolean = false,
    @XmlElement(true)
    @XmlSerialName("cascade-refresh")
    val cascadeRefresh: Boolean = false,
    @XmlElement(true)
    @XmlSerialName("cascade-detach")
    val cascadeDetach: Boolean = false
)

@Serializable
@XmlSerialName("join-column", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class JoinColumn(
    val name: String? = null,
    @XmlSerialName("referenced-column-name")
    val referencedColumnName: String = "id",
    val unique: Boolean = false,
    val nullable: Boolean = true,
    @XmlSerialName("on-delete")
    val onDelete: FkAction? = null,
    @XmlSerialName("column-definition")
    val columnDefinition: String? = null
)

@Serializable
@XmlSerialName("join-columns", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class JoinColumns(
    @XmlElement(true)
    @XmlSerialName("join-column")
    val joinColumn: List<JoinColumn> = emptyList()
)

@Serializable
@XmlSerialName("join-table", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class JoinTable(
    val name: String,
    val schema: String? = null,
    @XmlElement(true)
    @XmlSerialName("join-columns")
    val joinColumns: JoinColumns? = null,
    @XmlElement(true)
    @XmlSerialName("inverse-join-columns")
    val inverseJoinColumns: JoinColumns? = null
)

@Serializable
enum class FkAction {
    @XmlSerialName("CASCADE")
    CASCADE,
    @XmlSerialName("RESTRICT")
    RESTRICT,
    @XmlSerialName("SET NULL")
    SET_NULL
}

@Serializable
@XmlSerialName("order-by", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class OrderBy(
    @XmlElement(true)
    @XmlSerialName("order-by-field")
    val orderByField: List<OrderByField> = emptyList()
)

@Serializable
@XmlSerialName("order-by-field", "http://doctrine-project.org/schemas/orm/doctrine-mapping", "orm")
data class OrderByField(
    val name: String,
    val direction: OrderByDirection = OrderByDirection.ASC
)

@Serializable
enum class OrderByDirection {
    @XmlSerialName("ASC")
    ASC,
    @XmlSerialName("DESC")
    DESC
}

package com.deathnerd.doctrineToKotlin.converter

import com.deathnerd.doctrineToKotlin.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HibernateConverterTest {
    private val converter = HibernateConverter()

    @Test
    fun `should generate basic entity class`() {
        val entity = Entity(
            name = "User",
            table = "users",
            id = listOf(
                Id(
                    name = "id",
                    type = "integer",
                    generator = Generator(GeneratorStrategy.AUTO)
                )
            ),
            field = listOf(
                Field(name = "username", type = "string", nullable = false),
                Field(name = "email", type = "string", nullable = false, unique = true)
            )
        )

        val fileSpec = converter.convertEntity(entity, "com.example.entities")

        val code = fileSpec.toString()
        assertTrue(code.contains("@Entity"))
        assertTrue(code.contains("@Table(name = \"users\")"))
        assertTrue(code.contains("data class User"))
        assertTrue(code.contains("@Id"))
        assertTrue(code.contains("val id: Int"))
        assertTrue(code.contains("val username: String"))
        assertTrue(code.contains("val email: String"))
    }

    @Test
    fun `should generate entity with associations`() {
        val entity = Entity(
            name = "Post",
            table = "posts",
            id = listOf(
                Id(name = "id", type = "integer", generator = Generator(GeneratorStrategy.AUTO))
            ),
            field = listOf(
                Field(name = "title", type = "string", nullable = false)
            ),
            manyToOne = listOf(
                ManyToOne(
                    field = "author",
                    targetEntity = "User",
                    fetch = FetchType.LAZY
                )
            )
        )

        val fileSpec = converter.convertEntity(entity, "com.example.entities")

        val code = fileSpec.toString()
        assertTrue(code.contains("@ManyToOne"))
        assertTrue(code.contains("val author: User?"))
    }

    @Test
    fun `should handle nullable fields correctly`() {
        val entity = Entity(
            name = "Product",
            table = "products",
            id = listOf(Id(name = "id", type = "integer")),
            field = listOf(
                Field(name = "name", type = "string", nullable = false),
                Field(name = "description", type = "text", nullable = true)
            )
        )

        val fileSpec = converter.convertEntity(entity, "com.example.entities")

        val code = fileSpec.toString()
        assertTrue(code.contains("val name: String"))
        assertTrue(code.contains("val description: String?"))
    }

    @Test
    fun `converter name should be Hibernate`() {
        assertEquals("Hibernate", converter.name)
    }
}

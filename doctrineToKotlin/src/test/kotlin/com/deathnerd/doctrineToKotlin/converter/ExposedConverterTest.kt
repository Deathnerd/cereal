package com.deathnerd.doctrineToKotlin.converter

import com.deathnerd.doctrineToKotlin.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExposedConverterTest {
    private val converter = ExposedConverter()

    @Test
    fun `should generate table object and data class`() {
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

        val fileSpec = converter.convertEntity(entity, "com.example.tables")

        val code = fileSpec.toString()
        assertTrue(code.contains("object UserTable : Table(\"users\")"))
        assertTrue(code.contains("data class User"))
        assertTrue(code.contains("val id:"))
        assertTrue(code.contains("val username:"))
        assertTrue(code.contains("val email:"))
    }

    @Test
    fun `should generate table with auto-increment ID`() {
        val entity = Entity(
            name = "Post",
            table = "posts",
            id = listOf(
                Id(
                    name = "id",
                    type = "integer",
                    generator = Generator(GeneratorStrategy.IDENTITY)
                )
            ),
            field = listOf(
                Field(name = "title", type = "string", nullable = false)
            )
        )

        val fileSpec = converter.convertEntity(entity, "com.example.tables")

        val code = fileSpec.toString()
        assertTrue(code.contains("autoIncrement()"))
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

        val fileSpec = converter.convertEntity(entity, "com.example.tables")

        val code = fileSpec.toString()
        assertTrue(code.contains("val name: String"))
        assertTrue(code.contains("val description: String?"))
    }

    @Test
    fun `should generate extension functions`() {
        val entity = Entity(
            name = "User",
            table = "users",
            id = listOf(Id(name = "id", type = "integer")),
            field = listOf(Field(name = "username", type = "string", nullable = false))
        )

        val fileSpec = converter.convertEntity(entity, "com.example.tables")

        val code = fileSpec.toString()
        assertTrue(code.contains("fun ResultRow.toEntity()"))
        assertTrue(code.contains("fun UserTable.insert("))
    }

    @Test
    fun `converter name should be Exposed`() {
        assertEquals("Exposed", converter.name)
    }
}

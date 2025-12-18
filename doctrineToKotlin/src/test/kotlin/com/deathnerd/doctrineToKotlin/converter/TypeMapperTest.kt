package com.deathnerd.doctrineToKotlin.converter

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeMapperTest {

    @Test
    fun `should map string types correctly`() {
        val type = TypeMapper.mapToKotlinType("string", false)
        assertEquals("kotlin.String", type.toString())
        assertFalse(type.isNullable)
    }

    @Test
    fun `should map integer types correctly`() {
        val type = TypeMapper.mapToKotlinType("integer", false)
        assertEquals("kotlin.Int", type.toString())
    }

    @Test
    fun `should map bigint to Long`() {
        val type = TypeMapper.mapToKotlinType("bigint", false)
        assertEquals("kotlin.Long", type.toString())
    }

    @Test
    fun `should handle nullable types`() {
        val type = TypeMapper.mapToKotlinType("string", true)
        assertTrue(type.isNullable)
    }

    @Test
    fun `should convert table name to class name`() {
        assertEquals("UserAccount", TypeMapper.toClassName("user_account"))
        assertEquals("Product", TypeMapper.toClassName("product"))
        assertEquals("OrderItem", TypeMapper.toClassName("order_item"))
    }

    @Test
    fun `should extract class name from PHP namespace`() {
        assertEquals("User", TypeMapper.toKotlinClassName("App\\Entity\\User"))
        assertEquals("Product", TypeMapper.toKotlinClassName("Product"))
    }

    @Test
    fun `should map Doctrine types to JPA column types`() {
        assertEquals("VARCHAR", TypeMapper.mapToJpaColumnType("string"))
        assertEquals("INTEGER", TypeMapper.mapToJpaColumnType("integer"))
        assertEquals("TEXT", TypeMapper.mapToJpaColumnType("text"))
        assertEquals("TIMESTAMP", TypeMapper.mapToJpaColumnType("datetime"))
    }
}

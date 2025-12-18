package com.deathnerd.doctrineToKotlin.xml

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XmlValidatorTest {
    private val validator = XmlValidator()

    @Test
    fun `should validate correct XML`() {
        val validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <doctrine-mapping xmlns="http://doctrine-project.org/schemas/orm/doctrine-mapping"
                              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                              xsi:schemaLocation="http://doctrine-project.org/schemas/orm/doctrine-mapping
                                                  https://www.doctrine-project.org/schemas/orm/doctrine-mapping.xsd">
                <entity name="User" table="users">
                    <id name="id" type="integer" column="id">
                        <generator strategy="AUTO"/>
                    </id>
                    <field name="username" type="string" column="username" length="255" nullable="false"/>
                    <field name="email" type="string" column="email" length="255" nullable="false" unique="true"/>
                </entity>
            </doctrine-mapping>
        """.trimIndent()

        assertTrue(validator.isValid(validXml))
    }

    @Test
    fun `should reject invalid XML structure`() {
        val invalidXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <doctrine-mapping xmlns="http://doctrine-project.org/schemas/orm/doctrine-mapping">
                <invalid-element name="User">
                </invalid-element>
            </doctrine-mapping>
        """.trimIndent()

        assertFalse(validator.isValid(invalidXml))
    }

    @Test
    fun `should reject XML with missing required attributes`() {
        val invalidXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <doctrine-mapping xmlns="http://doctrine-project.org/schemas/orm/doctrine-mapping">
                <entity table="users">
                    <id name="id" type="integer"/>
                </entity>
            </doctrine-mapping>
        """.trimIndent()

        assertFalse(validator.isValid(invalidXml))
    }
}

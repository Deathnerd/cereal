package com.deathnerd.doctrineToKotlin.xml

import java.io.File
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

/**
 * Validates XML files against the Doctrine ORM XSD schema
 */
class XmlValidator {
    private val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    private val schema by lazy {
        val xsdStream = javaClass.classLoader.getResourceAsStream("doctrine-mapping.xsd")
            ?: throw IllegalStateException("Could not find doctrine-mapping.xsd in resources")
        schemaFactory.newSchema(StreamSource(xsdStream))
    }

    /**
     * Validates an XML file against the Doctrine ORM schema
     * @param xmlFile The XML file to validate
     * @throws org.xml.sax.SAXException if validation fails
     */
    fun validate(xmlFile: File) {
        val validator = schema.newValidator()
        validator.validate(StreamSource(xmlFile))
    }

    /**
     * Validates XML content from a string against the Doctrine ORM schema
     * @param xmlContent The XML content as a string
     * @throws org.xml.sax.SAXException if validation fails
     */
    fun validate(xmlContent: String) {
        val validator = schema.newValidator()
        validator.validate(StreamSource(StringReader(xmlContent)))
    }

    /**
     * Checks if an XML file is valid against the Doctrine ORM schema
     * @param xmlFile The XML file to check
     * @return true if valid, false otherwise
     */
    fun isValid(xmlFile: File): Boolean = try {
        validate(xmlFile)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Checks if XML content is valid against the Doctrine ORM schema
     * @param xmlContent The XML content as a string
     * @return true if valid, false otherwise
     */
    fun isValid(xmlContent: String): Boolean = try {
        validate(xmlContent)
        true
    } catch (e: Exception) {
        false
    }
}

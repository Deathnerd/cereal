package com.deathnerd.doctrineToKotlin.xml

import com.deathnerd.doctrineToKotlin.model.DoctrineMapping
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File

/**
 * Parses Doctrine ORM XML mapping files into Kotlin data structures
 */
class XmlParser(private val validator: XmlValidator = XmlValidator()) {

    private val xml = XML {
        indentString = "  "
        xmlDeclMode = nl.adaptivity.xmlutil.XmlDeclMode.Auto
    }

    /**
     * Parses a Doctrine XML mapping file with validation
     * @param xmlFile The XML file to parse
     * @param validateSchema Whether to validate against XSD schema (default: true)
     * @return Parsed DoctrineMapping object
     * @throws IllegalArgumentException if validation fails
     */
    fun parse(xmlFile: File, validateSchema: Boolean = true): DoctrineMapping {
        if (validateSchema) {
            try {
                validator.validate(xmlFile)
            } catch (e: Exception) {
                throw IllegalArgumentException("XML validation failed for ${xmlFile.name}: ${e.message}", e)
            }
        }

        return try {
            xml.decodeFromString(DoctrineMapping.serializer(), xmlFile.readText())
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse ${xmlFile.name}: ${e.message}", e)
        }
    }

    /**
     * Parses multiple Doctrine XML mapping files
     * @param xmlFiles List of XML files to parse
     * @param validateSchema Whether to validate against XSD schema (default: true)
     * @return List of parsed DoctrineMapping objects
     */
    fun parseAll(xmlFiles: List<File>, validateSchema: Boolean = true): List<DoctrineMapping> {
        return xmlFiles.map { parse(it, validateSchema) }
    }
}

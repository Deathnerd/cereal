# Doctrine to Kotlin Converter

A CLI tool that converts Doctrine ORM XML entity mapping files to Kotlin entity classes for various ORM frameworks.

## Features

- **XML Schema Validation**: Validates input XML files against the official Doctrine ORM XSD schema
- **Multiple Target Formats**: Currently supports Hibernate/JPA and Exposed
- **Extensible Architecture**: Easy to add support for new target frameworks
- **Type Mapping**: Automatically maps Doctrine field types to appropriate Kotlin types
- **Association Support**: Handles one-to-one, one-to-many, many-to-one, and many-to-many relationships
- **CLI Interface**: User-friendly command-line interface with helpful error messages

## Supported Target Formats

### Hibernate (JPA)
Generates standard JPA/Hibernate annotated entity classes with:
- `@Entity`, `@Table`, `@Column` annotations
- ID generation strategies (`@Id`, `@GeneratedValue`)
- Relationship annotations (`@OneToOne`, `@ManyToOne`, `@OneToMany`, `@ManyToMany`)
- Cascade and fetch type configurations
- Join column definitions

### Exposed
Generates Exposed DSL table definitions with:
- Table object extending `org.jetbrains.exposed.sql.Table`
- Data classes for entity representation
- Extension functions for converting between `ResultRow` and entities
- Helper functions for inserting entities

## Installation

Build the project:

```bash
./gradlew :doctrineToKotlin:build
```

Create a distribution:

```bash
./gradlew :doctrineToKotlin:installDist
```

The executable will be available at `doctrineToKotlin/build/install/doctrineToKotlin/bin/doctrineToKotlin`

## Usage

### Basic Usage

```bash
doctrineToKotlin -f <format> -o <output-dir> -p <package> <input-files...>
```

### Arguments

- `INPUT_FILES`: One or more Doctrine XML mapping files to convert (required)

### Options

- `-f, --format <format>`: Target format (`hibernate` or `exposed`) (required)
- `-o, --output <dir>`: Output directory for generated Kotlin files (required)
- `-p, --package <package>`: Package name for generated classes (required)
- `--skip-validation`: Skip XML schema validation (not recommended)
- `-v, --verbose`: Enable verbose output
- `-h, --help`: Show help message

### Examples

#### Convert to Hibernate entities

```bash
doctrineToKotlin -f hibernate \
  -o src/main/kotlin \
  -p com.example.entities \
  User.orm.xml Product.orm.xml Order.orm.xml
```

#### Convert to Exposed tables

```bash
doctrineToKotlin --format exposed \
  --output src/main/kotlin \
  --package com.example.tables \
  *.orm.xml
```

#### Convert with verbose output

```bash
doctrineToKotlin -f hibernate \
  -o output/entities \
  -p com.myapp.domain \
  -v \
  entities/*.xml
```

## Input Format

The tool expects Doctrine ORM XML mapping files following the official Doctrine schema:

```xml
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
        <many-to-one field="group" target-entity="Group" fetch="LAZY">
            <join-column name="group_id" referenced-column-name="id"/>
        </many-to-one>
    </entity>
</doctrine-mapping>
```

## Output Examples

### Hibernate Output

```kotlin
package com.example.entities

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    val id: Int,

    @Column(name = "username", length = 255, nullable = false)
    val username: String,

    @Column(name = "email", length = 255, nullable = false, unique = true)
    val email: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    var group: Group? = null
)
```

### Exposed Output

```kotlin
package com.example.tables

import org.jetbrains.exposed.sql.*

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val email = varchar("email", 255).uniqueIndex()
    val groupId = long("group_id").nullable().references(GroupTable.id)

    override fun primaryKey() = PrimaryKey(id)
}

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val groupId: Long? = null
)

fun ResultRow.toEntity(): User {
    return User(
        id = this[UserTable.id],
        username = this[UserTable.username],
        email = this[UserTable.email],
        groupId = this[UserTable.groupId]
    )
}

fun UserTable.insert(entity: User): Int {
    return UserTable.insert {
        it[username] = entity.username
        it[email] = entity.email
        it[groupId] = entity.groupId
    } get UserTable.id
}
```

## Type Mapping

| Doctrine Type | Kotlin Type | Hibernate/JPA | Exposed |
|---------------|-------------|---------------|---------|
| string | String | VARCHAR | varchar() |
| text | String | TEXT | text() |
| integer | Int | INTEGER | integer() |
| bigint | Long | BIGINT | long() |
| boolean | Boolean | BOOLEAN | bool() |
| decimal | Double | DECIMAL | decimal() |
| float | Double | DOUBLE | double() |
| datetime | LocalDateTime | TIMESTAMP | datetime() |
| date | LocalDateTime | DATE | date() |
| json | Map<String, Any?> | JSON | text() |
| uuid | UUID | UUID | uuid() |

## Architecture

The tool is designed with extensibility in mind:

### Core Components

1. **Model Layer** (`model/`): Kotlin data classes representing the Doctrine XML schema
2. **XML Layer** (`xml/`):
   - `XmlValidator`: Validates XML against XSD schema
   - `XmlParser`: Parses XML files into model objects
3. **Converter Layer** (`converter/`):
   - `EntityConverter`: Base interface for all converters
   - `ConverterRegistry`: Registry for managing available converters
   - `TypeMapper`: Utility for mapping types between systems
   - `HibernateConverter`: Generates Hibernate/JPA entities
   - `ExposedConverter`: Generates Exposed table definitions
4. **CLI Layer** (`cli/`): Command-line interface using Clikt

### Adding New Target Formats

To add support for a new target framework (e.g., Spring Data, jOOQ, Ktorm):

1. Create a new class implementing `EntityConverter`:

```kotlin
class MyFrameworkConverter : EntityConverter {
    override val name: String = "MyFramework"

    override fun convertEntity(entity: Entity, packageName: String): FileSpec {
        // Implementation here
    }
}
```

2. Register your converter in `ConverterRegistry`:

```kotlin
ConverterRegistry.register(MyFrameworkConverter())
```

That's it! The CLI will automatically detect and make your converter available.

## Development

### Building

```bash
./gradlew :doctrineToKotlin:build
```

### Running Tests

```bash
./gradlew :doctrineToKotlin:test
```

### Running from Source

```bash
./gradlew :doctrineToKotlin:run --args="-f hibernate -o output -p com.example entities/User.orm.xml"
```

## Dependencies

- Kotlin 2.2.20
- kotlinx.serialization for XML parsing
- KotlinPoet for code generation
- Clikt for CLI
- JUnit 5 for testing

## License

Part of the Cereal project.

## Contributing

To contribute a new converter or improvement:

1. Implement the `EntityConverter` interface
2. Add comprehensive tests
3. Update this README with usage examples
4. Submit a pull request

## Troubleshooting

### XML Validation Fails

Make sure your XML files follow the Doctrine ORM schema. Common issues:
- Missing required `name` attribute on `<entity>`
- Invalid field types
- Incorrect namespace declarations

You can temporarily skip validation with `--skip-validation`, but this is not recommended.

### Generated Code Doesn't Compile

- Check that all referenced entity classes exist
- Verify package names are correct
- Ensure field types are mapped correctly

### Unknown Converter Error

Make sure you're using one of the supported formats: `hibernate` or `exposed` (case-insensitive).

# Design Log #06 — Room Entity Design

- **Estado**: Draft
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Data Layer
- **Módulos afectados**: `:core:data` (nuevo)

---

## Background

El dominio está completo con `Character`, `GameCharacterSheet`, `GameSystemPlugin`, etc. Necesitamos persistencia real. Room es la elección natural en Android. Pero el dominio es agnóstico a Room: necesitamos una capa de traducción (mappers) entre entidades Room y entidades de dominio.

## Problem

¿Cómo modelamos las entidades Room para que: (a) no contaminen el dominio con anotaciones Android, (b) sean serializables a JSON para `GameCharacterSheet`, (c) manejen las relaciones Character → Sheet sin joins complejos?

## Questions & Answers

**Q**: ¿`GameCharacterSheet` como columna JSON o tabla separada?
**A**: Columna JSON (string) en `CharacterEntity`. El `GameCharacterSheet` es un value object plano (Map<AttributeKey, AttributeValue>). La serialización JSON es más simple que una tabla con 1NF perfecta. Trade-off: no se puede hacer queries SQL sobre atributos individuales. Aceptable para v1.

**Q**: ¿`Either` se serializa?
**A**: No. `Either` es solo para runtime. En persistencia guardamos los datos planos y reconstruimos `Character` con `Character.reconstruct()`.

**Q**: ¿Los timestamps los maneja Room o el dominio?
**A**: El dominio. `Character.create()` genera `createdAt`/`updatedAt`. Room solo los almacena como `Long` (epoch millis).

## Design

### `CharacterEntity` (Room)

```kotlin
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: String,           // CharacterId.value
    val name: String,         // NonEmptyString.value
    val systemId: String,     // SystemId.value
    val sheetJson: String,    // GameCharacterSheet serializado a JSON
    val createdAt: Long,      // Instant.toEpochMilli()
    val updatedAt: Long       // Instant.toEpochMilli()
)
```

### Mapper

```kotlin
object CharacterMapper {
    fun toDomain(entity: CharacterEntity): Character {
        return Character.reconstruct(
            id = CharacterId.fromString(entity.id),
            name = NonEmptyString.create(entity.name).getOrThrow(),
            systemId = SystemId(entity.systemId),
            createdAt = Instant.ofEpochMilli(entity.createdAt),
            updatedAt = Instant.ofEpochMilli(entity.updatedAt)
        )
    }

    fun toEntity(character: Character, sheetJson: String): CharacterEntity {
        return CharacterEntity(
            id = character.id.value,
            name = character.name.value,
            systemId = character.systemId.value,
            sheetJson = sheetJson,
            createdAt = character.createdAt.toEpochMilli(),
            updatedAt = character.updatedAt.toEpochMilli()
        )
    }
}
```

### `GameCharacterSheet` JSON Serialization

```kotlin
// Custom TypeConverter para Room
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSheet(sheet: GameCharacterSheet?): String? {
        return sheet?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toSheet(json: String?): GameCharacterSheet? {
        return json?.let { gson.fromJson(it, GameCharacterSheet::class.java) }
    }
}
```

⚠️ **Problema**: `AttributeValue` es sealed interface. Gson no maneja sealed interfaces nativamente. Necesitamos:
- Opción A: `PolymorphicJsonAdapterFactory` (Moshi)
- Opción B: `JsonDeserializer` custom (Gson)
- Opción C: Flatten a Map<String, JsonPrimitive> con type discriminator

## Examples

### ✅ Character con sheet persistida

```kotlin
val character = Character.create("Gimli", SystemId("dnd5e")).getOrThrow()
val sheet = GameCharacterSheet()
    .withAttribute(AttributeKey("strength"), NumberValue(18))

val entity = CharacterMapper.toEntity(character, sheetJson)
dao.insert(entity)
```

### ✅ Recuperación

```kotlin
val entity = dao.findById("uuid-123")
val character = CharacterMapper.toDomain(entity)
val sheet = gson.fromJson(entity.sheetJson, GameCharacterSheet::class.java)
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Una tabla simple, fácil de migrar | No se puede query por atributo individual |
| Sheet como JSON: flexible para cualquier sistema | JSON con sealed interface requiere serializer custom |
| Mapper explícito: separación clara | `NonEmptyString.create().getOrThrow()` en mapper puede lanzar si DB está corrupta |

## Implementation Plan

- [ ] Crear módulo `:core:data` con Room dependencies
- [ ] Definir `CharacterEntity` con anotaciones Room
- [ ] Implementar `CharacterDao` con `@Insert`, `@Query`, `@Delete`
- [ ] Implementar `Converters` para `GameCharacterSheet` ↔ JSON
- [ ] Resolver serialización de `AttributeValue` sealed interface (Moshi vs Gson custom)
- [ ] Implementar `CharacterRepositoryImpl` con mappers
- [ ] Tests con Room in-memory database

## Implementation Results

- Pendiente. Ver Design Log #06-futuro para resultados.

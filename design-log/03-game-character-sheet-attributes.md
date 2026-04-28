# Design Log #03 — GameCharacterSheet & Attribute System

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Domain Model
- **Módulos afectados**: `:core:domain`

---

## Background

Una hoja de personaje es un conjunto de atributos heterogéneos: nombres (texto), puntuaciones (números), flags (booleanos), listas (equipamiento, hechizos). Necesitamos un modelo que permita almacenar cualquier atributo de cualquier sistema sin perder type-safety al leer.

## Problem

¿Cómo modelamos una hoja de personaje genérica que soporte atributos heterogéneos y mantenga type-safety en tiempo de compilación?

## Questions & Answers

**Q**: ¿Usamos `Map<String, Any>`?
**A**: No. Pierde type-safety. `Any` requiere casts inseguros. El caller no sabe si `strength` es Int o String.

**Q**: ¿Clase con properties hardcoded por sistema?
**A**: No. Rompe el propósito del plugin system. Cada sistema nuevo requeriría modificar el modelo.

**Q**: ¿Sealed interface con generics?
**A**: Sí. `AttributeValue` como sealed interface con variantes tipadas. `GameCharacterSheet` como `Map<AttributeKey, AttributeValue>`. `find<T>()` usa reified generics para type-safe lookup.

## Design

### `AttributeValue` — Sealed Interface

```kotlin
sealed interface AttributeValue

data class TextValue(val text: String) : AttributeValue
data class NumberValue(val number: Int) : AttributeValue
data class BooleanValue(val flag: Boolean) : AttributeValue
data class ListValue(val items: List<String>) : AttributeValue
```

### `GameCharacterSheet` — Value Object Inmutable

```kotlin
data class GameCharacterSheet(
    val attributes: Map<AttributeKey, AttributeValue> = emptyMap(),
    val metadata: SheetMetadata = SheetMetadata()
)
```

### Type-safe Lookup

```kotlin
inline fun <reified T : AttributeValue> GameCharacterSheet.find(key: AttributeKey): T? {
    return attributes[key] as? T
}
```

### Inmutabilidad

```kotlin
fun withAttribute(key: AttributeKey, value: AttributeValue): GameCharacterSheet
fun withClean(): GameCharacterSheet  // remueve dirty flag
```

### `SheetMetadata`

```kotlin
data class SheetMetadata(
    val level: Int = 1,
    val experiencePoints: Int = 0,
    val isDirty: Boolean = false
)
```

## Examples

### ✅ Lectura type-safe

```kotlin
val str: NumberValue? = sheet.find(AttributeKey("strength"))
val name: TextValue? = sheet.find(AttributeKey("name"))
```

### ✅ Construcción inmutable

```kotlin
val sheet = GameCharacterSheet()
    .withAttribute(AttributeKey("strength"), NumberValue(16))
    .withAttribute(AttributeKey("name"), TextValue("Gimli"))
```

### ✅ Dirty tracking

```kotlin
val dirty = sheet.withAttribute(AttributeKey("hp"), NumberValue(20))
assertTrue(dirty.metadata.isDirty)

val clean = dirty.withClean()
assertFalse(clean.metadata.isDirty)
```

### ❌ Map<String, Any>

```kotlin
// NO. Requiere casts, no type-safe, rompe en runtime.
val sheet: Map<String, Any> = mapOf("strength" to 16)
val str = sheet["strength"] as Int  // puede fallar
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Type-safe con reified generics | `find<T>()` puede retornar null si el tipo no coincide |
| Inmutable: predictible, thread-safe | Crear copias en cada `withAttribute` tiene costo (aceptable para sheets pequeñas) |
| Genérico: funciona con cualquier sistema | Plugin debe traducir entre `AttributeKey` y conceptos del sistema |

## Implementation Plan

- [x] Crear `AttributeValue` sealed interface con 4 variantes
- [x] Crear `AttributeKey` como `@JvmInline value class`
- [x] Crear `GameCharacterSheet` con `Map<AttributeKey, AttributeValue>`
- [x] Implementar `find<T>()` con reified generics
- [x] Implementar `withAttribute()`, `withClean()`
- [x] Crear `SheetMetadata` con dirty tracking
- [x] Tests: lectura, escritura, reemplazo, dirty tracking, type mismatch

## Implementation Results

- Archivo: `core/domain/src/main/java/com/ddsheet/core/domain/model/Plugin.kt`
- Tests: `core/domain/src/test/java/com/ddsheet/core/domain/model/AttributeValueTest.kt`
- Tests pasando: 8/8 (find correct type, wrong type returns null, withAttribute add, replace, dirty, clean, ValidationResult valid/invalid)
- Desviaciones: Ninguna.

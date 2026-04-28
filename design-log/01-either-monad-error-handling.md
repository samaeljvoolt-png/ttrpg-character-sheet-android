# Design Log #01 — Either Monad & Error Handling

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Domain Layer
- **Módulos afectados**: `:core:domain`

---

## Background

El proyecto requiere un manejo de errores tipo-safe sin excepciones como control de flujo. Las excepciones en Kotlin ocultan paths de error, hacen el código difícil de razonar y no forzan al caller a manejar el caso de error.

## Problem

¿Cómo representamos errores de dominio (nombre vacío, personaje no encontrado, sistema inválido) de forma que el compilador fuerce el manejo?

## Questions & Answers

**Q**: ¿Usamos `Result<T>` de Kotlin o implementamos `Either<L,R>`?
**A**: `Either<L,R>` para dominio interno. `Result<T>` solo en boundaries de plugins. `Either` permite tipar el error de dominio (`DomainError`) en vez de `Throwable`. No requiere `try/catch`.

**Q**: ¿`Either` se mapea fácilmente a `Result`?
**A**: Sí. `Result<T>.toEither()` convierte `Result.success()` → `Either.Right` y `Result.failure()` → `Either.Left(ValidationFailed(...))`.

## Design

### `Either<L, R>` — Sealed Interface

```kotlin
sealed interface Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>
    data class Right<out R>(val value: R) : Either<Nothing, R>
}
```

### Extensiones (inline para no boxing)

```kotlin
inline fun <L, R, T> Either<L, R>.map(transform: (R) -> T): Either<L, T>
inline fun <L, R, T> Either<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T>
fun <T> Result<T>.toEither(): Either<DomainError, T>
```

### `DomainError` — Sealed Class

```kotlin
sealed class DomainError(val message: String) {
    class CharacterNotFound(val id: CharacterId) : DomainError("Character not found: ${id.value}")
    class InvalidSystem(val systemId: SystemId) : DomainError("Invalid system: ${systemId.value}")
    class ValidationFailed(message: String) : DomainError(message)
    class EmptyCharacterName(val attemptedName: String) : DomainError("Character name cannot be empty")
}
```

## Examples

### ✅ Chaining con flatMap

```kotlin
Character.create(name, systemId)           // Either<DomainError, Character>
    .flatMap { character ->
        repository.save(character)         // Either<DomainError, CharacterId>
            .map { character }             // devolvemos la entidad
    }
```

### ✅ Result a Either (boundary plugin)

```kotlin
val sheetResult = plugin.createEmptySheet() // Result<GameCharacterSheet>
if (sheetResult.isFailure) {
    return Either.Left(
        DomainError.ValidationFailed("Failed to create sheet: ${...}")
    )
}
val sheet = sheetResult.getOrThrow()
```

### ❌ Excepciones como control de flujo

```kotlin
// NO. El compilador no fuerza manejo. Stack trace innecesario.
fun compute(x: Int): Int {
    if (x < 0) throw IllegalArgumentException()
    return x * 2
}
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Type-safe: el compilador fuerza manejo de error | Más verbose que excepciones |
| Composable: `map`/`flatMap` encadenan fluídamente | Nuevos devs necesitan aprender el patrón |
| Facil de testear: asserts directos sobre `Left`/`Right` | No stack trace (a veces se quiere para debugging) |

## Implementation Plan

- [x] Crear `Either<L, R>` sealed interface con `Left`/`Right`
- [x] Implementar `map()` y `flatMap()` como inline functions
- [x] Crear `DomainError` como sealed class con 4 variantes
- [x] Implementar `Result<T>.toEither()`
- [x] Tests unitarios: `AttributeValueTest.kt` cubre `map`, `flatMap`, short-circuit

## Implementation Results

- Archivos: `core/domain/src/main/java/com/ddsheet/core/domain/model/Error.kt`
- Tests: `core/domain/src/test/java/com/ddsheet/core/domain/model/AttributeValueTest.kt` (líneas 89-120)
- Tests pasando: 4/4 (`map transform`, `map preserve left`, `flatMap chains`, `flatMap shortcircuits`)
- Desviaciones: Ninguna.

# Design Log #05 — GetCharacterSheetUseCase

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Domain Use Cases
- **Módulos afectados**: `:core:domain`

---

## Background

El caso de uso de lectura de hoja es más complejo que el de creación. Requiere: buscar el personaje en el repositorio, localizar el plugin correspondiente al sistema, crear la hoja vacía, calcular stats derivados y devolver la entidad + hoja enriquecida.

## Problem

¿Cómo orquestamos repositorio + plugin sin que el caso de use dependa de Android o de implementaciones específicas de sistemas?

## Questions & Answers

**Q**: ¿El caso de uso conoce todos los plugins?
**A**: No. Recibe un `pluginProvider: (SystemId) -> GameSystemPlugin?`. El registro de plugins vive en la capa de aplicación (Hilt module). El dominio solo conoce la función.

**Q**: ¿Qué pasa si el sistema no tiene plugin registrado?
**A**: Retorna `Either.Left(DomainError.InvalidSystem(systemId))`.

**Q**: ¿Por qué `createEmptySheet()` retorna `Result` y no `Either`?
**A**: El plugin es un boundary externo. `Result` es más natural para implementaciones de plugin. El caso de use hace la conversión a `Either<DomainError, T>`.

## Design

### `GetCharacterSheetUseCase`

```kotlin
class GetCharacterSheetUseCase(
    private val repository: CharacterRepository,
    private val pluginProvider: (SystemId) -> GameSystemPlugin?
) {
    suspend operator fun invoke(
        id: CharacterId
    ): Either<DomainError, Pair<Character, GameCharacterSheet>> {
        return repository.findById(id).flatMap { character ->
            val plugin = pluginProvider(character.systemId)
                ?: return Either.Left(
                    DomainError.InvalidSystem(character.systemId)
                )

            val sheetResult = plugin.createEmptySheet()
            if (sheetResult.isFailure) {
                return Either.Left(
                    DomainError.ValidationFailed("Failed to create sheet: ${sheetResult.exceptionOrNull()?.message}")
                )
            }

            val derived = plugin.computeDerivedStats(sheetResult.getOrThrow())
            Either.Right(character to derived)
        }
    }
}
```

## Examples

### ✅ Happy path

```kotlin
val result = useCase.invoke(CharacterId.generate())
// result is Either.Right(Character to GameCharacterSheet)
```

### ❌ Plugin no encontrado

```kotlin
// pluginProvider("unknown-system") returns null
// result is Either.Left(InvalidSystem(SystemId("unknown-system")))
```

### ❌ Plugin falla al crear sheet

```kotlin
// plugin.createEmptySheet() returns Result.failure(...)
// result is Either.Left(ValidationFailed("Failed to create sheet: ..."))
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Plugin provider inyectado: testable | `pluginProvider` como lambda pura puede ser difícil de rastrear en Hilt |
| Convierte Result→Either en boundary | `Pair<Character, GameCharacterSheet>` puede ser engorroso; en futuro podría ser una data class |

## Implementation Plan

- [x] Crear `GetCharacterSheetUseCase` con `repository` + `pluginProvider`
- [x] Manejar caso plugin no encontrado → `InvalidSystem`
- [x] Manejar caso plugin falla → `ValidationFailed`
- [ ] Tests unitarios con FakeRepository + FakePlugin (pendiente)

## Implementation Results

- Archivo: `core/domain/src/main/java/com/ddsheet/core/domain/usecase/GetCharacterSheetUseCase.kt`
- Tests: Pendientes (requieren FakePlugin)
- Desviaciones: Ninguna.

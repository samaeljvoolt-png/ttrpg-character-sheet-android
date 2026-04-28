# Design Log #04 — CreateCharacterUseCase

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Domain Use Cases
- **Módulos afectados**: `:core:domain`

---

## Background

Crear un personaje es el primer paso del flujo de la app. Necesitamos validar el nombre (no vacío), generar un ID único, asociar el sistema, persistir y devolver la entidad creada. Todo esto debe ser testeable sin necesidad de Room, Android, o UI.

## Problem

¿Cómo diseñamos el caso de uso de creación para que sea puro (sin Android), atómico y testeable con un fake de una línea?

## Questions & Answers

**Q**: ¿El caso de uso genera el ID o lo recibe?
**A**: El caso de uso recibe `name` y `systemId` como strings primitivos. `Character.create()` genera el `CharacterId` internamente y valida el nombre. Esto encapsula las reglas de creación en la entidad.

**Q**: ¿Qué pasa si el repositorio falla al persistir?
**A**: El caso de uso retorna `Either.Left` con el error del repositorio. El caller (ViewModel) decide cómo mostrarlo al usuario.

**Q**: ¿Por qué no usamos `Result<T>` aquí?
**A**: Por consistencia con el resto del dominio. `Either<DomainError, Character>` permite tipar el error exacto. `Result<T>` retorna `Throwable` genérico.

## Design

### `CreateCharacterUseCase`

```kotlin
class CreateCharacterUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(
        name: String,
        systemId: SystemId
    ): Either<DomainError, Character> {
        return Character.create(name, systemId).flatMap { character ->
            repository.save(character)
                .map { character } // devolvemos la entidad, no solo el ID
        }
    }
}
```

### Flujo

```
invoke(name, systemId)
  → Character.create(name, systemId)     // Either<DomainError, Character>
  → if Right: repository.save(character) // Either<DomainError, CharacterId>
  → if Right: map { character }          // Either<DomainError, Character>
```

## Examples

### ✅ Happy path

```kotlin
val result = useCase.invoke("Gimli", SystemId("dnd5e"))
// result is Either.Right(Character(name="Gimli", systemId="dnd5e", ...))
```

### ✅ Empty name (no persiste)

```kotlin
val result = useCase.invoke("", SystemId("dnd5e"))
// result is Either.Left(EmptyCharacterName(""))
// repository.findAll() == []
```

### ✅ Repository failure

```kotlin
repository.failNextSave = true
val result = useCase.invoke("Aragorn", SystemId("dnd5e"))
// result is Either.Left(ValidationFailed("Simulated save failure"))
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Puro: testable con FakeRepository | Suspend function requiere `runTest` en tests |
| Chain limpio con `flatMap` | `operator fun invoke` puede confundir a devs nuevos |
| No expone `CharacterId` al caller (encapsulación) | — |

## Implementation Plan

- [x] Crear `CreateCharacterUseCase` con `operator fun invoke`
- [x] Usar `Character.create()` para validación + generación de ID
- [x] Chain con `flatMap` → `repository.save` → `map`
- [x] Tests con `FakeCharacterRepository`: happy path, empty name, repo failure, system separation

## Implementation Results

- Archivo: `core/domain/src/main/java/com/ddsheet/core/domain/usecase/CreateCharacterUseCase.kt`
- Tests: `core/domain/src/test/java/com/ddsheet/core/domain/usecase/CreateCharacterUseCaseTest.kt`
- Tests pasando: 4/4
- Desviaciones: Ninguna.

# Design Log #02 — GameSystem Plugin Interface

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Plugin System
- **Módulos afectados**: `:core:domain`

---

## Background

El proyecto debe soportar múltiples sistemas de TTRPG (D&D 5e, Pathfinder, Call of Cthulhu, etc.). Cada sistema tiene reglas diferentes para atributos, progresión, cálculo de stats derivados y validación. En vez de if/else por sistema en todo el código, usamos un plugin system.

## Problem

¿Cómo diseñamos la interfaz de plugin para que sea lo suficientemente flexible para diferentes sistemas sin caer en generics salvajes o reflection?

## Questions & Answers

**Q**: ¿El plugin retorna tipos específicos de sistema o tipos genéricos?
**A**: Tipos genéricos del domain (`GameCharacterSheet`, `AttributeValue`). El plugin traduce entre el modelo específico del sistema (ej: `Dnd5eAbility.SCORE_STRENGTH`) y el modelo genérico del domain (`AttributeKey`, `AttributeValue`).

**Q**: ¿`createEmptySheet()` retorna `Result` o `Either`?
**A**: `Result<GameCharacterSheet>`. El plugin es un boundary externo desde la perspectiva del dominio. El caller (`GetCharacterSheetUseCase`) lo convierte a `Either<DomainError, T>`.

**Q**: ¿`computeDerivedStats` modifica la sheet o retorna una nueva?
**A**: Retorna una nueva `GameCharacterSheet` inmutable. La sheet es un value object.

## Design

### `GameSystemPlugin` — Interface

```kotlin
interface GameSystemPlugin {
    /**
     * Crea hoja vacía con defaults del sistema.
     * @return Result con sheet inicializada o fallo del plugin.
     */
    fun createEmptySheet(): Result<GameCharacterSheet>

    /**
     * Valida hoja según reglas del sistema.
     */
    fun validateSheet(sheet: GameCharacterSheet): ValidationResult

    /**
     * Calcula stats derivados (HP, AC, etc.) y retorna sheet nueva.
     */
    fun computeDerivedStats(sheet: GameCharacterSheet): GameCharacterSheet

    /**
     * Retorna lista de niveles disponibles en el sistema.
     */
    fun availableLevels(): List<Int>
}
```

### `ValidationResult`

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    data class ValidationError(
        val attribute: AttributeKey,
        val message: String
    )
}
```

## Examples

### ✅ Plugin provider en use case

```kotlin
class GetCharacterSheetUseCase(
    private val repository: CharacterRepository,
    private val pluginProvider: (SystemId) -> GameSystemPlugin?
) {
    suspend operator fun invoke(id: CharacterId): Either<DomainError, Pair<Character, GameCharacterSheet>> {
        return repository.findById(id).flatMap { character ->
            val plugin = pluginProvider(character.systemId)
                ?: return Either.Left(DomainError.InvalidSystem(character.systemId))

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

### ❌ Plugin con tipos específicos en la interfaz

```kotlin
// NO. Rompe el propósito del plugin system.
interface GameSystemPlugin {
    fun getStrengthScore(): Dnd5eAbilityScore  // <- acopla a D&D 5e
}
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Desacopla sistema de dominio | Plugin provider requiere registry/discovery |
| Fácil añadir nuevos sistemas | `AttributeValue` genérico pierde type-safety en el plugin |
| Testeable: FakePlugin en tests | Traducción attribute key ↔ sistema puede ser verbosa |

## Implementation Plan

- [x] Crear `GameSystemPlugin` interface con 4 métodos
- [x] Crear `ValidationResult` y `ValidationError`
- [x] Crear `GetCharacterSheetUseCase` que orquesta repo + plugin
- [ ] Implementar `Dnd5ePlugin` en `:domain:dnd5e` (pendiente)

## Implementation Results

- Archivos: `core/domain/src/main/java/com/ddsheet/core/domain/model/Plugin.kt`, `GetCharacterSheetUseCase.kt`
- Tests: `CreateCharacterUseCaseTest.kt` cubre creación con diferentes `SystemId` (separación de sistemas)
- Tests pasando: 14/14 (suite completa de domain)
- Desviaciones: Ninguna. El diseño se implementó tal cual.

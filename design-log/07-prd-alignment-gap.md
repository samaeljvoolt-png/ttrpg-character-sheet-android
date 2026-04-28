# Design Log #07 — PRD Alignment Gap Analysis

- **Estado**: Approved (plan de migración)
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-28
- **Sistema afectado**: Domain Layer, Data Layer, Feature Layer
- **Módulos afectados**: `:core:domain`, `:core:data`, `:game-systems:*`, `:feature:*`

---

## Background

El código actual es un **spike inicial** (logs 01-05) con un modelo de dominio mínimo: `Character` con 5 campos, `GameSystemPlugin` con 4 métodos, y `CharacterRepository` con `suspend` + `Either`. El PRD (`PRD.md`, `DESIGN.md`, `ARCHITECTURE.md`) define un modelo de dominio mucho más rico: `Character` con 14+ campos anidados, `GameSystem` con cálculos de reglas, `Flow`-based repository, y un ecosistema de features completo.

## Problem

¿Cómo migramos del spike inicial al modelo completo del PRD sin romper los 14 tests existentes y sin perder las buenas decisiones del spike (Either, value objects, fakes)?

## Questions & Answers

**Q**: ¿Reescribimos todo o evolucionamos incrementalmente?
**A**: Evolución incremental. El spike tiene buenas decisiones que queremos preservar: `Either<L,R>`, `DomainError`, `CharacterId` como value class, `FakeCharacterRepository`. El PRD expande el modelo pero no invalida estas bases.

**Q**: ¿El modelo actual `Character` (5 campos) se reemplaza o coexiste?
**A**: Se reemplaza. El `Character` actual se convierte en `CharacterSummary` (para listas) y creamos el `Character` completo del PRD. Esto evita romper la semántica: "Character" debe significar el aggregate root completo.

**Q**: ¿`GameSystemPlugin` se fusiona con `GameSystem` del PRD?
**A**: Sí. `GameSystem` del PRD es más rico (`abilityModifier`, `proficiencyBonus`, `spellSaveDC`, `spellAttackBonus`, `passiveScore`). `GameSystemPlugin` se depreca. Los métodos `createEmptySheet`, `validateSheet`, `computeDerivedStats` pueden vivir como extension functions o como parte de un builder separado, no en la interfaz de reglas puras.

**Q**: ¿`Either` se mantiene o se migra a `Result`?
**A**: Se mantiene `Either` en dominio. El PRD usa `Result<Unit>` en boundaries (repository save/delete). Alineamos: `CharacterRepository` retorna `Flow<Character>` (observación) y `suspend` métodos con `Either<DomainError, Unit>` (operaciones). Esto combina lo mejor de ambos mundos: reactivo para lectura, type-safe para escritura.

**Q**: ¿El XML parser del importer va en `:feature:importer` o `:core:domain`?
**A**: `:feature:importer` (adapter). El dominio solo define el puerto `CharacterImporter`. La implementación `FantasyGroundsXmlImporter` vive en `:feature:importer` como adapter. Esto sigue Clean Architecture: el dominio no conoce XML.

## Design

### Fase 1: Expandir Domain Model (sin tocar tests existentes)

Crear nuevos archivos en `model/` con el modelo completo del PRD. El `Character.kt` actual se renombra a `CharacterSummary.kt` (o se deja como legacy hasta que se migren los tests).

### Fase 2: Evolucionar GameSystem

1. Crear `GameSystem.kt` en `gamesystem/` con los 5 métodos del PRD.
2. Implementar `DnD5eGameSystem` en módulo `:game-systems:dnd5e`.
3. Marcar `GameSystemPlugin` como `@Deprecated`.

### Fase 3: Evolucionar Repository

1. Evolucionar `CharacterRepository` para soportar `Flow`:
   ```kotlin
   interface CharacterRepository {
       fun getCharacter(id: CharacterId): Flow<Character>
       fun listCharacters(): Flow<List<CharacterSummary>>
       suspend fun saveCharacter(character: Character): Either<DomainError, Unit>
       suspend fun deleteCharacter(id: CharacterId): Either<DomainError, Unit>
   }
   ```
2. Actualizar `FakeCharacterRepository` para soportar `Flow` + nuevo modelo.

### Fase 4: Nuevos Use Cases

Implementar los 13 use cases del PRD (ver `ARCHITECTURE.md` sección 7).

### Fase 5: Data Layer

1. Crear `:core:data` módulo.
2. Definir Room entities normalizadas (no JSON monolítico como en el draft del log 06).
3. Mappers explícitos `DomainToPersistence` / `PersistenceToDomain`.
4. `CharacterRepositoryImpl`.

### Fase 6: Features

1. `:feature:importer` — `FantasyGroundsXmlImporter`, `RichTextParser`
2. `:feature:character-sheet` — tabs (Overview, Abilities, Skills, Features, Personality)
3. `:feature:combat` — HP tracker, death saves, rests
4. `:feature:spells` — slots, list, detail
5. `:feature:inventory` — items, encumbrance, currency

## Examples

### ✅ Fase 1: Character completo

```kotlin
data class Character(
    val id: CharacterId,
    val identity: CharacterIdentity,
    val abilities: Abilities,
    val classes: List<CharacterClass>,
    val skills: List<Skill>,
    val combat: CombatStats,
    val spellBook: SpellBook,
    val inventory: Inventory,
    val features: List<CharacterFeature>,
    val traits: List<CharacterTrait>,
    val proficiencies: Proficiencies,
    val personality: Personality,
    val senses: Senses,
    val notes: String,
    val gameSystemId: String,
)
```

### ✅ Fase 2: DnD5eGameSystem

```kotlin
class DnD5eGameSystem : GameSystem {
    override val id = "dnd5e"
    override val name = "Dungeons & Dragons 5th Edition"

    override fun abilityModifier(score: Int): Int =
        floor((score - 10) / 2.0).toInt()

    override fun proficiencyBonus(level: Int): Int =
        ceil(level / 4.0).toInt() + 1

    override fun spellSaveDC(abilityMod: Int, profBonus: Int): Int =
        8 + profBonus + abilityMod

    override fun spellAttackBonus(abilityMod: Int, profBonus: Int): Int =
        profBonus + abilityMod

    override fun passiveScore(skillTotal: Int): Int =
        10 + skillTotal
}
```

### ❌ No: romper tests existentes antes de tener reemplazo

```kotlin
// NO. No borrar Character.kt (5 campos) hasta que
// CharacterSummary.kt exista y los tests estén migrados.
```

## Trade-offs

| A favor | En contra |
|---------|-----------|
| Preserva Either + value objects del spike | Modelo grande = más boilerplate en mappers |
| Incremental: tests siempre verdes | Fase 3 (Repository Flow) requiere actualizar todos los tests |
| GameSystem separado del importer | Dos abstracciones de sistema (GameSystem vs GameSystemPlugin) durante transición |

## Implementation Plan

- [x] **Fase 1**: Crear domain model completo (14+ archivos en `model/`)
- [ ] **Fase 2**: Crear `GameSystem` interface + `DnD5eGameSystem`
- [ ] **Fase 3**: Renombrar `Character` actual → `CharacterSummary`; crear `Character` nuevo
- [ ] **Fase 4**: Evolucionar `CharacterRepository` a Flow + Either
- [ ] **Fase 5**: Implementar 13 use cases del PRD
- [ ] **Fase 6**: Crear `:core:data` con Room entities + mappers
- [ ] **Fase 7**: Crear `:feature:importer` con XML parser
- [ ] **Fase 8**: Crear `:feature:character-sheet`, `:feature:combat`, `:feature:spells`
- [ ] **Fase 9**: Crear `:feature:inventory`
- [ ] **Fase 10**: Hilt setup + `:app` shell

## Implementation Results

- Pendiente. Este log es el plan maestro de migración post-PRD.

---

*Plan maestro de migración del spike inicial al modelo completo del PRD.*

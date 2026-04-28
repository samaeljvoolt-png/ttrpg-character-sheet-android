# Design Log #00 — Project Context & Architecture

- **Estado**: Living document (contexto base, actualizable)
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-27
- **Sistema afectado**: Todos
- **Módulos afectados**: Todos

---

## Background

Proyecto Android Kotlin para gestionar hojas de personaje de TTRPG (empezando con D&D 5e), con importación desde Fantasy Grounds XML y diseño extensible a otros sistemas (Pathfinder 2e, Call of Cthulhu, etc.) mediante sistema de plugins.

**Repo**: https://github.com/samaeljvoolt-png/ttrpg-character-sheet-android

**Docs oficiales del proyecto** (guardados en root del repo):
- `PRD.md` — Product Requirements Document completo
- `DESIGN.md` — Domain Model Reference (todas las data classes)
- `ARCHITECTURE.md` — Spec técnico de módulos, dependencias, features

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.x |
| UI | Jetpack Compose + Navigation Compose |
| Persistencia | Room |
| DI | Hilt |
| Testing | JUnit 5 + AssertJ + Turbine + Paparazzi |
| XML Parsing | JDK built-in (sin 3rd party) |

## Arquitectura

**Clean Architecture + Hexagonal / Ports & Adapters**

```
:app                        → thin shell, DI setup, navigation graph
:core:domain                → entities, use cases, repository interfaces (pure Kotlin)
:core:data                  → repository implementations, Room, mappers
:core:ui-components         → shared Compose design system
:feature:character-sheet    → Sheet overview, tabs
:feature:combat             → HP tracker, initiative, AC, death saves
:feature:spells             → Spell list, slot tracker, spell detail
:feature:inventory          → Items, encumbrance, currency
:feature:importer           → XML parsing, import flow
:game-systems:dnd5e         → D&D 5e specific rules
:game-systems:pathfinder2e  → Future
```

### Dependency Rules

```
:feature:* → :core:domain         ✅
:feature:* → :core:ui-components  ✅
:feature:* → :game-systems:*      ✅ (via abstraction)
:core:data → :core:domain         ✅
:core:domain → (nothing Android)  ✅
:game-systems:* → :core:domain    ✅
:core:domain → :game-systems:*    ❌ FORBIDDEN
```

---

## ⚠️ Discrepancia Crítica: Package Name

| Espec (PRD) | Código actual |
|-------------|--------------|
| `com.rpgcompanion` | `com.ddsheet` |

**Decisión**: Mantener `com.ddsheet` en el código actual. Migración a `com.rpgcompanion` es non-breaking y se puede hacer más adelante con refactor automático de Android Studio. Los design logs referencian el package real del código (`com.ddsheet`).

---

## ⚠️ Discrepancia Crítica: Modelo de Dominio

| Espec (PRD/DESIGN) | Código actual |
|-------------------|--------------|
| `Character` con 14+ campos (identity, abilities, classes, skills, combat, spellBook, inventory, features, traits, proficiencies, personality, senses, notes, gameSystemId) | `Character` con 5 campos (id, name, systemId, createdAt, updatedAt) |
| `GameSystem` interface con cálculos de reglas | `GameSystemPlugin` interface con `createEmptySheet`, `validateSheet`, `computeDerivedStats` |
| `CharacterRepository` con `Flow<Character>`, `Flow<List<CharacterSummary>>` | `CharacterRepository` con `suspend` + `Either<DomainError, T>` |

**Decisión**: El código actual es un **spike inicial** (logs 01-05). El PRD define el **target state**. La migración requiere:
1. Expandir `Character` a la estructura completa del PRD (o mantener el modelo actual como `CharacterSummary` y crear `Character` nuevo)
2. Evolucionar `GameSystemPlugin` → `GameSystem` o alinearlos
3. Evolucionar `CharacterRepository` para soportar `Flow` (reactive) + `Either`

Ver `07-prd-alignment-gap.md` para plan de migración detallado.

---

## Estado Actual del Código

### Implementado ✓ (Spike Inicial)

- `:core:domain` con modelo básico:
  - `Character` (5 campos), `CharacterId`, `SystemId`, `NonEmptyString`
  - `Either<L,R>` con `map`, `flatMap`, `toEither()`
  - `DomainError` sealed class
  - `GameSystemPlugin` interface (4 métodos)
  - `GameCharacterSheet` con `AttributeKey`, `AttributeValue` (TextValue, NumberValue, BooleanValue, ListValue)
  - `CharacterRepository` puerto (4 métodos, suspend + Either)
  - `CreateCharacterUseCase` y `GetCharacterSheetUseCase`
  - `FakeCharacterRepository` para tests
  - **14 tests pasando**

### Pendiente ○ (según PRD)

| Prioridad | Feature | Módulo |
|-----------|---------|--------|
| P0 | Expandir domain model a estructura PRD | `:core:domain` |
| P0 | Implementar `GameSystem` interface + `DnD5eGameSystem` | `:game-systems:dnd5e` |
| P0 | Room entities + DAOs + mappers | `:core:data` |
| P0 | Fantasy Grounds XML importer | `:feature:importer` |
| P0 | Character Sheet UI (tabs) | `:feature:character-sheet` |
| P0 | Combat tracker | `:feature:combat` |
| P0 | Spell manager | `:feature:spells` |
| P1 | Inventory manager | `:feature:inventory` |
| P1 | Hilt setup + navigation | `:app` |
| P1 | UI components design system | `:core:ui-components` |

---

## Terminología Core

| Término | Definición | Ubicación |
|---------|-----------|-----------|
| `Character` | Entidad raíz. Estructura completa PRD. | `core:domain` |
| `CharacterId` | `@JvmInline value class` UUID. | `core:domain` |
| `CharacterSummary` | Vista reducida para listas (name, level, class). | `core:domain` |
| `GameSystem` | Puerto de reglas del sistema. `abilityModifier`, `proficiencyBonus`, etc. | `core:domain` |
| `GameSystemPlugin` | **LEGACY** — interfaz anterior. Será deprecada o fusionada con `GameSystem`. | `core:domain` |
| `Either<L,R>` | `Left`/`Right` con `map`, `flatMap`. | `core:domain` |
| `DomainError` | Sealed class de errores de dominio. | `core:domain` |
| `CharacterRepository` | Puerto de persistencia. Target: `Flow` + reactive. | `core:domain` |
| `ResourceTracker` | Seguimiento de usos limitados (Channel Divinity, Rage, etc.). | `core:domain` |
| `RechargeType` | `SHORT_REST`, `LONG_REST`, `DAWN`, `MANUALLY`, `NEVER`. | `core:domain` |
| `CarriedState` | `ON_PERSON`, `EQUIPPED`, `STORED`. | `core:domain` |
| `DiceExpression` | `count`, `sides`, `bonus`. | `core:domain` |

---

## Estructura de archivos (domain — actual)

```
core/domain/src/main/java/com/ddsheet/core/domain/
├── model/
│   ├── Character.kt          # Character básico (spike)
│   ├── Error.kt              # DomainError, Either<L,R>
│   └── Plugin.kt             # GameSystemPlugin, GameCharacterSheet, AttributeValue
core/domain/src/test/java/com/ddsheet/core/domain/
├── fake/
│   └── FakeCharacterRepository.kt
├── model/
│   ├── CharacterTest.kt
│   └── AttributeValueTest.kt
└── usecase/
    └── CreateCharacterUseCaseTest.kt
```

## Estructura target (según PRD)

```
core/domain/src/main/java/com/ddsheet/core/domain/
├── model/
│   ├── Character.kt           # Character aggregate root (14+ campos)
│   ├── CharacterIdentity.kt   # name, race, background, etc.
│   ├── Abilities.kt           # 6 abilities + modifiers
│   ├── CharacterClass.kt      # name, level, hitDie, casterType
│   ├── CombatStats.kt         # AC, HP, initiative, death saves
│   ├── SpellBook.kt           # SpellSlot, Spell, SpellAction
│   ├── Inventory.kt           # InventoryItem, Currency, Encumbrance
│   ├── Skill.kt               # 18 skills
│   ├── Proficiencies.kt       # armor, weapons, tools, languages
│   ├── Personality.kt         # traits, ideals, bonds, flaws
│   ├── Senses.kt              # passive perception, darkvision
│   ├── ResourceTracker.kt     # limited-use abilities
│   ├── DiceExpression.kt      # 2d6+3
│   └── Error.kt               # DomainError, Either<L,R>
├── repository/
│   └── CharacterRepository.kt # Flow-based reactive port
├── usecase/
│   ├── ImportCharacterFromXmlUseCase.kt
│   ├── GetCharacterUseCase.kt
│   ├── ListCharactersUseCase.kt
│   ├── UpdateHitPointsUseCase.kt
│   ├── ExpendSpellSlotUseCase.kt
│   ├── ShortRestUseCase.kt
│   ├── LongRestUseCase.kt
│   └── ...
└── gamesystem/
    └── GameSystem.kt          # abilityModifier, proficiencyBonus, spellSaveDC, etc.
```

---

## Reglas del Proyecto

Ver `design-log-rules.md`, `core-personality.md`, `code-style-guide.md`.

**Documentos de spec oficiales**: `PRD.md`, `DESIGN.md`, `ARCHITECTURE.md` (root del repo).

---

Última actualización: 2026-04-28 (post-PRD alignment)

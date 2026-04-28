# RPG Companion — Architecture & Technical Specification

> **Status:** Draft — mantener sincronizado con PRD.md y DESIGN.md
> **Estructura de módulos:** Clean Architecture + Hexagonal / Ports & Adapters

---

## 1. Modules

```
:app                        → thin shell, DI setup, navigation graph
:core:domain                → entities, use cases, repository interfaces, NO Android deps
:core:data                  → repository implementations, local DB (Room), preferences
:core:ui-components         → shared Compose design system, theme, reusable widgets
:feature:character-sheet    → Sheet overview screen, tabs
:feature:combat             → HP tracker, initiative, AC, death saves
:feature:spells             → Spell list, slot tracker, spell detail
:feature:inventory          → Items list, encumbrance, currency
:feature:importer           → XML parsing, import flow
:feature:character-builder  → Manual creation flow (future)
:game-systems:dnd5e         → D&D 5e specific rules (ability mod calc, proficiency, etc.)
:game-systems:pathfinder2e  → Future
```

---

## 2. Dependency Rules (Clean Architecture)

```
:feature:* → :core:domain         ✅
:feature:* → :core:ui-components  ✅
:feature:* → :game-systems:*      ✅ (via abstraction)
:core:data → :core:domain         ✅
:core:domain → (nothing Android)  ✅ — pure Kotlin
:game-systems:* → :core:domain    ✅
:core:domain → :game-systems:*    ❌ FORBIDDEN — domain must not know specific games
```

---

## 3. Key Abstractions (Hexagonal)

### CharacterRepository (Port — :core:domain)

```kotlin
interface CharacterRepository {
    fun getCharacter(id: CharacterId): Flow<Character>
    fun saveCharacter(character: Character): Result<Unit>
    fun listCharacters(): Flow<List<CharacterSummary>>
    fun deleteCharacter(id: CharacterId): Result<Unit>
}
```

### CharacterImporter (Port — :core:domain)

```kotlin
interface CharacterImporter {
    fun canImport(source: ImportSource): Boolean
    fun import(source: ImportSource): Result<Character>
}
```

### GameSystem (Port — :core:domain)

```kotlin
interface GameSystem {
    val id: String         // "dnd5e", "pf2e", etc.
    val name: String
    fun abilityModifier(score: Int): Int
    fun proficiencyBonus(level: Int): Int
    fun spellSaveDC(abilityMod: Int, profBonus: Int): Int
    fun spellAttackBonus(abilityMod: Int, profBonus: Int): Int
    fun passiveScore(skillTotal: Int): Int
    // … extensible
}
```

### Adapter — FantasyGroundsXmlImporter (:feature:importer)

```kotlin
class FantasyGroundsXmlImporter(
    private val richTextParser: RichTextParser,
    private val gameSystem: GameSystem,
) : CharacterImporter {

    override fun canImport(source: ImportSource): Boolean
    override fun import(source: ImportSource): Result<Character>

    // Internal — each section is its own private fun
    private fun parseAbilities(node: Node): Abilities
    private fun parseClasses(node: Node): List<CharacterClass>
    private fun parseSpells(powersNode: Node, powerMetaNode: Node): SpellBook
    private fun parseInventory(node: Node): Inventory
    // … one private fun per domain section
}
```

Each `parseXxx` function is:
- **Pure** (no side effects)
- **Unit testable** in isolation
- Returns a domain object, not an XML node

---

## 4. Data Layer

- **Room** for local persistence.
- One `CharacterEntity` table + related tables (abilities, skills, items, spells, etc.) in normalized form.
- Mappers: `DomainToPersistence` and `PersistenceToDomain` — explicit, pure functions.
- No business logic in DAO or Entity classes.

---

## 5. UI Layer

- **Jetpack Compose** exclusively.
- **StateFlow** + **ViewModel** per screen.
- UI state is a single sealed class per screen (`UiState`).
- No business logic in composables — they are pure render functions.
- Navigation: **Navigation Compose** with typed routes.

---

## 6. Feature Specifications

### F-001 — Character Import (Fantasy Grounds XML)

**Priority:** P0
**Module:** `:feature:importer`

#### Acceptance Criteria

1. User can pick an `.xml` file from device storage.
2. The importer validates the file is a recognized FG format (`root/@release` contains `CoreRPG`).
3. All fields listed in PRD section 3 are parsed without data loss.
4. Rich text (`formattedtext`) is converted to `AnnotatedString` (Compose) via a `RichTextParser`.
5. If a field is missing in the XML, a sensible default is used and no crash occurs.
6. Import result is shown as a summary screen before saving.
7. If the same character (by name) already exists, user is prompted to overwrite or create a copy.

---

### F-002 — Character Sheet View

**Priority:** P0
**Module:** `:feature:character-sheet`

Tabbed interface:

| Tab | Contents |
|-----|----------|
| Overview | Name, race, class/level, HP widget, AC, speed, initiative, proficiency bonus, inspiration |
| Abilities | Six ability scores with modifiers, saving throws |
| Skills | 18 skills with proficiency indicators, passive perception |
| Features | Class features, racial traits, background feature, feats |
| Personality | Traits, ideals, bonds, flaws, notes |

All numeric values are **read-only by default**; an Edit Mode toggle unlocks inline editing.

---

### F-003 — Combat Tracker

**Priority:** P0
**Module:** `:feature:combat`

| Widget | Behavior |
|--------|----------|
| HP Bar | Tap to take damage or heal; shows current/max/temp |
| Death Saves | Toggle successes & failures |
| Hit Dice | Track used/remaining per class |
| Class Resources | Channel Divinity, Rage, Ki, etc. via generic `ResourceTracker` |
| Short Rest | Restores short-rest resources, prompts hit dice spend |
| Long Rest | Restores all resources, HP, spell slots |

---

### F-004 — Spell Manager

**Priority:** P0
**Module:** `:feature:spells`

| Feature | Notes |
|---------|-------|
| Spell Slots Grid | Shows levels 1–9, max/used/remaining, tap to expend/restore |
| Cantrips | Separate section, always available |
| Spell List | Grouped by level, filterable by prepared/all/ritual |
| Spell Detail Sheet | Full description, components, upcast info |
| Prepared Toggle | Mark/unmark prepared spells |
| Domain Spells | Always-prepared indicator |
| Pact Magic | Shown if Warlock class present |

---

### F-005 — Inventory Manager

**Priority:** P1
**Module:** `:feature:inventory`

| Feature | Notes |
|---------|-------|
| Item List | Grouped by type, show count and weight |
| Currency | PP/GP/EP/SP/CP display and edit |
| Encumbrance Bar | Visual indicator of load vs. thresholds |
| Item Detail | Full description, properties, rarity |
| Unidentified Items | Show nonid_name until identified toggle is set |
| Attunement | Count of attuned items (max 3 per 5e rules) |

---

### F-006 — Proficiencies & Languages

**Priority:** P1
**Module:** part of `:feature:character-sheet`

Simple read-only list view, grouped by armor / weapons / tools / languages.

---

### F-007 — Game System Plugin Interface

**Priority:** P1
**Module:** `:game-systems:dnd5e` (first implementation)

Every rule calculation must go through `GameSystem`:

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

    override fun passiveScore(skillTotal: Int): Int =
        10 + skillTotal
}
```

Future game systems drop in as new modules without touching existing code.

---

## 7. Use Cases

| Use Case | Description |
|----------|-------------|
| `ImportCharacterFromXmlUseCase` | Orchestrates `CharacterImporter` → validate → save |
| `GetCharacterUseCase` | Returns `Flow<Character>` by id |
| `ListCharactersUseCase` | All characters as `Flow<List<CharacterSummary>>` |
| `UpdateHitPointsUseCase` | Applies damage or healing; validates bounds |
| `ExpendSpellSlotUseCase` | Decrements used count; validates availability |
| `RestoreSpellSlotsUseCase` | Resets slots on long rest |
| `ToggleSpellPreparedUseCase` | Flips prepared flag; respects always-prepared |
| `ExpendResourceUseCase` | Generic for any `ResourceTracker` |
| `ShortRestUseCase` | Restores short-rest resources |
| `LongRestUseCase` | Restores all resources, HP, slots |
| `UpdateInventoryItemUseCase` | Edit count, carried state, identified flag |
| `DeleteCharacterUseCase` | Removes character and all related data |
| `ExportCharacterUseCase` | Serialises to JSON (future) |

---

## 8. Testing Strategy

| Layer | Test Type | Tool |
|-------|-----------|------|
| Domain entities / use cases | Unit tests | JUnit 5 + AssertJ |
| XML Parser | Unit tests with real XML fixtures | JUnit 5 |
| Game system rules | Unit + property-based | Kotest |
| Repository implementations | Integration tests | Room in-memory + Kotlin Coroutines test |
| ViewModels | Unit tests with fakes | JUnit 5 + Turbine |
| UI Composables | Screenshot / snapshot | Paparazzi |
| Import flow E2E | Instrumented test | Espresso + real FG XML |

**Mutation testing**: PITest on `:core:domain` and `:game-systems:dnd5e`.

Test directory structure mirrors source: one test class per production class.

---

## 9. Technical Constraints & Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.x | Language |
| Jetpack Compose BOM | Latest stable | UI |
| Room | Latest stable | Local DB |
| Hilt | Latest stable | DI |
| Navigation Compose | Latest stable | Navigation |
| Kotlinx.serialization | Latest stable | JSON for future export |
| Kotlin Coroutines | Latest stable | Async |
| Android XML (stdlib) | JDK built-in | FG XML parsing — no 3rd party |
| JUnit 5 | Latest stable | Unit tests |
| Turbine | Latest stable | Flow testing |
| Paparazzi | Latest stable | Snapshot tests |

**No business logic dependencies on Android framework** — domain module is pure Kotlin.

---

## 10. File / Package Conventions

```
com.rpgcompanion
├── core
│   ├── domain
│   │   ├── model            ← All data classes above
│   │   ├── repository       ← Interfaces (ports)
│   │   ├── usecase          ← One file per use case
│   │   └── gamesystem       ← GameSystem interface
│   ├── data
│   │   ├── local
│   │   │   ├── dao
│   │   │   ├── entity
│   │   │   └── mapper
│   │   └── repository       ← Implementations (adapters)
│   └── ui
│       ├── theme
│       ├── components
│       └── navigation
├── feature
│   ├── charactersheet
│   ├── combat
│   ├── spells
│   ├── inventory
│   └── importer
│       ├── FantasyGroundsXmlImporter
│       ├── RichTextParser
│       └── ImportViewModel
└── gamesystem
    └── dnd5e
        ├── DnD5eGameSystem
        └── DnD5eRuleConstants
```

Naming rules (Clean Code):
- Classes: nouns, `PascalCase`
- Functions: verbs, `camelCase`, express intent
- No abbreviations except well-known acronyms (HP, AC, DC)
- No magic numbers — all constants in named objects
- Max function length: 20 lines
- Max class responsibility: one

---

## 11. Open Questions / Future Scope

| # | Item |
|---|------|
| OQ-1 | Cloud sync strategy (Firebase vs. self-hosted) — post v1 |
| OQ-2 | Dice roller integration — separate `:feature:dice` module |
| OQ-3 | GM screen / shared session — requires network layer design |
| OQ-4 | Pathfinder 2e import format — separate importer adapter |
| OQ-5 | PDF character sheet export |
| OQ-6 | Rich text editing (not just display) for descriptions |
| OQ-7 | Compendium lookup (spell references, item references) |
| OQ-8 | Widget / Quick Tile for HP tracking without opening app |

---

*End of Architecture Spec — v1.0*

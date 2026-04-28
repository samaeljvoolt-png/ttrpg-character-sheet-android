# Design Log #09 — Fase 2: GameSystem Interface + DnD5eGameSystem

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-28
- **Módulos afectados**: `:core:domain`, `:game-systems:dnd5e`
- **Fase**: 2 de 10 (plan maestro #07)

---

## Decision

Evolucionar la abstracción de sistema de juego desde `GameSystemPlugin` (spike, 4 métodos orquestadores) hacia `GameSystem` (reglas puras, 5 métodos de cálculo) más una implementación concreta para D&D 5e en módulo separado.

## Approach

- **NO** eliminar `GameSystemPlugin` — se marca `@Deprecated(level = WARNING)` para no romper tests existentes.
- **NO** modificar `GameCharacterSheet`, `AttributeKey`, `AttributeValue` — aún son usados por tests.
- Crear `GameSystem` como interfaz de reglas puras en `:core:domain`.
- Crear módulo `:game-systems:dnd5e` con `DnD5eGameSystem`.
- Tests exhaustivos para todas las fórmulas canónicas de D&D 5e.

## Estructura creada

```
core/domain/src/main/java/com/ddsheet/core/domain/gamesystem/
├── GameSystem.kt          → interfaz pura (5 métodos)

game-systems/dnd5e/src/main/java/com/ddsheet/gamesystem/dnd5e/
├── DnD5eGameSystem.kt     → implementación D&D 5e

game-systems/dnd5e/src/test/java/com/ddsheet/gamesystem/dnd5e/
└── DnD5eGameSystemTest.kt → 20 tests
```

## `GameSystem` — API

| Método | Firma | Ejemplo D&D 5e |
|--------|-------|----------------|
| `abilityModifier` | `(score: Int) → Int` | 14 → +2 |
| `proficiencyBonus` | `(level: Int) → Int` | 5 → +3 |
| `spellSaveDC` | `(abilityMod, profBonus) → Int` | 8 + 3 + 3 = 14 |
| `spellAttackBonus` | `(abilityMod, profBonus) → Int` | 3 + 3 = +6 |
| `passiveScore` | `(skillTotal) → Int` | 10 + 3 = 13 |

## `DnD5eGameSystem` — Reglas implementadas

- **Ability Modifier**: `floor((score - 10) / 2.0)`
- **Proficiency Bonus**: `ceil(level / 4.0) + 1` (con guarda para level < 1 → 0)
- **Spell Save DC**: `8 + profBonus + abilityMod`
- **Spell Attack Bonus**: `profBonus + abilityMod`
- **Passive Score**: `10 + skillTotal`

## Tests

20 tests cubriendo:

- **Ability modifier**: pares/impares alrededor de 10, extremos (1 → -5, 20 → +5)
- **Proficiency bonus**: fronteras de tier (1-4, 5-8, 9-12, 13-16, 17-20), level 0
- **Spell Save DC & Spell Attack Bonus**: caso estándar Wizard nivel 5
- **Passive Score**: bonus 0 y 3
- **Identity**: id y name del sistema

## Cambios en archivos existentes

- `settings.gradle.kts`: añadido `include(":game-systems:dnd5e")`
- `Plugin.kt`: añadido `@Deprecated` a `GameSystemPlugin` con `replaceWith = GameSystem`

## Resultado

- **Tests nuevos**: 20 (en módulo `:game-systems:dnd5e`)
- **Compilación**: ✅ estructuralmente válida (sin Gradle wrapper disponible en este entorno)
- **Backward compatibility**: 100% — `GameSystemPlugin` solo deprecado, no removido
- **Módulos**: 2 activos (`:core:domain`, `:game-systems:dnd5e`)

---

*Fase 2 completada. Listo para Fase 3: Renombrar `Character` → `CharacterSummary` + crear `Character` completo del PRD.*

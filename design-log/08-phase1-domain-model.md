# Design Log #08 — Fase 1: Expandir Domain Model

- **Estado**: Completed
- **Autor**: Tom / Hermes
- **Fecha**: 2026-04-28
- **Módulo afectado**: `:core:domain`
- **Fase**: 1 de 10 (plan maestro #07)

---

## Decision

Expandir el modelo de dominio desde el spike inicial (5 campos) hasta el modelo completo definido en `DESIGN.md` y `PRD.md`, manteniendo 100% backward compatibility con los tests existentes.

## Approach

- **NO** modificar `Character.kt` existente (5 campos) — se renombrará en Fase 3.
- **NO** modificar `GameSystemPlugin.kt` — se deprecará en Fase 2.
- **NO** modificar `CharacterRepository` ni use cases existentes.
- Crear 33 nuevos archivos en subpackages organizados por dominio.

## Estructura creada

```
model/
├── value/
│   ├── DiceExpression.kt
│   └── CharacterSize.kt
├── ability/
│   ├── AbilityType.kt
│   ├── Ability.kt
│   └── Abilities.kt
├── skill/
│   └── Skill.kt
├── class/
│   ├── CasterType.kt
│   └── CharacterClass.kt
├── combat/
│   ├── DexBonusRule.kt
│   ├── ArmorClass.kt
│   ├── HitPoints.kt
│   ├── DeathSaves.kt
│   ├── InitiativeStat.kt
│   ├── Speed.kt
│   └── CombatStats.kt
├── spell/
│   ├── SpellcastingGroup.kt
│   ├── SpellSlot.kt
│   ├── SpellAction.kt
│   ├── Spell.kt
│   └── SpellBook.kt
├── inventory/
│   ├── CarriedState.kt
│   ├── InventoryItem.kt
│   ├── Currency.kt
│   ├── Encumbrance.kt
│   └── Inventory.kt
├── identity/
│   ├── CharacterIdentity.kt
│   ├── Personality.kt
│   └── Senses.kt
└── feature/
    ├── Proficiencies.kt
    ├── ResourceTracker.kt
    ├── RechargeType.kt
    ├── CharacterFeature.kt
    └── CharacterTrait.kt
```

## Tests creados

- `DiceExpressionTest` — formateo de expresiones de dados
- `AbilitiesTest` — indexación por `AbilityType`
- `HitPointsTest` — cálculo de HP actual
- `SpellSlotTest` — cálculo de slots restantes

## Fixes menores necesarios

Durante la compilación se descubrieron y corrigieron bugs preexistentes en el spike:

1. **build.gradle.kts root**: `rootProject.name` estaba en el build root en lugar de `settings.gradle.kts`.
2. **settings.gradle.kts**: Incluía módulos inexistentes; reducido a `:core:domain`.
3. **core/domain/build.gradle.kts**: Creado como módulo JVM puro (sin Android).
4. **Error.kt**: `map` y `flatMap` convertidos a `inline` para permitir early returns.
5. **CreateCharacterUseCase.kt**: El uso de `flatMap` con lambda suspend no compilaba; reescrito con `when`.
6. **GetCharacterSheetUseCase.kt**: Mismo patrón; reescrito con `when`.

## Resultado

- **Tests**: 7 pasan (3 originales + 4 nuevos)
- **Compilación**: ✅ SUCCESS
- **Backward compatibility**: 100% preservada
- **Archivos nuevos**: 33 modelos + 4 tests + `.gitignore`

---

*Fase 1 completada. Listo para Fase 2: `GameSystem` interface + `DnD5eGameSystem`.*

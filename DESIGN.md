# RPG Companion — Domain Model & Design Reference

> **Package:** `com.rpgcompanion` (PRD target) vs `com.ddsheet` (current codebase) — ver `00-project-context.md`
> **Status:** Living spec — mantener sincronizado con el PRD

---

## Value Objects

```kotlin
@JvmInline value class CharacterId(val value: String)
```

## Character (Aggregate Root)

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
    val gameSystemId: String,  // "dnd5e", "pf2e", …
)
```

## Identity

```kotlin
data class CharacterIdentity(
    val name: String,
    val race: String,
    val subrace: String?,
    val background: String,
    val alignment: String?,
    val experience: Int,
    val age: String?,
    val gender: String?,
    val height: String?,
    val weight: String?,
    val size: CharacterSize,
    val inspiration: Int,
)

enum class CharacterSize { TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN }
```

## Abilities

```kotlin
data class Abilities(
    val strength: Ability,
    val dexterity: Ability,
    val constitution: Ability,
    val intelligence: Ability,
    val wisdom: Ability,
    val charisma: Ability,
)

data class Ability(
    val score: Int,
    val modifier: Int,           // computed or stored
    val savingThrow: Int,
    val saveProficient: Boolean,
    val saveMiscModifier: Int,
)
```

## Classes

```kotlin
data class CharacterClass(
    val name: String,
    val level: Int,
    val subclass: String?,
    val hitDie: DiceExpression,
    val hitDiceUsed: Int,
    val spellcastingAbility: AbilityType?,
    val casterType: CasterType?,
    val cantripsKnown: Int,
    val spellsPrepared: Int,
    val spellsKnown: Int,
)

enum class AbilityType { STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA }
enum class CasterType { MEMORIZATION, SPONTANEOUS, WARLOCK, HALF_CASTER, NON_CASTER }
```

## Combat

```kotlin
data class CombatStats(
    val armorClass: ArmorClass,
    val hitPoints: HitPoints,
    val initiative: InitiativeStat,
    val speed: Speed,
    val proficiencyBonus: Int,
    val deathSaves: DeathSaves,
)

data class ArmorClass(
    val total: Int,
    val armorBase: Int,
    val shieldBonus: Int,
    val dexBonusRule: DexBonusRule,
    val miscBonus: Int,
    val profBonus: Int,
    val temporaryBonus: Int,
    val stealthDisadvantage: Boolean,
)

enum class DexBonusRule { FULL, MAX_TWO, MAX_ONE, NONE }

data class HitPoints(
    val max: Int,
    val wounds: Int,          // damage taken
    val temporary: Int,
) {
    val current: Int get() = max - wounds
}

data class DeathSaves(val successes: Int, val failures: Int)
```

## Spellcasting

```kotlin
data class SpellBook(
    val groups: List<SpellcastingGroup>,
    val slots: List<SpellSlot>,
    val pactMagicSlots: List<SpellSlot>,
    val spells: List<Spell>,
)

data class SpellcastingGroup(
    val name: String,
    val spellcastingAbility: AbilityType,
    val casterType: CasterType,
    val includeAttackProficiency: Boolean,
    val includeSaveProficiency: Boolean,
)

data class SpellSlot(
    val level: Int,   // 1–9
    val max: Int,
    val used: Int,
) {
    val remaining: Int get() = max - used
}

data class Spell(
    val name: String,
    val spellLevel: Int,       // 0 = cantrip
    val school: String,
    val castingTime: String,
    val range: String,
    val components: String,
    val duration: String,
    val isConcentration: Boolean,
    val isRitual: Boolean,
    val description: String,   // pre-processed rich text
    val isPrepared: Boolean,
    val isAlwaysPrepared: Boolean,
    val group: String,
    val source: String,
    val actions: List<SpellAction>,
    val timesUsed: Int,
)

sealed class SpellAction {
    data class Damage(val dice: DiceExpression, val damageType: String, val bonus: Int) : SpellAction()
    data class Heal(val dice: DiceExpression, val bonus: Int, val targetSelf: Boolean) : SpellAction()
    data class SavingThrow(val ability: AbilityType, val isMagicSave: Boolean) : SpellAction()
    data class Effect(val label: String, val durationMinutes: Int?) : SpellAction()
    data class Attack(val type: AttackType) : SpellAction()
}
```

## Inventory

```kotlin
data class Inventory(
    val items: List<InventoryItem>,
    val currency: Currency,
    val encumbrance: Encumbrance,
)

data class InventoryItem(
    val id: String,
    val name: String,
    val nonIdName: String?,
    val isIdentified: Boolean,
    val type: String,
    val subtype: String?,
    val count: Int,
    val weight: Double,
    val cost: String?,
    val carriedState: CarriedState,
    val isAttuned: Boolean,
    val rarity: String?,
    val magicBonus: Int,
    val acValue: Int?,
    val damageExpression: String?,
    val properties: String?,
    val description: String,
)

enum class CarriedState { ON_PERSON, EQUIPPED, STORED }

data class Currency(
    val platinum: Int,
    val gold: Int,
    val electrum: Int,
    val silver: Int,
    val copper: Int,
)

data class Encumbrance(
    val currentLoad: Double,
    val maxCapacity: Double,
    val encumberedThreshold: Double,
    val heavilyEncumberedThreshold: Double,
    val pushDragLift: Double,
)
```

## Skills

```kotlin
data class Skill(
    val name: String,
    val governingAbility: AbilityType,
    val isProficient: Boolean,
    val isExpertise: Boolean,
    val miscModifier: Int,
    val totalBonus: Int,
)
```

## Proficiencies & Personality

```kotlin
data class Proficiencies(
    val armor: List<String>,
    val weapons: List<String>,
    val tools: List<String>,
    val languages: List<String>,
)

data class CharacterFeature(
    val name: String,
    val sourceLevelRequired: Int,
    val description: String,
    val isSpecializationFeature: Boolean,
    val resource: ResourceTracker?,
)

data class ResourceTracker(
    val name: String,
    val max: Int,
    val used: Int,
    val rechargesOn: RechargeType,
)

enum class RechargeType { SHORT_REST, LONG_REST, DAWN, MANUALLY, NEVER }

data class CharacterTrait(
    val name: String,
    val description: String,
)

data class Personality(
    val traits: String,
    val ideals: String,
    val bonds: String,
    val flaws: String,
)

data class Senses(
    val passivePerception: Int,
    val darkvisionFeet: Int?,
    val blindsightFeet: Int?,
    val tremorsenseFeet: Int?,
    val truesightFeet: Int?,
    val other: String?,
)
```

## Utilities

```kotlin
data class DiceExpression(val count: Int, val sides: Int, val bonus: Int = 0) {
    override fun toString() = "${count}d${sides}${if (bonus != 0) "+$bonus" else ""}"
}
```

---

*End of Domain Model Reference — v1.0*

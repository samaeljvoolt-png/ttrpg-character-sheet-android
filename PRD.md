# RPG Companion — Product Requirements Document (PRD / SDD)
**Version:** 1.0
**Status:** Draft
**Target Platform:** Android (Kotlin · Jetpack Compose · Clean Architecture)
**Spec-Driven Development:** Feed directly to Google Project IDX / Android Studio Antigravity

---

## 0. Document Purpose

This document is both a **Product Requirements Document (PRD)** and a **Spec-Driven Design (SDD)**. Every section maps directly to Android modules, data models, or UI screens. It is intended to be consumed by AI-assisted code generation tooling.

Design philosophy:
- **Clean Code** (Robert C. Martin) — expressive names, small functions, no side effects, no surprises.
- **Código Sostenible** (Carlos Ble) — TDD, SOLID, hexagonal architecture, ports & adapters, mutation testing discipline.
- **Modular by domain**, not by technical layer.
- **Game-system agnostic core**: D&D 5e is the first concrete implementation, but the domain layer must not couple to it.

---

## 1. Product Vision

> **One app to manage any tabletop RPG character — fully offline, import-ready, and extensible.**

Users import characters from Fantasy Grounds XML (v5.x), view and edit all character data, manage combat state, and track resources in real time at the table.

### 1.1 Goals

| # | Goal |
|---|------|
| G1 | Full fidelity import from Fantasy Grounds 5e XML |
| G2 | Display every field a standard D&D 5e character sheet exposes |
| G3 | Support manual character creation without importing |
| G4 | Be game-system agnostic at the domain layer — plug in Pathfinder 2e, Call of Cthulhu, etc. later |
| G5 | Work entirely offline; cloud sync is a future enhancement |
| G6 | Maintainable, testable code following Clean Code & Código Sostenible principles |

### 1.2 Non-Goals (v1.0)

- Dice rolling engine (future module)
- Multiplayer / GM screen
- Cloud sync or cross-device sharing
- Map or encounter management

---

## 2. Reference: Complete D&D 5e Character Sheet Domain

The following is the canonical list of data a complete 5e sheet must track.
It is the ground truth for data modelling in this app.

### 2.1 Identity & Bio

| Field | Type | Notes |
|-------|------|-------|
| Name | String | Character's full name |
| Player Name | String | Optional |
| Race | String | e.g. Astral Elf |
| Subrace | String | Optional |
| Class(es) | List\<CharacterClass\> | Multiclass support |
| Level | Int | Derived from class list or stored flat |
| Subclass / Specialization | String | Per class |
| Background | String | |
| Alignment | String | |
| Experience Points | Int | |
| XP Needed for Next Level | Int | Derived |
| Age | String | |
| Gender | String | |
| Height | String | |
| Weight | String | |
| Size | String | Small / Medium / Large |

### 2.2 Ability Scores

Six abilities, each carrying:

| Sub-field | Type | Notes |
|-----------|------|-------|
| Score | Int | 1–30 |
| Modifier | Int | `(score - 10) / 2` floored |
| Saving Throw Bonus | Int | includes proficiency |
| Saving Throw Proficient | Boolean | |
| Save Modifier Override | Int | misc bonuses |

### 2.3 Combat & Defenses

| Field | Type | Notes |
|-------|------|-------|
| Armor Class | Int | total |
| AC (Armor base) | Int | |
| AC (Shield bonus) | Int | |
| AC (Dex bonus rule) | String | e.g. "max2", "none" |
| AC (Misc) | Int | |
| AC (Proficiency) | Int | |
| AC (Temporary) | Int | |
| Stealth Disadvantage | Boolean | Heavy armor |
| Initiative Total | Int | |
| Initiative Misc | Int | |
| Speed (Base) | Int | ft |
| Speed (Armor penalty) | Int | |
| Speed (Misc / Temp) | Int | |
| HP Max | Int | |
| HP Current | Int | `max - wounds` |
| HP Temporary | Int | |
| Wounds | Int | damage taken |
| Hit Dice | DiceExpression | e.g. 3d8 |
| Hit Dice Used | Int | |
| Death Save Failures | Int | 0–3 |
| Death Save Successes | Int | 0–3 |
| Inspiration | Int | 0 or 1 typically |
| Proficiency Bonus | Int | derived from level |

### 2.4 Skills (18 standard skills)

Per skill:

| Sub-field | Type |
|-----------|------|
| Name | String |
| Governing Ability | AbilityType enum |
| Proficient | Boolean |
| Expertise | Boolean |
| Misc Modifier | Int |
| Total Bonus | Int |

Full list: Acrobatics, Animal Handling, Arcana, Athletics, Deception, History, Insight, Intimidation, Investigation, Medicine, Nature, Perception, Performance, Persuasion, Religion, Sleight of Hand, Stealth, Survival.

Special: **Passive Perception** = `10 + Perception total`.

### 2.5 Senses

| Field | Notes |
|-------|-------|
| Passive Perception | Computed |
| Darkvision range | ft or null |
| Blindsight range | Optional |
| Tremorsense range | Optional |
| Truesight range | Optional |
| Custom Senses | Free text |

### 2.6 Spellcasting

#### Spell Slots

| Field | Type |
|-------|------|
| Level (1–9) | Int |
| Max Slots | Int |
| Used Slots | Int |
| Remaining Slots | Int (computed) |

Pact Magic slots (Warlock) modelled identically as a separate pool.

#### Spell Entry

| Field | Type | Notes |
|-------|------|-------|
| Name | String | |
| Spell Level | Int | 0 = cantrip |
| School | String | e.g. Evocation |
| Casting Time | String | |
| Range | String | |
| Components | String | V, S, M(…) |
| Duration | String | |
| Concentration | Boolean | |
| Ritual | Boolean | |
| Description | String | Rich text |
| Prepared | Boolean | |
| Always Prepared | Boolean | Domain spells etc. |
| Source | String | class list origin |
| Group | String | ties to PowerGroup |
| Damage / Heal Actions | List\<SpellAction\> | |

#### Spellcasting Metadata

| Field | Notes |
|-------|-------|
| Spellcasting Ability | wisdom / int / cha |
| Spell Attack Bonus | prof + ability mod |
| Spell Save DC | 8 + prof + ability mod |
| Caster Type | memorization / spontaneous / warlock |

### 2.7 Class Features & Traits

Each entry:

| Field | Type |
|-------|------|
| Name | String |
| Source Level | Int |
| Description | Rich Text |
| Specialization-gated | Boolean |
| Limited Uses | ResourceTracker? |

### 2.8 Racial Traits

Same structure as Class Features, sourced from race.

### 2.9 Background Features

Same structure, sourced from background.

### 2.10 Feats

| Field | Type |
|-------|------|
| Name | String |
| Description | Rich Text |
| Prerequisite | String? |

### 2.11 Inventory

#### Item Entry

| Field | Type | Notes |
|-------|------|-------|
| Name | String | |
| Type | ItemType enum | Weapon, Armor, Gear, Potion, Wondrous, Staff, … |
| Subtype | String | e.g. "Medium Armor" |
| Count / Quantity | Int | |
| Weight | Double | lbs |
| Cost | String | "50 gp" |
| Carried State | CarriedState enum | Carried / Equipped / Stored |
| Attuned | Boolean | |
| Identified | Boolean | |
| Non-ID Name | String? | Before identification |
| Description | Rich Text | |
| Rarity | String? | Common/Uncommon/Rare/Very Rare/Legendary |
| Bonus | Int? | +X magic bonus |
| AC Value | Int? | For armor |
| Damage | String? | For weapons |
| Properties | String? | Finesse, Versatile, etc. |

#### Encumbrance

| Field | Notes |
|-------|-------|
| Total Load | Sum of (item weight × count) |
| Carry Capacity | STR × 15 |
| Encumbered Threshold | Carry cap × 1/3 |
| Heavy Encumbered Threshold | Carry cap × 2/3 |
| Push/Drag/Lift | Carry cap × 2 |

#### Currency / Coins

PP, GP, EP, SP, CP — each with amount.
Conversion utilities in domain layer.

### 2.12 Weapons (Combat shortcut view)

Derived from inventory items of type Weapon, enriched with:

| Field | Notes |
|-------|-------|
| Attack Bonus | ability mod + prof + magic bonus |
| Damage Roll | dice + mod + type |
| Range | melee / thrown / ranged |
| Handling | one-hand / two-hand / versatile |

### 2.13 Proficiencies

Grouped:

| Group | Examples |
|-------|---------|
| Armor | Light, Medium, Heavy, Shields |
| Weapons | Simple, Martial, specific weapons |
| Tools | Thieves' Tools, Herbalism Kit, etc. |
| Languages | Common, Elvish, Dwarvish, etc. |

### 2.14 Personality & Roleplay

| Field |
|-------|
| Personality Traits |
| Ideals |
| Bonds |
| Flaws |
| Notes / Backstory (free text, multi-line) |

### 2.15 Class Resource Trackers

Generic structure for limited-use abilities:

| Field | Type |
|-------|------|
| Name | String | e.g. "Channel Divinity" |
| Max Uses | Int | |
| Used | Int | |
| Recharge On | RechargeType enum | Short Rest / Long Rest / Dawn / … |

---

## 3. Fantasy Grounds XML Import — Parser Coverage

Based on the `Aelfal.xml` reference file, the importer must handle these top-level XML nodes:

| XML Node | Maps To Domain |
|----------|---------------|
| `abilities.*` | AbilityScores (score, bonus, save, saveprof, savemodifier) |
| `age`, `gender`, `height` | CharacterIdentity |
| `background`, `backgroundlink` | Background |
| `bonds`, `flaws`, `ideals`, `personalitytraits` | Personality |
| `classes.id-NNNNN` | CharacterClass (name, level, specialization, spellability, hddie, hdused, cantrips) |
| `coins.id-NNNNN` | Currency |
| `defenses.ac.*` | ArmorClass |
| `encumbrance.*` | Encumbrance |
| `exp`, `expneeded` | Experience |
| `featurelist.id-NNNNN` | ClassFeature / RacialTrait |
| `hp.*` | HitPoints |
| `initiative.*` | Initiative |
| `inspiration` | Inspiration |
| `inventorylist.id-NNNNN` | InventoryItem |
| `languagelist.id-NNNNN` | Language |
| `level` | Total character level |
| `name` | Character name |
| `notes` | Notes/backstory |
| `perception`, `perceptionmodifier` | Passive Perception |
| `powergroup.id-NNNNN` | SpellcastingGroup metadata |
| `powermeta.spellslots[1-9]` | SpellSlot (max + used) |
| `powermeta.pactmagicslots[1-9]` | PactMagicSlot (Warlock) |
| `powers.id-NNNNN` | Spell or ClassAbility |
| `profbonus` | ProficiencyBonus |
| `proficiencylist.id-NNNNN` | Proficiency |
| `race`, `racename`, `racelink` | Race |
| `senses` | Senses (free text today, parseable later) |
| `size` | Size |
| `skilllist.id-NNNNN` | Skill (name, stat, prof, misc, total) |
| `speed.*` | Speed |
| `traitlist.id-NNNNN` | RacialTrait |
| `weaponlist.id-NNNNN` | WeaponShortcut (attack, damage, range) |

### 3.1 Import Edge Cases to Handle

- `formattedtext` nodes contain embedded HTML-like markup (`<p>`, `<b>`, `<i>`, `<list>`, `<li>`, `<linklist>`, `<link>`). Strip or convert to annotated string.
- `carried` field: 1 = on person, 2 = stored/not worn, handle as `CarriedState`.
- `isidentified = 0` means show `nonid_name` to player.
- `ritual = 1` flag on powers.
- `prepared = 0/1` on powers.
- Spells and class abilities share the `powers` node; differentiate by `group` field ("Spells (Cleric)" vs "Class (Cleric)").
- `cast = 1` on a power means it has been used (for limited-use abilities).
- `level = 0` inside `powers` = cantrip; inside `featurelist` = non-level-gated feature.

---

*End of PRD — v1.0*

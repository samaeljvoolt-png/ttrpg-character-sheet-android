package com.ddsheet.core.domain.model.ability

data class Abilities(
    val strength: Ability,
    val dexterity: Ability,
    val constitution: Ability,
    val intelligence: Ability,
    val wisdom: Ability,
    val charisma: Ability,
) {
    operator fun get(type: AbilityType): Ability = when (type) {
        AbilityType.STRENGTH -> strength
        AbilityType.DEXTERITY -> dexterity
        AbilityType.CONSTITUTION -> constitution
        AbilityType.INTELLIGENCE -> intelligence
        AbilityType.WISDOM -> wisdom
        AbilityType.CHARISMA -> charisma
    }
}

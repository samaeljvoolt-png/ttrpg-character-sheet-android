package com.ddsheet.core.domain.model.`class`

import com.ddsheet.core.domain.model.ability.AbilityType
import com.ddsheet.core.domain.model.value.DiceExpression

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

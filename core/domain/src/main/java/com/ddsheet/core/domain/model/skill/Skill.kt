package com.ddsheet.core.domain.model.skill

import com.ddsheet.core.domain.model.ability.AbilityType

data class Skill(
    val name: String,
    val governingAbility: AbilityType,
    val isProficient: Boolean,
    val isExpertise: Boolean,
    val miscModifier: Int,
    val totalBonus: Int,
)

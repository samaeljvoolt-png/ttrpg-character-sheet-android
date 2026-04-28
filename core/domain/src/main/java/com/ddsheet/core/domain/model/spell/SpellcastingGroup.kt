package com.ddsheet.core.domain.model.spell

import com.ddsheet.core.domain.model.ability.AbilityType
import com.ddsheet.core.domain.model.`class`.CasterType

data class SpellcastingGroup(
    val name: String,
    val spellcastingAbility: AbilityType,
    val casterType: CasterType,
    val includeAttackProficiency: Boolean,
    val includeSaveProficiency: Boolean,
)

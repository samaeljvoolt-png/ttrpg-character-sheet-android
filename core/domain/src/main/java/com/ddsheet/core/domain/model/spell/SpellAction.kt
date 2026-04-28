package com.ddsheet.core.domain.model.spell

import com.ddsheet.core.domain.model.ability.AbilityType
import com.ddsheet.core.domain.model.value.DiceExpression

sealed class SpellAction {
    data class Damage(
        val dice: DiceExpression,
        val damageType: String,
        val bonus: Int
    ) : SpellAction()

    data class Heal(
        val dice: DiceExpression,
        val bonus: Int,
        val targetSelf: Boolean
    ) : SpellAction()

    data class SavingThrow(
        val ability: AbilityType,
        val isMagicSave: Boolean
    ) : SpellAction()

    data class Effect(
        val label: String,
        val durationMinutes: Int?
    ) : SpellAction()

    data class Attack(
        val type: AttackType
    ) : SpellAction()
}

enum class AttackType {
    MELEE_SPELL, RANGED_SPELL, MELEE_WEAPON, RANGED_WEAPON
}

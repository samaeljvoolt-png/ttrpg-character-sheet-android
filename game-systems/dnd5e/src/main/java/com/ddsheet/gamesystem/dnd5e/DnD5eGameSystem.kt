package com.ddsheet.gamesystem.dnd5e

import com.ddsheet.core.domain.gamesystem.GameSystem
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Implementación de [GameSystem] para Dungeons & Dragons 5th Edition.
 *
 * Stateless — no mantiene estado de personaje. Solo aplica las fórmulas
 * canónicas del Player's Handbook / Basic Rules.
 */
class DnD5eGameSystem : GameSystem {

    override val id: String = SYSTEM_ID
    override val name: String = SYSTEM_NAME

    override fun abilityModifier(score: Int): Int =
        floor((score - 10) / 2.0).toInt()

    override fun proficiencyBonus(level: Int): Int =
        when {
            level < 1 -> 0
            else -> ceil(level / 4.0).toInt() + 1
        }

    override fun spellSaveDC(abilityMod: Int, profBonus: Int): Int =
        BASE_SPELL_DC + profBonus + abilityMod

    override fun spellAttackBonus(abilityMod: Int, profBonus: Int): Int =
        profBonus + abilityMod

    override fun passiveScore(skillTotal: Int): Int =
        BASE_PASSIVE + skillTotal

    companion object {
        const val SYSTEM_ID = "dnd5e"
        const val SYSTEM_NAME = "Dungeons & Dragons 5th Edition"

        private const val BASE_SPELL_DC = 8
        private const val BASE_PASSIVE = 10
    }
}

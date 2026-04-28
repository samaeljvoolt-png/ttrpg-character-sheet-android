package com.ddsheet.gamesystem.dnd5e

import org.junit.Assert.assertEquals
import org.junit.Test

class DnD5eGameSystemTest {

    private val system = DnD5eGameSystem()

    // ─────────────────────────────────────────────────────────────
    // abilityModifier
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `abilityModifier - score 10 returns 0`() {
        assertEquals(0, system.abilityModifier(10))
    }

    @Test
    fun `abilityModifier - score 11 returns 0`() {
        assertEquals(0, system.abilityModifier(11))
    }

    @Test
    fun `abilityModifier - score 14 returns plus 2`() {
        assertEquals(2, system.abilityModifier(14))
    }

    @Test
    fun `abilityModifier - score 9 returns minus 1`() {
        assertEquals(-1, system.abilityModifier(9))
    }

    @Test
    fun `abilityModifier - score 20 returns plus 5`() {
        assertEquals(5, system.abilityModifier(20))
    }

    @Test
    fun `abilityModifier - score 1 returns minus 5`() {
        assertEquals(-5, system.abilityModifier(1))
    }

    // ─────────────────────────────────────────────────────────────
    // proficiencyBonus
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `proficiencyBonus - level 1 returns 2`() {
        assertEquals(2, system.proficiencyBonus(1))
    }

    @Test
    fun `proficiencyBonus - level 4 returns 2`() {
        assertEquals(2, system.proficiencyBonus(4))
    }

    @Test
    fun `proficiencyBonus - level 5 returns 3`() {
        assertEquals(3, system.proficiencyBonus(5))
    }

    @Test
    fun `proficiencyBonus - level 8 returns 3`() {
        assertEquals(3, system.proficiencyBonus(8))
    }

    @Test
    fun `proficiencyBonus - level 9 returns 4`() {
        assertEquals(4, system.proficiencyBonus(9))
    }

    @Test
    fun `proficiencyBonus - level 13 returns 5`() {
        assertEquals(5, system.proficiencyBonus(13))
    }

    @Test
    fun `proficiencyBonus - level 17 returns 6`() {
        assertEquals(6, system.proficiencyBonus(17))
    }

    @Test
    fun `proficiencyBonus - level 20 returns 6`() {
        assertEquals(6, system.proficiencyBonus(20))
    }

    @Test
    fun `proficiencyBonus - level 0 returns 0`() {
        assertEquals(0, system.proficiencyBonus(0))
    }

    // ─────────────────────────────────────────────────────────────
    // spellSaveDC
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `spellSaveDC - standard wizard values`() {
        // Wizard level 5: prof +3, INT 16 → mod +3
        // DC = 8 + 3 + 3 = 14
        assertEquals(14, system.spellSaveDC(abilityMod = 3, profBonus = 3))
    }

    // ─────────────────────────────────────────────────────────────
    // spellAttackBonus
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `spellAttackBonus - standard wizard values`() {
        // +3 (prof) + 3 (INT) = +6
        assertEquals(6, system.spellAttackBonus(abilityMod = 3, profBonus = 3))
    }

    // ─────────────────────────────────────────────────────────────
    // passiveScore
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `passiveScore - perception with total bonus 3`() {
        assertEquals(13, system.passiveScore(skillTotal = 3))
    }

    @Test
    fun `passiveScore - perception with total bonus 0`() {
        assertEquals(10, system.passiveScore(skillTotal = 0))
    }

    // ─────────────────────────────────────────────────────────────
    // identity
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `system id is dnd5e`() {
        assertEquals("dnd5e", system.id)
    }

    @Test
    fun `system name is DnD 5e`() {
        assertEquals("Dungeons & Dragons 5th Edition", system.name)
    }
}

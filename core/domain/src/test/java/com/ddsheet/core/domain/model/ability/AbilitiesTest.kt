package com.ddsheet.core.domain.model.ability

import org.junit.Assert.assertEquals
import org.junit.Test

class AbilitiesTest {

    private val abilities = Abilities(
        strength = Ability(10, 0, 0, false, 0),
        dexterity = Ability(14, 2, 2, true, 0),
        constitution = Ability(12, 1, 1, false, 0),
        intelligence = Ability(8, -1, -1, false, 0),
        wisdom = Ability(16, 3, 3, true, 0),
        charisma = Ability(18, 4, 4, false, 0),
    )

    @Test
    fun `get returns correct ability by type`() {
        assertEquals(10, abilities[AbilityType.STRENGTH].score)
        assertEquals(14, abilities[AbilityType.DEXTERITY].score)
        assertEquals(16, abilities[AbilityType.WISDOM].score)
    }
}

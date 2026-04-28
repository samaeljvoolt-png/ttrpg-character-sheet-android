package com.ddsheet.core.domain.model.spell

import org.junit.Assert.assertEquals
import org.junit.Test

class SpellSlotTest {

    @Test
    fun `remaining is max minus used`() {
        val slot = SpellSlot(level = 1, max = 4, used = 2)
        assertEquals(2, slot.remaining)
    }

    @Test
    fun `remaining is zero when fully used`() {
        val slot = SpellSlot(level = 3, max = 3, used = 3)
        assertEquals(0, slot.remaining)
    }
}

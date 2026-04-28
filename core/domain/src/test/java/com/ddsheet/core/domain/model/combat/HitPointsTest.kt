package com.ddsheet.core.domain.model.combat

import org.junit.Assert.assertEquals
import org.junit.Test

class HitPointsTest {

    @Test
    fun `current HP is max minus wounds`() {
        val hp = HitPoints(max = 50, wounds = 12, temporary = 5)
        assertEquals(38, hp.current)
    }

    @Test
    fun `current HP equals max when no wounds`() {
        val hp = HitPoints(max = 30, wounds = 0, temporary = 0)
        assertEquals(30, hp.current)
    }
}

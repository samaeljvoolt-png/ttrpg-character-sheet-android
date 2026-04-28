package com.ddsheet.core.domain.model.value

import org.junit.Assert.assertEquals
import org.junit.Test

class DiceExpressionTest {

    @Test
    fun `toString formats basic dice`() {
        assertEquals("1d6", DiceExpression(1, 6).toString())
    }

    @Test
    fun `toString includes positive bonus`() {
        assertEquals("2d8+3", DiceExpression(2, 8, 3).toString())
    }

    @Test
    fun `toString omits zero bonus`() {
        assertEquals("3d10", DiceExpression(3, 10, 0).toString())
    }
}

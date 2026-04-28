package com.ddsheet.core.domain.model.value

data class DiceExpression(
    val count: Int,
    val sides: Int,
    val bonus: Int = 0
) {
    override fun toString(): String =
        "${count}d${sides}${if (bonus != 0) "+$bonus" else ""}"
}

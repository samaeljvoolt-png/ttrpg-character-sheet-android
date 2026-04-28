package com.ddsheet.core.domain.model.spell

data class SpellSlot(
    val level: Int,
    val max: Int,
    val used: Int,
) {
    val remaining: Int get() = max - used
}

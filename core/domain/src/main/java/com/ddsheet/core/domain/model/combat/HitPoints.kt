package com.ddsheet.core.domain.model.combat

data class HitPoints(
    val max: Int,
    val wounds: Int,
    val temporary: Int,
) {
    val current: Int get() = max - wounds
}

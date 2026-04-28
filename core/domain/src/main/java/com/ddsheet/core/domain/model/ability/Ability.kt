package com.ddsheet.core.domain.model.ability

data class Ability(
    val score: Int,
    val modifier: Int,
    val savingThrow: Int,
    val saveProficient: Boolean,
    val saveMiscModifier: Int,
)

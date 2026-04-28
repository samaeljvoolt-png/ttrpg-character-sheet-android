package com.ddsheet.core.domain.model.combat

data class Speed(
    val baseFeet: Int,
    val flyFeet: Int?,
    val swimFeet: Int?,
    val climbFeet: Int?,
    val burrowFeet: Int?,
)

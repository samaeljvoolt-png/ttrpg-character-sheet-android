package com.ddsheet.core.domain.model.inventory

data class Encumbrance(
    val currentLoad: Double,
    val maxCapacity: Double,
    val encumberedThreshold: Double,
    val heavilyEncumberedThreshold: Double,
    val pushDragLift: Double,
)

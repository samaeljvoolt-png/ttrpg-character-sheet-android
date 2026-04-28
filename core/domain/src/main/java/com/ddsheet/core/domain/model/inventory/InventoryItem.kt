package com.ddsheet.core.domain.model.inventory

data class InventoryItem(
    val id: String,
    val name: String,
    val nonIdName: String?,
    val isIdentified: Boolean,
    val type: String,
    val subtype: String?,
    val count: Int,
    val weight: Double,
    val cost: String?,
    val carriedState: CarriedState,
    val isAttuned: Boolean,
    val rarity: String?,
    val magicBonus: Int,
    val acValue: Int?,
    val damageExpression: String?,
    val properties: String?,
    val description: String,
)

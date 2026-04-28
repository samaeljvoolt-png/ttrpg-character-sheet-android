package com.ddsheet.core.domain.model.combat

data class ArmorClass(
    val total: Int,
    val armorBase: Int,
    val shieldBonus: Int,
    val dexBonusRule: DexBonusRule,
    val miscBonus: Int,
    val profBonus: Int,
    val temporaryBonus: Int,
    val stealthDisadvantage: Boolean,
)

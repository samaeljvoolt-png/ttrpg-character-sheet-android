package com.ddsheet.core.domain.model.combat

data class CombatStats(
    val armorClass: ArmorClass,
    val hitPoints: HitPoints,
    val initiative: InitiativeStat,
    val speed: Speed,
    val proficiencyBonus: Int,
    val deathSaves: DeathSaves,
)

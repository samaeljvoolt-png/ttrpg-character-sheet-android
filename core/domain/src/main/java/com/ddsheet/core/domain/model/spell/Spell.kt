package com.ddsheet.core.domain.model.spell

data class Spell(
    val name: String,
    val spellLevel: Int,
    val school: String,
    val castingTime: String,
    val range: String,
    val components: String,
    val duration: String,
    val isConcentration: Boolean,
    val isRitual: Boolean,
    val description: String,
    val isPrepared: Boolean,
    val isAlwaysPrepared: Boolean,
    val group: String,
    val source: String,
    val actions: List<SpellAction>,
    val timesUsed: Int,
)

package com.ddsheet.core.domain.model.spell

data class SpellBook(
    val groups: List<SpellcastingGroup>,
    val slots: List<SpellSlot>,
    val pactMagicSlots: List<SpellSlot>,
    val spells: List<Spell>,
)

package com.ddsheet.core.domain.model.inventory

data class Inventory(
    val items: List<InventoryItem>,
    val currency: Currency,
    val encumbrance: Encumbrance,
)

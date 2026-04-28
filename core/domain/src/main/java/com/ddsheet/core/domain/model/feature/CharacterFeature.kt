package com.ddsheet.core.domain.model.feature

data class CharacterFeature(
    val name: String,
    val sourceLevelRequired: Int,
    val description: String,
    val isSpecializationFeature: Boolean,
    val resource: ResourceTracker?,
)

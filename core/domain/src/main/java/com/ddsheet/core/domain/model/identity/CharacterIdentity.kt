package com.ddsheet.core.domain.model.identity

import com.ddsheet.core.domain.model.value.CharacterSize

data class CharacterIdentity(
    val name: String,
    val race: String,
    val subrace: String?,
    val background: String,
    val alignment: String?,
    val experience: Int,
    val age: String?,
    val gender: String?,
    val height: String?,
    val weight: String?,
    val size: CharacterSize,
    val inspiration: Int,
)

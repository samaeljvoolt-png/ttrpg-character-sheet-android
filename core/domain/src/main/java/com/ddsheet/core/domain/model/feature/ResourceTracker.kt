package com.ddsheet.core.domain.model.feature

data class ResourceTracker(
    val name: String,
    val max: Int,
    val used: Int,
    val rechargesOn: RechargeType,
)

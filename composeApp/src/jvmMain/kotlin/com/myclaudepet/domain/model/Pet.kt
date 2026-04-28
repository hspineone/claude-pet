package com.myclaudepet.domain.model

import kotlinx.datetime.Instant

data class Pet(
    val position: PetPosition,
    val satiation: Satiation,
    val affinity: Affinity,
    val animationState: PetAnimationState,
    val keystrokes: Long,
    val updatedAt: Instant,
    val equippedAccessory: Accessory? = null,
) {
    val mood: PetMood get() = satiation.mood
    val level: Int get() = affinity.level
    val tier: DialogueTier get() = affinity.tier
}

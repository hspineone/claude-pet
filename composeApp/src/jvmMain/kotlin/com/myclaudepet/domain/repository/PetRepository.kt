package com.myclaudepet.domain.repository

import com.myclaudepet.domain.model.Accessory
import com.myclaudepet.domain.model.Affinity
import com.myclaudepet.domain.model.Pet
import com.myclaudepet.domain.model.PetAnimationState
import com.myclaudepet.domain.model.PetPosition
import com.myclaudepet.domain.model.Satiation
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observe(): Flow<Pet>
    suspend fun current(): Pet
    suspend fun updatePosition(position: PetPosition)
    suspend fun updateSatiation(value: Satiation)
    suspend fun updateAffinity(value: Affinity)
    suspend fun updateAnimationState(state: PetAnimationState)
    suspend fun incrementKeystrokes(by: Long = 1L)
    suspend fun setAccessory(value: Accessory?)
    suspend fun resetProgress()
}

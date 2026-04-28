package com.myclaudepet.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import com.myclaudepet.data.install.InstallId
import com.myclaudepet.data.time.Clock
import com.myclaudepet.db.PetDatabase
import com.myclaudepet.domain.model.Accessory
import com.myclaudepet.domain.model.Affinity
import com.myclaudepet.domain.model.Pet
import com.myclaudepet.domain.model.PetAnimationState
import com.myclaudepet.domain.model.PetPosition
import com.myclaudepet.domain.model.Satiation
import com.myclaudepet.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightPetRepository(
    private val db: PetDatabase,
    private val clock: Clock,
    private val defaultPosition: PetPosition,
) : PetRepository {

    private val queries get() = db.petStateQueries

    init {
        queries.seedDefault(
            x = defaultPosition.x.toLong(),
            y = defaultPosition.y.toLong(),
            updatedAt = clock.nowMillis(),
        )
        enforceInstallIdReset()
    }

    /**
     * 배포 빌드 UUID 와 DB 저장 값이 다르면 진행 데이터를 초기화.
     * 개발 빌드(`install_id.txt` 미주입 → bundled == null)에서는 건너뜀.
     */
    private fun enforceInstallIdReset() {
        val bundled = InstallId.bundled() ?: return
        val saved = queries.selectInstallId().executeAsOneOrNull().orEmpty()
        if (saved == bundled) return
        val now = clock.nowMillis()
        queries.resetProgress(updatedAt = now)
        queries.updateInstallId(installId = bundled, updatedAt = now)
    }

    override fun observe(): Flow<Pet> =
        queries.selectPet()
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { it.toDomain() }

    override suspend fun current(): Pet = withContext(Dispatchers.IO) {
        queries.selectPet().executeAsOne().toDomain()
    }

    override suspend fun updatePosition(position: PetPosition): Unit = withContext(Dispatchers.IO) {
        queries.updatePosition(
            x = position.x.toLong(),
            y = position.y.toLong(),
            updatedAt = clock.nowMillis(),
        )
    }

    override suspend fun updateSatiation(value: Satiation): Unit = withContext(Dispatchers.IO) {
        queries.updateSatiation(value.raw, clock.nowMillis())
    }

    override suspend fun updateAffinity(value: Affinity): Unit = withContext(Dispatchers.IO) {
        queries.updateAffinity(value.points.toLong(), clock.nowMillis())
    }

    override suspend fun updateAnimationState(state: PetAnimationState): Unit = withContext(Dispatchers.IO) {
        queries.updateAnimationState(state.name, clock.nowMillis())
    }

    override suspend fun incrementKeystrokes(by: Long): Unit = withContext(Dispatchers.IO) {
        queries.incrementKeystrokes(by, clock.nowMillis())
    }

    override suspend fun setAccessory(value: Accessory?): Unit = withContext(Dispatchers.IO) {
        queries.updateAccessory(accessoryId = value?.id, updatedAt = clock.nowMillis())
    }

    override suspend fun resetProgress(): Unit = withContext(Dispatchers.IO) {
        queries.resetProgress(clock.nowMillis())
    }

    private fun com.myclaudepet.db.SelectPet.toDomain(): Pet = Pet(
        position = PetPosition(x.toInt(), y.toInt()),
        satiation = Satiation(satiation),
        affinity = Affinity(affinity.toInt()),
        animationState = runCatching { PetAnimationState.valueOf(animation_state) }
            .getOrDefault(PetAnimationState.Default),
        keystrokes = keystrokes,
        updatedAt = Instant.fromEpochMilliseconds(updated_at),
        equippedAccessory = Accessory.fromId(equipped_accessory),
    )
}

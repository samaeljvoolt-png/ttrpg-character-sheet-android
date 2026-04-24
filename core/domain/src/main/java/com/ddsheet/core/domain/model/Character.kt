package com.ddsheet.core.domain.model

import java.time.Instant

/**
 * Entidad raíz: un personaje agnóstico al sistema de juego.
 *
 * Principios:
 * - Single Responsibility: solo identifica y metadatos comunes.
 * - Open/Closed: los sistemas extienden vía [GameSystemPlugin] sin modificar esto.
 * - Inmutable: character se recrea, no muta. Fácil de testear, thread-safe.
 */
data class Character(
    val id: CharacterId,
    val name: NonEmptyString,
    val systemId: SystemId,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        /**
         * Smart constructor que nunca devuelve null ni lanza excepciones.
         * Preferimos [Result] sobre excepciones o nulls.
         */
        fun create(
            name: String,
            systemId: SystemId
        ): Either<DomainError, Character> {
            val nonEmptyName = NonEmptyString.create(name)
            return if (nonEmptyName.isFailure) {
                Either.Left(
                    DomainError.EmptyCharacterName(name)
                )
            } else {
                Either.Right(
                    Character(
                        id = CharacterId.generate(),
                        name = nonEmptyName.getOrThrow(),
                        systemId = systemId,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                )
            }
        }

        fun reconstruct(
            id: CharacterId,
            name: NonEmptyString,
            systemId: SystemId,
            createdAt: Instant,
            updatedAt: Instant
        ): Character = Character(id, name, systemId, createdAt, updatedAt)
    }

    fun withUpdatedAt(now: Instant = Instant.now()): Character =
        reconstruct(id, name, systemId, createdAt, now)
}

@JvmInline
value class CharacterId private constructor(val value: String) {
    companion object {
        fun generate(): CharacterId = CharacterId(java.util.UUID.randomUUID().toString())
        fun fromString(value: String): CharacterId = CharacterId(value)
    }
}

@JvmInline
value class SystemId(val value: String)

@JvmInline
value class NonEmptyString private constructor(val value: String) {
    companion object {
        fun create(value: String): Result<NonEmptyString> =
            if (value.isBlank()) Result.failure(IllegalArgumentException("String must not be blank"))
            else Result.success(NonEmptyString(value))
    }
}

package com.ddsheet.core.domain.fake

import com.ddsheet.core.domain.model.Character
import com.ddsheet.core.domain.model.CharacterId
import com.ddsheet.core.domain.model.DomainError
import com.ddsheet.core.domain.model.Either
import com.ddsheet.core.domain.repository.CharacterRepository

/**
 * Test double: FakeRepository.
 *
 * Preferimos fakes sobre mocks cuando la implementación es trivial.
 * - Más robustos (no rompen por cambios en la interfaz)
 * - Más legibles (el comportamiento está explícito)
 * - Reflejan mejor la realidad
 *
 * Según Carlos Blé: "Si un mock es difícil de configurar, usá un fake."
 * En este caso: un simple MutableMap es suficiente.
 */
class FakeCharacterRepository : CharacterRepository {

    private val characters = mutableMapOf<CharacterId, Character>()

    var failNextSave: Boolean = false
    var failNextFind: Boolean = false

    override suspend fun save(character: Character): Either<DomainError, CharacterId> {
        if (failNextSave) {
            failNextSave = false
            return Either.Left(DomainError.ValidationFailed("Simulated save failure"))
        }
        characters[character.id] = character
        return Either.Right(character.id)
    }

    override suspend fun findById(id: CharacterId): Either<DomainError, Character> {
        if (failNextFind) {
            failNextFind = false
            return Either.Left(DomainError.CharacterNotFound(id))
        }
        val character = characters[id]
            ?: return Either.Left(DomainError.CharacterNotFound(id))
        return Either.Right(character)
    }

    override suspend fun findAll(): Either<DomainError, List<Character>> {
        return Either.Right(characters.values.toList())
    }

    override suspend fun delete(id: CharacterId): Either<DomainError, Unit> {
        characters.remove(id)
        return Either.Right(Unit)
    }

    /**
     * Reset state between tests. Evitamos acoplarnos a JUnit @After.
     */
    fun reset() {
        characters.clear()
        failNextSave = false
        failNextFind = false
    }
}

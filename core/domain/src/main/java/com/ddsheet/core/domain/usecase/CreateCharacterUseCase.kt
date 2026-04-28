package com.ddsheet.core.domain.usecase

import com.ddsheet.core.domain.model.Character
import com.ddsheet.core.domain.model.DomainError
import com.ddsheet.core.domain.model.Either
import com.ddsheet.core.domain.model.SystemId
import com.ddsheet.core.domain.repository.CharacterRepository

/**
 * Caso de uso: crear un personaje.
 *
 * Principio de Single Responsibility: este caso de uso SOLO crea
 * y persiste. No calcula stats, no valida reglas de sistema —
* eso es del plugin.
 */
class CreateCharacterUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(
        name: String,
        systemId: SystemId
    ): Either<DomainError, Character> {
        return when (val createResult = Character.create(name, systemId)) {
            is Either.Left -> createResult
            is Either.Right -> {
                val character = createResult.value
                when (val saveResult = repository.save(character)) {
                    is Either.Left -> saveResult
                    is Either.Right -> Either.Right(character)
                }
            }
        }
    }
}

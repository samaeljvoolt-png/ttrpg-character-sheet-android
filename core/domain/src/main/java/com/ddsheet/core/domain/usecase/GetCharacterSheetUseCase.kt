package com.ddsheet.core.domain.usecase

import com.ddsheet.core.domain.model.Character
import com.ddsheet.core.domain.model.CharacterId
import com.ddsheet.core.domain.model.DomainError
import com.ddsheet.core.domain.model.Either
import com.ddsheet.core.domain.model.GameSystemPlugin
import com.ddsheet.core.domain.repository.CharacterRepository

/**
 * Caso de uso: obtener hoja de personaje con stats derivados.
 *
 * Orquesta: busca el personaje y le pide al plugin que calcule
 * estadísticas derivadas según el sistema activo.
 */
class GetCharacterSheetUseCase(
    private val repository: CharacterRepository,
    private val pluginProvider: (com.ddsheet.core.domain.model.SystemId) -> GameSystemPlugin?
) {
    suspend operator fun invoke(
        id: CharacterId
    ): Either<DomainError, Pair<Character, com.ddsheet.core.domain.model.GameCharacterSheet>> {
        return repository.findById(id).flatMap { character ->
            val plugin = pluginProvider(character.systemId)
                ?: return Either.Left(
                    DomainError.InvalidSystem(character.systemId)
                )

            val sheetResult = plugin.createEmptySheet()
            if (sheetResult.isFailure) {
                return Either.Left(
                    DomainError.ValidationFailed("Failed to create sheet: ${sheetResult.exceptionOrNull()?.message}")
                )
            }

            val derived = plugin.computeDerivedStats(sheetResult.getOrThrow())
            Either.Right(character to derived)
        }
    }
}

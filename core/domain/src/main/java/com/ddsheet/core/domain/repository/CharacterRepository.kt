package com.ddsheet.core.domain.repository

import com.ddsheet.core.domain.model.Character
import com.ddsheet.core.domain.model.CharacterId
import com.ddsheet.core.domain.model.Either
import com.ddsheet.core.domain.model.DomainError

/**
 * Repository contract — puerto en arquitectura hexagonal.
 *
 * La capa de datos implementa. El dominio solo conoce esta interfaz.
 * Esto permite que los tests del dominio usen un FakeRepository
 * sin necesidad de Room, Base de Datos, ni Android.
 */
interface CharacterRepository {
    /**
     * Guarda o actualiza un personaje.
     * @return Either.Left si falla persistencia, Either.Right con el ID guardado.
     */
    suspend fun save(character: Character): Either<DomainError, CharacterId>

    /**
     * Recupera un personaje por ID.
     * @return DomainError.CharacterNotFound si no existe.
     */
    suspend fun findById(id: CharacterId): Either<DomainError, Character>

    /**
     * Lista todos los personajes.
     */
    suspend fun findAll(): Either<DomainError, List<Character>>

    /**
     * Elimina un personaje por ID.
     */
    suspend fun delete(id: CharacterId): Either<DomainError, Unit>
}

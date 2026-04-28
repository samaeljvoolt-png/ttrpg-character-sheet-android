package com.ddsheet.core.domain.model

/**
 * Errores de dominio explícitos.
 * Nunca usamos excepciones para control de flujo.
 * Cada error es un valor que el caller maneja con when().
 */
sealed class DomainError(val message: String) {
    data class CharacterNotFound(
        val id: CharacterId
    ) : DomainError("Character ${id.value} not found")

    data class InvalidSystem(
        val systemId: SystemId
    ) : DomainError("System ${systemId.value} is not available")

    data class ValidationFailed(
        val details: String
    ) : DomainError("Validation failed: $details")

    data class EmptyCharacterName(
        val attemptedName: String
    ) : DomainError("Character name cannot be blank, got: '${attemptedName}'")
}

sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}

inline fun <L, R, T> Either<L, R>.map(fn: (R) -> T): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> Either.Right(fn(value))
    }

inline fun <L, R, T> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> fn(value)
    }

fun <R> Result<R>.toEither(): Either<DomainError, R> =
    fold(
        onSuccess = { Either.Right(it) },
        onFailure = { Either.Left(DomainError.ValidationFailed(it.message ?: "Unknown error")) }
    )

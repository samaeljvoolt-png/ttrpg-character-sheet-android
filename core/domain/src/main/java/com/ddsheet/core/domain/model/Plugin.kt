package com.ddsheet.core.domain.model

/**
 * Puerto (Interface) que cada sistema de juego implementa.
 *
 * Arquitectura Hexagonal / Clean Architecture:
 * - El dominio define el contrato (puerto).
 * - Los adaptadores (módulos como :domain:dnd5e) implementan.
 * - La app no depende de D&D 5e; depende de esta abstracción.
 */
interface GameSystemPlugin {
    val systemId: SystemId
    val displayName: String
    val version: String

    /**
     * Crea una hoja de personaje vacía según las reglas del sistema.
     * @return Result para manejo explícito de errores — sin excepciones como control de flujo.
     */
    fun createEmptySheet(): Result<GameCharacterSheet>

    /**
     * Valida una hoja completa según las reglas del sistema.
     */
    fun validateSheet(sheet: GameCharacterSheet): ValidationResult

    /**
     * Recalcula estadísticas derivadas (HP, AC, modifiers).
     * Función pura: misma entrada = misma salida. Zero side-effects.
     */
    fun computeDerivedStats(baseSheet: GameCharacterSheet): GameCharacterSheet

    /**
     * Niveles válidos para este sistema.
     */
    fun availableLevels(): ClosedRange<Int>
}

/**
 * Hoja de personaje como mapa inmutable de atributos tipados.
 * Agnóstico al sistema — el plugin interpreta las claves.
 *
 * Usamos [AttributeKey] explícito para evitar stringly-typed attributes
 * (un anti-pattern que lleva a errores silenciosos).
 */
data class GameCharacterSheet(
    val attributes: Map<AttributeKey, AttributeValue> = emptyMap(),
    val metadata: SheetMetadata = SheetMetadata()
) {
    inline fun <reified T : AttributeValue> find(key: AttributeKey): T? =
        attributes[key] as? T

    fun withAttribute(key: AttributeKey, value: AttributeValue): GameCharacterSheet =
        copy(attributes = attributes + (key to value), metadata = metadata.copy(isDirty = true))

    fun withClean(): GameCharacterSheet =
        copy(metadata = metadata.copy(isDirty = false))
}

data class SheetMetadata(
    val level: Int = 1,
    val experiencePoints: Long = 0L,
    val isDirty: Boolean = false
)

@JvmInline
value class AttributeKey(val value: String)

sealed interface AttributeValue
@JvmInline value class TextValue(val text: String) : AttributeValue
@JvmInline value class NumberValue(val number: Int) : AttributeValue
@JvmInline value class BooleanValue(val flag: Boolean) : AttributeValue
@JvmInline value class ListValue(val items: List<String>) : AttributeValue

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    data class ValidationError(
        val attributeKey: AttributeKey,
        val message: String
    )
}

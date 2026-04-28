package com.ddsheet.core.domain.gamesystem

/**
 * Interfaz pura de reglas de un sistema de juego TTRPG.
 *
 * Responsabilidad única: calcular valores derivados según las reglas del sistema.
 * No crea hojas, no valida estructuras complejas, no conoce XML ni UI.
 *
 * Diseñada para ser stateless e inmutable: cada implementación es un singleton
 * de reglas puras (funciones determinísticas sin side-effects).
 *
 * @see com.ddsheet.core.domain.model.GameSystemPlugin — interfaz legacy de spike,
 *      será removida en fase posterior. GameSystemPlugin orquestaba creación de
 *      hojas y validación; esas responsabilidades migrarán a builders/validators
 *      independientes.
 */
interface GameSystem {
    /** Identificador canónico, ej: "dnd5e", "pf2e", "coc7e". */
    val id: String

    /** Nombre legible para humanos. */
    val name: String

    /**
     * Modificador de habilidad a partir de puntuación bruta.
     *
     * Ejemplo D&D 5e: score 14 → +2; score 9 → -1.
     */
    fun abilityModifier(score: Int): Int

    /**
     * Bonus de competencia (proficiency bonus) según nivel.
     *
     * Ejemplo D&D 5e: nivel 1–4 → +2; 5–8 → +3; etc.
     */
    fun proficiencyBonus(level: Int): Int

    /**
     * Dificultad de salvación de conjuro (Spell Save DC).
     *
     * @param abilityMod Modificador de la habilidad de lanzamiento.
     * @param profBonus Bonus de competencia del personaje.
     */
    fun spellSaveDC(abilityMod: Int, profBonus: Int): Int

    /**
     * Bonus de ataque de conjuro (Spell Attack Bonus).
     *
     * @param abilityMod Modificador de la habilidad de lanzamiento.
     * @param profBonus Bonus de competencia del personaje.
     */
    fun spellAttackBonus(abilityMod: Int, profBonus: Int): Int

    /**
     * Puntuación pasiva para una skill (ej: Percepción Pasiva).
     *
     * @param skillTotal Bonus total de la skill (habilidad + competencia + misc).
     */
    fun passiveScore(skillTotal: Int): Int
}

package com.ddsheet.core.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests para [Character] — entidad raíz del dominio.
 *
 * Estilo: TDD. Cubrimos happy path + casos edge.
 *
 * Principio aplicado: "Tests son la primera documentación."
 */
class CharacterTest {

    @Test
    fun `create with valid name and systemId returns right`() {
        val result = Character.create("Gandalf", SystemId("dnd5e"))

        assertTrue("Expected Right, got $result", result is Either.Right)
        val character = (result as Either.Right).value
        assertEquals("Gandalf", character.name.value)
        assertEquals("dnd5e", character.systemId.value)
    }

    @Test
    fun `create with empty name returns left with EmptyCharacterName`() {
        val result = Character.create("", SystemId("dnd5e"))

        assertTrue("Expected Left, got $result", result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue("Expected EmptyCharacterName, got $error", error is DomainError.EmptyCharacterName)
        assertEquals("", (error as DomainError.EmptyCharacterName).attemptedName)
    }

    @Test
    fun `create with blank name returns left`() {
        val result = Character.create("   ", SystemId("dnd5e"))

        assertTrue(result is Either.Left)
    }

    @Test
    fun `create generates unique ids`() {
        val c1 = (Character.create("Frodo", SystemId("dnd5e")) as Either.Right).value
        val c2 = (Character.create("Sam", SystemId("dnd5e")) as Either.Right).value

        assertNotEquals(c1.id.value, c2.id.value)
    }

    @Test
    fun `create sets timestamps`() {
        val before = java.time.Instant.now()
        val character = (Character.create("Aragorn", SystemId("dnd5e")) as Either.Right).value
        val after = java.time.Instant.now()

        assertTrue(character.createdAt.isAfter(before) || character.createdAt == before)
        assertTrue(character.createdAt.isBefore(after) || character.createdAt == after)
    }

    @Test
    fun `withUpdatedAt creates updated copy without mutating original`() {
        val original = (Character.create("Legolas", SystemId("dnd5e")) as Either.Right).value
        val originalUpdatedAt = original.updatedAt

        java.lang.Thread.sleep(10) // asegurar diferencia de tiempo
        val updated = original.withUpdatedAt()

        assertNotEquals(originalUpdatedAt, updated.updatedAt)
        assertEquals(original.id, updated.id) // identidad se preserva
        assertEquals(original.name, updated.name)
        assertEquals(original.createdAt, updated.createdAt)
    }

    @Test
    fun `reconstruct preserves all fields exactly`() {
        val id = CharacterId.generate()
        val name = NonEmptyString.create("Boromir").getOrThrow()
        val systemId = SystemId("dnd5e")
        val created = java.time.Instant.parse("2024-01-01T00:00:00Z")
        val updated = java.time.Instant.parse("2024-02-01T00:00:00Z")

        val character = Character.reconstruct(id, name, systemId, created, updated)

        assertEquals(id, character.id)
        assertEquals("Boromir", character.name.value)
        assertEquals("dnd5e", character.systemId.value)
        assertEquals(created, character.createdAt)
        assertEquals(updated, character.updatedAt)
    }

    @Test
    fun `CharacterId fromString returns correct id`() {
        val id = CharacterId.fromString("abc-123")
        assertEquals("abc-123", id.value)
    }
}

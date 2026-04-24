package com.ddsheet.core.domain.usecase

import com.ddsheet.core.domain.fake.FakeCharacterRepository
import com.ddsheet.core.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreateCharacterUseCaseTest {

    private lateinit var repository: FakeCharacterRepository
    private lateinit var useCase: CreateCharacterUseCase

    @Before
    fun setup() {
        repository = FakeCharacterRepository()
        useCase = CreateCharacterUseCase(repository)
    }

    @Test
    fun `invoke with valid name creates and persists character`() = runTest {
        val result = useCase.invoke("Gimli", SystemId("dnd5e"))

        assertTrue("Expected Right, got $result", result is Either.Right)
        val character = (result as Either.Right).value
        assertEquals("Gimli", character.name.value)
        assertEquals("dnd5e", character.systemId.value)

        // Verificamos que se persistió
        val found = repository.findById(character.id)
        assertTrue("Character should be persisted", found is Either.Right)
    }

    @Test
    fun `invoke with empty name returns left without persisting`() = runTest {
        val result = useCase.invoke("", SystemId("dnd5e"))

        assertTrue("Expected Left for empty name, got $result", result is Either.Left)
        val all = repository.findAll()
        assertEquals(0, (all as Either.Right).value.size)
    }

    @Test
    fun `invoke when repository fails returns left with persistence error`() = runTest {
        repository.failNextSave = true

        val result = useCase.invoke("Aragorn", SystemId("dnd5e"))

        assertTrue("Expected Left, got $result", result is Either.Left)
        val error = (result as Either.Left).value
        assertTrue("Should be ValidationFailed", error is DomainError.ValidationFailed)
    }

    @Test
    fun `invoke with different systemIds preserves system separation`() = runTest {
        val lotrSystem = SystemId("lotr")
        
        val result = useCase.invoke("Legolas", lotrSystem)

        assertTrue(result is Either.Right)
        val character = (result as Either.Right).value
        assertEquals("lotr", character.systemId.value)
    }
}

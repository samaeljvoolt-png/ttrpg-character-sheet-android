package com.ddsheet.core.domain.model

import org.junit.Assert.*
import org.junit.Test

class AttributeValueTest {

    @Test
    fun `GameCharacterSheet find returns correct type`() {
        val key = AttributeKey("strength")
        val sheet = GameCharacterSheet(
            attributes = mapOf(key to NumberValue(16))
        )

        val found: NumberValue? = sheet.find(key)
        assertNotNull(found)
        assertEquals(16, found?.number)
    }

    @Test
    fun `find with wrong type returns null`() {
        val key = AttributeKey("name")
        val sheet = GameCharacterSheet(
            attributes = mapOf(key to TextValue("Gandalf"))
        )

        val found: NumberValue? = sheet.find(key)
        assertNull(found)
    }

    @Test
    fun `withAttribute adds new value`() {
        val sheet = GameCharacterSheet()
            .withAttribute(AttributeKey("strength"), NumberValue(16))
            .withAttribute(AttributeKey("dexterity"), NumberValue(14))

        assertEquals(16, sheet.find<NumberValue>(AttributeKey("strength"))?.number)
        assertEquals(14, sheet.find<NumberValue>(AttributeKey("dexterity"))?.number)
    }

    @Test
    fun `withAttribute replaces existing value`() {
        val sheet = GameCharacterSheet()
            .withAttribute(AttributeKey("level"), NumberValue(1))
            .withAttribute(AttributeKey("level"), NumberValue(2))

        assertEquals(2, sheet.find<NumberValue>(AttributeKey("level"))?.number)
        assertEquals(1, sheet.attributes.size) // solo una key
    }

    @Test
    fun `withAttribute marks dirty`() {
        val sheet = GameCharacterSheet()
            .withAttribute(AttributeKey("name"), TextValue("Test"))

        assertTrue(sheet.metadata.isDirty)
    }

    @Test
    fun `withClean removes dirty flag preserving attributes`() {
        val sheet = GameCharacterSheet()
            .withAttribute(AttributeKey("str"), NumberValue(10))
            .withClean()

        assertFalse(sheet.metadata.isDirty)
        assertEquals(10, sheet.find<NumberValue>(AttributeKey("str"))?.number)
    }

    @Test
    fun `ValidationResult with no errors is valid`() {
        val result = ValidationResult(isValid = true)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `ValidationResult with errors is invalid`() {
        val result = ValidationResult(
            isValid = false,
            errors = listOf(
                ValidationResult.ValidationError(AttributeKey("hp"), "HP cannot be negative")
            )
        )
        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun `Either map transforms right value`() {
        val either: Either<String, Int> = Either.Right(5)
        val result = either.map { it * 2 }

        assertEquals(10, (result as Either.Right).value)
    }

    @Test
    fun `Either map preserves left value`() {
        val either: Either<String, Int> = Either.Left("error")
        val result = either.map { it * 2 }

        assertEquals("error", (result as Either.Left).value)
    }

    @Test
    fun `Either flatMap chains right values`() {
        val either: Either<String, Int> = Either.Right(5)
        val result = either.flatMap { Either.Right(it + 3) }

        assertEquals(8, (result as Either.Right).value)
    }

    @Test
    fun `flatMap shortcircuits on left`() {
        val either: Either<String, Int> = Either.Left("fail")
        var called = false
        val result = either.flatMap { called = true; Either.Right(it) }

        assertEquals("fail", (result as Either.Left).value)
        assertFalse("flatMap body should NOT execute for Left", called)
    }

    @Test
    fun `NonEmptyString create succeeds for non-empty string`() {
        val result = NonEmptyString.create("hello")
        assertTrue(result.isSuccess)
        assertEquals("hello", result.getOrThrow().value)
    }

    @Test
    fun `NonEmptyString create fails for empty string`() {
        val result = NonEmptyString.create("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `NonEmptyString create fails for whitespace-only string`() {
        val result = NonEmptyString.create("   ")
        assertTrue(result.isFailure)
    }
}

---
name: code-style-guide
description: Guía de estilo de código para el proyecto TTRPG Character Sheet Android.
version: 1.0.0
alwaysApply: true
---

# TTRPG Character Sheet — Code Style Guide

## 1. Core Philosophy
- **Código Sostenible (Sustainable Code)**: Priorizar legibilidad, mantenibilidad y testabilidad. El código se escribe para los humanos que lo mantendrán.
- **SOLID Principles**: Aplicarlos diligentemente para reducir acoplamiento y aumentar cohesión.
  - **S**ingle Responsibility: Clases y funciones deben hacer una sola cosa bien.
  - **O**pen/Closed: Abiertos para extensión, cerrados para modificación.
  - **L**iskov Substitution: Los subtipos deben ser sustituibles por sus tipos base.
  - **I**nterface Segregation: Interfaces pequeñas y específicas para cada cliente.
  - **D**ependency Inversion: Depender de abstracciones, no de concreciones.

## 2. Kotlin Standards
- **Conventions**: Seguir las [Kotlin Coding Conventions oficiales](https://kotlinlang.org/docs/coding-conventions.html).
- **Naming**:
  - Clases / Interfaces / Composables: `PascalCase`
  - Funciones / Variables / Properties: `camelCase`
  - Constants (`const val`): `UPPER_SNAKE_CASE`
- **Immutability**: Preferir `val` sobre `var` por default. Favorecer colecciones inmutables (`List` vs `MutableList`).
- **Null Safety**: Evitar `!!` por completo. Manejar nulls con safe calls (`?.`), Elvis (`?:`), o `let`.

## 3. Architecture & UI (Jetpack Compose)
- **Terminology**: Usar terminología estandarizada del codebase (`ViewState`, `Contract`, intents/events).
- **Data Flow**:
  - Seguir Unidirectional Data Flow (UDF).
  - El estado de UI debe ser colectado desde un `ViewModel` o state holder reaccionando a un evento `Contract`.
- **Composable Functions**:
  - Siempre incluir argumento `modifier: Modifier = Modifier` en todos los Composables que emitan UI.
  - Practicar State Hoisting: componentes UI stateless recibiendo primitivas/data classes y emitiendo eventos (callbacks).
  - Evitar pasar instancias de `ViewModel` a Composables granulares para mantenerlos testeables y reusables.

## 4. Domain Layer Rules
- **Dominio puro**: `core:domain` y `:domain:*` **sin imports de Android**.
- **Either over Exceptions**: Usar `Either<DomainError, T>` para control de flujo. Nunca excepciones como control de flujo en dominio.
- **Value objects**: Preferir `@JvmInline value class` para tipos primitivos con semántica (ej: `CharacterId`, `SystemId`).
- **Result vs Either**: `Result<T>` se permite en plugins y boundaries externos. `Either<DomainError, T>` es el default interno en domain.
- **Smart constructors**: Usar `companion object` con métodos factory (`create`, `of`) que retornen `Either` o `Result` en vez de constructores que lancen.

## 5. Documentation
- **Public APIs**: Toda función, interfaz, clase o constructo `public` DEBE tener KDoc adecuado explicando qué hace y por qué.
- **Complex Logic**: Todo bloque de lógica compleja, nuanced o workaround debe tener comentario inline explicando su racional. Explicar el *Porqué*, no el *Qué*.

## 6. Evolution & Commits
- **Backward Compatibility**: Default a cambios no-breaking. Mantener funcionalidad existente mientras se despliega lo nuevo.
- **Test-Driven Ethos**: Recordar las reglas del design-log: escribir/actualizar tests *primero* para reflejar el nuevo comportamiento.
- **Commits atómicos**: Cada commit debe compilar y pasar tests. Un commit por fase del implementation plan cuando sea posible.

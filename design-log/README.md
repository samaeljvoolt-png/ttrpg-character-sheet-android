# Design Log — TTRPG Character Sheet Android

> Directorio `./design-log/` del repositorio.
> Cerebro arquitectónico del proyecto. La IA lee esto antes de tocar código.

## ¿Qué es esto?

Sistema de **Architecture Decision Records (ADRs) adaptados para flujo con IA**.
Cada archivo es un "snapshot in time" de una decisión, feature o diseño.
A diferencia del README (que intenta estar "actualizado" y falla), estos logs son **inmutables**.

## ¿Por qué?

- Evitar el "Context Wall": la IA contradice decisiones previas porque no las recuerda.
- Reducir prompts de 500 tokens a 1 oración: "Implementá el Design Log #04".
- Preservar racional de decisiones. En 3 meses, sabemos POR QUÉ se hizo así.

## Archivos de reglas obligatorias

| Archivo | Propósito |
|---------|-----------|
| `design-log-rules.md` | Reglas del framework Design-Log (metodología) |
| `core-personality.md` | Comportamiento y personalidad de la IA |
| `code-style-guide.md` | Estilo de código Kotlin, SOLID, Compose |

## Logs existentes

| # | Archivo | Estado | Tema |
|---|---------|--------|------|
| 00 | `00-project-context.md` | Living doc | Contexto base, arquitectura, stack, PRD alignment |
| 01 | `01-either-monad-error-handling.md` | Completed | Either<L,R> y DomainError |
| 02 | `02-gamesystem-plugin-interface.md` | Completed | GameSystemPlugin |
| 03 | `03-game-character-sheet-attributes.md` | Completed | GameCharacterSheet, AttributeKey, AttributeValue |
| 04 | `04-create-character-usecase.md` | Completed | CreateCharacterUseCase |
| 05 | `05-get-character-sheet-usecase.md` | Completed | GetCharacterSheetUseCase |
| 06 | `06-room-entity-design.md` | Draft | Persistencia Room |
| 07 | `07-prd-alignment-gap.md` | Approved | Plan de migración: spike → modelo PRD completo |
| 08 | `08-phase1-domain-model.md` | Completed | Fase 1: Expandir Domain Model |

## Documentos de spec oficiales (root del repo)

| Archivo | Propósito |
|---------|-----------|
| `PRD.md` | Product Requirements Document — todas las features, campos, specs de D&D 5e |
| `DESIGN.md` | Domain Model Reference — todas las data classes del modelo de dominio |
| `ARCHITECTURE.md` | Spec técnico de módulos, dependencias, features, testing strategy |

## Convención de nombres

```
{NN} - {tema breve en kebab-case}.md
```

## Flujo de trabajo

1. **Nueva tarea**: Buscar logs existentes relacionados.
2. **No existe log**: Crear usando `template.md`. Llenar Background, Problem, Q&A, Design.
3. **Review**: Usuario revisa el markdown. Se responden preguntas en Q&A.
4. **Aprobación**: Se congela Design. Se autoriza implementación.
5. **Implementación**: Se codea siguiendo Implementation Plan.
6. **Post-implementación**: Se agrega `Implementation Results` con tests, desvíos, aprendizajes.

## Reglas rápidas para humanos

- Nunca edites la sección `Design` de un log cuya implementación ya empezó.
- Si el scope cambia, crea un nuevo log, no mutées el viejo.
- Mantené los logs concisos. 2-3 páginas máximo. Brain dump → editado.
- Usá Mermaid para diagramas cuando sea más rápido que explicar con texto.

---

Última actualización de este README: 2026-04-28.

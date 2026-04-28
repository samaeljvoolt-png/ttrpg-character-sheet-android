---
name: design-log-rules
description: Reglas obligatorias del framework Design-Log para el proyecto TTRPG Character Sheet.
version: 1.0.0
alwaysApply: true
---

# Design Log Framework — Project Rules

> El proyecto sigue una metodología rigurosa de design logs para toda feature significativa y cambio arquitectónico.

---

## Antes de hacer cambios

1. **Revisar** design logs existentes en `./design-log/` para encontrar diseños e implementaciones previas.
2. **Para features nuevas**: Crear design log primero, obtener aprobación, luego implementar.
3. **Leer** logs relacionados para entender contexto y constraints.

---

## Al crear Design Logs

1. **Estructura**: Background → Problem → Questions and Answers → Design → Implementation Plan → Examples → Trade-offs
2. **Especificidad**: Incluir file paths, type signatures, reglas de validación.
3. **Ejemplos**: Usar ✅/❌ para patrones correctos vs incorrectos. Incluir código realista.
4. **Explicar el porqué**: No solo describir qué, sino la racional y los trade-offs.
5. **Hacer preguntas** (en el archivo): Para cualquier cosa que no esté clara o falte información.
6. **Al responder**: Mantener la pregunta, agregar la respuesta debajo.
7. **Brevedad**: Explicaciones cortas, solo lo más relevante.
8. **Diagramas**: Usar Mermaid inline cuando aporte claridad.

---

## Al implementar

1. Seguir las fases del implementation plan del design log.
2. Escribir tests primero o actualizar tests existentes para reflejar el nuevo comportamiento.
3. **NO actualizar** las secciones iniciales del design log una vez comenzada la implementación.
4. **Apendizar** el design log con sección "Implementation Results" a medida que avanza.
5. **Documentar desviaciones**: Explicar por qué la implementación difiere del diseño.
6. **Reportar tests**: Incluir resultados (X/Y passing) en las notas de implementación.
7. Al finalizar, agregar un resumen de desviaciones del diseño original.

---

## Al responder preguntas o hacer referencias

1. Referenciar design logs por número cuando sea relevante (ej: "Ver Design Log #02").
2. Usar terminología del codebase: `Character`, `GameSystemPlugin`, `GameCharacterSheet`, `AttributeKey`, `AttributeValue`, `DomainError`, `Either`, `ViewState`, `Contract`.
3. Mostrar type signatures: este es un proyecto Kotlin con uso intensivo de tipos.
4. Considerar backward compatibility: default a cambios no-breaking.

---

## Regla de oro

> **Si no hay log, no hay luz verde.** No escribas código que no entiendas arquitectónicamente.

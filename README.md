# Recordatorios Citas Service

**Universidad Francisco de Paula Santander — Patrones de Diseño de Software (1156701-A)**
**Estudiante:** Diego Armando Márquez Rigaud — **Código:** 1152115

## Descripción

Servicio Spring Boot que envía recordatorios de citas por el canal preferido del paciente, construido con
Strategy (`ReminderChannel`) y Factory Method (`ReminderChannelFactory`) inyectados por constructor.

## Parte 1 — Pruebas Unitarias con JUnit 5 y Mockito

`CitaReminderServiceImplTest` aísla `CitaReminderServiceImpl` de sus tres colaboradores:

| Colaborador | Test Double | Por qué |
|---|---|---|
| `CitaRepository` | Mock | Se controla la respuesta de la consulta y además se verifica que `save()` se llama una vez con la cita marcada. |
| `ReminderChannelFactory` / `ReminderChannel` | Mock | Lo que se prueba es la selección de estrategia: que se pida el canal correcto y que reciba el mensaje. |
| `Clock` | Fake (`Clock.fixed`) | El JDK ya trae una implementación real con el instante congelado; mockearlo sería más frágil. |

```bash
mvn test
```

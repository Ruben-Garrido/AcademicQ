# 📅 Plan y Cronograma de Trabajo - AcademiQ (Hitos 2.5 y 3)

Este documento detalla las tareas estructuradas para finalizar el sistema de Triage y Gestión de Solicitudes Académicas, alineado con los Requerimientos Funcionales (RF) del proyecto.

## 🛠️ FASE 1: Correcciones y Refactorización Core
**Objetivo:** Asegurar que la lógica de negocio actual sea a prueba de fallos antes de exponerla a Angular.

- [ ] **1.1. Validar y Restringir Fechas Límite (SLA)**
  - **Acción:** Añadir validación (`@Future` o `@FutureOrPresent`) en el DTO `ClasificarRequest` y establecer un tope máximo (ej. 15 o 30 días) en la lógica de negocio (`SolicitudService`).
  - **Justificación:** Evitar que fechas en el pasado generen falsos positivos (prioridad ALTA por accidente). Además, imponer un límite máximo evita que los funcionarios asignen plazos absurdamente largos que violen los tiempos de respuesta de la universidad, balanceando la flexibilidad del funcionario con el control institucional.
- [ ] **1.2. Implementar `GlobalExceptionHandler` (Controlador de Excepciones)**
  - **Acción:** Crear una clase con `@ControllerAdvice` para atrapar `TransicionEstadoInvalidaException` u otros errores.
  - **Justificación:** Proteger la aplicación de crashear o enviar pantallas de error 500 al cliente. Se deben devolver JSONs estructurados que Angular pueda interpretar fácilmente y mostrar como alertas ordenadas al usuario.
- [ ] **1.3. Endpoint de Simulación de Prioridad (Previo a Guardar)**
  - **Acción:** Crear un endpoint `GET /api/v1/solicitudes/simular-prioridad` que reciba el impacto y la fecha, y devuelva la prioridad calculada sin guardar en BD.
  - **Justificación:** Mantener la Single Source of Truth (Única fuente de verdad) en el backend. Permite al frontend (Angular) consultar y mostrar al usuario qué prioridad le asignará el sistema *antes* de confirmar la clasificación.

## 🔐 FASE 2: Seguridad y Autenticación (RF-13)
**Objetivo:** Proteger la API, restringir acceso por roles y gestionar la trazabilidad real de los usuarios.

- [ ] **2.1. Implementar Spring Security y JWT**
  - **Acción:** Crear la configuración de seguridad, el proveedor de tokens JWT y los filtros de intercepción HTTP.
  - **Justificación:** (Cubrir RF-13). Hasta ahora se usa un UUID plano en el Header (`X-Usuario-Id`), lo que permite el *spoofing* (hackeo de identidad). Con JWT forzamos autenticación encriptada e inviolable.
- [ ] **2.2. Endpoint de Autenticación (Login)**
  - **Acción:** Crear un `AuthController` que devuelva el Token JWT al validar un usuario y contraseña existentes en BD.
  - **Justificación:** Sistema obligatorio para el acceso de operadores y estudiantes en la versión web.
- [ ] **2.3. Restricción de Endpoints por Roles**
  - **Acción:** Aplicar `@PreAuthorize` en el `SolicitudController`.
  - **Justificación:** Un "Estudiante" solo debe registrar solicitudes. Un "Coordinador/Responsable" es el único autorizado para interactuar con los endpoints PATCH (clasificar, asignar, atender). 

## 🤖 FASE 3: Valor Agregado - Integración de IA (RF-09 / RF-10)
**Objetivo:** Cumplir el criterio de **Excelencia (5.0)** en la rúbrica, garantizando que el sistema principal no dependa de ella (RF-11).

- [ ] **3.1. Desarrollo de Endpoint Asistente AI (Sugeridor o Resumidor)**
  - **Acción:** Implementar un servicio HTTP (`RestClient`) que se comunique con la API de Google Gemini (nivel gratuito) para generar un resumen de la solicitud en base al historial de la misma.
  - **Justificación:** Ataca el RF-09. Provee un resumen textual instantáneo para los funcionarios. Ayuda a descomprimir casos donde el estudiante escribe justificaciones muy largas o el historial tiene demasiadas idas y vueltas.

## 🖥️ FASE 4: Frontend e Integración (Angular - Hito 3)
**Objetivo:** Materializar el backend en la interfaz visual solicitada por la guía guía maestra.

- [ ] **4.1. Configuración Angular: Servicios y Guards**
  - **Acción:** Integrar cliente HTTP, interceptores JWT para poder enviar el token a Spring Boot, y "Route Guards" para proteger las pantallas de panel.
  - **Justificación:** Interfaz requerida para el usuario final en el Hito 3.
- [ ] **4.2. Dashboards de Triage y Tableros**
  - **Acción:** Vistas dinámicas donde los responsables ven listas ordenadas por el color de prioridad (ALTA, MEDIA, BAJA) gracias al `Listar()`.
  - **Justificación:** Materializa la filosofía del negocio "Triage", facilitando visibilidad operativa (RF-07).

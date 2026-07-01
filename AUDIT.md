# AUDIT.md — Auditoría técnica de Semtex Backend

> Fase 1 del encargo. Revisión del repositorio **tal cual está hoy** (`main`, commit `f9621b5`), contrastado
> con la especificación funcional (`docs/API_CONTRACT.md`) y la arquitectura objetivo solicitada.
> Este documento **no modifica código**; es la base para acordar el plan de la Fase 3.

---

## 1. Resumen ejecutivo

El proyecto es un MVP **sorprendentemente completo y prolijo** para ser un primer commit: arquitectura
hexagonal global razonable, dominio POJO limpio, JSONB bien mapeado con Hibernate 6, migraciones Flyway
con índices pensados, manejo de errores centralizado y un contrato de API documentado. La base es buena.

Sin embargo, hay **dos hallazgos bloqueantes** que invalidan promesas centrales del producto:

1. **El aislamiento multi-inquilino NO está garantizado en la capa de aplicación/persistencia.** Depende de
   que cada controller recuerde llamar a `TenantGuard` y de que el `organizationId` viaje correctamente como
   parámetro en cada query. No hay filtro Hibernate, ni repositorio tenant-aware, ni `TenantContext`. Un solo
   endpoint nuevo que olvide el guard = fuga de datos entre empresas. Los comentarios "*desnormalizado para
   RLS eficiente*" son engañosos: **el RLS de Supabase no aplica cuando Java se conecta por JDBC con pool**.
2. **El agente de IA no cierra el ciclo de function calling y bloquea el hilo HTTP + la transacción de BD.**
   El `@Transactional` sobre la llamada al LLM mantiene una conexión del pool retenida durante toda la
   inferencia lenta; y cuando el modelo responde con un `tool_call`, no hay segundo round-trip, por lo que
   `agentResponse` suele quedar **vacío**.

El resto son brechas de alineación con la arquitectura objetivo (LangChain4j, ArchUnit, tests, MapStruct,
storage real, AOP de auditoría) y mejoras de calidad. La estructura **hexagonal global por capas**
(`domain · application · infrastructure`) se mantiene tal cual: es correcta y suficiente para el alcance.

**Veredicto:** base aprovechable, pero requiere refactor de tenancy y del adaptador de IA antes de poder
considerarse seguro. Recomiendo **conservar y migrar** el código actual, no reescribir desde cero.

---

## 2. Inventario: lo que existe vs. lo que debería existir

### 2.1 Estructura actual (hexagonal **global por capas**)

```
com.semtex
├── domain
│   ├── model        Organization, User, Document, FinancialRecord, ChatMessage, AuditLog   ✔ POJOs puros
│   ├── enums        UserRole(ADMIN/OPERATOR/AUDITOR), MessageRole, AuditAction
│   └── port
│       ├── in       UploadDocumentUseCase, QueryFinancialDataUseCase, SendEmailUseCase
│       └── out      *RepositoryPort (6), AiServicePort, EmailServicePort                    ✖ falta FileStoragePort
├── application
│   └── service      DocumentApplicationService, ChatApplicationService                      ✖ importa infraestructura
└── infrastructure
    ├── in
    │   ├── rest     6 controllers + DTOs + mappers manuales + advice (GlobalExceptionHandler) + TenantGuard
    │   ├── parsing  ExcelCsvParserService                                                   ✖ ubicación incorrecta
    │   └── security JwtAuthFilter, JwtService, SecurityConfig, SemtexPrincipal
    └── out
        ├── ai        OllamaAiAdapter (RestClient manual)                                     ✖ no LangChain4j
        ├── email     SmtpEmailAdapter
        └── persistence  entity(6) + repository(6) + adapter(6) + mapper(5, manuales)
```

### 2.2 Brecha contra la arquitectura objetivo

| Área | Spec objetivo | Estado actual | Gap |
|---|---|---|---|
| Estructura | Hexagonal **global por capas** (`domain · application · infrastructure`) | Hexagonal global por capas | ✔ Se mantiene |
| Java | 21 | **17** (`pom.xml`) | ✖ |
| Tenancy | `TenantContext` request-scoped + filtrado en persistencia | `TenantGuard` manual en controllers | ✖ **Bloqueante** |
| IA | LangChain4j + Ollama, `@Tool` (`consultarDatosFinancieros`, `compararPeriodos`, `enviarCorreo`), Llama **3.1** 8B | RestClient manual, parsing a mano, 1 tool (`send_email`), `llama3:8b` (3.0) | ✖ **Bloqueante** |
| Storage | `FileStoragePort` + adaptador (Supabase/S3) | No existe; bytes se descartan | ✖ |
| Mapeo entidad↔dominio | MapStruct | Mappers **manuales** | △ funciona, no cumple spec |
| Auditoría | **AOP aspect** en contexto `audit` | Llamadas manuales inline | ✖ |
| Calidad | **ArchUnit** + Testcontainers + JUnit5/Mockito | **0 tests, sin `src/test`** | ✖ |
| JSONB | hypersistence-utils o JSON nativo H6 | JSON nativo H6 (`@JdbcTypeCode`) | ✔ OK |
| Migraciones | Flyway, sin `ddl-auto=update` | Flyway + `ddl-auto=validate` | ✔ OK |
| OpenAPI | springdoc en controllers | springdoc presente | △ falta anotar |
| Validación | Bean Validation en DTOs | `@Valid` presente | ✔ OK |
| GitFlow | main/develop/feature/release + commits fechados | 1 commit, sin ramas/tags | ✖ |

---

## 3. Hallazgos por severidad

### 🔴 CRÍTICO

| ID | Hallazgo | Evidencia |
|---|---|---|
| **C-1** | **Aislamiento tenant sólo por convención.** Sin filtro Hibernate ni repos tenant-aware; cada query recibe `organizationId` por parámetro y cada controller debe acordarse de `TenantGuard`. Olvidarlo en un endpoint = fuga cross-tenant. | `TenantGuard.java`, todos los `*JpaRepository` |
| **C-2** | **El cliente provee su propio `organizationId`/`userId`** (en body/params) y el backend sólo lo compara contra el token. El tenant debería derivarse **exclusivamente** del token, nunca confiar en el input. | `ChatController` L42-53, `FinancialRecordController` L33-39, `DocumentController` (form fields) |
| **C-3** | **`@Transactional` envuelve la inferencia del LLM.** `ChatApplicationService.query()` retiene una conexión del pool de Hikari durante toda la llamada lenta a Ollama → agotamiento de pool bajo concurrencia. | `ChatApplicationService.java` L29, L75-77 |
| **C-4** | **Function calling roto:** cuando Ollama devuelve `tool_calls`, el `content` viene vacío y **no hay segundo round-trip** para devolverle el resultado de la tool al modelo. La respuesta al usuario queda vacía y la "conversación" se corta. | `OllamaAiAdapter.parseOllamaResponse` L87-108; `ChatApplicationService.handleFunctionCall` L115-126 |

### 🟠 ALTO

| ID | Hallazgo | Evidencia |
|---|---|---|
| **A-1** | **Hilo HTTP bloqueado sin timeout.** `RestClient` a Ollama sin `connect/read timeout` y `stream=false`. Una inferencia colgada bloquea el worker de Tomcat indefinidamente. No hay SSE ni `@Async`. | `OllamaAiAdapter` L51-85 |
| **A-2** | **Archivos subidos sin destino real.** Tras parsear, los bytes se descartan; `storagePath` es un string fabricado (`org-id/filename`) que no apunta a nada. No hay `FileStoragePort`. Imposible re-descargar el original. | `DocumentApplicationService` L56-66 |
| **A-3** | **Violación de la regla de dependencia:** la capa `application` importa `infrastructure.in.parsing.ExcelCsvParserService`. El parseo debería ser un puerto OUT (o estar en domain/application como servicio puro). | `DocumentApplicationService` L10-11 |
| **A-4** | **Controllers inyectan puertos OUT directamente** (`FinancialRecordRepositoryPort`, `ChatMessageRepositoryPort`), saltándose los casos de uso. El adaptador *driving* habla con un puerto *driven*: rompe el flujo hexagonal y dispersa lógica de tenant. | `FinancialRecordController` L19, `ChatController` L24, `AuditLogController` |
| **A-5** | **JWT con secreto simétrico HS256.** Supabase moderno firma con claves asimétricas (RS256/ES256 vía JWKS). Falta validación de `audience` y tolerancia de reloj. Un filtro `jjwt` manual reimplementa lo que `spring-boot-starter-oauth2-resource-server` haría de forma estándar. | `JwtService` L32-50 |
| **A-6** | **0 tests.** No existe `src/test`. Sin ArchUnit no hay nada que impida que mañana el dominio importe infraestructura (ya ocurre, ver A-3). | repo completo |

### 🟡 MEDIO

| ID | Hallazgo | Evidencia |
|---|---|---|
| **M-1** | Búsqueda JSONB fuerza el valor a string en `jsonb_build_object(:fieldName, :fieldValue)`; un match exacto sobre un número (`ingreso = 1000`) falla porque en BD es numérico, no `"1000"`. | `FinancialRecordJpaRepository` L31-46 |
| **M-2** | `findAllByOrganizationId` y `findByJsonField` mezclan filas de **distintos documentos/hojas** sin discriminar; el contexto de IA puede contaminarse entre archivos no relacionados. | `FinancialRecordPersistenceAdapter` L56-71 |
| **M-3** | Contexto de IA inyectado como texto plano en el system prompt (no como tool ni RAG). Con `max-context-records=200` filas el prompt puede exceder la ventana del modelo y degradar respuestas. | `OllamaAiAdapter.serializeContext` L115-124 |
| **M-4** | `spring.jpa...preferred_uuid_jdbc_type=varchar` guarda UUID como texto en vez de tipo `uuid` nativo de PG; rompe joins/índices óptimos y es inconsistente con migraciones que usan `UUID`. | `application-example.properties` L29 |
| **M-5** | Mappers REST y de persistencia **manuales y duplicados** (toEntity/toDomain repetido en cada adapter). Propenso a olvidar un campo. Spec pide MapStruct. | `*PersistenceAdapter`, `*RestMapper` |
| **M-6** | Auditoría acoplada: cada servicio llama `auditLogRepo.log(...)` a mano. Fácil de olvidar; spec pide **AOP**. | `ChatApplicationService`, `DocumentApplicationService` |
| **M-7** | Manejo de errores con `throw new RuntimeException(...)` genérico en adaptadores (Ollama, parser). Se pierde el tipo y el `ApiExceptionHandler` no puede mapearlos a un status adecuado (todo cae a 500). | `OllamaAiAdapter` L83, `ExcelCsvParserService` L94, L127 |

### 🟢 BAJO

| ID | Hallazgo | Evidencia |
|---|---|---|
| **B-1** | `parse()` con precedencia de operadores confusa: `mimeType.contains("csv") || fileName...endsWith(".csv")` mezclado con `&&` sin paréntesis claros. Funciona por suerte de precedencia, pero es frágil. | `ExcelCsvParserService` L46 |
| **B-2** | Nomenclatura de rol: la spec del encargo dice `OPERADOR`, el código/contrato usa `OPERATOR`. Conviene fijar uno (recomiendo `OPERATOR` por consistencia con el API ya documentado). | `UserRole.java` |
| **B-3** | `README` declara "Java 17" y `mvn test` como verificación, pero no hay tests. Documentación adelantada al estado real. | `README.md` |
| **B-4** | Sin paginación real (solo `limit` + tope 1000). Para datasets grandes faltará cursor/offset. | controllers de listado |
| **B-5** | CORS y Swagger públicos están OK, pero `actuator` sólo expone health/info; revisar que no se filtre `/actuator/env` en prod. | `SecurityConfig`, properties |

---

## 4. Validación de los 5 puntos que pediste verificar

### 4.1 Aislamiento multi-inquilino (RLS vs. capa de aplicación) — ❌ **NO resuelto. BLOQUEANTE.**
Confirmado: con conexión JDBC + pool Hikari, **el RLS de Postgres/Supabase no se aplica** (no hay `SET app.current_tenant`
ni `SET ROLE` por request; todas las conexiones usan el mismo rol). El aislamiento real depende 100% de:
(a) que el controller llame `TenantGuard.requireSameOrganization`, y (b) que el `organizationId` se pase bien en cada
query. **No existe** filtro Hibernate (`@FilterDef/@Filter`), ni repositorio tenant-aware, ni `TenantContext`
request-scoped. Es exactamente el escenario que marcaste como bloqueante.
**Recomendación:** `TenantContext` (request-scoped) poblado por el filtro JWT + filtro Hibernate global por
`organization_id` activado por interceptor, de modo que el aislamiento sea **por defecto** y no por convención.

### 4.2 Function Calling — ❌ **Enfoque frágil de parsing manual.**
No usa LangChain4j ni Llama **3.1** (usa `llama3:8b` = 3.0). Arma el JSON de `tools` a mano, hace `POST /api/chat`
con `RestClient`, y parsea `tool_calls[0].function.arguments` manualmente. Solo existe `send_email`; faltan
`consultarDatosFinancieros` y `compararPeriodos` (los datos se "empujan" al prompt en vez de exponerse como tool).
Además **no cierra el loop** (ver C-4). **Recomendación:** LangChain4j `AiServices` + `@Tool` sobre Llama 3.1 8B,
que gestiona el round-trip de tools nativamente.

### 4.3 JSONB — ✅ **Bien resuelto** (con un matiz).
`@JdbcTypeCode(SqlTypes.JSON)` sobre `Map<String,Object>` + `columnDefinition="jsonb"` es el enfoque nativo correcto
de Hibernate 6 (no hace falta hypersistence-utils). Índice GIN presente y consulta con `@>` que sí lo aprovecha.
Matiz: el match exacto castea a string (M-1) y `preferred_uuid_jdbc_type=varchar` es subóptimo (M-4).

### 4.4 Almacenamiento de archivos — ❌ **Sin destino real.**
`storage_path` se construye como string pero los bytes nunca se guardan (ni disco, ni S3, ni Supabase Storage):
tras parsear se descartan. No hay `FileStoragePort`. **Recomendación:** definir `FileStoragePort` + adaptador
(Supabase Storage o S3/MinIO local para dev) y persistir el `storagePath` real devuelto por el adaptador.

### 4.5 Inferencia lenta / bloqueo de hilo — ❌ **Bloquea hilo y transacción.**
`stream=false`, `RestClient` síncrono **sin timeouts**, y peor aún dentro de un `@Transactional` (C-3): se retiene
conexión de BD + worker de Tomcat durante toda la inferencia. No hay SSE ni ejecución asíncrona.
**Recomendación mínima:** sacar la llamada al LLM fuera de la transacción + timeouts de connect/read.
**Recomendación completa:** streaming SSE (`text/event-stream`) y/o `@Async` con `DeferredResult`/`SseEmitter`.

---

## 5. Cuestionamientos de diseño (decisiones a validar contigo)

1. **¿El frontend debe seguir enviando `organizationId`/`userId` en el body?** Recomiendo **eliminarlos del
   contrato** y derivarlos siempre del token (rompe compatibilidad con `API_CONTRACT.md` actual). — *Decisión tuya.*
2. **Estructura de paquetes:** se mantiene la **hexagonal global por capas** (`domain / application /
   infrastructure`), por simplicidad y claridad expositiva. No se reorganiza por bounded context; las áreas
   funcionales (identity, document, financial, chat, email, audit) conviven como clases dentro de cada capa.
3. **JWT: ¿HS256 (secreto compartido, dev rápido) o RS256/JWKS (producción Supabase real)?** Recomiendo soportar
   **resource-server con JWKS** y dejar HS256 sólo para tests locales. — *Decisión tuya.*
4. **Storage para dev:** ¿Supabase Storage real, S3, o **MinIO en Docker** para no depender de la nube en local?
   Recomiendo MinIO/S3 para dev + perfil Supabase para prod.
5. **MapStruct vs. mappers manuales:** la spec pide MapStruct; introducirlo añade `annotationProcessor`. Lo
   recomiendo, pero confirma que aceptas el coste de build.
6. **Estrategia de IA:** ¿reemplazo total por LangChain4j, o adaptador nuevo conviviendo detrás del mismo
   `AiGatewayPort` para poder comparar? Recomiendo reemplazo detrás del puerto (el dominio no se entera).

---

## 6. Plan propuesto hacia la arquitectura objetivo (resumen, detalle en Fase 3)

1. **Cimientos:** subir a Java 21; añadir deps (LangChain4j, ArchUnit, Testcontainers, MapStruct, AWS/MinIO SDK).
2. **`shared`:** `TenantContext` request-scoped, `GlobalExceptionHandler`, base de seguridad, filtro Hibernate tenant.
3. **Tenancy by-default:** filtro JWT puebla `TenantContext`; interceptor activa el filtro Hibernate; los repos dejan
   de recibir `organizationId` por parámetro (lo aplica la infraestructura). Tests que prueben la fuga imposible.
4. **Mantener la hexagonal global por capas**; introducir MapStruct y ArchUnit verificando la regla de
   dependencia `infrastructure → application → domain` en cada paso.
5. **Storage real** (`FileStoragePort`).
6. **IA:** `AiGatewayPort` + LangChain4j/Ollama (Llama 3.1 8B) con `@Tool`; sacar el LLM de la transacción; timeouts; SSE.
7. **Auditoría AOP** en `audit`.
8. **Tests** (use cases con Mockito; adaptadores con Testcontainers; ArchUnit) y **GitFlow** con el historial fechado.

---

## 7. Lo que ya está bien (no tocar sin razón)

- Dominio POJO **sin** anotaciones de Spring/JPA (verificado: 0 imports prohibidos en `domain`).
- JSONB nativo Hibernate 6 + índice GIN + consulta `@>`.
- Flyway con `ddl-auto=validate` (nada de `update` en prod) y migraciones legibles con comentarios.
- `ApiExceptionHandler` centralizado y formato de error consistente para el frontend.
- Separación entity (infra) ↔ domain model con mappers (aunque manuales).
- `application.properties` real fuera de git con `application-example.properties`.
- Índices compuestos (V6) pensados para las rutas reales de la API.

---

*Fin de la Fase 1. A la espera de tu visto bueno y de las decisiones de la sección 5 antes de iniciar la Fase 3.*

# Piano di Intervento — Progetto HighCard

**Autore:** Alina Valega
**Data:** 08 giugno 2026
**Oggetto:** Piano di intervento e iterazioni di sviluppo, a partire dalle criticità rilevate in `pre-analysis.md`.

Il lavoro è stato organizzato in **iterazioni**, un task alla volta, seguendo il principio "prima l'analisi, poi l'implementazione" e con verifica manuale (`mvn compile` / `mvn test` / smoke test in Postman) dopo ogni passo.

---

## 1. Contestualizzazione e analisi

### 1.1 Obiettivo
Fornire allo strumento AI il contesto (ruolo, architettura a layer, vincolo di non modificare l'architettura) e produrre l'analisi iniziale del progetto.

### 1.2 Esito
Analisi statica completa delle criticità (dettagli in `pre-analysis.md`).

---

## 2. Migrazione a JPA

### 2.1 Entità e repository
- `User` trasformata in `@Entity` (`@Id`, `@GeneratedValue`, `guid` come business key).
- `UserRepository` riscritto come interfaccia `JpaRepository<User, Long>`.
- Eliminazione di `FakeDatabase`.

### 2.2 Configurazione
- H2 in-memory in `application.properties` (`ddl-auto=create-drop`, console H2, `show-sql`).
- Adeguamento di `UserServiceImpl` alla nuova firma di `save()`.

---

## 3. Validazione dei dati

- `@NotBlank` + `@Email` su `email`; `@NotBlank` + `@Pattern` su `phoneNumber` (regex per numeri italiani fissi e mobili, prefisso `+39`/`0039` opzionale) in `AddUserRequest`.
- `@Valid` sul `@RequestBody` nel controller.

---

## 4. Exception handling HTTP 200

- `GlobalExceptionHandler` (`@RestControllerAdvice`) che mappa le eccezioni su **HTTP 200** con `StatusDTO` nel body, incluso `MethodArgumentNotValidException`.
- Verificato in Postman: email non valida → HTTP 200 con `code 400` nel body; guid inesistente → HTTP 200 con `code 404`.

---

## 5. Paginazione, sorting e ricerca

- `GetUsersRequest` con campi `search`, `page`, `size`, `order` (default sensati).
- `GetUsersAssembler` (request → `CriteriaGetUsers`) con gestione dei null (`search` null → `""`, `page < 0` → `0`, `size <= 0` → `20`).
- Metodo `searchUsers` in `UserRepository` con `@Query` JPQL e `LOWER(...) LIKE` su firstName/lastName/email + `Pageable`.
- `UserServiceImpl.getUsers()` con `PageRequest.of(...)` e mappatura `OrderType → campo Sort` (aggiunti i valori `BY_EMAIL`/`BY_EMAIL_DESC`).

---

## 6. Bug fixing: getUsers(null)

- Il controller passava `null` al service; corretto facendo passare i criteri reali tramite `GetUsersAssembler` e `@ModelAttribute`.

---

## 7. Endpoint PUT con fix SQL Injection

- DTO `UpdateUserRequest` / `UpdateUserResponse`, criterio `CriteriaUpdateUser`, `UpdateUserAssembler`.
- `UserServiceImpl.updateUser()` con `@Transactional`, ricerca via `findByGuid()` + `save()` (**nessuna query nativa o concatenata**: questo è il fix SQL Injection). Utente non trovato → `GenericException(404, "User not found")`.
- Endpoint `@PutMapping("/update")` con `@Valid`.

---

## 8. JWT Security

### 8.1 Dipendenze e configurazione
- Librerie `jjwt` 0.13.0 (`jjwt-api`, `jjwt-impl` runtime, `jjwt-jackson` runtime).
- `jwt.secret` (≥ 256 bit), `jwt.issuer`, `jwt.expiration-ms` in `application.properties`.

### 8.2 Componenti
- `JwtUtils` (API jjwt 0.13, non deprecata): `generateToken`, `validateToken`, `extractUsername`; validazione di firma, issuer ed expiration.
- `JwtAuthenticationFilter` (`OncePerRequestFilter`): legge `Authorization: Bearer ...`, valida, popola il `SecurityContext`; non lancia eccezioni.
- `SecurityConfig` (Spring Security 6, Lambda DSL): STATELESS, CSRF off, `/auth/**` pubblici, resto autenticato, filtro JWT prima di `UsernamePasswordAuthenticationFilter`.
- `AuthController` con `/auth/login` (login semplificato per emettere token di test).

### 8.3 Policy / ruoli (autorizzazione)
- `JwtUtils`: claim `roles` nel token, overload `generateToken(username, roles)` (default `ROLE_USER`) ed `extractRoles`.
- `JwtAuthenticationFilter`: estrae i ruoli e popola le `SimpleGrantedAuthority`.
- `SecurityConfig`: regola di policy `requestMatchers(HttpMethod.PUT, "/user/update").hasRole("ADMIN")`.
- `AuthController`/`LoginRequest`: ruoli opzionali nel login per generare token con autorizzazioni diverse.

### 8.4 Verifica
- Smoke test end-to-end in Postman: login → token → endpoint protetti. Token ADMIN passa `PUT /user/update` (200), token USER viene bloccato (403). Token mancante → 401/403.

---

## 9. Bug fixing finale

- **`phoneNumber` perso:** `UserAssembler.toDTO` non mappava il telefono (campo `null` in lettura nonostante fosse salvato correttamente) → aggiunta la riga mancante.
- **`AddUserAssembler`:** confermata la corretta mappatura del `lastName` (bug iniziale risolto) e di tutti i campi.
- **Email:** confermato il fix (ora restituita completa, non più solo il dominio).

---

## 10. Unit test

- `UserServiceImplTest` (JUnit 5 + Mockito): `addUser`, `getUsers` (lista piena/vuota), `updateUser` (successo / 404).
- `JwtUtilsTest`: generazione token, validazione (valido/malformato), estrazione username, estrazione ruoli (ROLE_ADMIN e default ROLE_USER); campi `@Value` iniettati con `ReflectionTestUtils`.
- `UserControllerTest` (`@WebMvcTest` + `@MockitoBean`): `add` con body valido (200), `add` con email non valida (HTTP 200 con codice 400 nel body), `list` (200).
- **Esito: 15 test verdi** (`mvn test` BUILD SUCCESS).

---

## 11. Javadoc

- Documentazione in italiano, concisa, su `UserController`, `UserServiceImpl`, `JwtUtils`, `SecurityConfig`, `AuthController`, `JwtAuthenticationFilter` (con `@author Alina Valega`).

---

## 12. Documentazione /ai-assisted/

- Stesura di `pre-analysis.md`, `plan.md`, `report.md`.

---

## 13. Divisione del lavoro

| Attività | Eseguito da |
|---|---|
| Generazione codice (DTO, service, filtri, config, test) | Gemini (CLI / IDE) |
| Strategia e formulazione dei prompt, review dei diff | Sviluppatore (con supporto di un assistente AI esterno) |
| `mvn compile` / `mvn test` | Sviluppatore (manualmente) |
| Smoke test end-to-end (Postman) | Sviluppatore |
| Accettazione/rifiuto dei diff e verifica rispetto all'architettura | Sviluppatore |
| Inserimento manuale dei Javadoc | Sviluppatore (per esaurimento quota dello strumento) |
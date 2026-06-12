# Report di Sviluppo — Progetto HighCard

**Autore:** Alina Valega
**Data:** 11 giugno 2026
**Oggetto:** Sintesi del lavoro svolto con assistenza AI, prompt significativi, errori riscontrati e valutazione dello strumento.

---

## 1. Sintesi degli interventi

### 1.1 Bug fixing e paginazione (ricerca utenti)
Risolto il bug che passava `null` ai criteri di ricerca. Introdotta `GetUsersRequest`; paginazione con `Pageable`; sorting con l'enum `OrderType` mappato dinamicamente ai campi dell'entità; query JPQL `searchUsers` con filtro case-insensitive su firstName/lastName/email.

### 1.2 Endpoint PUT sicuro (update utente)
Implementato `PUT /user/update`. SQL Injection prevenuta usando esclusivamente metodi standard JPA (`findByGuid` + `save`) con parametri tipizzati. Aggiornati solo i campi anagrafici; GUID e ID immutati. Validazione con `@NotBlank`, `@Email`, `@Pattern`.

### 1.3 Sicurezza con JWT
Autenticazione stateless con `jjwt` 0.13.0 (API non deprecate). Validazione di firma, issuer ed expiration. **Policy/ruoli**: claim `roles` nel token, authorities nel filtro e regola `hasRole("ADMIN")` sull'endpoint di update. `AuthController` per generare token di test (con ruoli opzionali).

### 1.4 Correzione bug minori
Perdita del `phoneNumber` in `UserAssembler.toDTO` risolta; mapping del cognome verificato; email restituita completa.

### 1.5 Qualità del codice e testing
Suite di test (15 verdi): `UserServiceImplTest`, `JwtUtilsTest`, `UserControllerTest` (`@WebMvcTest`). Javadoc in italiano sui componenti principali.

---

## 2. Prompt più significativi

### 2.1 Contestualizzazione iniziale
Definizione di ruolo, architettura a layer, vincolo di non alterare l'architettura, richiesta di analizzare i file prima di implementare.

### 2.2 Vincolo API jjwt
Imposizione esplicita dell'uso della sola API jjwt 0.13 (vietando `parserBuilder()`, `setSigningKey()`, `parseClaimsJws()`), per evitare codice non compilabile.

### 2.3 Fix SQL Injection
Richiesta di usare esclusivamente `findByGuid()` + `save()` (zero concatenazione) nell'endpoint PUT.

### 2.4 Tracciamento bug `phoneNumber`
Richiesta di seguire il campo lungo l'intera catena (entità → assembler → service → DTO) per individuare il punto di perdita.

### 2.5 Audit finale per requisiti
Prompt di verifica mappato 1:1 sugli 8 task, per individuare i gap prima della consegna (ha fatto emergere la mancanza della Policy/ruoli nel JWT).

---

## 3. Errori commessi dall'AI e relative correzioni

| Errore | Come è stato corretto |
|---|---|
| `getUsers(null)` passato al service nel controller | Introdotti `GetUsersRequest` + `GetUsersAssembler` per passare i criteri reali |
| `UserAssembler.toDTO` non mappava `phoneNumber` | Aggiunta la riga `setPhoneNumber(user.getPhoneNumber())` |
| Tendenza a generare l'API jjwt 0.11.x deprecata | Vincolo esplicito sull'API 0.13 nei prompt, con elenco dei metodi vietati |
| Proposta di `WriteFile` con placeholder `// ...` al posto dei metodi esistenti | Imposto l'uso di `Edit` mirati; verifica del numero di righe e del diff prima di applicare |
| **`JwtUtils` corrotto durante l'aggiunta dei ruoli**: codice orfano fuori dalla classe + import `List` mancante → non compilava | Sostituzione integrale del file con una versione corretta verificata a mano |
| **Audit: falso positivo** sull'email "in chiaro" segnalata come bug (lo strumento ha invertito bug e correzione: il mascheramento era il bug) | Riconosciuto come falso positivo e ignorato; l'email completa è la correzione corretta |
| **Test del controller**: asserzione `$.status.code = 0` mentre il valore reale è 200 | Corretta l'asserzione a 200 |
| Uso di `@MockBean` deprecato in Spring Boot 3.4+ | Sostituito con `@MockitoBean` (non deprecato) |
| Uso di un costruttore potenzialmente inesistente (`GenericException(404, ...)`) | Verificato sul file reale: esisteva, conferma prima di procedere |
| Letture a vuoto di file dentro `.venv` (cartella Python irrilevante) | Ignorate; analisi reindirizzata sui file effettivi |

---

## 4. Difficoltà e limitazioni dello strumento

### 4.1 Nessuna compilazione/esecuzione
La CLI non disponeva di `mvn`/`java`; la verifica reale (compilazione, test, smoke test in Postman) è stata sempre manuale.

### 4.2 Corruzione dei file e sovrascritture
Più volte lo strumento ha tentato `WriteFile` integrali con il rischio di cancellare metodi esistenti; in un caso (`JwtUtils`) ha effettivamente corrotto il file. La revisione dei diff prima dell'applicazione è stata essenziale.

### 4.3 Gestione del contesto e quota
Per sessioni lunghe lo strumento ha prodotto riassunti dello stato; al raggiungimento del limite di utilizzo, l'ultima attività (inserimento dei Javadoc) è stata completata manualmente.

---

## 5. Valutazione onesta dell'utilità

### 5.1 Punti di forza
- Velocità nella generazione di codice ripetitivo (DTO, assembler, filtri, configurazioni, test).
- Individuazione di bug reali già in analisi (mapping `lastName`/`firstName`, email mascherata, `phoneNumber`, `catch` che oscura le eccezioni).
- Buona aderenza ai pattern esistenti quando guidato con prompt precisi e file di riferimento.

### 5.2 Punti deboli
- Supervisione continua indispensabile: senza verifica, errori come il file corrotto, l'API deprecata o il falso positivo dell'audit sarebbero passati.
- Non sostituisce la conoscenza dell'architettura: la qualità dipende dalla specificità dei prompt.
- Affidabilità sui dettagli (versioni, firme di metodi, asserzioni di test) sempre da verificare a mano.

### 5.3 Conclusione
Come acceleratore sotto supervisione lo strumento ha fatto risparmiare tempo significativo; usato senza verifica avrebbe introdotto regressioni (un file non compilabile e un test con asserzione errata ne sono la prova). Il valore reale è emerso dalla combinazione tra generazione automatica e revisione critica umana: l'AI ha prodotto il codice, ma decisioni architetturali, test di accettazione e controllo dei diff sono rimasti responsabilità dello sviluppatore.

---

## 6. Esito finale

Tutti gli 8 task risultano implementati e verificati (validazione, prevenzione SQL injection, paginazione/sorting/ricerca, exception handling HTTP 200, JWT con policy/issuer/expiration, bug fixing, unit test, Javadoc), nel rispetto dell'architettura a layer e con librerie aggiornate e prive di CVE note.
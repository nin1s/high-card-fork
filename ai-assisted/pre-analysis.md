# Pre-Analisi della Codebase — Progetto HighCard

**Autore:** Alina Valega
**Data:** 06 giugno 2026
**Oggetto:** Analisi statica dello stato iniziale del progetto, prima di qualsiasi intervento.

A seguito di un'ispezione dei file sorgenti dell'applicazione allo stato di partenza, sono state riscontrate diverse criticità strutturali, lacune nella validazione dei dati in ingresso, l'assenza di un layer di sicurezza e alcuni bug logici. L'analisi è organizzata per categoria; per ciascun problema è indicata la gravità (Alta / Media / Bassa) e si distinguono i **bug reali presenti** dalle **funzionalità semplicemente mancanti**.

> Nota metodologica: lo strumento di analisi non disponeva di `mvn`/`java` nell'ambiente, quindi non è stata eseguita la compilazione; la verifica finale dei problemi è a carico dello sviluppatore.

---

## 1. Architettura e persistenza

### 1.1 Persistenza non standard — Gravità: Alta
Il progetto non utilizza Spring Data JPA né un database reale. La persistenza è affidata a una classe `FakeDatabase` basata su una `List<User>` statica in memoria. Ciò comporta la perdita dei dati a ogni riavvio e l'impossibilità di gestire transazioni reali o query complesse.

### 1.2 Dipendenze mancanti nel `pom.xml` — Gravità: Media
Mancano librerie fondamentali: `spring-boot-starter-data-jpa` (accesso ai dati), `spring-boot-starter-validation` (validazione dichiarativa dei DTO) e `spring-boot-starter-security` (autenticazione e autorizzazione).

### 1.3 Controller incompleti — Gravità: Alta
L'endpoint di recupero utenti è vuoto (restituisce `ResponseEntity.ok().build()` senza invocare il service). Le annotazioni sono in stile datato (`@RequestMapping(method = ...)` invece dei più moderni `@GetMapping`/`@PostMapping`/`@PutMapping`).

---

## 2. Validazione dei dati

### 2.1 Assenza di validazione dichiarativa — Gravità: Alta
I DTO di richiesta (`AddUserRequest`, `GetUsersRequest`) non usano annotazioni come `@NotBlank`, `@Email`, `@Pattern`, `@Size`.

### 2.2 Mancanza di `@Valid` nei controller — Gravità: Alta
In `UserController` manca `@Valid`/`@Validated` sui parametri `@RequestBody`, quindi la validazione di Spring non si attiverebbe nemmeno se i DTO fossero annotati.

### 2.3 Validazione manuale vanificata — Gravità: Media
In `UserServiceImpl.addUser` la validazione è fatta a mano tramite `StringUtil`, ma viene resa inefficace dal blocco `catch (Exception e)` che cattura anche le eccezioni di validazione e le trasforma in errore generico (vedi sezione 4).

---

## 3. Sicurezza della persistenza / SQL Injection

### 3.1 Layer di persistenza non parametrizzato — Gravità: Bassa
Non essendoci un database SQL né query con concatenazione di stringhe per i filtri, **non è presente un rischio immediato di SQL Injection**. L'architettura attuale è però non-standard e priva dei meccanismi di sicurezza intrinseci offerti dai framework di persistenza (parametrizzazione delle query). Il rischio teorico emergerebbe introducendo un database reale senza repository standard: la prevenzione consiste nell'adottare Spring Data JPA con metodi derivati e JPQL parametrizzato (`@Param`).

---

## 4. Gestione delle eccezioni

### 4.1 Assenza di un handler centralizzato — Gravità: Alta
Non esiste un `@RestControllerAdvice`. Ogni eccezione non catturata viene gestita dal meccanismo di default di Spring, che restituisce codici HTTP 4xx/5xx e un body non conforme a `StatusDTO`, **violando il requisito di restituire sempre HTTP 200**.

### 4.2 Oscuramento delle eccezioni — Gravità: Alta
In `UserServiceImpl.addUser` il try-catch è implementato in modo errato:

```java
catch (Exception e) {
    throw new GenericException(GenericException.GENERIC_ERROR);
}
```

Poiché `GenericException` estende `Exception`, un errore specifico (es. "First name is required") viene catturato qui e trasformato in un "Generic error", facendo perdere al client l'informazione sul reale errore di validazione.

---

## 5. Sicurezza / autenticazione

### 5.1 Totale assenza di sicurezza — Gravità: Alta
Non è presente alcun meccanismo di autenticazione (JWT, Basic Auth, OAuth2). Le API sono completamente esposte: chiunque può aggiungere o consultare utenti. Mancano anche configurazioni per CORS e protezione CSRF.

---

## 6. Bug logici

### 6.1 Errore di mappatura in `AddUserAssembler` — Gravità: Alta
Nel metodo `toCriteria`, il `lastName` viene valorizzato con il `firstName` della richiesta: `returnValue.setLastName(addUserRequest.getFirstName());` *(AddUserAssembler.java, riga 13)*.

### 6.2 Dati alterati in `UserAssembler` — Gravità: Alta
Nel metodo `toDTO` l'email viene troncata per restituire solo il dominio: `returnValue.setEmail(user.getEmail().substring(user.getEmail().lastIndexOf("@") + 1));` *(UserAssembler.java, riga 13)*, causando perdita di dati nelle risposte.

### 6.3 Mappatura incompleta in `UserAssembler` — Gravità: Media
Il campo `phoneNumber`, presente sia nel modello `User` sia nel `UserDTO`, non viene mappato verso il DTO.

### 6.4 Bug nell'enum `OrderType` — Gravità: Bassa
In `CriteriaGetUsers.OrderType`, il valore `BY_LASTNAME_DESC` ha `displayName` "by lastName", identico a quello di `BY_LASTNAME`, rendendo ambigua la logica basata sul nome visualizzato.

### 6.5 Risultato non valorizzato — Gravità: Media
`UserServiceImpl.addUser` crea e restituisce un `AddUserResult` nuovo ma vuoto, rendendo inutile l'oggetto di ritorno se dovesse contenere dati dell'utente creato (es. il GUID).

### 6.6 Mancanza di `@Transactional` — Gravità: Bassa
L'assenza di `@Transactional` sul metodo `addUser` (operazione di scrittura) è una lacuna rispetto agli standard Spring, indipendentemente dall'attuale assenza di un DB reale.

---

## 7. Funzionalità mancanti

### 7.1 Recupero utenti non implementato — Gravità: Alta
La ricerca utenti non è implementata né nel controller (body vuoto) né nel service (restituisce `null`).

### 7.2 Paginazione e sorting — Gravità: Media
Pur essendo definiti in `CriteriaGetUsers`, offset, limit e sorting non sono implementati nella business logic.

### 7.3 Ricerca — Gravità: Media
Il campo `query` di `CriteriaGetUsers` non viene usato per filtrare la lista utenti.

---

## 8. Qualità del codice

### 8.1 Assenza di unit test — Gravità: Media
È presente solo il test di caricamento del contesto generato da Spring Initializr; mancano test unitari per service, assembler e controller.

### 8.2 Assenza di Javadoc — Gravità: Bassa
I componenti principali non sono documentati.

---

> **Stato del documento:** questa fase di pre-analisi è da considerarsi **completa**. Le criticità qui elencate costituiscono la base per il piano di intervento documentato in `plan.md`.
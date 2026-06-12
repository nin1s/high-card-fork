package it.sara.demo.web.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.assembler.GetUsersAssembler;
import it.sara.demo.web.assembler.UpdateUserAssembler;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.request.GetUsersRequest;
import it.sara.demo.web.user.request.UpdateUserRequest;
import it.sara.demo.web.user.response.AddUserResponse;
import it.sara.demo.web.user.response.GetUsersResponse;
import it.sara.demo.web.user.response.UpdateUserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per la gestione delle operazioni anagrafiche degli utenti.
 * Espone gli endpoint per inserimento, ricerca e aggiornamento.
 * 
 * @author Alina Valega
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddUserAssembler addUserAssembler;

    @Autowired
    private GetUsersAssembler getUsersAssembler;

    @Autowired
    private UpdateUserAssembler updateUserAssembler;

    /**
     * Aggiunge un nuovo utente al sistema.
     * 
     * @param request Dati dell'utente da inserire
     * @return Risposta con esito dell'operazione
     * @throws GenericException in caso di errori di validazione o di sistema
     */
    @PostMapping("/add")
    public AddUserResponse addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        userService.addUser(addUserAssembler.toCriteria(request));
        AddUserResponse response = new AddUserResponse();
        response.setStatus(it.sara.demo.web.response.GenericResponse.success("User added successfully").getStatus());
        return response;
    }

    /**
     * Recupera la lista degli utenti con supporto a ricerca, paginazione e ordinamento.
     * 
     * @param request Parametri di ricerca e paginazione
     * @return Lista di utenti e totale risultati
     * @throws GenericException in caso di errori durante il recupero
     */
    @GetMapping("/list")
    public GetUsersResponse getUsers(@ModelAttribute GetUsersRequest request) throws GenericException {
        GetUsersResult result = userService.getUsers(getUsersAssembler.toCriteria(request));
        GetUsersResponse response = new GetUsersResponse();
        response.setUsers(result.getUsers());
        response.setTotal(result.getTotal());
        response.setStatus(it.sara.demo.web.response.GenericResponse.success("Users retrieved successfully").getStatus());
        return response;
    }

    /**
     * Aggiorna i dati anagrafici di un utente esistente.
     * 
     * @param request Nuovi dati dell'utente identificato dal GUID
     * @return Risposta con esito dell'operazione
     * @throws GenericException se l'utente non viene trovato o in caso di errore
     */
    @PutMapping("/update")
    public UpdateUserResponse updateUser(@Valid @RequestBody UpdateUserRequest request) throws GenericException {
        userService.updateUser(updateUserAssembler.toCriteria(request));
        UpdateUserResponse response = new UpdateUserResponse();
        response.setStatus(it.sara.demo.web.response.GenericResponse.success("User updated successfully").getStatus());
        return response;
    }
}

package it.sara.demo.web.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.util.JwtUtils;
import it.sara.demo.web.user.request.LoginRequest;
import it.sara.demo.web.user.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per la gestione delle operazioni di autenticazione.
 * Fornisce gli endpoint per il login e il rilascio dei token JWT.
 * 
 * @author Alina Valega
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    /**
     * Costruttore per l'iniezione delle utility JWT.
     * 
     * @param jwtUtils Utility per la generazione dei token
     */
    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Gestisce la richiesta di autenticazione e restituisce un token JWT.
     * Supporta la specifica opzionale dei ruoli per fini di test.
     * 
     * @param request Dati di login contenenti username e ruoli opzionali
     * @return Risposta contenente il token JWT generato
     * @throws GenericException in caso di errore durante la procedura
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws GenericException {
        String token;
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            token = jwtUtils.generateToken(request.getUsername(), request.getRoles());
        } else {
            token = jwtUtils.generateToken(request.getUsername());
        }
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setStatus(it.sara.demo.web.response.GenericResponse.success("Login successful").getStatus());
        
        return response;
    }
}

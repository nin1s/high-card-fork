package it.sara.demo.exception;

import it.sara.demo.dto.StatusDTO;
import it.sara.demo.web.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGenericException(GenericException ex) {
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatus());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        StatusDTO status = new StatusDTO();
        status.setCode(400);
        status.setMessage("Validation error: " + errorMessage);
        status.setTraceId(java.util.UUID.randomUUID().toString());

        GenericResponse response = new GenericResponse();
        response.setStatus(status);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleException(Exception ex) {
        GenericResponse response = new GenericResponse();
        response.setStatus(GenericException.GENERIC_ERROR);
        return ResponseEntity.ok(response);
    }
}

package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginRequest extends GenericRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private List<String> roles;
}

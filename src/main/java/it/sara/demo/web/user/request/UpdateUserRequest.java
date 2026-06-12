package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest extends GenericRequest {

    @NotBlank(message = "GUID is required")
    private String guid;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+39|0039)?(3\\d{2}\\s?\\d{7}|0\\d{1,3}\\s?\\d{6,8})$", 
             message = "Invalid Italian phone number format")
    private String phoneNumber;
}

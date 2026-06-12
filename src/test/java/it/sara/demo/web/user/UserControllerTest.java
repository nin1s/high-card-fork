package it.sara.demo.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.util.JwtUtils;
import it.sara.demo.security.JwtAuthenticationFilter;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.assembler.GetUsersAssembler;
import it.sara.demo.web.assembler.UpdateUserAssembler;
import it.sara.demo.web.user.request.AddUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AddUserAssembler addUserAssembler;

    @MockitoBean
    private GetUsersAssembler getUsersAssembler;

    @MockitoBean
    private UpdateUserAssembler updateUserAssembler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("POST /user/add con body valido → status HTTP 200 e StatusDTO di successo")
    void addUser_ValidBody_ShouldReturnSuccess() throws Exception {
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        request.setEmail("mario.rossi@example.it");
        request.setPhoneNumber("0212345678");

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code", is(200)))
                .andExpect(jsonPath("$.status.message", is("User added successfully")));
    }

    @Test
    @DisplayName("POST /user/add con email non valida → status HTTP 200 con codice errore nel body")
    void addUser_InvalidEmail_ShouldReturnHttp200WithErrorInBody() throws Exception {
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        request.setEmail("email-non-valida"); // Email errata
        request.setPhoneNumber("0212345678");

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Requisito: deve essere 200
                .andExpect(jsonPath("$.status.code", is(400))) // Errore gestito dal GlobalExceptionHandler
                .andExpect(jsonPath("$.status.message").value(org.hamcrest.Matchers.containsString("Invalid email format")));
    }

    @Test
    @DisplayName("GET /user/list → status HTTP 200")
    void getUsers_ShouldReturnHttp200() throws Exception {
        // Mocking minimo del result per evitare null pointer se necessario, 
        // ma qui testiamo l'accessibilità dell'endpoint
        
        mockMvc.perform(get("/user/list")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }
}

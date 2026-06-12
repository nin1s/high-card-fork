package it.sara.demo.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private final String SECRET = "questo_e_un_segreto_molto_lungo_che_supera_i_32_caratteri";
    private final String ISSUER = "HighCardApp";
    private final long EXPIRATION_MS = 3600000; // 1 ora

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "issuer", ISSUER);
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken(username) produce un token non null e non vuoto")
    void generateToken_ProducesNonNullNotEmptyToken() {
        String token = jwtUtils.generateToken("userTest");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("validateToken ritorna true su un token appena generato")
    void validateToken_ReturnsTrueForValidToken() {
        String token = jwtUtils.generateToken("userTest");
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    @DisplayName("validateToken ritorna false su un token malformato (\"abc.def.ghi\")")
    void validateToken_ReturnsFalseForMalformedToken() {
        assertFalse(jwtUtils.validateToken("abc.def.ghi"));
    }

    @Test
    @DisplayName("extractUsername restituisce lo username originale")
    void extractUsername_ReturnsOriginalUsername() {
        String username = "mario.rossi";
        String token = jwtUtils.generateToken(username);
        assertEquals(username, jwtUtils.extractUsername(token));
    }

    @Test
    @DisplayName("generateToken(username, List.of(\"ROLE_ADMIN\")) + extractRoles restituisce [\"ROLE_ADMIN\"]")
    void extractRoles_ReturnsAdminRole() {
        List<String> roles = List.of("ROLE_ADMIN");
        String token = jwtUtils.generateToken("adminUser", roles);
        List<String> extractedRoles = jwtUtils.extractRoles(token);
        assertEquals(roles, extractedRoles);
    }

    @Test
    @DisplayName("extractRoles su un token creato con generateToken(username) (default) restituisce [\"ROLE_USER\"]")
    void extractRoles_ReturnsDefaultUserRole() {
        String token = jwtUtils.generateToken("standardUser");
        List<String> extractedRoles = jwtUtils.extractRoles(token);
        assertEquals(List.of("ROLE_USER"), extractedRoles);
    }
}

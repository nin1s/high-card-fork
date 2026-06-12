package it.sara.demo.security;

import it.sara.demo.service.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro che autentica ogni richiesta tramite token JWT. Legge l'header
 * "Authorization: Bearer <token>", lo valida e, se valido, popola il
 * SecurityContext con username e ruoli. Non interrompe mai la catena dei filtri.
 *
 * @author Alina Valega
 * @version 1.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Estrae e valida il token JWT dall'header Authorization; se valido, imposta
     * l'autenticazione nel SecurityContext con i ruoli estratti. Eventuali errori
     * vengono loggati senza interrompere la catena dei filtri.
     *
     * @param request la richiesta HTTP in ingresso
     * @param response la risposta HTTP
     * @param filterChain la catena dei filtri da proseguire
     * @throws ServletException in caso di errore servlet
     * @throws IOException in caso di errore di I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.extractUsername(token);
                    List<String> roles = jwtUtils.extractRoles(token);

                    List<SimpleGrantedAuthority> authorities = roles == null ? List.of() : roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

package it.sara.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configurazione della sicurezza dell'applicazione.
 * Definisce le policy di accesso agli endpoint e la configurazione del filtro JWT.
 * 
 * @author Alina Valega
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Costruttore per l'iniezione del filtro di autenticazione.
     * 
     * @param jwtAuthenticationFilter Filtro per la gestione del token JWT
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la catena dei filtri di sicurezza e le regole di autorizzazione.
     * 
     * @param http Oggetto per la configurazione della sicurezza HTTP
     * @return La catena di filtri configurata
     * @throws Exception in caso di errore nella configurazione
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/user/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/user/list").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

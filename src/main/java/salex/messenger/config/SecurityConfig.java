package salex.messenger.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import salex.messenger.filter.JwtAuthFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**")
                        .not()
                        .authenticated()
                        .requestMatchers("/signup", "/signin")
                        .not()
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(e -> e.accessDeniedHandler((request, response, ex) -> {
                    if (isApiRequest(request)) {
                        response.setContentType("application/json");
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.getWriter()
                                .write(String.format(
                                        "{\"error\":\"%s\",\"text\":\"%s\"}",
                                        HttpStatus.FORBIDDEN.name(), "Доступ запрещен"));
                    } else {
                        response.sendRedirect("/403");
                    }
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }
}

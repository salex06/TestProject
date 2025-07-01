package salex.messenger.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.jwt.JwtHelper;
import salex.messenger.service.UserService;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtHelper jwtHelper;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String jwt = Arrays.stream(cookies)
                        .filter(c -> "jwt".equals(c.getName()))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
                String username = null;
                if (jwt != null) {
                    username = jwtHelper.extractUsername(jwt);
                }

                if (Objects.nonNull(username)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService
                            .findUser(username)
                            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
                    boolean isTokenValidated = jwtHelper.validateToken(jwt, userDetails);
                    if (isTokenValidated) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        usernamePasswordAuthenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        } catch (ExpiredJwtException ex) {
            request.setAttribute("exception", ex);
        } catch (BadCredentialsException | UnsupportedJwtException | MalformedJwtException ex) {
            // TODO: логирование
            request.setAttribute("exception", ex);
        }
        filterChain.doFilter(request, response);
    }
}

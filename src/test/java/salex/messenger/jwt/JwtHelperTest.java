package salex.messenger.jwt;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import salex.messenger.config.JwtConfig;
import salex.messenger.entity.User;

class JwtHelperTest {
    private JwtConfig jwtConfig;

    private JwtHelper jwtHelper;

    public JwtHelperTest() {
        jwtConfig = new JwtConfig(
                Duration.ofSeconds(5), "bc53ddd22a491b45c5e6535e9075c5f3938506f483607259279b31a6b11b0901");
        jwtHelper = new JwtHelper(jwtConfig);
    }

    @Test
    @DisplayName("Проверка корректности токена (время жизни истекло)")
    public void validateToken_WhenTokenExpired_ThenThrowException() throws InterruptedException {
        String username = "user";
        String token = jwtHelper.createToken(Collections.emptyMap(), username);
        UserDetails details = new User(null, username, "", "", "", "", "");

        Thread.sleep(jwtConfig.lifetime().plusSeconds(1));

        assertThrows(ExpiredJwtException.class, () -> jwtHelper.validateToken(token, details));
    }

    @Test
    @DisplayName("Проверка корректности токена (неверный субъект)")
    public void validateToken_WhenInvalidUserDetails_ThenReturnFalse() {
        String username = "user";
        String token = jwtHelper.createToken(Collections.emptyMap(), username);
        UserDetails details = new User(null, "unknown", "", "", "", "", "");

        boolean result = jwtHelper.validateToken(token, details);

        assertFalse(result);
    }

    @Test
    @DisplayName("Проверка корректности токена (токен корректный)")
    public void validateToken_WhenValidToken_ThenReturnTrue() {
        String username = "user";
        String token = jwtHelper.createToken(Collections.emptyMap(), username);
        UserDetails details = new User(null, username, "", "", "", "", "");

        boolean result = jwtHelper.validateToken(token, details);

        assertTrue(result);
    }

    @Test
    @DisplayName("Получение имени субъекта из токена")
    public void extractUsernameReturnsCorrectUsername() {
        String username = "user";
        String token = jwtHelper.createToken(Collections.emptyMap(), username);

        String actualUsername = jwtHelper.extractUsername(token);

        assertEquals(username, actualUsername);
    }
}

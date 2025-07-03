package salex.messenger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import salex.messenger.dto.signin.SignInRequest;
import salex.messenger.entity.User;
import salex.messenger.jwt.JwtHelper;
import salex.messenger.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @MockitoBean
    private JwtHelper jwtHelper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Пользователь не найден")
    public void authenticate_WhenUserNotFound_ThenThrowException() {
        SignInRequest request = new SignInRequest("username", "12345");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        verify(passwordEncoder, times(0)).matches(any(), any());
        verify(jwtHelper, times(0)).createToken(any(), any());
    }

    @Test
    @DisplayName("Пароль неверный")
    public void authenticate_WhenWrongPassword_ThenThrowException() {
        SignInRequest request = new SignInRequest("username", "12345");
        User user = new User(null, "username", "123", "", "", "", "");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getEncryptedPassword()))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request), "Неправильный пароль!");
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtHelper, times(0)).createToken(any(), any());
    }

    @Test
    @DisplayName("Пользователь аутентифицирован успешно")
    public void authenticate_WhenCorrectRequest_ThenReturnToken() {
        SignInRequest request = new SignInRequest("username", "12345");
        User user = new User(null, "username", "123", "", "", "", "");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getEncryptedPassword()))
                .thenReturn(true);

        authService.authenticate(request);
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtHelper, times(1)).createToken(any(), any());
    }
}

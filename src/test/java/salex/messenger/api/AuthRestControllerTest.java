package salex.messenger.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.config.JwtConfig;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.dto.signin.SignInRequest;
import salex.messenger.dto.signup.SignUpRequest;
import salex.messenger.dto.signup.SignUpResponse;
import salex.messenger.entity.User;
import salex.messenger.exception.UsernameAlreadyExistsException;
import salex.messenger.service.AuthService;
import salex.messenger.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    @DisplayName("Попытка зарегистрироваться с уже занятым username")
    public void signUp_WhenUsernameIsAlreadyTaken_ThenReturn400Response() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SignUpRequest request = new SignUpRequest("user", "123");
        when(userService.saveUser(any(SignUpRequest.class))).thenAnswer(ans -> {
            throw new UsernameAlreadyExistsException(
                    "Пользователь с именем '" + request.username() + "' уже существует");
        });
        String expectedResponse = mapper.writeValueAsString(new ApiErrorResponse(
                "Такое имя уже занято!",
                "400",
                "UsernameAlreadyExistsException",
                "Пользователь с именем '" + request.username() + "' уже существует"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Успешная попытка регистрации")
    public void signUp_WhenCorrectRequest_ThenSaveUserAndReturnOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SignUpRequest request = new SignUpRequest("user", "123");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("user");
        savedUser.setEncryptedPassword("fdk34f76ads2f5n12mr");
        when(userService.saveUser(any(SignUpRequest.class))).thenReturn(savedUser);
        String expectedResponse = mapper.writeValueAsString(
                new SignUpResponse("Пользователь зарегистрирован", savedUser.getId(), savedUser.getUsername()));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Неудачная попытка аутентификации - пользователь не найден")
    public void signIn_WhenUserNotFound_ThenReturn400() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(authService.authenticate(any(SignInRequest.class))).thenAnswer(ans -> {
            throw new BadCredentialsException("Пользователь с таким именем не найден!");
        });
        SignInRequest request = new SignInRequest("user", "123");
        String expectedResponse = mapper.writeValueAsString(new ApiErrorResponse(
                "Пользователь с таким именем не найден!",
                "400",
                "BadCredentialsException",
                "Пользователь с таким именем не найден!"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Неудачная попытка аутентификации - пароль неверный")
    public void signIn_WhenWrongPassword_ThenReturn400() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(authService.authenticate(any(SignInRequest.class))).thenAnswer(ans -> {
            throw new BadCredentialsException("Неправильный пароль!");
        });
        SignInRequest request = new SignInRequest("user", "321");
        String expectedResponse = mapper.writeValueAsString(
                new ApiErrorResponse("Неправильный пароль!", "400", "BadCredentialsException", "Неправильный пароль!"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Удачная попытка аутентификации")
    public void signIn_WhenCorrectRequest_ThenReturnJwt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jwt = "123abcdef";
        when(authService.authenticate(any(SignInRequest.class))).thenReturn(jwt);
        SignInRequest request = new SignInRequest("user", "321");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", jwt))
                .andExpect(cookie().secure("jwt", true))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().maxAge("jwt", (int) jwtConfig.lifetime().toSeconds()));
    }
}

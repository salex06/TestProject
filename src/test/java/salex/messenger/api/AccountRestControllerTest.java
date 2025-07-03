package salex.messenger.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.account.UserInfo;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.entity.User;
import salex.messenger.service.UserService;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class AccountRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Попытка получить данные аккаунта, но пользователь не авторизован")
    public void getUserData_WhenUnauthorized_ThenReturn401Response() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Попытка получить данные аккаунта, пользователь авторизован, но его нет в БД")
    @WithMockUser("unknown")
    public void getUserData_WhenAuthorizedButNotFoundInDatabase_ThenReturn404Response() throws Exception {
        String expectedResponse = new ObjectMapper()
                .writeValueAsString(new ApiErrorResponse(
                        "Пользователь не найден (или удален)!",
                        "404",
                        "UserNotFoundException",
                        "Пользователь не найден!"));

        mockMvc.perform(get("/api/account").contentType("multipart/form-data"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Попытка получить данныые аккаунта, пользователь авторизован и есть в БД")
    @WithMockUser("friend")
    public void getUserData_WhenAuthorizedAndWasFoundInDatabase_ThenReturnData() throws Exception {
        String expectedResponse =
                new ObjectMapper().writeValueAsString(new UserInfo("friend", "name", "surname", "123", "about"));
        User expectedUser = new User(null, "friend", "pass", "name", "surname", "about", "123");
        when(userService.findUser("friend")).thenReturn(Optional.of(expectedUser));

        mockMvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Метод quit удаляет jwt из cookie")
    public void quitRemovesCookieAndReturnsOk() throws Exception {
        String expectedValue = null;
        mockMvc.perform(post("/api/account/quit"))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().maxAge("jwt", 0))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().secure("jwt", true))
                .andExpect(cookie().value("jwt", expectedValue));
    }
}

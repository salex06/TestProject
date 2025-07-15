package salex.messenger.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.dto.users.UserProfileInfo;
import salex.messenger.entity.User;
import salex.messenger.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ProfileRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    @DisplayName("Неудачная попытка получения профиля пользователя: user не авторизован")
    public void getProfileInfo_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/profile/123")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Неудачная попытка получения профиля пользователя: попытка получить информацию о себе")
    @WithMockUser("user")
    public void getProfileInfo_WhenTryGetInfoAboutYourself_ThenRedirectToAccount() throws Exception {
        String username = "user";
        String profileUsername = "user";

        mockMvc.perform(get("/api/profile/" + profileUsername))
                .andExpect(redirectedUrl("/account"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Неудачная попытка получения профиля пользователя: пользователь не найден")
    @WithMockUser("user")
    public void getProfileInfo_WhenUserNotFound_ThenReturnApiErrorResponse() throws Exception {
        String username = "user";
        String profileUsername = "alex";
        when(userService.findUser(profileUsername)).thenReturn(Optional.empty());
        ApiErrorResponse response = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь 'alex' не найден!");

        mockMvc.perform(get("/api/profile/" + profileUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Успешная попытка получения информации о пользователе")
    @WithMockUser("user")
    public void getProfileInfo_WhenCorrectRequest_ThenReturnUserProfileInfo() throws Exception {
        String username = "user";
        User user = new User(1L, "alex", "12345", "", "", "", "");
        when(userService.findUser(user.getUsername())).thenReturn(Optional.of(user));
        UserProfileInfo response = new UserProfileInfo(
                user.getUsername(), user.getName(), user.getSurname(), user.getAbout(), user.getPhotoPath());

        mockMvc.perform(get("/api/profile/" + user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}

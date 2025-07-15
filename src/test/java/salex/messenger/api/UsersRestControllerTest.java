package salex.messenger.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.dto.users.SearchResponse;
import salex.messenger.dto.users.UserProfileInfo;
import salex.messenger.entity.User;
import salex.messenger.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UsersRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    @DisplayName("Неудачная попытка поиска пользователей: пользователь не авторизован")
    public void searchUsers_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/users/search").param("query", "alex")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешная попытка поиска пользователей")
    @WithMockUser("user")
    public void searchUsers_WhenCorrectRequest_ThenReturnUserList() throws Exception {
        String username = "user";
        String query = "alex";
        int page = 0;
        int size = 2;
        User user1 = new User(1L, "alex1", "12345", "", "", "", "");
        User user2 = new User(2L, "alex2", "12345", "", "", "", "");
        // User user3 = new User(3L, "alex3", "12345", "", "", "", "");
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = new PageImpl<>(List.of(user1, user2), pageable, 3);
        List<UserProfileInfo> converted = List.of(
                new UserProfileInfo(
                        user1.getUsername(),
                        user1.getName(),
                        user1.getSurname(),
                        user1.getAbout(),
                        user1.getPhotoPath()),
                new UserProfileInfo(
                        user2.getUsername(),
                        user2.getName(),
                        user2.getSurname(),
                        user2.getAbout(),
                        user2.getPhotoPath()));
        when(userService.getUsersByUsernamePattern(query, pageable, username)).thenReturn(users);
        SearchResponse resp = new SearchResponse(
                converted, users.getTotalPages(), users.getTotalElements(), users.getNumber(), users.getSize());

        mockMvc.perform(get("/api/users/search")
                        .param("query", query)
                        .param("page", "" + page)
                        .param("size", "" + size))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @DisplayName("Неудачная попытка получения пути к фото: пользователь не авторизован")
    public void getPhotoPath_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/users/photo").param("username", "alex")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Неудачная попытка получения пути к фото: пользователь не найден")
    @WithMockUser("user")
    public void getPhotoPath_WhenUserNotFound_ThenReturnApiErrorResponse() throws Exception {
        String username = "user";
        String requestedUsername = "alex";
        when(userService.findUser(requestedUsername)).thenReturn(Optional.empty());
        ApiErrorResponse response = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь 'alex' не найден");

        mockMvc.perform(get("/api/users/photo").param("username", requestedUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Успешная попытка получения пути к фото")
    @WithMockUser("user")
    public void getPhotoPath_WhenCorrectRequest_ThenReturnPathToPhoto() throws Exception {
        String username = "user";
        String requestedUsername = "alex";
        User alex = new User(1L, requestedUsername, "12345", "", "", "", "path");
        when(userService.findUser(requestedUsername)).thenReturn(Optional.of(alex));

        mockMvc.perform(get("/api/users/photo").param("username", requestedUsername))
                .andExpect(status().isOk())
                .andExpect(content().string(alex.getPhotoPath()));
    }
}

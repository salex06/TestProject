package salex.messenger.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.account.update.about.UpdateAboutRequest;
import salex.messenger.dto.account.update.about.UpdateAboutResponse;
import salex.messenger.dto.account.update.name.UpdateNameRequest;
import salex.messenger.dto.account.update.name.UpdateNameResponse;
import salex.messenger.dto.account.update.photo.UpdatePhotoRequest;
import salex.messenger.dto.account.update.photo.UpdatePhotoResponse;
import salex.messenger.dto.account.update.surname.UpdateSurnameRequest;
import salex.messenger.dto.account.update.surname.UpdateSurnameResponse;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.dto.users.AccountInfo;
import salex.messenger.dto.users.UsernameResponse;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
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
                new ObjectMapper().writeValueAsString(new AccountInfo("friend", "name", "surname", "123", "about"));
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

    @Test
    @DisplayName("Неудачное обновление имени - пользователь не авторизован")
    public void updateName_WhenUnauthorized_ThenReturn401() throws Exception {
        UpdateNameRequest request = new UpdateNameRequest("newName");
        mockMvc.perform(patch("/api/account/name")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Неудачное обновление имени - пользователь не найден")
    @WithMockUser("unknown")
    public void updateName_WhenUserNotFound_ThenReturn404() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateNameRequest request = new UpdateNameRequest("newName");
        when(userService.updateName(principalName, request)).thenAnswer(i -> {
            throw new UserNotFoundException("Пользователь '" + principalName + "' не найден");
        });
        ApiErrorResponse expectedResponse = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь '" + principalName + "' не найден");

        mockMvc.perform(patch("/api/account/name")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Удачное обновление имени")
    @WithMockUser("unknown")
    public void updateName_WhenCorrectRequest_ThenUpdateNameAndReturn200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateNameRequest request = new UpdateNameRequest("newName");
        User user = new User();
        user.setName(request.newName());
        when(userService.updateName(principalName, request)).thenReturn(user);
        UpdateNameResponse expectedResponse = new UpdateNameResponse(user.getName());

        mockMvc.perform(patch("/api/account/name")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Неудачное обновление фамилии - пользователь не авторизован")
    public void updateSurname_WhenUnauthorized_ThenReturn401() throws Exception {
        UpdateSurnameRequest request = new UpdateSurnameRequest("newSurname");
        mockMvc.perform(patch("/api/account/surname")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Неудачное обновление фамилии - пользователь не найден")
    @WithMockUser("unknown")
    public void updateSurname_WhenUserNotFound_ThenReturn404() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateSurnameRequest request = new UpdateSurnameRequest("newSurname");
        when(userService.updateSurname(principalName, request)).thenAnswer(i -> {
            throw new UserNotFoundException("Пользователь '" + principalName + "' не найден");
        });
        ApiErrorResponse expectedResponse = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь '" + principalName + "' не найден");

        mockMvc.perform(patch("/api/account/surname")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Удачное обновление фамилии")
    @WithMockUser("unknown")
    public void updateSurname_WhenCorrectRequest_ThenUpdateSurnameAndReturn200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateSurnameRequest request = new UpdateSurnameRequest("newName");
        User user = new User();
        user.setSurname(request.newSurname());
        when(userService.updateSurname(principalName, request)).thenReturn(user);
        UpdateSurnameResponse expectedResponse = new UpdateSurnameResponse(user.getSurname());

        mockMvc.perform(patch("/api/account/surname")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Неудачное обновление 'о себе' - пользователь не авторизован")
    public void updateAbout_WhenUnauthorized_ThenReturn401() throws Exception {
        UpdateAboutRequest request = new UpdateAboutRequest("newAbout");
        mockMvc.perform(patch("/api/account/about")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Неудачное обновление информации 'о себе' - пользователь не найден")
    @WithMockUser("unknown")
    public void updateAbout_WhenUserNotFound_ThenReturn404() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateAboutRequest request = new UpdateAboutRequest("newAbout");
        when(userService.updateAbout(principalName, request)).thenAnswer(i -> {
            throw new UserNotFoundException("Пользователь '" + principalName + "' не найден");
        });
        ApiErrorResponse expectedResponse = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь '" + principalName + "' не найден");

        mockMvc.perform(patch("/api/account/about")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Удачное обновление информации 'о себе'")
    @WithMockUser("unknown")
    public void updateAbout_WhenCorrectRequest_ThenUpdateSurnameAndReturn200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String principalName = "unknown";
        UpdateAboutRequest request = new UpdateAboutRequest("newAbout");
        User user = new User();
        user.setAbout(request.newAbout());
        when(userService.updateAbout(principalName, request)).thenReturn(user);
        UpdateAboutResponse expectedResponse = new UpdateAboutResponse(user.getAbout());

        mockMvc.perform(patch("/api/account/about")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Неудачное обновление фото - пользователь не авторизован")
    public void updatePhoto_WhenUnauthorized_ThenReturn401() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("avatar", "test-avatar.jpg", "image/jpeg", "test image content".getBytes());
        UpdatePhotoRequest request = new UpdatePhotoRequest(file);
        mockMvc.perform(multipart("/api/account/photo").file(file).with(req -> {
                    req.setMethod("PATCH");
                    return req;
                }))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Неудачное обновление фото - пользователь не найден")
    @WithMockUser("unknown")
    public void updatePhoto_WhenUserNotFound_ThenReturn404() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("avatar", "test-avatar.jpg", "image/jpeg", "test image content".getBytes());
        String principalName = "unknown";
        when(userService.replacePhoto(eq(principalName), any())).thenAnswer(i -> {
            throw new UserNotFoundException("Пользователь '" + principalName + "' не найден");
        });
        ApiErrorResponse expectedResponse = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь '" + principalName + "' не найден");

        mockMvc.perform(multipart("/api/account/photo").file(file).with(req -> {
                    req.setMethod("PATCH");
                    return req;
                }))
                .andExpect(status().isNotFound())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(expectedResponse)));
    }

    @Test
    @DisplayName("Удачное обновление фото")
    @WithMockUser("unknown")
    public void updatePhoto_WhenCorrectRequest_ThenReplacePhotoAndReturn200() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("avatar", "test-avatar.jpg", "image/jpeg", "test image content".getBytes());
        String principalName = "unknown";
        UpdatePhotoRequest request = new UpdatePhotoRequest(file);
        User user = new User();
        user.setPhotoPath(file.getOriginalFilename());
        when(userService.replacePhoto(eq(principalName), any())).thenReturn(user);
        UpdatePhotoResponse response = new UpdatePhotoResponse(user.getPhotoPath());

        mockMvc.perform(multipart("/api/account/photo").file(file).with(req -> {
                    req.setMethod("PATCH");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    @DisplayName("Неудачное получение username - пользователь не авторизован")
    public void getUsername_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/account/username")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос на получение username")
    @WithMockUser("user")
    public void getUsername_WhenAuthorized_ThenReturnUsername() throws Exception {
        String username = "user";
        UsernameResponse expected = new UsernameResponse(username);

        mockMvc.perform(get("/api/account/username"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(expected)));
    }
}

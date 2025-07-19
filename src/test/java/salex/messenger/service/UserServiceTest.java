package salex.messenger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import salex.messenger.dto.account.remove.RemoveUserRequest;
import salex.messenger.dto.account.update.about.UpdateAboutRequest;
import salex.messenger.dto.account.update.name.UpdateNameRequest;
import salex.messenger.dto.account.update.photo.UpdatePhotoRequest;
import salex.messenger.dto.account.update.surname.UpdateSurnameRequest;
import salex.messenger.dto.signup.SignUpRequest;
import salex.messenger.entity.User;
import salex.messenger.exception.StorageException;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.exception.UsernameAlreadyExistsException;
import salex.messenger.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("Сохранение пользователя: пользователь с таким именем уже сохранен")
    public void saveUser_WhenUserIsAlreadySaved_ThenThrowException() {
        SignUpRequest request = new SignUpRequest(
                "username", "password", "name", "surname", "about", new MockMultipartFile("image", new byte[] {}));
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.saveUser(request));
    }

    @Test
    @DisplayName("Сохранение пользователя: фото не прошло валидацию")
    public void saveUser_WhenPhotoInvalid_ThenThrowException() {
        SignUpRequest request = new SignUpRequest(
                "username", "password", "name", "surname", "about", new MockMultipartFile("image", new byte[] {1, 2, 3
                }));
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        doThrow(StorageException.class).when(imageStorageService).store(eq(request.photo()), any(), any());

        assertThrows(StorageException.class, () -> userService.saveUser(request));
    }

    @Test
    @DisplayName("Пользователь успешно сохранен")
    public void saveUser_WhenCorrectRequest_ThenReturnSavedUser() {
        SignUpRequest request = new SignUpRequest(
                "username",
                "password",
                "name",
                "surname",
                "about",
                new MockMultipartFile("image", "image", "image/png", new byte[] {1, 2, 3}));
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(imageStorageService.store(eq(request.photo()), any(), any()))
                .thenReturn(request.photo().getOriginalFilename());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            return i.getArgument(0);
        });

        User actualUser = userService.saveUser(request);

        assertEquals(request.username(), actualUser.getUsername());
        assertEquals(request.name(), actualUser.getName());
        assertEquals(request.surname(), actualUser.getSurname());
        assertEquals(request.about(), actualUser.getAbout());
        assertEquals(request.photo().getOriginalFilename(), actualUser.getPhotoPath());
    }

    @Test
    @DisplayName("Удаление пользователя: пользователь не найден")
    public void removeUser_WhenUserNotFound_ThenThrowException() {
        RemoveUserRequest request = new RemoveUserRequest("user");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.removeUser(request));
    }

    @Test
    @DisplayName("Удаление пользователя: пользователь успешно удален")
    public void removeUser_WhenCorrectRequest_ThenRemoveUser() {
        RemoveUserRequest request = new RemoveUserRequest("user");
        User user = new User();
        user.setId(1L);
        user.setUsername(request.username());
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.removeUser(request));
        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    @DisplayName("Обновление имени: пользователь не найден")
    public void updateName_WhenUserNotFound_ThenThrowException() {
        UpdateNameRequest request = new UpdateNameRequest("user");
        String username = "principal";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateName(username, request));
    }

    @Test
    @DisplayName("Успешное обновление имени")
    public void updateName_WhenCorrectRequest_ThenUpdateUserInfo() {
        String username = "principal";
        String oldName = "oldName";
        User user = new User();
        user.setName(oldName);
        UpdateNameRequest request = new UpdateNameRequest("user");
        when(userRepository.save(any())).thenAnswer(i -> {
            return i.getArgument(0);
        });
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User actual = userService.updateName(username, request);

        assertEquals(request.newName(), actual.getName());
    }

    @Test
    @DisplayName("Обновление фамилии: пользователь не найден")
    public void updateSurname_WhenUserNotFound_ThenThrowException() {
        UpdateSurnameRequest request = new UpdateSurnameRequest("surname");
        String username = "principal";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateSurname(username, request));
    }

    @Test
    @DisplayName("Успешное обновление фамилии")
    public void updateSurname_WhenCorrectRequest_ThenUpdateUserInfo() {
        String username = "principal";
        String oldSurname = "oldSurname";
        User user = new User();
        user.setSurname(oldSurname);
        UpdateSurnameRequest request = new UpdateSurnameRequest("surname");
        when(userRepository.save(any())).thenAnswer(i -> {
            return i.getArgument(0);
        });
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User actual = userService.updateSurname(username, request);

        assertEquals(request.newSurname(), actual.getSurname());
    }

    @Test
    @DisplayName("Обновление данных 'о себе': пользователь не найден")
    public void updateAbout_WhenUserNotFound_ThenThrowException() {
        UpdateAboutRequest request = new UpdateAboutRequest("about");
        String username = "principal";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateAbout(username, request));
    }

    @Test
    @DisplayName("Успешное обновление данных 'о себе'")
    public void updateAbout_WhenCorrectRequest_ThenUpdateUserInfo() {
        String username = "principal";
        String oldAbout = "oldAbout";
        User user = new User();
        user.setAbout(oldAbout);
        UpdateAboutRequest request = new UpdateAboutRequest("about");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> {
            return i.getArgument(0);
        });

        User actual = userService.updateAbout(username, request);

        assertEquals(request.newAbout(), actual.getAbout());
    }

    @Test
    @DisplayName("Обновление фото: пользователь не найден")
    public void updatePhoto_WhenUserNotFound_ThenThrowException() {
        UpdatePhotoRequest request = new UpdatePhotoRequest(new MockMultipartFile("name", new byte[] {}));
        String username = "principal";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.replacePhoto(username, request));
    }

    @Test
    @DisplayName("Обновление фото: предыдущее фото удалено, сохранено новое")
    public void updatePhoto_WhenCorrectRequest_ThenReplacePhoto() {
        String username = "principal";
        User user = new User();
        user.setPhotoPath("oldPhoto.png");
        MockMultipartFile photo = new MockMultipartFile("name", "photo.png", "image/png", new byte[] {});
        UpdatePhotoRequest request = new UpdatePhotoRequest(photo);
        when(imageStorageService.store(eq(photo), any(), any())).thenReturn(photo.getOriginalFilename());
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> {
            return i.getArgument(0);
        });

        User actual = userService.replacePhoto(username, request);

        assertEquals(photo.getOriginalFilename(), actual.getPhotoPath());
        verify(imageStorageService, times(1)).remove(any(), any());
    }
}

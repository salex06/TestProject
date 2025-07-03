package salex.messenger.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import salex.messenger.dto.account.update.about.UpdateAboutRequest;
import salex.messenger.dto.account.update.name.UpdateNameRequest;
import salex.messenger.dto.account.update.photo.UpdatePhotoRequest;
import salex.messenger.dto.account.update.surname.UpdateSurnameRequest;
import salex.messenger.dto.signup.SignUpRequest;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.exception.UsernameAlreadyExistsException;
import salex.messenger.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUser(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.username())) {
            throw new UsernameAlreadyExistsException(
                    "Пользователь с именем '" + signUpRequest.username() + "' уже существует");
        }

        String filename = null;
        if (!signUpRequest.photo().isEmpty()) {
            imageStorageService.validateImageFile(signUpRequest.photo());
            filename = imageStorageService.store(
                    signUpRequest.photo(),
                    ImageStorageService.generateFilename(signUpRequest.username(), signUpRequest.photo()));
        }

        User user = new User(
                null,
                signUpRequest.username(),
                passwordEncoder.encode(signUpRequest.password()),
                signUpRequest.name(),
                signUpRequest.surname(),
                signUpRequest.about(),
                filename);
        return userRepository.save(user);
    }

    // TODO: заменить DTO signUpRequest на RemoveUserRequest
    public void removeUser(SignUpRequest signUpRequest) {
        User user = userRepository
                .findByUsername(signUpRequest.username())
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь '" + signUpRequest.username() + "' не найден"));

        userRepository.deleteById(user.getId());
    }

    public User updateName(String username, UpdateNameRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        user.setName(request.newName());

        return userRepository.save(user);
    }

    public User updateSurname(String username, UpdateSurnameRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        user.setSurname(request.newSurname());

        return userRepository.save(user);
    }

    public User updateAbout(String username, UpdateAboutRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        user.setAbout(request.newAbout());

        return userRepository.save(user);
    }

    public User replacePhoto(String username, UpdatePhotoRequest request) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        if (user.getPhotoPath() != null) {
            imageStorageService.remove(user.getPhotoPath());
            user.setPhotoPath(null);
        }

        imageStorageService.validateImageFile(request.newPhoto());
        String filepath = imageStorageService.store(
                request.newPhoto(), ImageStorageService.generateFilename(user.getUsername(), request.newPhoto()));

        user.setPhotoPath(filepath);
        return userRepository.save(user);
    }
}

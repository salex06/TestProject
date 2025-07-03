package salex.messenger.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

        imageStorageService.validateImageFile(signUpRequest.photo());
        String filename = imageStorageService.store(
                signUpRequest.photo(),
                signUpRequest.username()
                        + "-"
                        + UUID.randomUUID()
                        + '.' + FilenameUtils.getExtension(signUpRequest.photo().getOriginalFilename()));

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

    public void removeUser(SignUpRequest signUpRequest) {
        User user = userRepository
                .findByUsername(signUpRequest.username())
                .orElseThrow(
                        () -> new UserNotFoundException("Пользователь '" + signUpRequest.username() + "' не найден"));

        userRepository.deleteById(user.getId());
    }
}

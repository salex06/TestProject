package salex.messenger.service;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import salex.messenger.dto.signin.SignInRequest;
import salex.messenger.entity.User;
import salex.messenger.jwt.JwtHelper;
import salex.messenger.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;

    public String authenticate(SignInRequest signInRequest) {
        User user = userRepository
                .findByUsername(signInRequest.username())
                .orElseThrow(() -> new BadCredentialsException("Пользователь с таким именем не найден!"));

        if (!passwordEncoder.matches(signInRequest.password(), user.getEncryptedPassword())) {
            throw new BadCredentialsException("Неправильный пароль!");
        }

        return jwtHelper.createToken(Collections.emptyMap(), user.getUsername());
    }
}

package salex.messenger.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salex.messenger.config.JwtConfig;
import salex.messenger.dto.signin.SignInRequest;
import salex.messenger.dto.signup.SignUpRequest;
import salex.messenger.dto.signup.SignUpResponse;
import salex.messenger.entity.User;
import salex.messenger.service.AuthService;
import salex.messenger.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    private final JwtConfig jwtConfig;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signUp(@Valid @ModelAttribute SignUpRequest signUpRequest) {
        User user = userService.saveUser(signUpRequest);

        return new ResponseEntity<>(
                new SignUpResponse("Пользователь зарегистрирован", user.getId(), user.getUsername()),
                HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
        String jwt = authService.authenticate(signInRequest);
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtConfig.lifetime().toSeconds());
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}

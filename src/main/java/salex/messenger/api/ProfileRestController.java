package salex.messenger.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salex.messenger.dto.users.UserProfileInfo;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileRestController {
    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfileInfo(
            @PathVariable String username, Principal principal, HttpServletResponse response) throws IOException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (Objects.equals(principal.getName(), username)) {
            response.sendRedirect("/account");
        }

        User user = userService
                .findUser(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден!"));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new UserProfileInfo(
                        user.getUsername(), user.getName(), user.getSurname(), user.getAbout(), user.getPhotoPath()));
    }
}

package salex.messenger.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salex.messenger.dto.account.UserInfo;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountRestController {
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<?> getUserData(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService
                .findUser(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден!"));

        return new ResponseEntity<>(new UserInfo(user.getUsername()), HttpStatus.OK);
    }

    @PostMapping("/quit")
    public ResponseEntity<?> quit(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}

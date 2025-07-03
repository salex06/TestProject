package salex.messenger.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salex.messenger.dto.account.UserInfo;
import salex.messenger.dto.account.update.about.UpdateAboutRequest;
import salex.messenger.dto.account.update.about.UpdateAboutResponse;
import salex.messenger.dto.account.update.name.UpdateNameRequest;
import salex.messenger.dto.account.update.name.UpdateNameResponse;
import salex.messenger.dto.account.update.photo.UpdatePhotoRequest;
import salex.messenger.dto.account.update.photo.UpdatePhotoResponse;
import salex.messenger.dto.account.update.surname.UpdateSurnameRequest;
import salex.messenger.dto.account.update.surname.UpdateSurnameResponse;
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

        return new ResponseEntity<>(
                new UserInfo(
                        user.getUsername(), user.getName(), user.getSurname(), user.getPhotoPath(), user.getAbout()),
                HttpStatus.OK);
    }

    @PatchMapping("/name")
    public ResponseEntity<?> updateName(@RequestBody UpdateNameRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.updateName(principal.getName(), request);

        return new ResponseEntity<>(new UpdateNameResponse(user.getName()), HttpStatus.OK);
    }

    @PatchMapping("/surname")
    public ResponseEntity<?> updateSurname(@RequestBody UpdateSurnameRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.updateSurname(principal.getName(), request);

        return new ResponseEntity<>(new UpdateSurnameResponse(user.getSurname()), HttpStatus.OK);
    }

    @PatchMapping("/about")
    public ResponseEntity<?> updateAbout(@RequestBody UpdateAboutRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.updateAbout(principal.getName(), request);

        return new ResponseEntity<>(new UpdateAboutResponse(user.getAbout()), HttpStatus.OK);
    }

    @PatchMapping("/photo")
    public ResponseEntity<?> updatePhoto(@ModelAttribute UpdatePhotoRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.replacePhoto(principal.getName(), request);

        return new ResponseEntity<>(new UpdatePhotoResponse(user.getPhotoPath()), HttpStatus.OK);
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

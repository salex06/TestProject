package salex.messenger.api;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import salex.messenger.dto.users.SearchResponse;
import salex.messenger.dto.users.UserProfileInfo;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersRestController {
    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String query, @PageableDefault(size = 5) Pageable pageable, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Page<User> users = userService.getUsersByUsernamePattern(query, pageable, principal.getName());

        return new ResponseEntity<>(
                new SearchResponse(
                        users.map(this::convertToUserInfo).getContent(),
                        users.getTotalPages(),
                        users.getTotalElements(),
                        users.getNumber(),
                        users.getSize()),
                HttpStatus.OK);
    }

    @GetMapping("/photo")
    public ResponseEntity<?> getPhotoPath(@RequestParam(name = "username") String username, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService
                .findUser(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        return ResponseEntity.ok(user.getPhotoPath());
    }

    private UserProfileInfo convertToUserInfo(User user) {
        return new UserProfileInfo(
                user.getUsername(), user.getName(), user.getSurname(), user.getAbout(), user.getPhotoPath());
    }
}

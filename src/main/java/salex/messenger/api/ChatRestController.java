package salex.messenger.api;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salex.messenger.dto.chat.messages.ChatHistoryResponse;
import salex.messenger.dto.chat.messages.MessageInfo;
import salex.messenger.dto.chat.user.ChatPartnerInfo;
import salex.messenger.dto.chat.user.ChatPartnersResponse;
import salex.messenger.entity.Message;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.service.MessageService;
import salex.messenger.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {
    private final MessageService messageService;
    private final UserService userService;

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestParam("username") String chatPartnerUsername, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Message> history = messageService.getChatHistory(principal.getName(), chatPartnerUsername);

        return new ResponseEntity<>(
                new ChatHistoryResponse(
                        history.stream().map(this::convertToMessageInfo).toList(), history.size()),
                HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getChatPartners(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<User> chatPartners = messageService.findChatPartners(principal.getName());

        return new ResponseEntity<>(
                new ChatPartnersResponse(
                        chatPartners.stream()
                                .map(this::convertToChatPartnerInfo)
                                .toList(),
                        chatPartners.size()),
                HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getChatPartnerInfo(@RequestParam String username, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService
                .findUser(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        return new ResponseEntity<>(convertToChatPartnerInfo(user), HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<?> removeChat(@RequestParam("second") String secondUsername, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        messageService.removeChat(principal.getName(), secondUsername);

        return ResponseEntity.ok("");
    }

    private MessageInfo convertToMessageInfo(Message message) {
        return new MessageInfo(
                message.getText(),
                message.getCreatedAt(),
                message.getSender().getUsername(),
                message.getReceiver().getUsername());
    }

    private ChatPartnerInfo convertToChatPartnerInfo(User user) {
        return new ChatPartnerInfo(user.getUsername(), user.getPhotoPath());
    }
}

package salex.messenger.websocket;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import salex.messenger.dto.chat.messages.MessageInfo;
import salex.messenger.dto.chat.messages.WebSocketMessage;
import salex.messenger.entity.Message;
import salex.messenger.service.MessageService;
import salex.messenger.service.UserService;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final UserService userService;
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat/{to}")
    public void getMessages(@DestinationVariable String to, WebSocketMessage message, Principal principal) {
        if (principal == null) {
            return;
        }

        Message savedMessage = messageService.saveMessage(principal.getName(), to, message);

        simpMessagingTemplate.convertAndSend("/topic/messages/" + to, convertToMessageInfo(savedMessage));
    }

    private MessageInfo convertToMessageInfo(Message message) {
        return new MessageInfo(
                message.getText(),
                message.getPathToMessageImage(),
                message.getCreatedAt(),
                message.getSender().getUsername(),
                message.getReceiver().getUsername());
    }
}

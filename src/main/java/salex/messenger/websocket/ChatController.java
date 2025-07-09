package salex.messenger.websocket;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import salex.messenger.dto.chat.messages.MessageInfo;
import salex.messenger.dto.chat.messages.SimpleMessage;
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
    public Message getMessages(@DestinationVariable String to, SimpleMessage message, Principal principal) {
        if (principal == null) {
            return null;
        }

        Message savedMessage = messageService.saveMessage(principal.getName(), to, message);
        simpMessagingTemplate.convertAndSend("/topic/messages/" + to, convertToMessageInfo(savedMessage));

        return savedMessage;
    }

    private MessageInfo convertToMessageInfo(Message message) {
        return new MessageInfo(
                message.getText(),
                message.getCreatedAt(),
                message.getSender().getUsername(),
                message.getReceiver().getUsername());
    }
}

package salex.messenger.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import salex.messenger.dto.chat.messages.SimpleMessage;
import salex.messenger.entity.Message;
import salex.messenger.entity.MessageStatus;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.repository.MessageRepository;
import salex.messenger.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<User> findChatPartners(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        return messageRepository.getAllChatPartners(user.getId());
    }

    public List<Message> getChatHistory(String firstUsername, String secondUsername) {
        User first = userRepository
                .findByUsername(firstUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + firstUsername + "' не найден"));
        User second = userRepository
                .findByUsername(secondUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + secondUsername + "' не найден"));

        List<Message> messages = messageRepository.getChatHistory(first.getId(), second.getId());

        return messages.stream()
                .map(msg -> {
                    if (Objects.equals(msg.getReceiver().getUsername(), first.getUsername())) {
                        msg.setStatus(MessageStatus.RECEIVED);
                        return messageRepository.save(msg);
                    }
                    return msg;
                })
                .toList();
    }

    public Message saveMessage(String senderUsername, String receiverUsername, SimpleMessage simpleMessage) {
        User sender = userRepository
                .findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + senderUsername + "' не найден"));
        User receiver = userRepository
                .findByUsername(receiverUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + receiverUsername + "' не найден"));

        Message message =
                new Message(null, simpleMessage.text(), LocalDateTime.now(), sender, receiver, MessageStatus.SENT);

        return messageRepository.save(message);
    }

    public void removeChat(String firstUsername, String secondUsername) {
        User first = userRepository
                .findByUsername(firstUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + firstUsername + "' не найден"));
        User second = userRepository
                .findByUsername(secondUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + secondUsername + "' не найден"));

        messageRepository.removeChat(first.getId(), second.getId());
    }

    public int getUnreadMessageCount(String firstUsername, String secondUsername) {
        User first = userRepository
                .findByUsername(firstUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + firstUsername + "' не найден"));
        User second = userRepository
                .findByUsername(secondUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + secondUsername + "' не найден"));

        return messageRepository.getUnreadMessageCount(first.getId(), second.getId());
    }

    public void markAllMessagesAsRead(String firstUsername, String secondUsername) {
        User first = userRepository
                .findByUsername(firstUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + firstUsername + "' не найден"));
        User second = userRepository
                .findByUsername(secondUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + secondUsername + "' не найден"));

        messageRepository.markAllMessagesAsReadByReceiverAndSender(first.getId(), second.getId());
    }
}

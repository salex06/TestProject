package salex.messenger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static salex.messenger.entity.MessageStatus.SENT;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import salex.messenger.dto.chat.messages.WebSocketMessage;
import salex.messenger.entity.Message;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.repository.MessageRepository;
import salex.messenger.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class MessageServiceTest {
    @Autowired
    private MessageService messageService;

    @MockitoBean
    private MessageRepository messageRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Неудачная попытка получения списка собеседников: пользователь не найден")
    public void findChatPartners_WhenUserNotFound_ThenThrowException() {
        String username = "alex";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> messageService.findChatPartners(username));
    }

    @Test
    @DisplayName("Успешная попытка получения списка собеседников")
    public void findChatPartners_WhenCorrectUsername_ThenReturnUserList() {
        String username = "alex";
        User user = new User(1L, username, "123", "", "", "", "");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        User alice = new User(2L, "alice", "123", "", "", "", "");
        User andrew = new User(3L, "andrew", "123", "", "", "", "");
        List<User> expected = List.of(alice, andrew);
        when(messageRepository.getAllChatPartners(user.getId())).thenReturn(expected);

        List<User> actual = messageService.findChatPartners(username);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Неудачная попытка получения истории чата: user1 не найден")
    public void getChatHistory_WhenFirstUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> messageService.getChatHistory(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Неудачная попытка получения истории чата: user2 не найден")
    public void getChatHistory_WhenSecondUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> messageService.getChatHistory(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Успешная попытка получения истории чата")
    public void getChatHistory_WHenCorrectUsername_ThenReturnChatHistory() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        User alex = new User(1L, secondUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.of(alex));
        List<Message> expected = List.of(
                new Message(1L, "hello", null, LocalDateTime.now(), alice, alex, SENT),
                new Message(2L, "how are you?", null, LocalDateTime.now(), alex, alice, SENT));
        when(messageRepository.getChatHistory(alice.getId(), alex.getId())).thenReturn(expected);
        when(messageRepository.save(any())).thenAnswer(i -> {
            return i.getArgument(0);
        });

        List<Message> actual = messageService.getChatHistory(firstUsername, secondUsername);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Неудачная попытка сохранения сообщения: отправитель не найден")
    public void saveMessage_WhenSenderNotFound_ThenThrowException() {
        WebSocketMessage message = new WebSocketMessage("text", null);
        String senderUsername = "alice";
        String receiverUsername = "alex";
        when(userRepository.findByUsername(senderUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> messageService.saveMessage(senderUsername, receiverUsername, message));
    }

    @Test
    @DisplayName("Неудачная попытка сохранения сообщения: получатель не найден")
    public void saveMessage_WhenReceiverNotFound_ThenThrowException() {
        WebSocketMessage message = new WebSocketMessage("text", null);
        String senderUsername = "alice";
        User alice = new User(2L, senderUsername, "123", "", "", "", "");
        String receiverUsername = "alex";
        when(userRepository.findByUsername(senderUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(receiverUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> messageService.saveMessage(senderUsername, receiverUsername, message));
    }

    @Test
    @DisplayName("Успешная попытка сохранения сообщения")
    public void saveMessage_WhenCorrectParams_ThenSaveMessage() {
        WebSocketMessage message = new WebSocketMessage("text", null);
        String senderUsername = "alice";
        User alice = new User(2L, senderUsername, "123", "", "", "", "");
        String receiverUsername = "alex";
        User alex = new User(1L, receiverUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(senderUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(receiverUsername)).thenReturn(Optional.of(alex));
        Message expected = new Message(1L, message.text(), null, LocalDateTime.MIN, alice, alex, SENT);
        when(messageRepository.save(any())).thenAnswer(i -> {
            Message message1 = i.getArgument(0);
            return new Message(
                    1L,
                    message1.getText(),
                    null,
                    LocalDateTime.MIN,
                    message1.getSender(),
                    message1.getReceiver(),
                    SENT);
        });

        Message actual = messageService.saveMessage(senderUsername, receiverUsername, message);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Неудачная попытка удаления чата: user1 не найден")
    public void removeChat_WhenFirstUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> messageService.removeChat(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Неудачная попытка удаления чата: user2 не найден")
    public void removeChat_WhenSecondUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> messageService.removeChat(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Успешная попытка удаления чата")
    public void removeChat_WhenCorrectParams_ThenRemoveChat() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        User alex = new User(1L, secondUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.of(alex));

        messageService.removeChat(firstUsername, secondUsername);

        verify(messageRepository, times(1)).removeChat(alice.getId(), alex.getId());
    }

    @Test
    @DisplayName("Неудачная попытка получения количества непрочитанных сообщений: user1 не найден")
    public void getUnreadMessageCount_WhenFirstUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> messageService.getUnreadMessageCount(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Неудачная попытка получения количества непрочитанных сообщений: user2 не найден")
    public void getUnreadMessageCount_WhenSecondUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> messageService.getUnreadMessageCount(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Успешная попытка получения количества непрочитанных сообщений")
    public void getUnreadMessageCount_WhenCorrectUsername_ThenReturnMessageCount() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        User alex = new User(1L, secondUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.of(alex));
        int expected = 5;
        when(messageRepository.getUnreadMessageCount(alice.getId(), alex.getId()))
                .thenReturn(expected);

        int actual = messageService.getUnreadMessageCount(firstUsername, secondUsername);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Неудачная попытка изменения статуса сообщений: user1 не найден")
    public void markAllMessagesAsRead_WhenFirstUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> messageService.markAllMessagesAsRead(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Неудачная попытка изменения статуса сообщений: user2 не найден")
    public void markAllMessagesAsRead_WhenSecondUserNotFound_ThenThrowException() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> messageService.markAllMessagesAsRead(firstUsername, secondUsername));
    }

    @Test
    @DisplayName("Успешная попытка изменения статуса сообщений")
    public void markAllMessagesAsRead_WhenCorrectUsername_ThenUpdateStatus() {
        String firstUsername = "alice";
        User alice = new User(2L, firstUsername, "123", "", "", "", "");
        String secondUsername = "alex";
        User alex = new User(1L, secondUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(firstUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(secondUsername)).thenReturn(Optional.of(alex));

        messageService.markAllMessagesAsRead(firstUsername, secondUsername);

        verify(messageRepository, times(1)).markAllMessagesAsReadByReceiverAndSender(alice.getId(), alex.getId());
    }
}

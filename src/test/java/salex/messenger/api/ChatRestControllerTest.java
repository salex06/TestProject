package salex.messenger.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static salex.messenger.entity.MessageStatus.SENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.chat.messages.ChatHistoryResponse;
import salex.messenger.dto.chat.messages.MessageInfo;
import salex.messenger.dto.chat.messages.UnreadMessageCountResponse;
import salex.messenger.dto.chat.user.ChatPartnerInfo;
import salex.messenger.dto.chat.user.ChatPartnersResponse;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.entity.Message;
import salex.messenger.entity.User;
import salex.messenger.service.MessageService;
import salex.messenger.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    @DisplayName("Неудчаное получение истории чата: пользователь не авторизован")
    public void getChatHistory_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/chat/history").param("username", "firstUsername"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешное получение истории чата")
    @WithMockUser("user")
    public void getChatHistory_WhenCorrectRequest_ThenReturnOkResponse() throws Exception {
        User firstUser = new User(1L, "user", "12345", "name", "surname", "", "");
        User secondUser = new User(2L, "contact", "12345", "name", "surname", "", "");
        Message msg1 = new Message(1L, "hello", LocalDateTime.now(), firstUser, secondUser, SENT);
        Message msg2 = new Message(2L, "how are you?", LocalDateTime.now(), secondUser, firstUser, SENT);
        List<Message> expectedHistory = List.of(msg1, msg2);
        List<MessageInfo> expectedConvertedHistory = List.of(
                new MessageInfo(
                        msg1.getText(),
                        msg1.getCreatedAt(),
                        msg1.getSender().getUsername(),
                        msg1.getReceiver().getUsername()),
                new MessageInfo(
                        msg2.getText(),
                        msg2.getCreatedAt(),
                        msg2.getSender().getUsername(),
                        msg2.getReceiver().getUsername()));
        when(messageService.getChatHistory(firstUser.getUsername(), secondUser.getUsername()))
                .thenReturn(expectedHistory);
        String response = objectMapper.writeValueAsString(
                new ChatHistoryResponse(expectedConvertedHistory, expectedHistory.size()));

        mockMvc.perform(get("/api/chat/history").param("username", secondUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }

    @Test
    @DisplayName("Неудачный запрос списка собеседников: пользователь не авторизован")
    public void getChatPartners_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/chat/list")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос списка собеседников")
    @WithMockUser("user")
    public void getChatPartners_WhenAuthorized_ThenReturnListOfUsers() throws Exception {
        String username = "user";
        User first = new User(1L, "user1", "12345", "", "", "", "");
        User second = new User(2L, "user2", "12345", "", "", "", "");
        List<User> chatPartners = List.of(first, second);
        List<ChatPartnerInfo> convertedChatPartners = List.of(
                new ChatPartnerInfo(first.getUsername(), first.getPhotoPath()),
                new ChatPartnerInfo(second.getUsername(), second.getPhotoPath()));
        when(messageService.findChatPartners(username)).thenReturn(chatPartners);
        ChatPartnersResponse response = new ChatPartnersResponse(convertedChatPartners, convertedChatPartners.size());

        mockMvc.perform(get("/api/chat/list"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Неудачный запрос информации о пользователе: пользователь не авторизован")
    public void getChatPartnerInfo_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/chat/user").param("username", "user")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Неудачный запрос информации о пользователе: пользователь не найден")
    @WithMockUser("user")
    public void getChatPartnerInfo_WhenUserNotFound_ThenReturnApiErrorEx() throws Exception {
        String username = "user";
        String requestedUsername = "user2";
        when(userService.findUser(requestedUsername)).thenReturn(Optional.empty());
        ApiErrorResponse response = new ApiErrorResponse(
                "Пользователь не найден (или удален)!",
                "404",
                "UserNotFoundException",
                "Пользователь 'user2' не найден");

        mockMvc.perform(get("/api/chat/user").param("username", requestedUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Удачный запрос информации о пользователе")
    @WithMockUser("user")
    public void getChatPartnerInfo_WhenCorrectRequest_ThenReturnInfo() throws Exception {
        String username = "user";
        String requestedUsername = "user2";
        User user = new User(1L, requestedUsername, "12345", "", "", "", "");
        when(userService.findUser(requestedUsername)).thenReturn(Optional.of(user));
        ChatPartnerInfo response = new ChatPartnerInfo(user.getUsername(), user.getPhotoPath());

        mockMvc.perform(get("/api/chat/user").param("username", requestedUsername))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Неудачный запрос на удаление чата: пользователь не авторизован")
    public void removeChat_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(delete("/api/chat").param("second", "user")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Удачный запрос на удаление чата")
    @WithMockUser("user")
    public void removeChat_WhenCorrectRequest_ThenRemoveChat() throws Exception {
        String username = "user";
        String secondUsername = "user2";

        mockMvc.perform(delete("/api/chat").param("second", secondUsername)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Неудачный запрос получения количества непрочитанных сообщений: пользователь не авторизован")
    public void getUnreadMessageCount_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/chat/history/unread/count").param("username", "user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос получения количества прочитанных сообщений")
    @WithMockUser("user")
    public void getUnreadMessageCount_WhenCorrectRequest_ThenReturnCount() throws Exception {
        String username = "user";
        String chatPartnerUsername = "alex";
        int expectedAns = 5;
        when(messageService.getUnreadMessageCount(username, chatPartnerUsername))
                .thenReturn(expectedAns);
        UnreadMessageCountResponse response = new UnreadMessageCountResponse(expectedAns);

        mockMvc.perform(get("/api/chat/history/unread/count").param("username", chatPartnerUsername))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Неудачный запрос на обновление состояния сообщений: пользователь не авторизован")
    public void markAllMessagesAsRead_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(patch("/api/chat/history/mark/read").param("username", "username"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос на обновление состояния сообщений")
    @WithMockUser("user")
    public void markAllMessagesAsRead_WhenCorrectRequest_ThenUpdateMessageStatus() throws Exception {
        String username = "user";
        String chatPartnerUsername = "alex";

        mockMvc.perform(patch("/api/chat/history/mark/read").param("username", chatPartnerUsername))
                .andExpect(status().isOk());

        verify(messageService, times(1)).markAllMessagesAsRead(username, chatPartnerUsername);
    }
}

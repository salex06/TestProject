package salex.messenger.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.contact.ContactInfo;
import salex.messenger.dto.contact.ContactListResponse;
import salex.messenger.dto.contact.RemoveContactRequest;
import salex.messenger.dto.contact.SaveContactRequest;
import salex.messenger.entity.Contact;
import salex.messenger.entity.User;
import salex.messenger.service.ContactService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ContactRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    @DisplayName("Неудачный запрос на получение контактов: пользователь не авторизован")
    public void getContacts_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/contacts")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос на получение контактов")
    @WithMockUser("user")
    public void getContacts_WhenCorrectRequest_ThenReturnContactList() throws Exception {
        String username = "user";
        User user1 = new User(1L, username, "12345", "", "", "", "");
        User user2 = new User(2L, "alice", "12345", "", "", "", "");
        User user3 = new User(3L, "andrew", "12345", "", "", "", "");
        Contact contact1 = new Contact(1L, user1, user2);
        Contact contact2 = new Contact(2L, user1, user3);
        List<Contact> contactList = List.of(contact1, contact2);
        when(contactService.getAllContactsByUsername(username)).thenReturn(contactList);
        List<ContactInfo> converted = List.of(
                new ContactInfo(user2.getUsername(), user2.getName(), user2.getSurname(), user2.getPhotoPath()),
                new ContactInfo(user3.getUsername(), user3.getName(), user3.getSurname(), user3.getPhotoPath()));
        ContactListResponse response = new ContactListResponse(converted, converted.size());

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("Неудачный запрос на сохранение контакта: пользователь не авторизован")
    public void saveContact_WhenUnauthorized_ThenReturn401() throws Exception {
        SaveContactRequest request = new SaveContactRequest("123");
        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Неудачный запрос на сохранение контакта: контакт уже сохранен")
    @WithMockUser("user")
    public void saveContact_WhenContactAlreadyExists_ThenReturnBadRequest() throws Exception {
        String username = "user";
        String contactUsername = "321";
        SaveContactRequest request = new SaveContactRequest(contactUsername);
        when(contactService.existsByUsernames(username, contactUsername)).thenReturn(true);

        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешный запрос на сохранение контакта")
    @WithMockUser("user")
    public void saveContact_WhenCorrectRequest_ThenSaveContactAndReturnOk() throws Exception {
        String username = "user";
        String contactUsername = "321";
        SaveContactRequest request = new SaveContactRequest(contactUsername);
        when(contactService.existsByUsernames(username, contactUsername)).thenReturn(false);

        mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(contactService, times(1)).saveContact(username, request.contact());
    }

    @Test
    @DisplayName("Неудачный запрос на удаление контакта: пользователь не авторизован")
    public void removeContact_WhenUnauthorized_ThenReturn401() throws Exception {
        RemoveContactRequest request = new RemoveContactRequest("123");
        mockMvc.perform(delete("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Неудачный запрос на удаление контакта: контакт не найден")
    @WithMockUser("user")
    public void removeContact_WhenContactNotFound_ThenReturnBadRequest() throws Exception {
        String username = "user";
        String contactUsername = "321";
        RemoveContactRequest request = new RemoveContactRequest(contactUsername);
        when(contactService.existsByUsernames(username, contactUsername)).thenReturn(false);

        mockMvc.perform(delete("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешный запрос на удаление контакта")
    @WithMockUser("user")
    public void removeContact_WhenCorrectRequest_ThenRemoveContactAndReturnOk() throws Exception {
        String username = "user";
        String contactUsername = "321";
        RemoveContactRequest request = new RemoveContactRequest(contactUsername);
        when(contactService.existsByUsernames(username, contactUsername)).thenReturn(true);

        mockMvc.perform(delete("/api/contacts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(contactService, times(1)).removeContact(username, request.contact());
    }

    @Test
    @DisplayName("Неудачный запрос на проверку наличия контакта: пользователь не авторизован")
    public void checkExistsContact_WhenUnauthorized_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/contacts/check").param("contact", "user1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Успешный запрос на проверку наличия контакта")
    @WithMockUser("user")
    public void checkExistsContact_WhenCorrectRequest_ThenReturnCheckResult() throws Exception {
        String username = "user";
        String contactUsername = "user1";
        when(contactService.existsByUsernames(username, contactUsername)).thenReturn(true);

        mockMvc.perform(get("/api/contacts/check").param("contact", contactUsername))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}

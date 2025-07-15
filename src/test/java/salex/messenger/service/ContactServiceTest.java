package salex.messenger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import salex.messenger.entity.Contact;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.repository.ContactRepository;
import salex.messenger.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class ContactServiceTest {
    @Autowired
    private ContactService contactService;

    @MockitoBean
    private ContactRepository contactRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Неудачное получение контактов по username: пользователь не найден")
    public void getAllContactsByUsername_WhenUserNotFound_ThenThrowException() {
        String username = "alice";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contactService.getAllContactsByUsername(username));
    }

    @Test
    @DisplayName("Успешное получение контактов по username")
    public void getAllContactsByUsername_WhenCorrectUsername_ThenReturnContactList() {
        String username = "alex";
        User user = new User(1L, username, "123", "", "", "", "");
        User alice = new User(2L, "alice", "123", "", "", "", "");
        User john = new User(3L, "john", "123", "", "", "", "");
        Contact first = new Contact(1L, user, alice);
        Contact second = new Contact(2L, user, john);
        List<Contact> contactList = List.of(first, second);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(contactRepository.getAllContactsByOwnerId(user.getId())).thenReturn(contactList);

        List<Contact> actual = contactService.getAllContactsByUsername(username);

        assertEquals(contactList, actual);
    }

    @Test
    @DisplayName("Неудачная попытка сохранения контакта: owner не найден")
    public void saveContact_WhenFirstUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contactService.saveContact(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Неудачная попытка сохранения контакта: contact не найден")
    public void saveContact_WhenSecondUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        User alice = new User(2L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contactService.saveContact(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Успешная попытка сохранения контакта")
    public void saveContact_WhenCorrectUsername_ThenSaveContact() {
        String ownerUsername = "alice";
        User alice = new User(1L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        User alex = new User(2L, contactUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.of(alex));
        Contact contact = new Contact(1L, alice, alex);
        when(contactRepository.save(any())).thenAnswer(i -> {
            Contact contact1 = i.getArgument(0);
            return new Contact(1L, contact1.getOwner(), contact1.getContact());
        });

        Contact actual = contactService.saveContact(ownerUsername, contactUsername);

        assertEquals(contact, actual);
    }

    @Test
    @DisplayName("Неудачная попытка удаления контакта: owner не найден")
    public void removeContact_WhenFirstUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contactService.removeContact(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Неудачная попытка удаления контакта: contact не найден")
    public void removeContact_WhenSecondUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        User alice = new User(2L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> contactService.removeContact(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Успешная попытка удаления контакта")
    public void removeContact_WhenCorrectUsername_ThenRemoveContact() {
        String ownerUsername = "alice";
        User alice = new User(1L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        User alex = new User(2L, contactUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.of(alex));

        contactService.removeContact(ownerUsername, contactUsername);

        verify(contactRepository, times(1)).deleteContactByOwnerAndContactIds(alice.getId(), alex.getId());
    }

    @Test
    @DisplayName("Неудачная попытка проверки наличия контакта: owner не найден")
    public void existsByUsernames_WhenFirstUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> contactService.existsByUsernames(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Неудачная попытка проверки наличия контакта: contact не найден")
    public void existsByUsernames_WhenSecondUserNotFound_ThenThrowException() {
        String ownerUsername = "alice";
        User alice = new User(2L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> contactService.existsByUsernames(ownerUsername, contactUsername));
    }

    @Test
    @DisplayName("Успешная попытка проверки наличия контакта")
    public void existsByUsernames_WhenCorrectUsername_ThenReturnResult() {
        String ownerUsername = "alice";
        User alice = new User(1L, ownerUsername, "123", "", "", "", "");
        String contactUsername = "alex";
        User alex = new User(2L, contactUsername, "123", "", "", "", "");
        when(userRepository.findByUsername(ownerUsername)).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername(contactUsername)).thenReturn(Optional.of(alex));
        Contact contact = new Contact(1L, alice, alex);
        when(contactRepository.existsContact(alice.getId(), alex.getId())).thenReturn(contact);

        boolean result = contactService.existsByUsernames(ownerUsername, contactUsername);

        assertTrue(result);
    }
}

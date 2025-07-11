package salex.messenger.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import salex.messenger.entity.Contact;
import salex.messenger.entity.User;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.repository.ContactRepository;
import salex.messenger.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public List<Contact> getAllContactsByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        return contactRepository.getAllContactsByOwnerId(user.getId());
    }

    public Contact saveContact(String ownerUsername, String contactUsername) {
        User ownerUser = userRepository
                .findByUsername(ownerUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + ownerUsername + "' не найден"));
        User contactUser = userRepository
                .findByUsername(contactUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + contactUsername + "' не найден"));

        Contact contact = new Contact(null, ownerUser, contactUser);

        return contactRepository.save(contact);
    }

    public void removeContact(String ownerUsername, String contactUsername) {
        User ownerUser = userRepository
                .findByUsername(ownerUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + ownerUsername + "' не найден"));
        User contactUser = userRepository
                .findByUsername(contactUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + contactUsername + "' не найден"));

        contactRepository.deleteContactByOwnerAndContactIds(ownerUser.getId(), contactUser.getId());
    }

    public boolean existsByUsernames(String ownerUsername, String contactUsername) {
        User ownerUser = userRepository
                .findByUsername(ownerUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + ownerUsername + "' не найден"));
        User contactUser = userRepository
                .findByUsername(contactUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + contactUsername + "' не найден"));

        return contactRepository.existsContact(ownerUser.getId(), contactUser.getId()) != null;
    }

    public List<Contact> getContacts(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь '" + username + "' не найден"));

        return contactRepository.getAllContactsByOwnerId(user.getId());
    }
}

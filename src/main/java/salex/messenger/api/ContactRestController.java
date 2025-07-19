package salex.messenger.api;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salex.messenger.dto.contact.ContactInfo;
import salex.messenger.dto.contact.ContactListResponse;
import salex.messenger.dto.contact.RemoveContactRequest;
import salex.messenger.dto.contact.SaveContactRequest;
import salex.messenger.entity.Contact;
import salex.messenger.entity.User;
import salex.messenger.service.ContactService;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactRestController {
    private final ContactService contactService;

    @GetMapping("")
    public ResponseEntity<?> getContacts(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Contact> contacts = contactService.getAllContactsByUsername(principal.getName());

        return new ResponseEntity<>(
                new ContactListResponse(
                        contacts.stream().map(this::convertToContactInfo).toList(), contacts.size()),
                HttpStatus.OK);
    }

    private ContactInfo convertToContactInfo(Contact contact) {
        User contactUser = contact.getContact();
        return new ContactInfo(
                contactUser.getUsername(), contactUser.getName(), contactUser.getSurname(), contactUser.getPhotoPath());
    }

    @PostMapping("")
    public ResponseEntity<?> saveContact(@Valid @RequestBody SaveContactRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (contactService.existsByUsernames(principal.getName(), request.contact())) {
            return ResponseEntity.badRequest().build();
        }

        contactService.saveContact(principal.getName(), request.contact());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("")
    public ResponseEntity<?> removeContact(@Valid @RequestBody RemoveContactRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!contactService.existsByUsernames(principal.getName(), request.contact())) {
            return ResponseEntity.badRequest().build();
        }

        contactService.removeContact(principal.getName(), request.contact());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkExistsContact(
            @RequestParam(name = "contact") String contactUsername, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean result = contactService.existsByUsernames(principal.getName(), contactUsername);

        return ResponseEntity.ok(result);
    }
}

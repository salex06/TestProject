package salex.messenger.dto.contact;

import jakarta.validation.Valid;
import java.util.List;

public record ContactListResponse(@Valid List<ContactInfo> contacts, Integer size) {}

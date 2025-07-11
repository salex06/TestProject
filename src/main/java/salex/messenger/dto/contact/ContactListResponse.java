package salex.messenger.dto.contact;

import java.util.List;

public record ContactListResponse(List<ContactInfo> contacts, Integer size) {}

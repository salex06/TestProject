package salex.messenger.dto.account.update.photo;

import org.springframework.web.multipart.MultipartFile;

public record UpdatePhotoRequest(MultipartFile newPhoto) {}

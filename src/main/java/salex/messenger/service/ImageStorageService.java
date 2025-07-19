package salex.messenger.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import salex.messenger.config.LocalStorageConfig;
import salex.messenger.exception.StorageException;

@Service
public class ImageStorageService {
    private final LocalStorageConfig config;

    @Getter
    private final Path userPhotoDir;

    @Getter
    private final Path messageMediaDir;

    public ImageStorageService(LocalStorageConfig config) {
        this.config = config;
        this.userPhotoDir = convertToPath(config.userPhotoLocation());
        this.messageMediaDir = convertToPath(config.messageMediaLocation());
    }

    public String store(MultipartFile image, String filename, Path destDir) {
        try {
            validateFile(image);

            Path dest = destDir.resolve(filename).normalize().toAbsolutePath();

            Files.createDirectories(destDir);
            image.transferTo(dest);
            return filename;
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить файл " + filename, e);
        }
    }

    public void remove(String filename, Path sourceDir) {
        Path dir = sourceDir.resolve(filename).normalize().toAbsolutePath();
        try {
            if (dir.toFile().exists()) Files.delete(dir);
        } catch (IOException e) {
            throw new StorageException("Не удалось удалить файл " + filename, e);
        }
    }

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Файл не должен быть пустым");
        }

        if (file.getSize() > config.maxFileSize().toBytes()) {
            throw new StorageException("Размер файла не должен превышать " + config.maxFileSize() + " Байт");
        }

        if (!"image/jpeg".equals(file.getContentType()) && !"image/png".equals(file.getContentType())) {
            throw new StorageException("Допустимы только JPEG и PNG изображения");
        }
    }

    public Resource loadAsResource(String filename, Path sourceDir) {
        Path path = sourceDir.resolve(filename);
        Resource resource = new PathResource(path);

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else if (!resource.exists()) {
            return null;
        } else {
            throw new StorageException("Не удалось прочитать файл " + filename);
        }
    }

    public static String generateFilename(String prefix, MultipartFile file) {
        return prefix + "-" + UUID.randomUUID() + '.' + FilenameUtils.getExtension(file.getOriginalFilename());
    }

    private static Path convertToPath(String pathStr) {
        return Path.of(pathStr).toAbsolutePath().normalize();
    }
}

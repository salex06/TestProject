package salex.messenger.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import salex.messenger.config.LocalStorageConfig;
import salex.messenger.exception.StorageException;

@Service
public class ImageStorageService {
    private final LocalStorageConfig config;
    private final Path rootDir;

    public ImageStorageService(LocalStorageConfig config) {
        this.config = config;
        this.rootDir = convertToPath(config.location());
    }

    public String store(MultipartFile image, String filename) {
        try {
            Path dest = rootDir.resolve(filename).normalize().toAbsolutePath();

            image.transferTo(dest);
            return filename;
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить файл " + filename, e);
        }
    }

    public void remove(String filename) {
        Path dest = rootDir.resolve(filename).normalize().toAbsolutePath();
        try {
            if (dest.toFile().exists()) Files.delete(dest);
        } catch (IOException e) {
            throw new StorageException("Не удалось удалить файл " + filename, e);
        }
    }

    public void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Файл не должен быть пустым");
        }

        if (file.getSize() > config.maxFileSize().toBytes()) {
            throw new StorageException("Размер файла не должен превышать " + config.maxFileSize() + " Байт");
        }

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new StorageException("Допустимы только JPEG и PNG изображения");
        }
    }

    public Resource loadAsResource(String filename) {
        Path path = rootDir.resolve(filename);
        Resource resource = new PathResource(path);

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new StorageException("Не удалось прочитать файл " + filename);
        }
    }

    private static Path convertToPath(String pathStr) {
        return Path.of(pathStr).toAbsolutePath().normalize();
    }
}

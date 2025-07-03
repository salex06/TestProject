package salex.messenger.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import salex.messenger.config.LocalStorageConfig;
import salex.messenger.exception.StorageException;

class ImageStorageServiceTest {
    private ImageStorageService service;

    private LocalStorageConfig config;

    public ImageStorageServiceTest() {
        config = new LocalStorageConfig("src/test/java/salex/messenger/service/images/", DataSize.ofBytes(1));
        service = new ImageStorageService(config);
    }

    @Test
    @DisplayName("Файлы сохраняются успешно")
    public void store_WhenCorrectFile_ThenSaveFile() throws InterruptedException, IOException {
        String filename = "ex1.png";
        MockMultipartFile file = new MockMultipartFile("ex1", filename, "image/png", "test image content".getBytes());
        Path pathToFile = Path.of(config.location() + filename);

        service.store(file, filename);

        assertTrue(Files.exists(pathToFile));

        Files.delete(pathToFile);
    }

    @Test
    @DisplayName("Файлы удаляются успешно")
    public void remove_WhenCorrectFilename_ThenRemoveFile() throws IOException {
        String filename = "ex2.png";
        Path pathToFile = Path.of(config.location() + filename);
        Files.createFile(pathToFile);

        service.remove(filename);

        assertFalse(Files.exists(pathToFile));
    }

    @Test
    @DisplayName("Валидация: пустой файл")
    public void validateImageFile_WhenEmptyFile_ThenThrowException() {
        MockMultipartFile file = new MockMultipartFile("ex1", "ex1.png", "image/png", new byte[] {});

        assertThrows(StorageException.class, () -> service.validateImageFile(file));
    }

    @Test
    @DisplayName("Валидация: слишком большой файл")
    public void validateImageFile_WhenFileTooLarge_ThenThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "ex1",
                "ex1.png",
                "image/png",
                new byte[(int) config.maxFileSize().toBytes() + 1]);

        assertThrows(
                StorageException.class,
                () -> service.validateImageFile(file),
                "Размер файла не должен превышать " + config.maxFileSize() + " Байт");
    }

    @Test
    @DisplayName("Валидация: некорректный формат")
    public void validateImageFile_WhenInvalidFileFormat_ThenThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "ex1",
                "ex1.txt",
                "plain/txt",
                new byte[(int) config.maxFileSize().toBytes()]);

        assertThrows(StorageException.class, () -> service.validateImageFile(file));
    }

    @Test
    @DisplayName("Валидация: файл корректный")
    public void validateImageFile_WhenCorrectFile_ThenDoNotThrowAnyException() {
        MockMultipartFile file = new MockMultipartFile(
                "ex1",
                "ex1.png",
                "image/png",
                new byte[(int) config.maxFileSize().toBytes()]);

        assertDoesNotThrow(() -> service.validateImageFile(file));
    }

    @Test
    @DisplayName("Чтение файла: файла не существует")
    public void loadAsResource_WhenFileDoesNotExist_ThenReturnNull() {
        String filename = "wrongName";

        Resource resource = service.loadAsResource(filename);

        assertNull(resource);
    }

    @Test
    @DisplayName("Чтение файла: файл успешно прочитан")
    public void loadAsResource_WhenFileExists_ThenReturnResource() throws IOException {
        String filename = "ex1.png";
        MockMultipartFile file = new MockMultipartFile("ex1", filename, "image/png", "test image content".getBytes());
        Path pathToFile = Path.of(config.location() + filename);
        Files.createFile(pathToFile);
        file.transferTo(pathToFile);

        Resource resource = service.loadAsResource(filename);

        assertEquals(filename, resource.getFilename());
        assertEquals(
                new String(file.getBytes(), StandardCharsets.UTF_8),
                resource.getContentAsString(StandardCharsets.UTF_8));

        Files.delete(pathToFile);
    }
}

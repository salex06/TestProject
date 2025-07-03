package salex.messenger.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.exception.StorageException;
import salex.messenger.service.ImageStorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("Файл не найден, вернуть 404")
    public void getImage_WhenFileNotFound_ThenReturn404() throws Exception {
        String filename = "image";
        when(imageStorageService.loadAsResource(filename)).thenReturn(null);

        mockMvc.perform(get("/api/images/" + filename).content(filename.getBytes()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Файл не удается прочитать, вернуть 500")
    public void getImage_WhenInvalidFile_ThenReturn500() throws Exception {
        String filename = "image";
        when(imageStorageService.loadAsResource(filename)).thenAnswer(ans -> {
            throw new StorageException("Не удалось прочитать файл " + filename);
        });
        String expectedResponse = new ObjectMapper()
                .writeValueAsString(new ApiErrorResponse(
                        "Не удалось прочитать файл " + filename,
                        "500",
                        "StorageException",
                        "Не удалось прочитать файл " + filename));

        mockMvc.perform(get("/api/images/" + filename))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Файл .png найден успешно, возврат 200")
    public void getImage_WhenFileWasFound_ThenReturnFileAnd200() throws Exception {
        String filename = "test.png";
        String filepath = "./src/test/java/salex/messenger/api/test.png";
        Path path = Path.of(filepath);
        Resource resource = new PathResource(path);
        when(imageStorageService.loadAsResource(filename)).thenReturn(resource);

        mockMvc.perform(get("/api/images/" + filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
}

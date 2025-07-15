package salex.messenger.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;
import salex.messenger.dto.contact.SaveContactRequest;
import salex.messenger.dto.error.ApiErrorResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// Common api exceptions handling test
class ControllerExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Недопустимый тип запроса (код 405)")
    @WithMockUser
    public void whenUnsupportedMethodType_ThenReturn405Response() throws Exception {
        String expectedResponse = new ObjectMapper()
                .writeValueAsString(new ApiErrorResponse(
                        "Некорректный тип запроса",
                        "405",
                        "HttpRequestMethodNotSupportedException",
                        "Request method 'PUT' is not supported"));
        mockMvc.perform(put("/api/account"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Ресурс не найден (запрос к API, код 404)")
    public void whenNoResourceFoundExceptionAndRequestToApi_ThenReturn404ApiErrorResponse() throws Exception {
        String expectedResponse = new ObjectMapper()
                .writeValueAsString(new ApiErrorResponse(
                        "Страница не найдена", "404", "NoResourceFoundException", "No static resource api/example."));
        mockMvc.perform(get("/api/example/"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @DisplayName("Ресурс не найден (веб-страница, код 404)")
    public void whenNoResourceFoundExceptionAndRequestToWebPage_ThenReturn404Page() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/error/404.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        mockMvc.perform(get("/example/"))
                .andExpect(status().isOk())
                .andExpect(content().string(new String(bytes, StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Переданы некорректные параметры в запросе (не прошли валидацию)")
    public void whenMethodArgumentNotValidException_ThenReturnApiErrorResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SaveContactRequest invalidRequest = new SaveContactRequest("", "");

        mockMvc.perform(post("/api/contacts")
                        .content(mapper.writeValueAsString(invalidRequest))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}

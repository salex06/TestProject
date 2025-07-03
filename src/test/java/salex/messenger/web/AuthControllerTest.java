package salex.messenger.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Получение страницы входа (/signin)")
    public void signInReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/signin.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/signin"))
                .andExpect(status().isOk())
                .andExpect(content().string(page));
    }

    @Test
    @DisplayName("Получение страницы входа (/signup)")
    public void signUpReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/signup.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(page));
    }
}

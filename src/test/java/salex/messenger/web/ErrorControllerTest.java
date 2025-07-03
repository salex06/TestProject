package salex.messenger.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Получение страницы 404 ('Нет страницы', /404)")
    public void signInReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/error/404.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/404"))
                .andExpect(status().isOk())
                .andExpect(content().string(page));
    }

    @Test
    @DisplayName("Получение страницы 'Доступ ограничен' (/403)")
    @WithMockUser
    public void signUpReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/error/403.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(content().string(page));
    }
}

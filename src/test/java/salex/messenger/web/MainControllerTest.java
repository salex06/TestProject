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
class MainControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Получение главной страницы ('/')")
    public void indexReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/index.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(page));
    }

    @Test
    @DisplayName("Получение главной страницы, редирект в лк для авторизованного пользователя")
    @WithMockUser
    public void indexRedirectToAccountPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/index.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/")).andExpect(status().is(302)).andExpect(redirectedUrl("/account"));
    }

    @Test
    @DisplayName("Получение страницы профиля ('/account')")
    @WithMockUser
    public void accountReturnsWebPage() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:/templates/account.html");
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
        String page = new String(bytes, StandardCharsets.UTF_8);

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(content().string(page));
    }
}

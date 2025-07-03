package salex.messenger.web;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String index(HttpServletResponse response, Principal principal) throws IOException {
        if (principal != null) {
            response.sendRedirect("/account");
        }
        return "index";
    }

    @GetMapping("/account")
    public String account() {
        return "account";
    }
}

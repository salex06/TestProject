package salex.messenger.web;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/profile/{username}")
    public String profile(@PathVariable String username) {
        return "profile";
    }

    @GetMapping("/chats")
    public String chats(@RequestParam(required = false) String receiverUsername, Model model, Principal principal) {
        if (principal == null || !Objects.equals(principal.getName(), receiverUsername))
            model.addAttribute("receiverUsername", receiverUsername);
        return "chats";
    }
}

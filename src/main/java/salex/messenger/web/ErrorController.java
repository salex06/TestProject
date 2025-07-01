package salex.messenger.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("/404")
    public String handleNotFound() {
        return "error/404";
    }

    @GetMapping("/403")
    public String handleForbidden() {
        return "error/403";
    }
}

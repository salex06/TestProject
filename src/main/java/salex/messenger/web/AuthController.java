package salex.messenger.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/signin")
    public String signinPage() {
        return "signin";
    }
}

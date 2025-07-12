package salex.messenger.dto.signin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @NotBlank
                @Size(min = 3, max = 20, message = "Имя не должно быть меньше 3 символов или больше 20 символов")
                @Pattern(regexp = "[a-zA-Z0-9]+", message = "Имя может состоять только из цифр или лат. символов")
                String username,
        @NotBlank
                @Size(min = 8, message = "Минимум 8 символов для пароля")
                @Pattern(
                        regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                        message = "Минимум 8 символов, 1 заглавная буква и 1 цифра")
                String password) {}

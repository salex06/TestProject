package salex.messenger.dto.error;

import jakarta.validation.constraints.NotBlank;

public record ApiErrorResponse(
        @NotBlank String description, @NotBlank String status, String exceptionName, String exceptionMessage) {}

package salex.messenger.dto.chat.messages;

import jakarta.validation.constraints.AssertTrue;

public record WebSocketMessage(String text, String pathToImage) {
    @AssertTrue(message = "Хотя бы 1 поле не равно null")
    @SuppressWarnings("PMD")
    private boolean atLeastOneFieldIsNotNull() {
        return (text != null) || (pathToImage != null);
    }
}

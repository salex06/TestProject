package salex.messenger.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import salex.messenger.dto.error.ApiErrorResponse;
import salex.messenger.exception.StorageException;
import salex.messenger.exception.UserNotFoundException;
import salex.messenger.exception.UsernameAlreadyExistsException;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameAlreadyExistsException(@NotNull UsernameAlreadyExistsException ex) {
        return handleIncorrectRequest("Такое имя уже занято!", ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(@NotNull UserNotFoundException ex) {
        return handleIncorrectRequest("Пользователь не найден (или удален)!", ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(@NotNull BadCredentialsException ex) {
        return handleIncorrectRequest(ex.getMessage(), ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(@NotNull HttpRequestMethodNotSupportedException ex) {
        return handleIncorrectRequest("Некорректный тип запроса", ex, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(@NotNull NoResourceFoundException ex, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api")) {
            return handleIncorrectRequest("Страница не найдена", ex, HttpStatus.NOT_FOUND);
        }
        return "error/404";
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageException(@NotNull StorageException ex) {
        return handleIncorrectRequest(ex.getMessage(), ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(@NotNull MethodArgumentNotValidException ex) {
        return handleIncorrectRequest("Некорректные параметры", ex, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiErrorResponse> handleIncorrectRequest(
            String errorMessage, Exception ex, HttpStatusCode status) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        errorMessage,
                        String.valueOf(status.value()),
                        ex.getClass().getSimpleName(),
                        ex.getMessage()),
                status);
    }
}

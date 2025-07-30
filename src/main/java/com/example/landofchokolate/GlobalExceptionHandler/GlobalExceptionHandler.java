package com.example.landofchokolate.GlobalExceptionHandler;

import com.example.landofchokolate.exception.SubscriptionAlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionAlreadyExists(SubscriptionAlreadyExistsException e) {
        log.warn("Попытка создать дубликат подписки: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message("Ви вже підписані на нашу розсилку! 😊 Дякуємо за інтерес!")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .error("SUBSCRIPTION_ALREADY_EXISTS")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = errors.containsKey("email") ?
                "Некоректна email адреса. Перевірте правильність введених даних." :
                "Помилка валідації даних";

        ErrorResponse error = ErrorResponse.builder()
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .error("VALIDATION_ERROR")
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message("Некоректні дані в запиті")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .error("INVALID_REQUEST")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // НОВЫЙ ОБРАБОТЧИК для статических ресурсов
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Игнорируем тихо favicon.ico и другие служебные файлы
        if (requestURI.endsWith("/favicon.ico") ||
                requestURI.endsWith("/robots.txt") ||
                requestURI.endsWith("/sitemap.xml") ||
                requestURI.contains("/static/") ||
                requestURI.contains("/assets/")) {
            // Возвращаем 404 без логирования
            return ResponseEntity.notFound().build();
        }

        // Логируем только реальные проблемы с ресурсами
        log.warn("Статический ресурс не найден: {} (URI: {})", ex.getMessage(), requestURI);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Игнорируем ошибки для служебных файлов
        if (requestURI.endsWith("/favicon.ico") ||
                requestURI.endsWith("/robots.txt") ||
                requestURI.contains("/static/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .message("Внутрішня помилка сервера. Спробуйте пізніше або зверніться до підтримки.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .error("INTERNAL_SERVER_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String errorMessage = e.getMessage();

        // Игнорируем ошибки favicon.ico и других служебных файлов
        if (requestURI.endsWith("/favicon.ico") ||
                requestURI.endsWith("/robots.txt") ||
                requestURI.contains("/static/") ||
                (errorMessage != null && errorMessage.contains("favicon.ico"))) {
            // Тихо возвращаем 404 без логирования
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        log.error("Неожиданная ошибка: {} (URI: {})", errorMessage, requestURI, e);

        ErrorResponse error = ErrorResponse.builder()
                .message("Сталася неочікувана помилка. Спробуйте ще раз.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .error("UNEXPECTED_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
        private LocalDateTime timestamp;
        private String error;
        private Map<String, String> details;
    }
}
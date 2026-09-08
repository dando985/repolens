package com.dando.repolens.exception;

import com.dando.repolens.dto.ApiErrorResponse;
import com.dando.repolens.embedding.EmbeddingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRepositoryNotFound(RepositoryNotFoundException exception) {
        ApiErrorResponse response = new ApiErrorResponse("Repository directory was not found", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidSearchRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSearchRequest(InvalidSearchRequestException exception) {
        ApiErrorResponse response = new ApiErrorResponse("Invalid search request", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(IOException exception) {
        String details = exception.getMessage() == null ? "No additional details" : exception.getMessage();

        ApiErrorResponse response = new ApiErrorResponse("Unable to process repository", details);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(EmbeddingException.class)
    public ResponseEntity<ApiErrorResponse> handleEmbeddingException(EmbeddingException exception) {
        ApiErrorResponse response = new ApiErrorResponse("Embedding provider is unavailable", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}
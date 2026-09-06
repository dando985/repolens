package com.dando.repolens.dto;

public record ApiErrorResponse(String error, String details) {

    public ApiErrorResponse(String error) {
        this(error, null);
    }
}
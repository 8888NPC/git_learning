package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.api.error.ApiError;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> successWithoutData() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }
}

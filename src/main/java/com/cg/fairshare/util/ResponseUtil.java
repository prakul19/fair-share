package com.cg.fairshare.util;

import com.cg.fairshare.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    // Success response with data
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String msg) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, msg, data));
    }

    // Success response without data
    public static ResponseEntity<ApiResponse<Void>> ok(String msg) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, msg, null));
    }

    // Error response with a status code
    public static <T> ResponseEntity<ApiResponse<T>> error(int status, String msg) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorValid(int status, String msg,T data) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, msg, data));
    }

}

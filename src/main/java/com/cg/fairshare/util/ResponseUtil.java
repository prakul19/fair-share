package com.cg.fairshare.util;

import com.cg.fairshare.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String msg) {
        return ResponseEntity.ok(new ApiResponse<>(200, msg, data));
    }

    public static ResponseEntity<ApiResponse<Void>> ok(String msg) {
        return ResponseEntity.ok(new ApiResponse<>(200, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(int code, String msg) {
        return ResponseEntity.status(code).body(new ApiResponse<>(code, msg, null));
    }
}

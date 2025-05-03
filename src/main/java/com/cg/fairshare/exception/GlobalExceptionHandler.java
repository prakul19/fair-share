package com.cg.fairshare.exception;

import com.cg.fairshare.response.ApiResponse;
import com.cg.fairshare.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGroupNotFound(GroupNotFoundException ex) {
        return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

}

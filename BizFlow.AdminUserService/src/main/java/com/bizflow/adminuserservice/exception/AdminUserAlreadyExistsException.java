package com.bizflow.adminuserservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AdminUserAlreadyExistsException extends RuntimeException {

    public AdminUserAlreadyExistsException(String message) {
        super(message);
    }

    public AdminUserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

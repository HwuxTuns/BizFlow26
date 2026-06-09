package com.bizflow.adminuserservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AdminUserNotFoundException extends RuntimeException {

    public AdminUserNotFoundException(Long userId) {
        super("Admin user " + userId + " not found");
    }
}

package com.bizflow.adminuserservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BranchInactiveException extends RuntimeException {

    public BranchInactiveException(Long branchId) {
        super("Branch " + branchId + " is not active");
    }
}

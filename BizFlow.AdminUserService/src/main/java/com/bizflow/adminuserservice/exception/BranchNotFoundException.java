package com.bizflow.adminuserservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BranchNotFoundException extends RuntimeException {

    public BranchNotFoundException(Long branchId) {
        super("Branch with id " + branchId + " not found");
    }
}

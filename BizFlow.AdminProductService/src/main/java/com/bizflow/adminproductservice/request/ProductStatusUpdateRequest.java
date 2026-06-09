package com.bizflow.adminproductservice.request;

import jakarta.validation.constraints.NotNull;

public class ProductStatusUpdateRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

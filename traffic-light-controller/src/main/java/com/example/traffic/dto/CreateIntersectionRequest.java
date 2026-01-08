
package com.example.traffic.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateIntersectionRequest {
    @NotBlank
    private String id;

    public String getId() { return id; }
    public void setId(String id)
    {
        this.id = id;
    }
}

package com.cg.fairshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {
    @NotNull
    @Size(min = 4, message = "Name should be 4 characters long")
    private String name;
}


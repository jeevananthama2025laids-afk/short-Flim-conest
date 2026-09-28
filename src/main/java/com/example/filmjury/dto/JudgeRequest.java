package com.example.filmjury.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class JudgeRequest {

    @NotBlank(message = "name cannot be empty")
    private String name;

    @NotBlank(message = "email cannot be empty")
    @Email(message = "email must be valid")
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
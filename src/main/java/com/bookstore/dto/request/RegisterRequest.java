package com.bookstore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 128) String password,
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number") String phone
) {}

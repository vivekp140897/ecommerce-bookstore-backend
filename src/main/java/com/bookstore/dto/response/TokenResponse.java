package com.bookstore.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}

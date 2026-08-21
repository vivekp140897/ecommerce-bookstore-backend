package com.bookstore.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddWishlistItemRequest(@NotNull UUID bookId) {}

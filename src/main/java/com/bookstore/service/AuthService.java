package com.bookstore.service;

import com.bookstore.dto.request.LoginRequest;
import com.bookstore.dto.request.RefreshRequest;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.response.TokenResponse;
import com.bookstore.dto.response.UserResponse;
import com.bookstore.entity.RefreshToken;
import com.bookstore.entity.User;
import com.bookstore.entity.Wishlist;
import com.bookstore.entity.Cart;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.RefreshTokenRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered.");
        }
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .build();
        user = userRepository.save(user);

        // Bootstrap cart and wishlist for the new user
        Cart cart = Cart.builder().user(user).build();
        cartRepository.save(cart);
        Wishlist wishlist = Wishlist.builder().user(user).build();
        wishlistRepository.save(wishlist);

        return toResponse(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials.");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Refresh token expired.");
        }
        refreshTokenRepository.delete(stored);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String rawRefresh = generateSecureToken();
        RefreshToken rt = RefreshToken.builder()
                .token(rawRefresh)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .build();
        refreshTokenRepository.save(rt);
        return new TokenResponse(accessToken, rawRefresh,
                jwtTokenProvider.getAccessTokenExpiryMs() / 1000);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(),
                u.getEmail(), u.getPhone(), u.getRole(), u.getCreatedAt(), u.getUpdatedAt());
    }
}

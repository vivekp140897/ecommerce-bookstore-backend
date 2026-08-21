package com.bookstore.service;

import com.bookstore.dto.request.UserUpdateRequest;
import com.bookstore.dto.response.UserResponse;
import com.bookstore.entity.User;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserResponse getMe() {
        return toResponse(securityUtils.getCurrentUser());
    }

    @Transactional
    public UserResponse updateMe(UserUpdateRequest request) {
        User user = securityUtils.getCurrentUser();
        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use.");
        }
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return toResponse(userRepository.save(user));
    }

    public UserResponse getUserById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(),
                u.getEmail(), u.getPhone(), u.getRole(), u.getCreatedAt(), u.getUpdatedAt());
    }
}

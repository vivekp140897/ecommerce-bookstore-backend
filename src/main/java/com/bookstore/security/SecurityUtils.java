package com.bookstore.security;

import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }
}

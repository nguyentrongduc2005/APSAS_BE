package com.project.apsas.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthUserResolver {

    /**
     * Lấy ID của user hiện tại từ token (JWT)
     */
    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        // Nếu bạn dùng kiểu CustomUserDetails
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid user ID format in token");
            }
        }

        // fallback
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot resolve current user ID");
        }
    }
}

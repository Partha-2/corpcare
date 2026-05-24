package com.corpcare.config;

import com.corpcare.config.JwtAuthFilter.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static JwtUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof JwtUser)) {
            return null;
        }
        return (JwtUser) auth.getPrincipal();
    }

    public static boolean isAdmin() {
        JwtUser user = getCurrentUser();
        return user != null && "ADMIN".equals(user.role());
    }

    public static boolean isClient() {
        JwtUser user = getCurrentUser();
        return user != null && "CLIENT".equals(user.role());
    }

    public static boolean isHospital() {
        JwtUser user = getCurrentUser();
        return user != null && "HOSPITAL".equals(user.role());
    }

    public static boolean isEmployee() {
        JwtUser user = getCurrentUser();
        return user != null && "EMPLOYEE".equals(user.role());
    }

    public static void requireOwnership(Long targetId, String role) {
        JwtUser user = getCurrentUser();
        if (user == null) throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        if ("ADMIN".equals(user.role())) return;
        if (!user.role().equals(role) || !user.userId().equals(targetId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }
}

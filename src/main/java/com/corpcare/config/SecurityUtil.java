package com.corpcare.config;

import com.corpcare.config.JwtAuthFilter.JwtUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_HOSPITAL = "HOSPITAL";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    public static JwtUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof JwtUser)) {
            return null;
        }
        return (JwtUser) auth.getPrincipal();
    }

    public static JwtUser requireAuthenticated() {
        JwtUser user = getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        return user;
    }

    public static boolean isAdmin() {
        JwtUser user = getCurrentUser();
        return user != null && ROLE_ADMIN.equals(user.role());
    }

    public static boolean isClient() {
        JwtUser user = getCurrentUser();
        return user != null && ROLE_CLIENT.equals(user.role());
    }

    public static boolean isHospital() {
        JwtUser user = getCurrentUser();
        return user != null && ROLE_HOSPITAL.equals(user.role());
    }

    public static boolean isEmployee() {
        JwtUser user = getCurrentUser();
        return user != null && ROLE_EMPLOYEE.equals(user.role());
    }

    public static void requireAdmin() {
        JwtUser user = requireAuthenticated();
        if (!ROLE_ADMIN.equals(user.role())) throw new AccessDeniedException("Admin access required");
    }

    public static void requireOwnership(Long targetId, String role) {
        JwtUser user = requireAuthenticated();
        if (ROLE_ADMIN.equals(user.role())) return;
        if (!user.role().equals(role) || !user.userId().equals(targetId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}

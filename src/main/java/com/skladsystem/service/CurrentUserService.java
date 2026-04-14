package com.skladsystem.service;

import com.skladsystem.model.AppUser;
import com.skladsystem.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private static final String SESSION_USER_ID = "activeUserId";

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser(HttpSession session) {
        Object rawId = session.getAttribute(SESSION_USER_ID);

        if (rawId instanceof Long userId) {
            AppUser user = appUserRepository.findById(userId);
            if (user != null && Boolean.TRUE.equals(user.getActive())) {
                return user;
            }
        }

        if (rawId instanceof Integer userId) {
            AppUser user = appUserRepository.findById(userId.longValue());
            if (user != null && Boolean.TRUE.equals(user.getActive())) {
                session.setAttribute(SESSION_USER_ID, user.getId());
                return user;
            }
        }

        AppUser fallback = appUserRepository.findFirstActive();
        if (fallback != null) {
            session.setAttribute(SESSION_USER_ID, fallback.getId());
        }
        return fallback;
    }

    public Long getCurrentUserId(HttpSession session) {
        AppUser user = getCurrentUser(session);
        return user != null ? user.getId() : null;
    }

    public void switchUser(HttpSession session, Long userId) {
        AppUser user = appUserRepository.findById(userId);
        if (user != null && Boolean.TRUE.equals(user.getActive())) {
            session.setAttribute(SESSION_USER_ID, user.getId());
        }
    }

    public boolean isAdmin(HttpSession session) {
        return hasRole(session, "ADMIN");
    }

    public boolean canViewProducts(HttpSession session) {
        return hasAnyRole(session,
                "ADMIN",
                "RECEIPT_OPERATOR",
                "SHIPMENT_OPERATOR",
                "LOADER"
        );
    }

    public boolean canManageProducts(HttpSession session) {
        return hasRole(session, "ADMIN");
    }

    public boolean canAccessReceipts(HttpSession session) {
        return hasAnyRole(session, "ADMIN", "RECEIPT_OPERATOR");
    }

    public boolean canAccessShipments(HttpSession session) {
        return hasAnyRole(session, "ADMIN", "SHIPMENT_OPERATOR");
    }

    public boolean canAccessInventory(HttpSession session) {
        return hasRole(session, "ADMIN");
    }

    private boolean hasRole(HttpSession session, String roleName) {
        AppUser user = getCurrentUser(session);
        return user != null
                && user.getRoleName() != null
                && roleName.equalsIgnoreCase(user.getRoleName());
    }

    private boolean hasAnyRole(HttpSession session, String... roleNames) {
        AppUser user = getCurrentUser(session);
        if (user == null || user.getRoleName() == null) {
            return false;
        }

        for (String roleName : roleNames) {
            if (roleName.equalsIgnoreCase(user.getRoleName())) {
                return true;
            }
        }

        return false;
    }
}
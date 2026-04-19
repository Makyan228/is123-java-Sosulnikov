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

        return null;
    }

    public boolean isAuthenticated(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    public void setCurrentUser(HttpSession session, Long userId) {
        session.setAttribute(SESSION_USER_ID, userId);
    }

    public void clearCurrentUser(HttpSession session) {
        session.removeAttribute(SESSION_USER_ID);
    }

    public Long getCurrentUserId(HttpSession session) {
        AppUser user = getCurrentUser(session);
        return user != null ? user.getId() : null;
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
        return isAdmin(session);
    }

    public boolean canAccessReceipts(HttpSession session) {
        return hasAnyRole(session, "ADMIN", "RECEIPT_OPERATOR");
    }

    public boolean canAccessShipments(HttpSession session) {
        return hasAnyRole(session, "ADMIN", "SHIPMENT_OPERATOR");
    }

    public boolean canAccessInventory(HttpSession session) {
        return isAdmin(session);
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
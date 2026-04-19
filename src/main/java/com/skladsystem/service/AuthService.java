package com.skladsystem.service;

import com.skladsystem.model.AppUser;
import com.skladsystem.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    public AuthService(AppUserRepository appUserRepository,
                       CurrentUserService currentUserService) {
        this.appUserRepository = appUserRepository;
        this.currentUserService = currentUserService;
    }

    public boolean login(HttpSession session, Long userId, String password) {
        if (userId == null || password == null || password.isBlank()) {
            return false;
        }

        AppUser user = appUserRepository.findByIdAndPassword(userId, password.trim());
        if (user == null) {
            return false;
        }

        currentUserService.setCurrentUser(session, user.getId());
        return true;
    }

    public void logout(HttpSession session) {
        currentUserService.clearCurrentUser(session);
    }
}
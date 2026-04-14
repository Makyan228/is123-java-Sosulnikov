package com.skladsystem.controller;

import com.skladsystem.repository.AppUserRepository;
import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;

    public GlobalModelAdvice(CurrentUserService currentUserService,
                             AppUserRepository appUserRepository) {
        this.currentUserService = currentUserService;
        this.appUserRepository = appUserRepository;
    }

    @ModelAttribute
    public void addCommonData(Model model, HttpSession session) {
        model.addAttribute("currentUser", currentUserService.getCurrentUser(session));
        model.addAttribute("switchableUsers", appUserRepository.findAllActive());

        model.addAttribute("canViewProducts", currentUserService.canViewProducts(session));
        model.addAttribute("canManageProducts", currentUserService.canManageProducts(session));
        model.addAttribute("canAccessReceipts", currentUserService.canAccessReceipts(session));
        model.addAttribute("canAccessShipments", currentUserService.canAccessShipments(session));
        model.addAttribute("canAccessInventory", currentUserService.canAccessInventory(session));
    }
}
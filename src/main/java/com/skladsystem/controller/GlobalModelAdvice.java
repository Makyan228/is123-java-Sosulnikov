package com.skladsystem.controller;

import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CurrentUserService currentUserService;

    public GlobalModelAdvice(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute
    public void addCommonData(Model model, HttpSession session) {
        model.addAttribute("currentUser", currentUserService.getCurrentUser(session));

        model.addAttribute("canViewProducts", currentUserService.canViewProducts(session));
        model.addAttribute("canManageProducts", currentUserService.canManageProducts(session));
        model.addAttribute("canAccessReceipts", currentUserService.canAccessReceipts(session));
        model.addAttribute("canAccessShipments", currentUserService.canAccessShipments(session));
        model.addAttribute("canAccessInventory", currentUserService.canAccessInventory(session));
    }
}
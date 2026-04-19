package com.skladsystem.controller;

import com.skladsystem.repository.AppUserRepository;
import com.skladsystem.service.AuthService;
import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public LoginController(AppUserRepository appUserRepository,
                           AuthService authService,
                           CurrentUserService currentUserService) {
        this.appUserRepository = appUserRepository;
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (currentUserService.isAuthenticated(session)) {
            return "redirect:/";
        }

        model.addAttribute("users", appUserRepository.findAllActive());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam Long userId,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        boolean success = authService.login(session, userId, password);

        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверный пароль.");
            redirectAttributes.addFlashAttribute("selectedUserId", userId);
            return "redirect:/login";
        }

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logout(session);
        redirectAttributes.addFlashAttribute("successMessage", "Вы вышли из системы.");
        return "redirect:/login";
    }
}
package com.skladsystem.controller;

import com.skladsystem.service.AuthService;
import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public LoginController(AuthService authService,
                           CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (currentUserService.isAuthenticated(session)) {
            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String roleName,
                        @RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        boolean success = authService.login(session, roleName, username, password);

        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверный тип пользователя, логин или пароль.");
            redirectAttributes.addFlashAttribute("roleName", roleName);
            redirectAttributes.addFlashAttribute("username", username);
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
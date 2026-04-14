package com.skladsystem.controller;

import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserSwitchController {

    private final CurrentUserService currentUserService;

    public UserSwitchController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @PostMapping("/switch-user")
    public String switchUser(@RequestParam Long userId,
                             @RequestHeader(value = "Referer", required = false) String referer,
                             HttpSession session) {
        currentUserService.switchUser(session, userId);
        return "redirect:" + (referer != null ? referer : "/");
    }
}
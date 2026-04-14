package com.skladsystem.controller;

import com.skladsystem.repository.ProductRepository;
import com.skladsystem.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public InventoryController(ProductRepository productRepository,
                               CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/inventory")
    public String inventory(Model model, HttpSession session) {
        if (!currentUserService.canAccessInventory(session)) {
            return "redirect:/";
        }

        model.addAttribute("products", productRepository.findAll());
        return "inventory";
    }
}
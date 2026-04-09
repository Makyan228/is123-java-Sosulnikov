package com.skladsystem.controller;

import com.skladsystem.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductService productService;

    public DashboardController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("productsCount", productService.countProducts());
        model.addAttribute("totalQuantity", productService.sumQuantity());
        model.addAttribute("lowStockCount", productService.countLowStock(0));
        model.addAttribute("totalInventoryValue", productService.totalInventoryValue());
        model.addAttribute("latestProducts", productService.findLatestFive());

        return "index";
    }
}
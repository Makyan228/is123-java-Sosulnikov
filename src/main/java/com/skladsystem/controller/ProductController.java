package com.skladsystem.controller;

import com.skladsystem.model.Product;
import com.skladsystem.repository.MeasureUnitRepository;
import com.skladsystem.repository.ProductCategoryRepository;
import com.skladsystem.service.CurrentUserService;
import com.skladsystem.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;
    private final MeasureUnitRepository measureUnitRepository;
    private final CurrentUserService currentUserService;

    public ProductController(ProductService productService,
                             ProductCategoryRepository productCategoryRepository,
                             MeasureUnitRepository measureUnitRepository,
                             CurrentUserService currentUserService) {
        this.productService = productService;
        this.productCategoryRepository = productCategoryRepository;
        this.measureUnitRepository = measureUnitRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/products")
    public String products(@RequestParam(value = "search", required = false) String search,
                           Model model,
                           HttpSession session) {
        if (!currentUserService.canViewProducts(session)) {
            return "redirect:/";
        }

        model.addAttribute("products", productService.search(search));
        model.addAttribute("search", search);
        return "products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model, HttpSession session) {
        if (!currentUserService.canManageProducts(session)) {
            return "redirect:/products";
        }

        Product product = new Product();
        product.setActive(true);

        model.addAttribute("product", product);
        model.addAttribute("categories", productCategoryRepository.findAll());
        model.addAttribute("units", measureUnitRepository.findAll());
        model.addAttribute("pageTitle", "Новый товар");
        model.addAttribute("pageSubtitle", "Добавление записи в таблицу PRODUCT");
        model.addAttribute("submitButtonText", "Сохранить товар");

        return "product-form";
    }

    @PostMapping("/products")
    public String saveProduct(Product product, HttpSession session) {
        if (!currentUserService.canManageProducts(session)) {
            return "redirect:/products";
        }

        productService.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model, HttpSession session) {
        if (!currentUserService.canManageProducts(session)) {
            return "redirect:/products";
        }

        Product product = productService.findById(id);

        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", productCategoryRepository.findAll());
        model.addAttribute("units", measureUnitRepository.findAll());
        model.addAttribute("pageTitle", "Редактирование товара");
        model.addAttribute("pageSubtitle", "Изменение данных товара");
        model.addAttribute("submitButtonText", "Сохранить изменения");

        return "product-form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id, Product product, HttpSession session) {
        if (!currentUserService.canManageProducts(session)) {
            return "redirect:/products";
        }

        product.setId(id);
        productService.update(product);
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!currentUserService.canManageProducts(session)) {
            return "redirect:/products";
        }

        productService.softDelete(id);
        return "redirect:/products";
    }
}
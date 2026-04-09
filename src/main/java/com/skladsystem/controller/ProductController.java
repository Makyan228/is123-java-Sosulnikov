package com.skladsystem.controller;

import com.skladsystem.model.Product;
import com.skladsystem.repository.MeasureUnitRepository;
import com.skladsystem.repository.ProductCategoryRepository;
import com.skladsystem.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryRepository productCategoryRepository;
    private final MeasureUnitRepository measureUnitRepository;

    public ProductController(ProductService productService,
                             ProductCategoryRepository productCategoryRepository,
                             MeasureUnitRepository measureUnitRepository) {
        this.productService = productService;
        this.productCategoryRepository = productCategoryRepository;
        this.measureUnitRepository = measureUnitRepository;
    }

    @GetMapping("/products")
    public String products(@RequestParam(value = "search", required = false) String search,
                           Model model) {
        model.addAttribute("products", productService.search(search));
        model.addAttribute("search", search);
        return "products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        Product product = new Product();
        product.setActive(true);

        model.addAttribute("product", product);
        model.addAttribute("categories", productCategoryRepository.findAll());
        model.addAttribute("units", measureUnitRepository.findAll());

        return "product-form";
    }

    @PostMapping("/products")
    public String saveProduct(Product product) {
        productService.save(product);
        return "redirect:/products";
    }
}
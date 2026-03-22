package com.skladsystem.service;

import com.skladsystem.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    public List<Product> getAllProducts() {
        return List.of(
                new Product(1L, "Ноутбук Lenovo", 12, "Стеллаж A-1"),
                new Product(2L, "Мышь Logitech", 35, "Стеллаж B-2"),
                new Product(3L, "Клавиатура Redragon", 20, "Стеллаж C-3")
        );
    }
}
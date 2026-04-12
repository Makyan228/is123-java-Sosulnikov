package com.skladsystem.service;

import com.skladsystem.model.Product;
import com.skladsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> search(String search) {
        return productRepository.search(search);
    }

    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void update(Product product) {
        productRepository.update(product);
    }

    public void softDelete(Long id) {
        productRepository.softDelete(id);
    }

    public long countProducts() {
        return productRepository.countProducts();
    }

    public BigDecimal sumQuantity() {
        return productRepository.sumQuantity();
    }

    public long countLowStock(int threshold) {
        return productRepository.countLowStock(threshold);
    }

    public BigDecimal totalInventoryValue() {
        return productRepository.totalInventoryValue();
    }

    public List<Product> findLatestFive() {
        return productRepository.findLatestFive();
    }
}
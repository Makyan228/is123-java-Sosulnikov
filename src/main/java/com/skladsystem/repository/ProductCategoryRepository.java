package com.skladsystem.repository;

import com.skladsystem.model.ProductCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductCategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductCategory> findAll() {
        String sql = """
                select id, name
                from product_category
                order by name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductCategory category = new ProductCategory();
            category.setId(rs.getLong("id"));
            category.setName(rs.getString("name"));
            return category;
        });
    }
}
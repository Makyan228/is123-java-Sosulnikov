package com.skladsystem.repository;

import com.skladsystem.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product();

        product.setId(rs.getLong("id"));
        product.setArticle(rs.getString("article"));
        product.setBarcode(rs.getString("barcode"));
        product.setName(rs.getString("name"));

        product.setCategoryId(rs.getLong("category_id"));
        product.setCategoryName(rs.getString("category_name"));

        product.setUnitId(rs.getLong("unit_id"));
        product.setUnitName(rs.getString("unit_name"));

        product.setPrice(rs.getBigDecimal("price"));
        product.setMinStock(rs.getBigDecimal("min_stock"));
        product.setTotalQuantity(rs.getBigDecimal("total_quantity"));

        int activeValue = rs.getInt("is_active");
        product.setActive(!rs.wasNull() && activeValue == 1);

        product.setNotes(rs.getString("notes"));

        return product;
    };

    private String baseSelect() {
        return """
                select
                    p.id,
                    p.article,
                    p.barcode,
                    p.name,
                    p.category_id,
                    pc.name as category_name,
                    p.unit_id,
                    coalesce(mu.short_name, mu.name) as unit_name,
                    p.price,
                    p.min_stock,
                    coalesce(sum(sb.quantity), 0) as total_quantity,
                    p.is_active,
                    p.notes
                from product p
                left join product_category pc on pc.id = p.category_id
                left join measure_unit mu on mu.id = p.unit_id
                left join stock_balance sb on sb.product_id = p.id
                """;
    }

    private String groupByPart() {
        return """
                group by
                    p.id,
                    p.article,
                    p.barcode,
                    p.name,
                    p.category_id,
                    pc.name,
                    p.unit_id,
                    mu.short_name,
                    mu.name,
                    p.price,
                    p.min_stock,
                    p.is_active,
                    p.notes
                """;
    }

    public List<Product> findAll() {
        String sql = baseSelect() + """
                
                """ + groupByPart() + """
                
                order by p.name
                """;

        return jdbcTemplate.query(sql, productRowMapper);
    }

    public List<Product> search(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }

        String sql = baseSelect() + """
                where
                    p.name containing ?
                    or coalesce(p.article, '') containing ?
                    or coalesce(p.barcode, '') containing ?
                """ + groupByPart() + """
                
                order by p.name
                """;

        String term = search.trim();
        return jdbcTemplate.query(sql, productRowMapper, term, term, term);
    }

    public void save(Product product) {
        String sql = """
                insert into product (
                    article,
                    barcode,
                    name,
                    category_id,
                    unit_id,
                    price,
                    min_stock,
                    is_active,
                    notes,
                    created_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """;

        boolean active = product.getActive() == null || product.getActive();

        jdbcTemplate.update(
                sql,
                nullIfBlank(product.getArticle()),
                nullIfBlank(product.getBarcode()),
                nullIfBlank(product.getName()),
                product.getCategoryId(),
                product.getUnitId(),
                product.getPrice(),
                product.getMinStock(),
                active ? 1 : 0,
                nullIfBlank(product.getNotes())
        );
    }

    public long countProducts() {
        Long result = jdbcTemplate.queryForObject("""
                select count(*)
                from product
                where coalesce(is_active, 0) = 1
                """, Long.class);

        return result != null ? result : 0L;
    }

    public BigDecimal sumQuantity() {
        BigDecimal result = jdbcTemplate.queryForObject("""
                select coalesce(sum(quantity), 0)
                from stock_balance
                """, BigDecimal.class);

        return result != null ? result : BigDecimal.ZERO;
    }

    public long countLowStock(int ignoredThreshold) {
        Long result = jdbcTemplate.queryForObject("""
                select count(*)
                from (
                    select
                        p.id,
                        coalesce(sum(sb.quantity), 0) as total_quantity,
                        coalesce(p.min_stock, 0) as min_stock
                    from product p
                    left join stock_balance sb on sb.product_id = p.id
                    where coalesce(p.is_active, 0) = 1
                    group by p.id, p.min_stock
                ) t
                where t.total_quantity <= t.min_stock
                """, Long.class);

        return result != null ? result : 0L;
    }

    public BigDecimal totalInventoryValue() {
        BigDecimal result = jdbcTemplate.queryForObject("""
                select coalesce(sum(sb.quantity * p.price), 0)
                from stock_balance sb
                join product p on p.id = sb.product_id
                where coalesce(p.is_active, 0) = 1
                """, BigDecimal.class);

        return result != null ? result : BigDecimal.ZERO;
    }

    public List<Product> findLatestFive() {
        String sql = """
            select
                p.id,
                p.article,
                p.barcode,
                p.name,
                p.category_id,
                pc.name as category_name,
                p.unit_id,
                coalesce(mu.short_name, mu.name) as unit_name,
                p.price,
                p.min_stock,
                coalesce(sum(sb.quantity), 0) as total_quantity,
                p.is_active,
                p.notes,
                p.created_at as created_at
            from product p
            left join product_category pc on pc.id = p.category_id
            left join measure_unit mu on mu.id = p.unit_id
            left join stock_balance sb on sb.product_id = p.id
            group by
                p.id,
                p.article,
                p.barcode,
                p.name,
                p.category_id,
                pc.name,
                p.unit_id,
                mu.short_name,
                mu.name,
                p.price,
                p.min_stock,
                p.is_active,
                p.notes,
                p.created_at
            order by p.created_at desc, p.id desc
            rows 5
            """;

        return jdbcTemplate.query(sql, productRowMapper);
    }

    private String nullIfBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
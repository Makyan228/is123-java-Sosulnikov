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

        long categoryId = rs.getLong("category_id");
        product.setCategoryId(rs.wasNull() ? null : categoryId);
        product.setCategoryName(rs.getString("category_name"));

        long unitId = rs.getLong("unit_id");
        product.setUnitId(rs.wasNull() ? null : unitId);
        product.setUnitName(rs.getString("unit_name"));

        product.setPrice(rs.getBigDecimal("price"));
        product.setMinStock(rs.getBigDecimal("min_stock"));
        product.setTotalQuantity(rs.getBigDecimal("total_quantity"));

        int activeValue = rs.getInt("is_active");
        product.setActive(!rs.wasNull() && activeValue == 1);

        product.setNotes(rs.getString("notes"));

        return product;
    };

    private String quantitySubquery() {
        return """
                left join (
                    select
                        sdi.product_id,
                        coalesce(sum(
                            case
                                when sd.doc_type = 'IN' and coalesce(sd.doc_status, '') <> 'CANCELLED' then sdi.quantity
                                when sd.doc_type = 'OUT' and coalesce(sd.doc_status, '') <> 'CANCELLED' then -sdi.quantity
                                else 0
                            end
                        ), 0) as total_quantity
                    from stock_document_item sdi
                    join stock_document sd on sd.id = sdi.document_id
                    group by sdi.product_id
                ) q on q.product_id = p.id
                """;
    }

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
                    coalesce(q.total_quantity, 0) as total_quantity,
                    p.is_active,
                    p.notes
                from product p
                left join product_category pc on pc.id = p.category_id
                left join measure_unit mu on mu.id = p.unit_id
                """ + quantitySubquery();
    }

    public List<Product> findAll() {
        String sql = baseSelect() + """
                where coalesce(p.is_active, 0) = 1
                order by p.name
                """;

        return jdbcTemplate.query(sql, productRowMapper);
    }

    public List<Product> search(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }

        String sql = baseSelect() + """
                where coalesce(p.is_active, 0) = 1
                  and (
                    p.name containing ?
                    or coalesce(p.article, '') containing ?
                    or coalesce(p.barcode, '') containing ?
                  )
                order by p.name
                """;

        String term = search.trim();
        return jdbcTemplate.query(sql, productRowMapper, term, term, term);
    }

    public Product findById(Long id) {
        String sql = baseSelect() + """
                where coalesce(p.is_active, 0) = 1
                  and p.id = ?
                """;

        List<Product> products = jdbcTemplate.query(sql, productRowMapper, id);
        return products.isEmpty() ? null : products.get(0);
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

    public void update(Product product) {
        String sql = """
                update product
                set
                    article = ?,
                    barcode = ?,
                    name = ?,
                    category_id = ?,
                    unit_id = ?,
                    price = ?,
                    min_stock = ?,
                    is_active = ?,
                    notes = ?
                where id = ?
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
                nullIfBlank(product.getNotes()),
                product.getId()
        );
    }

    public void softDelete(Long id) {
        jdbcTemplate.update("""
                update product
                set is_active = 0
                where id = ?
                """, id);
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
                select coalesce(sum(
                    case
                        when sd.doc_type = 'IN' and coalesce(sd.doc_status, '') <> 'CANCELLED' then sdi.quantity
                        when sd.doc_type = 'OUT' and coalesce(sd.doc_status, '') <> 'CANCELLED' then -sdi.quantity
                        else 0
                    end
                ), 0)
                from stock_document_item sdi
                join stock_document sd on sd.id = sdi.document_id
                join product p on p.id = sdi.product_id
                where coalesce(p.is_active, 0) = 1
                """, BigDecimal.class);

        return result != null ? result : BigDecimal.ZERO;
    }

    public long countLowStock(int ignoredThreshold) {
        Long result = jdbcTemplate.queryForObject("""
                select count(*)
                from product p
                left join (
                    select
                        sdi.product_id,
                        coalesce(sum(
                            case
                                when sd.doc_type = 'IN' and coalesce(sd.doc_status, '') <> 'CANCELLED' then sdi.quantity
                                when sd.doc_type = 'OUT' and coalesce(sd.doc_status, '') <> 'CANCELLED' then -sdi.quantity
                                else 0
                            end
                        ), 0) as total_quantity
                    from stock_document_item sdi
                    join stock_document sd on sd.id = sdi.document_id
                    group by sdi.product_id
                ) q on q.product_id = p.id
                where coalesce(p.is_active, 0) = 1
                  and coalesce(q.total_quantity, 0) <= coalesce(p.min_stock, 0)
                """, Long.class);

        return result != null ? result : 0L;
    }

    public BigDecimal totalInventoryValue() {
        BigDecimal result = jdbcTemplate.queryForObject("""
                select coalesce(sum(coalesce(q.total_quantity, 0) * p.price), 0)
                from product p
                left join (
                    select
                        sdi.product_id,
                        coalesce(sum(
                            case
                                when sd.doc_type = 'IN' and coalesce(sd.doc_status, '') <> 'CANCELLED' then sdi.quantity
                                when sd.doc_type = 'OUT' and coalesce(sd.doc_status, '') <> 'CANCELLED' then -sdi.quantity
                                else 0
                            end
                        ), 0) as total_quantity
                    from stock_document_item sdi
                    join stock_document sd on sd.id = sdi.document_id
                    group by sdi.product_id
                ) q on q.product_id = p.id
                where coalesce(p.is_active, 0) = 1
                """, BigDecimal.class);

        return result != null ? result : BigDecimal.ZERO;
    }

    public List<Product> findLatestFive() {
        String sql = baseSelect() + """
                where coalesce(p.is_active, 0) = 1
                order by p.id desc
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
package com.skladsystem.repository;

import com.skladsystem.model.StockDocumentItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockDocumentItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public StockDocumentItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StockDocumentItem> stockDocumentItemRowMapper = (rs, rowNum) -> {
        StockDocumentItem item = new StockDocumentItem();

        item.setId(rs.getLong("id"));
        item.setDocumentId(rs.getLong("document_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductArticle(rs.getString("product_article"));
        item.setProductName(rs.getString("product_name"));

        long sourceLocationId = rs.getLong("source_location_id");
        item.setSourceLocationId(rs.wasNull() ? null : sourceLocationId);
        item.setSourceLocationName(rs.getString("source_location_name"));

        long targetLocationId = rs.getLong("target_location_id");
        item.setTargetLocationId(rs.wasNull() ? null : targetLocationId);
        item.setTargetLocationName(rs.getString("target_location_name"));

        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setNotes(rs.getString("notes"));

        return item;
    };

    public List<StockDocumentItem> findByDocumentId(Long documentId) {
        String sql = """
                select
                    sdi.id,
                    sdi.document_id,
                    sdi.product_id,
                    p.article as product_article,
                    p.name as product_name,
                    sdi.source_location_id,
                    coalesce(sls.code, '') || ' — ' || coalesce(sls.name, '') as source_location_name,
                    sdi.target_location_id,
                    coalesce(slt.code, '') || ' — ' || coalesce(slt.name, '') as target_location_name,
                    sdi.quantity,
                    sdi.price,
                    sdi.notes
                from stock_document_item sdi
                join product p on p.id = sdi.product_id
                left join storage_location sls on sls.id = sdi.source_location_id
                left join storage_location slt on slt.id = sdi.target_location_id
                where sdi.document_id = ?
                order by sdi.id
                """;

        return jdbcTemplate.query(sql, stockDocumentItemRowMapper, documentId);
    }

    public void saveIncoming(StockDocumentItem item) {
        String sql = """
                insert into stock_document_item (
                    document_id,
                    product_id,
                    source_location_id,
                    target_location_id,
                    quantity,
                    price,
                    notes
                )
                values (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getDocumentId(),
                item.getProductId(),
                null,
                item.getTargetLocationId(),
                item.getQuantity(),
                item.getPrice(),
                nullIfBlank(item.getNotes())
        );
    }

    public void saveOutgoing(StockDocumentItem item) {
        String sql = """
                insert into stock_document_item (
                    document_id,
                    product_id,
                    source_location_id,
                    target_location_id,
                    quantity,
                    price,
                    notes
                )
                values (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getDocumentId(),
                item.getProductId(),
                item.getSourceLocationId(),
                null,
                item.getQuantity(),
                item.getPrice(),
                nullIfBlank(item.getNotes())
        );
    }

    public void deleteByIdAndDocumentId(Long itemId, Long documentId) {
        String sql = """
                delete from stock_document_item
                where id = ?
                  and document_id = ?
                """;

        jdbcTemplate.update(sql, itemId, documentId);
    }

    private String nullIfBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
    public void deleteByDocumentId(Long documentId) {
        String sql = """
            delete from stock_document_item
            where document_id = ?
            """;

        jdbcTemplate.update(sql, documentId);
    }
}
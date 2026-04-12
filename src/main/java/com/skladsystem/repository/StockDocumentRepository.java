package com.skladsystem.repository;

import com.skladsystem.model.StockDocument;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class StockDocumentRepository {

    private static final String RECEIPT_TYPE = "IN";
    private static final String DRAFT_STATUS = "DRAFT";

    private final JdbcTemplate jdbcTemplate;

    public StockDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StockDocument> stockDocumentRowMapper = (rs, rowNum) -> {
        StockDocument document = new StockDocument();

        document.setId(rs.getLong("id"));
        document.setDocNumber(rs.getString("doc_number"));
        document.setDocType(rs.getString("doc_type"));
        document.setDocStatus(rs.getString("doc_status"));

        Date docDate = rs.getDate("doc_date");
        if (docDate != null) {
            document.setDocDate(docDate.toLocalDate());
        }

        long warehouseId = rs.getLong("warehouse_id");
        document.setWarehouseId(rs.wasNull() ? null : warehouseId);
        document.setWarehouseName(rs.getString("warehouse_name"));

        long counterpartyId = rs.getLong("counterparty_id");
        document.setCounterpartyId(rs.wasNull() ? null : counterpartyId);
        document.setCounterpartyName(rs.getString("counterparty_name"));

        int createdBy = rs.getInt("created_by");
        document.setCreatedBy(rs.wasNull() ? null : createdBy);

        document.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            document.setCreatedAt(createdAt.toLocalDateTime());
        }

        return document;
    };

    public List<StockDocument> findAllReceipts() {
        String sql = """
                select
                    sd.id,
                    sd.doc_number,
                    sd.doc_type,
                    sd.doc_status,
                    sd.doc_date,
                    sd.warehouse_id,
                    w.name as warehouse_name,
                    sd.counterparty_id,
                    c.name as counterparty_name,
                    sd.created_by,
                    sd.notes,
                    sd.created_at
                from stock_document sd
                join warehouse w on w.id = sd.warehouse_id
                left join counterparty c on c.id = sd.counterparty_id
                where sd.doc_type = ?
                order by sd.doc_date desc, sd.id desc
                """;

        return jdbcTemplate.query(sql, stockDocumentRowMapper, RECEIPT_TYPE);
    }

    public StockDocument findReceiptById(Long id) {
        String sql = """
                select
                    sd.id,
                    sd.doc_number,
                    sd.doc_type,
                    sd.doc_status,
                    sd.doc_date,
                    sd.warehouse_id,
                    w.name as warehouse_name,
                    sd.counterparty_id,
                    c.name as counterparty_name,
                    sd.created_by,
                    sd.notes,
                    sd.created_at
                from stock_document sd
                join warehouse w on w.id = sd.warehouse_id
                left join counterparty c on c.id = sd.counterparty_id
                where sd.doc_type = ?
                  and sd.id = ?
                """;

        List<StockDocument> documents = jdbcTemplate.query(sql, stockDocumentRowMapper, RECEIPT_TYPE, id);
        return documents.isEmpty() ? null : documents.get(0);
    }

    public void saveReceipt(StockDocument document) {
        Integer createdBy = findFirstActiveUserId();

        String sql = """
                insert into stock_document (
                    doc_number,
                    doc_type,
                    doc_status,
                    doc_date,
                    warehouse_id,
                    counterparty_id,
                    created_by,
                    notes,
                    created_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """;

        jdbcTemplate.update(
                sql,
                nullIfBlank(document.getDocNumber()),
                RECEIPT_TYPE,
                DRAFT_STATUS,
                document.getDocDate(),
                document.getWarehouseId(),
                document.getCounterpartyId(),
                createdBy,
                nullIfBlank(document.getNotes())
        );
    }

    public void updateReceipt(StockDocument document) {
        String sql = """
                update stock_document
                set
                    doc_number = ?,
                    doc_status = ?,
                    doc_date = ?,
                    warehouse_id = ?,
                    counterparty_id = ?,
                    notes = ?
                where id = ?
                  and doc_type = ?
                """;

        jdbcTemplate.update(
                sql,
                nullIfBlank(document.getDocNumber()),
                nullIfBlank(document.getDocStatus()),
                document.getDocDate(),
                document.getWarehouseId(),
                document.getCounterpartyId(),
                nullIfBlank(document.getNotes()),
                document.getId(),
                RECEIPT_TYPE
        );
    }

    private Integer findFirstActiveUserId() {
        Integer createdBy = jdbcTemplate.queryForObject("""
                select first 1 id
                from app_user
                where coalesce(is_active, 0) = 1
                order by id
                """, Integer.class);

        if (createdBy == null) {
            throw new IllegalStateException("В таблице APP_USER нет активного пользователя.");
        }

        return createdBy;
    }

    private String nullIfBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
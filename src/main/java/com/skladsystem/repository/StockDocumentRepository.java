package com.skladsystem.repository;

import com.skladsystem.model.StockDocument;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockDocumentRepository {

    private static final String RECEIPT_TYPE = "IN";
    private static final String SHIPMENT_TYPE = "OUT";
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

        document.setPartnerName(rs.getString("partner_name"));

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
        return findAllReceipts(null, null, null, null, null, null);
    }

    public List<StockDocument> findAllShipments() {
        return findAllShipments(null, null, null, null, null, null);
    }

    public List<StockDocument> findAllReceipts(LocalDate dateFrom,
                                               LocalDate dateTo,
                                               Long warehouseId,
                                               String partnerName,
                                               String status,
                                               String docNumber) {
        return findAllByType(RECEIPT_TYPE, dateFrom, dateTo, warehouseId, partnerName, status, docNumber);
    }

    public List<StockDocument> findAllShipments(LocalDate dateFrom,
                                                LocalDate dateTo,
                                                Long warehouseId,
                                                String partnerName,
                                                String status,
                                                String docNumber) {
        return findAllByType(SHIPMENT_TYPE, dateFrom, dateTo, warehouseId, partnerName, status, docNumber);
    }

    public StockDocument findReceiptById(Long id) {
        return findByIdAndType(id, RECEIPT_TYPE);
    }

    public StockDocument findShipmentById(Long id) {
        return findByIdAndType(id, SHIPMENT_TYPE);
    }

    public String getNextReceiptNumber() {
        return getNextDocNumber("IN", RECEIPT_TYPE);
    }

    public String getNextShipmentNumber() {
        return getNextDocNumber("OUT", SHIPMENT_TYPE);
    }

    public Long saveReceiptAndReturnId(StockDocument document) {
        return saveDocumentAndReturnId(document, RECEIPT_TYPE);
    }

    public Long saveShipmentAndReturnId(StockDocument document) {
        return saveDocumentAndReturnId(document, SHIPMENT_TYPE);
    }

    public void updateReceipt(StockDocument document) {
        updateDocument(document, RECEIPT_TYPE);
    }

    public void updateShipment(StockDocument document) {
        updateDocument(document, SHIPMENT_TYPE);
    }

    public void deleteReceiptById(Long id) {
        deleteByIdAndType(id, RECEIPT_TYPE);
    }

    public void deleteShipmentById(Long id) {
        deleteByIdAndType(id, SHIPMENT_TYPE);
    }

    public long countReceiptsByPeriod(LocalDate dateFrom, LocalDate dateTo) {
        return countDocumentsByType(RECEIPT_TYPE, dateFrom, dateTo);
    }

    public long countShipmentsByPeriod(LocalDate dateFrom, LocalDate dateTo) {
        return countDocumentsByType(SHIPMENT_TYPE, dateFrom, dateTo);
    }

    public BigDecimal sumReceiptsAmountByPeriod(LocalDate dateFrom, LocalDate dateTo) {
        return sumDocumentAmountByType(RECEIPT_TYPE, dateFrom, dateTo);
    }

    public BigDecimal sumShipmentsAmountByPeriod(LocalDate dateFrom, LocalDate dateTo) {
        return sumDocumentAmountByType(SHIPMENT_TYPE, dateFrom, dateTo);
    }

    private List<StockDocument> findAllByType(String docType,
                                              LocalDate dateFrom,
                                              LocalDate dateTo,
                                              Long warehouseId,
                                              String partnerName,
                                              String status,
                                              String docNumber) {
        StringBuilder sql = new StringBuilder("""
                select
                    sd.id,
                    sd.doc_number,
                    sd.doc_type,
                    sd.doc_status,
                    sd.doc_date,
                    sd.warehouse_id,
                    w.name as warehouse_name,
                    sd.partner_name,
                    sd.created_by,
                    sd.notes,
                    sd.created_at
                from stock_document sd
                join warehouse w on w.id = sd.warehouse_id
                where sd.doc_type = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(docType);

        if (dateFrom != null) {
            sql.append(" and sd.doc_date >= ? ");
            params.add(dateFrom);
        }

        if (dateTo != null) {
            sql.append(" and sd.doc_date <= ? ");
            params.add(dateTo);
        }

        if (warehouseId != null) {
            sql.append(" and sd.warehouse_id = ? ");
            params.add(warehouseId);
        }

        if (partnerName != null && !partnerName.isBlank()) {
            sql.append(" and coalesce(sd.partner_name, '') containing ? ");
            params.add(partnerName.trim());
        }

        if (status != null && !status.isBlank()) {
            sql.append(" and coalesce(sd.doc_status, '') = ? ");
            params.add(status.trim());
        }

        if (docNumber != null && !docNumber.isBlank()) {
            sql.append(" and coalesce(sd.doc_number, '') containing ? ");
            params.add(docNumber.trim());
        }

        sql.append(" order by sd.doc_date desc, sd.id desc ");

        return jdbcTemplate.query(sql.toString(), stockDocumentRowMapper, params.toArray());
    }

    private long countDocumentsByType(String docType, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder("""
                select count(*)
                from stock_document sd
                where sd.doc_type = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(docType);

        if (dateFrom != null) {
            sql.append(" and sd.doc_date >= ? ");
            params.add(dateFrom);
        }

        if (dateTo != null) {
            sql.append(" and sd.doc_date <= ? ");
            params.add(dateTo);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0L;
    }

    private BigDecimal sumDocumentAmountByType(String docType, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder("""
                select coalesce(sum(coalesce(sdi.quantity, 0) * coalesce(sdi.price, 0)), 0)
                from stock_document sd
                join stock_document_item sdi on sdi.document_id = sd.id
                where sd.doc_type = ?
                  and coalesce(sd.doc_status, '') <> 'CANCELLED'
                """);

        List<Object> params = new ArrayList<>();
        params.add(docType);

        if (dateFrom != null) {
            sql.append(" and sd.doc_date >= ? ");
            params.add(dateFrom);
        }

        if (dateTo != null) {
            sql.append(" and sd.doc_date <= ? ");
            params.add(dateTo);
        }

        BigDecimal result = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return result != null ? result : BigDecimal.ZERO;
    }

    private StockDocument findByIdAndType(Long id, String docType) {
        String sql = """
                select
                    sd.id,
                    sd.doc_number,
                    sd.doc_type,
                    sd.doc_status,
                    sd.doc_date,
                    sd.warehouse_id,
                    w.name as warehouse_name,
                    sd.partner_name,
                    sd.created_by,
                    sd.notes,
                    sd.created_at
                from stock_document sd
                join warehouse w on w.id = sd.warehouse_id
                where sd.doc_type = ?
                  and sd.id = ?
                """;

        List<StockDocument> documents = jdbcTemplate.query(sql, stockDocumentRowMapper, docType, id);
        return documents.isEmpty() ? null : documents.get(0);
    }

    private String getNextDocNumber(String prefix, String docType) {
        String sql = """
                select doc_number
                from stock_document
                where doc_type = ?
                  and doc_number starting with ?
                order by id desc
                rows 100
                """;

        List<String> numbers = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("doc_number"),
                docType,
                prefix + "-"
        );

        int maxNumber = 0;

        for (String docNumber : numbers) {
            if (docNumber == null) {
                continue;
            }

            String expectedPrefix = prefix + "-";
            if (!docNumber.startsWith(expectedPrefix)) {
                continue;
            }

            String numericPart = docNumber.substring(expectedPrefix.length()).trim();

            try {
                int value = Integer.parseInt(numericPart);
                if (value > maxNumber) {
                    maxNumber = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return prefix + "-" + String.format("%03d", maxNumber + 1);
    }

    private Long saveDocumentAndReturnId(StockDocument document, String docType) {
        Integer createdBy = document.getCreatedBy() != null
                ? document.getCreatedBy()
                : findFirstActiveUserId();

        String docNumber = nullIfBlank(document.getDocNumber());
        if (docNumber == null) {
            docNumber = RECEIPT_TYPE.equals(docType)
                    ? getNextReceiptNumber()
                    : getNextShipmentNumber();
        }

        String sql = """
                insert into stock_document (
                    doc_number,
                    doc_type,
                    doc_status,
                    doc_date,
                    warehouse_id,
                    partner_name,
                    created_by,
                    notes,
                    created_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                returning id
                """;

        Number result = jdbcTemplate.queryForObject(
                sql,
                Number.class,
                docNumber,
                docType,
                DRAFT_STATUS,
                document.getDocDate(),
                document.getWarehouseId(),
                nullIfBlank(document.getPartnerName()),
                createdBy,
                nullIfBlank(document.getNotes())
        );

        if (result == null) {
            throw new IllegalStateException("Не удалось получить ID созданного документа.");
        }

        return result.longValue();
    }

    private void updateDocument(StockDocument document, String docType) {
        String sql = """
                update stock_document
                set
                    doc_number = ?,
                    doc_status = ?,
                    doc_date = ?,
                    warehouse_id = ?,
                    partner_name = ?,
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
                nullIfBlank(document.getPartnerName()),
                nullIfBlank(document.getNotes()),
                document.getId(),
                docType
        );
    }

    private void deleteByIdAndType(Long id, String docType) {
        String sql = """
                delete from stock_document
                where id = ?
                  and doc_type = ?
                """;

        jdbcTemplate.update(sql, id, docType);
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
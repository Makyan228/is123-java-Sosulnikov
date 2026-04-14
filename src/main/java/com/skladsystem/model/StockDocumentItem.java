package com.skladsystem.model;

import java.math.BigDecimal;

public class StockDocumentItem {

    private Long id;
    private Long documentId;
    private Long productId;
    private String productArticle;
    private String productName;

    private Long sourceLocationId;
    private String sourceLocationName;

    private Long targetLocationId;
    private String targetLocationName;

    private BigDecimal quantity;
    private BigDecimal price;
    private String notes;

    public StockDocumentItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductArticle() {
        return productArticle;
    }

    public void setProductArticle(String productArticle) {
        this.productArticle = productArticle;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getSourceLocationId() {
        return sourceLocationId;
    }

    public void setSourceLocationId(Long sourceLocationId) {
        this.sourceLocationId = sourceLocationId;
    }

    public String getSourceLocationName() {
        return sourceLocationName;
    }

    public void setSourceLocationName(String sourceLocationName) {
        this.sourceLocationName = sourceLocationName;
    }

    public Long getTargetLocationId() {
        return targetLocationId;
    }

    public void setTargetLocationId(Long targetLocationId) {
        this.targetLocationId = targetLocationId;
    }

    public String getTargetLocationName() {
        return targetLocationName;
    }

    public void setTargetLocationName(String targetLocationName) {
        this.targetLocationName = targetLocationName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getLineTotal() {
        if (quantity == null || price == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(price);
    }
}
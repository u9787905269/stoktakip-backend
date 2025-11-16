package com.stoktakip.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class StockReportItem {

    private Long productId;
    private String productName;
    private String warehouseName;
    private Integer stockQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal totalBtwAmount;
    private Integer btwRate;
    private Instant lastMovementDate;
    private String lastMovementNote;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalBtwAmount() {
        return totalBtwAmount;
    }

    public void setTotalBtwAmount(BigDecimal totalBtwAmount) {
        this.totalBtwAmount = totalBtwAmount;
    }

    public Integer getBtwRate() {
        return btwRate;
    }

    public void setBtwRate(Integer btwRate) {
        this.btwRate = btwRate;
    }

    public Instant getLastMovementDate() {
        return lastMovementDate;
    }

    public void setLastMovementDate(Instant lastMovementDate) {
        this.lastMovementDate = lastMovementDate;
    }

    public String getLastMovementNote() {
        return lastMovementNote;
    }

    public void setLastMovementNote(String lastMovementNote) {
        this.lastMovementNote = lastMovementNote;
    }
}



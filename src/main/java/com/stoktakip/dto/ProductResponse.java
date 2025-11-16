package com.stoktakip.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {

    private Long id;
    private String name;
    private Long productNameId;
    private String productName;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long serialNumberId;
    private String serialNumberValue;
    private BigDecimal unitPrice;
    private BigDecimal unitPriceBtwAmount;
    private Integer btwRate;
    private Integer stockQuantity;
    private String barcode;
    private BigDecimal totalPrice;
    private BigDecimal totalBtwAmount;
    private BigDecimal totalNetPrice;
    private Long warehouseId;
    private String warehouseName;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProductNameId() {
        return productNameId;
    }

    public void setProductNameId(Long productNameId) {
        this.productNameId = productNameId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getSerialNumberId() {
        return serialNumberId;
    }

    public void setSerialNumberId(Long serialNumberId) {
        this.serialNumberId = serialNumberId;
    }

    public String getSerialNumberValue() {
        return serialNumberValue;
    }

    public void setSerialNumberValue(String serialNumberValue) {
        this.serialNumberValue = serialNumberValue;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPriceBtwAmount() {
        return unitPriceBtwAmount;
    }

    public void setUnitPriceBtwAmount(BigDecimal unitPriceBtwAmount) {
        this.unitPriceBtwAmount = unitPriceBtwAmount;
    }

    public Integer getBtwRate() {
        return btwRate;
    }

    public void setBtwRate(Integer btwRate) {
        this.btwRate = btwRate;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public BigDecimal getTotalNetPrice() {
        return totalNetPrice;
    }

    public void setTotalNetPrice(BigDecimal totalNetPrice) {
        this.totalNetPrice = totalNetPrice;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

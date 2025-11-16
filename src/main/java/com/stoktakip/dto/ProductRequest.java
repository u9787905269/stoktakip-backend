package com.stoktakip.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductRequest {

    @NotNull
    private Long productNameId;

    private String description;

    private Long categoryId;

    private Long serialNumberId;

    @NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0.0")
    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer btwRate;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @NotNull
    private Long warehouseId;

    private String stockAdjustmentReason;

    private String stockAdjustmentType;

    public Long getProductNameId() {
        return productNameId;
    }

    public void setProductNameId(Long productNameId) {
        this.productNameId = productNameId;
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

    public Long getSerialNumberId() {
        return serialNumberId;
    }

    public void setSerialNumberId(Long serialNumberId) {
        this.serialNumberId = serialNumberId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getStockAdjustmentReason() {
        return stockAdjustmentReason;
    }

    public void setStockAdjustmentReason(String stockAdjustmentReason) {
        this.stockAdjustmentReason = stockAdjustmentReason;
    }

    public String getStockAdjustmentType() {
        return stockAdjustmentType;
    }

    public void setStockAdjustmentType(String stockAdjustmentType) {
        this.stockAdjustmentType = stockAdjustmentType;
    }
}

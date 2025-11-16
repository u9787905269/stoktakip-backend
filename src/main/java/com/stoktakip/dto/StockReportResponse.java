package com.stoktakip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StockReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalProducts;
    private Long totalStockQuantity;
    private BigDecimal totalStockValue;
    private Map<String, Long> movementCounts;
    private List<StockReportItem> items;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public void setTotalStockQuantity(Long totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }

    public BigDecimal getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(BigDecimal totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    public Map<String, Long> getMovementCounts() {
        return movementCounts;
    }

    public void setMovementCounts(Map<String, Long> movementCounts) {
        this.movementCounts = movementCounts;
    }

    public List<StockReportItem> getItems() {
        return items;
    }

    public void setItems(List<StockReportItem> items) {
        this.items = items;
    }
}



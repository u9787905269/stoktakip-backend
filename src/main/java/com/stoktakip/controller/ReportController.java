package com.stoktakip.controller;

import com.stoktakip.dto.StockReportResponse;
import com.stoktakip.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/stock")
    public ResponseEntity<?> generateStockReport(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Long warehouseId,
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "JSON") String format
    ) {
        StockReportResponse report = reportService.generateStockReport(
            startDate,
            endDate,
            warehouseId,
            productId,
            categoryId,
            minPrice,
            maxPrice
        );
        if ("CSV".equalsIgnoreCase(format)) {
            String csv = reportService.generateStockReportCsv(report);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stok-raporu.csv")
                .contentType(Objects.requireNonNull(MediaType.parseMediaType("text/csv; charset=UTF-8")))
                .body(csv);
        } else if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdf = reportService.generateStockReportPdf(report);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        }
        return ResponseEntity.ok(report);
    }
}


package com.stoktakip.service;

import com.stoktakip.dto.StockReportItem;
import com.stoktakip.dto.StockReportResponse;
import com.stoktakip.model.MovementType;
import com.stoktakip.model.Product;
import com.stoktakip.model.StockMovement;
import com.stoktakip.repository.ProductRepository;
import com.stoktakip.repository.StockMovementRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ReportService(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockReportResponse generateStockReport(LocalDate startDate, LocalDate endDate, Long warehouseId, Long productId, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        Instant startInstant = null;
        Instant endInstant = null;
        if (startDate != null) {
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        if (endDate != null) {
            endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();
        }

        List<Product> products = productRepository.findAll();
        if (products == null) {
            products = new java.util.ArrayList<>();
        }
        
        // Lazy loading trigger for all products
        products.forEach(product -> {
            try {
                if (product.getWarehouse() != null) {
                    product.getWarehouse().getId();
                }
            } catch (Exception e) {
                // Ignore lazy loading errors
            }
            try {
                if (product.getCategory() != null) {
                    product.getCategory().getId();
                }
            } catch (Exception e) {
                // Ignore lazy loading errors
            }
        });
        
        if (warehouseId != null) {
            products = products.stream()
                .filter(product -> {
                    try {
                        return product.getWarehouse() != null && Objects.equals(product.getWarehouse().getId(), warehouseId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
        }

        if (productId != null) {
            products = products.stream()
                .filter(product -> Objects.equals(product.getId(), productId))
                .toList();
        }

        if (categoryId != null) {
            products = products.stream()
                .filter(product -> {
                    try {
                        return product.getCategory() != null && Objects.equals(product.getCategory().getId(), categoryId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                .filter(product -> product.getUnitPrice() != null && product.getUnitPrice().compareTo(minPrice) >= 0)
                .toList();
        }

        if (maxPrice != null) {
            products = products.stream()
                .filter(product -> product.getUnitPrice() != null && product.getUnitPrice().compareTo(maxPrice) <= 0)
                .toList();
        }

        Set<Long> productIds = products.stream()
            .map(Product::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<StockMovement> movements = fetchMovements(startInstant, endInstant);
        if (movements == null) {
            movements = new java.util.ArrayList<>();
        }
        
        // Lazy loading trigger for movements
        movements.forEach(movement -> {
            try {
                if (movement.getProduct() != null) {
                    movement.getProduct().getId();
                }
            } catch (Exception e) {
                // Ignore lazy loading errors
            }
        });
        
        if (!productIds.isEmpty()) {
            movements = movements.stream()
                .filter(movement -> {
                    try {
                        return movement.getProduct() != null && movement.getProduct().getId() != null && productIds.contains(movement.getProduct().getId());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
        }

        Map<Long, List<StockMovement>> movementsByProduct = movements.stream()
            .filter(movement -> {
                try {
                    return movement.getProduct() != null && movement.getProduct().getId() != null;
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.groupingBy(movement -> {
                try {
                    return movement.getProduct().getId();
                } catch (Exception e) {
                    return null;
                }
            }));
        movementsByProduct.remove(null); // Remove entries with null keys

        EnumMap<MovementType, Long> movementCounts = new EnumMap<>(MovementType.class);
        for (MovementType type : MovementType.values()) {
            long count = movements.stream()
                .filter(movement -> movement.getType() == type)
                .count();
            movementCounts.put(type, count);
        }

        List<StockReportItem> items = new ArrayList<>();
        BigDecimal totalStockValue = BigDecimal.ZERO;
        long totalStockQuantity = 0;

        for (Product product : products) {
            StockReportItem item = new StockReportItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            try {
                item.setWarehouseName(product.getWarehouse() != null ? product.getWarehouse().getName() : "—");
            } catch (Exception e) {
                item.setWarehouseName("—");
            }
            item.setStockQuantity(product.getStockQuantity());
            item.setUnitPrice(product.getUnitPrice());
            item.setTotalPrice(product.getTotalPrice());
            item.setTotalBtwAmount(product.getTotalBtwAmount());
            item.setBtwRate(product.getBtwRate());

            totalStockValue = totalStockValue.add(product.getTotalPrice() != null ? product.getTotalPrice() : BigDecimal.ZERO);
            totalStockQuantity += product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            List<StockMovement> productMovements = movementsByProduct.getOrDefault(product.getId(), List.of());
            try {
                productMovements.stream()
                    .filter(m -> m.getMovementDate() != null)
                    .max((a, b) -> a.getMovementDate().compareTo(b.getMovementDate()))
                    .ifPresent(latest -> {
                        item.setLastMovementDate(latest.getMovementDate());
                        item.setLastMovementNote(latest.getNote());
                    });
            } catch (Exception e) {
                // Ignore errors in movement processing
            }

            items.add(item);
        }

        StockReportResponse response = new StockReportResponse();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalProducts(items.size());
        response.setTotalStockQuantity(totalStockQuantity);
        response.setTotalStockValue(totalStockValue.setScale(2, RoundingMode.HALF_UP));
        response.setMovementCounts(Map.of(
            "INBOUND", movementCounts.getOrDefault(MovementType.INBOUND, 0L),
            "OUTBOUND", movementCounts.getOrDefault(MovementType.OUTBOUND, 0L)
        ));
        response.setItems(items);
        return response;
    }

    public String generateStockReportCsv(StockReportResponse report) {
        StringBuilder builder = new StringBuilder();
        builder.append("Product ID;Product Name;Warehouse;Stock Quantity;Unit Price;Total Price;Total VAT;VAT Rate;Last Movement Date;Last Movement Note");
        builder.append(System.lineSeparator());
        for (StockReportItem item : report.getItems()) {
            builder.append(value(item.getProductId()))
                .append(';')
                .append(value(item.getProductName()))
                .append(';')
                .append(value(item.getWarehouseName()))
                .append(';')
                .append(value(item.getStockQuantity()))
                .append(';')
                .append(formatDecimal(item.getUnitPrice()))
                .append(';')
                .append(formatDecimal(item.getTotalPrice()))
                .append(';')
                .append(formatDecimal(item.getTotalBtwAmount()))
                .append(';')
                .append(value(item.getBtwRate()))
                .append(';')
                .append(item.getLastMovementDate() != null ? item.getLastMovementDate() : "")
                .append(';')
                .append(value(item.getLastMovementNote()))
                .append(System.lineSeparator());
        }
        return builder.toString();
    }

    public byte[] generateStockReportPdf(StockReportResponse report) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("Stock Report", titleFont));
            document.add(new Paragraph(
                "Period: " +
                    (report.getStartDate() != null ? report.getStartDate().toString() : "N/A") +
                    " - " +
                    (report.getEndDate() != null ? report.getEndDate().toString() : "N/A"),
                subtitleFont
            ));
            document.add(new Paragraph(
                String.format(
                    "Products: %d | Total Stock: %d | Total Value: %s",
                    report.getTotalProducts(),
                    report.getTotalStockQuantity(),
                    formatDecimal(report.getTotalStockValue())
                ),
                subtitleFont
            ));
            document.add(new Paragraph(
                String.format(
                    "Inbound Movements: %d | Outbound Movements: %d",
                    report.getMovementCounts().getOrDefault("INBOUND", 0L),
                    report.getMovementCounts().getOrDefault("OUTBOUND", 0L)
                ),
                subtitleFont
            ));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3, 3, 3, 2, 2, 2, 2, 3, 4 });

            addHeaderCell(table, "Product", headerFont);
            addHeaderCell(table, "Warehouse", headerFont);
            addHeaderCell(table, "Stock Quantity", headerFont);
            addHeaderCell(table, "Unit Price", headerFont);
            addHeaderCell(table, "Total Price", headerFont);
            addHeaderCell(table, "Total VAT", headerFont);
            addHeaderCell(table, "VAT %", headerFont);
            addHeaderCell(table, "Last Movement Date", headerFont);
            addHeaderCell(table, "Last Movement Note", headerFont);

            for (StockReportItem item : report.getItems()) {
                addCell(table, value(item.getProductName()), cellFont);
                addCell(table, value(item.getWarehouseName()), cellFont);
                addCell(table, value(item.getStockQuantity()), cellFont);
                addCell(table, formatDecimal(item.getUnitPrice()), cellFont);
                addCell(table, formatDecimal(item.getTotalPrice()), cellFont);
                addCell(table, formatDecimal(item.getTotalBtwAmount()), cellFont);
                addCell(table, value(item.getBtwRate()), cellFont);
                addCell(table, item.getLastMovementDate() != null ? item.getLastMovementDate().toString() : "", cellFont);
                addCell(table, value(item.getLastMovementNote()), cellFont);
            }

            document.add(table);
        } catch (DocumentException ex) {
            throw new IllegalStateException("PDF raporu oluşturulamadı", ex);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(33, 150, 243));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private List<StockMovement> fetchMovements(Instant start, Instant end) {
        if (start != null && end != null) {
            return stockMovementRepository.findByMovementDateBetween(start, end);
        }
        if (start != null) {
            return stockMovementRepository.findByMovementDateAfter(start);
        }
        if (end != null) {
            return stockMovementRepository.findByMovementDateBefore(end);
        }
        return stockMovementRepository.findAll();
    }

    private String value(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}



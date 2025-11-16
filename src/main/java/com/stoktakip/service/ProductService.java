package com.stoktakip.service;

import com.stoktakip.dto.ProductRequest;
import com.stoktakip.dto.ProductResponse;
import com.stoktakip.model.Category;
import com.stoktakip.model.MovementType;
import com.stoktakip.model.Product;
import com.stoktakip.model.ProductName;
import com.stoktakip.model.SerialNumber;
import com.stoktakip.model.Warehouse;
import com.stoktakip.repository.ProductRepository;
import com.stoktakip.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final CategoryService categoryService;
    private final ProductNameService productNameService;
    private final SerialNumberService serialNumberService;
    private final BarcodeService barcodeService;
    private final StockMovementService stockMovementService;

    public ProductService(
        ProductRepository productRepository,
        WarehouseRepository warehouseRepository,
        CategoryService categoryService,
        ProductNameService productNameService,
        SerialNumberService serialNumberService,
        BarcodeService barcodeService,
        StockMovementService stockMovementService
    ) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.categoryService = categoryService;
        this.productNameService = productNameService;
        this.serialNumberService = serialNumberService;
        this.barcodeService = barcodeService;
        this.stockMovementService = stockMovementService;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = mapRequestToEntity(request, new Product());
        if (product.getBarcode() == null) {
            product.setBarcode(barcodeService.generateTemporaryBarcode(
                product.getSerialNumber() != null ? product.getSerialNumber().getValue() : null
            ));
        }
        Product saved = productRepository.save(product);

        if (saved.getBarcode() == null || saved.getBarcode().startsWith("TMP-") || saved.getBarcode().startsWith("SN-TMP-")) {
            saved.setBarcode(barcodeService.generateBarcode(
                saved.getSerialNumber() != null ? saved.getSerialNumber().getValue() : null,
                saved.getId()
            ));
            saved = productRepository.save(saved);
        }

        stockMovementService.recordInitialInboundMovement(saved);

        return toResponse(saved);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Long productId = Objects.requireNonNull(id, "Ürün kimliği gereklidir");
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı"));
        int previousStock = Optional.ofNullable(product.getStockQuantity()).orElse(0);
        Warehouse previousWarehouse = product.getWarehouse();
        Product updated = mapRequestToEntity(request, product);
        Warehouse targetWarehouse = updated.getWarehouse() != null ? updated.getWarehouse() : previousWarehouse;
        Product saved = productRepository.save(updated);
        if (saved.getBarcode() == null || saved.getBarcode().startsWith("TMP-") || saved.getBarcode().startsWith("SN-TMP-")) {
            saved.setBarcode(barcodeService.generateBarcode(
                saved.getSerialNumber() != null ? saved.getSerialNumber().getValue() : null,
                saved.getId()
            ));
            saved = productRepository.save(saved);
        }
        int newStock = Optional.ofNullable(saved.getStockQuantity()).orElse(0);
        int stockDelta = newStock - previousStock;
        if (stockDelta != 0 && targetWarehouse != null) {
            MovementType movementType = stockDelta > 0 ? MovementType.INBOUND : MovementType.OUTBOUND;
            String requestedType = request.getStockAdjustmentType();
            if (requestedType != null && !requestedType.isBlank()) {
                try {
                    movementType = MovementType.valueOf(requestedType.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    movementType = stockDelta > 0 ? MovementType.INBOUND : MovementType.OUTBOUND;
                }
            }
            if (stockDelta > 0 && movementType == MovementType.OUTBOUND) {
                movementType = MovementType.INBOUND;
            } else if (stockDelta < 0 && movementType == MovementType.INBOUND) {
                movementType = MovementType.OUTBOUND;
            }

            String note = Optional.ofNullable(request.getStockAdjustmentReason())
                .filter(s -> !s.isBlank())
                .orElseGet(() -> stockDelta > 0 ? "Stok artışı" : "Stok azalışı");

            stockMovementService.logManualAdjustment(saved, targetWarehouse, Math.abs(stockDelta), movementType, note);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        Long productId = Objects.requireNonNull(id, "Ürün kimliği gereklidir");
        return productRepository.findById(productId);
    }

    public void deleteById(Long id) {
        Long productId = Objects.requireNonNull(id, "Ürün kimliği gereklidir");
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı"));
        Integer currentStock = Optional.ofNullable(product.getStockQuantity()).orElse(0);
        Warehouse warehouse = product.getWarehouse();
        if (warehouse != null && currentStock > 0) {
            stockMovementService.logManualAdjustment(
                product,
                warehouse,
                currentStock,
                MovementType.OUTBOUND,
                "Ürün silindi"
            );
        }
        stockMovementService.detachMovementsFromProduct(product);
        productRepository.deleteById(productId);
    }

    private Product mapRequestToEntity(ProductRequest request, Product product) {
        ProductName productName = productNameService.findById(request.getProductNameId())
            .orElseThrow(() -> new IllegalArgumentException("Ürün adı bulunamadı"));
        product.setProductName(productName);
        product.setName(productName.getName());
        product.setDescription(request.getDescription());
        Long categoryId = request.getCategoryId();
        if (categoryId != null) {
            Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı"));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        Long serialNumberId = request.getSerialNumberId();
        if (serialNumberId != null) {
            SerialNumber serialNumber = serialNumberService.findById(serialNumberId)
                .orElseThrow(() -> new IllegalArgumentException("Seri numarası bulunamadı"));
            product.setSerialNumber(serialNumber);
        } else {
            product.setSerialNumber(null);
        }
        BigDecimal unitPrice = normalizePrice(request.getUnitPrice());
        product.setUnitPrice(unitPrice);
        product.setStockQuantity(Optional.ofNullable(request.getStockQuantity()).orElse(0));
        product.setBtwRate(Optional.ofNullable(request.getBtwRate()).orElse(0));

        applyBtwCalculations(product);

        Long warehouseId = request.getWarehouseId();
        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Depo bulunamadı"));
            product.setWarehouse(warehouse);
        } else {
            product.setWarehouse(null);
        }

        return product;
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setProductName(product.getName());
        if (product.getProductName() != null) {
            response.setProductNameId(product.getProductName().getId());
            response.setProductName(product.getProductName().getName());
        }
        response.setDescription(product.getDescription());
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getSerialNumber() != null) {
            response.setSerialNumberId(product.getSerialNumber().getId());
            response.setSerialNumberValue(product.getSerialNumber().getValue());
        }
        response.setUnitPrice(product.getUnitPrice());
        response.setUnitPriceBtwAmount(product.getUnitPriceBtwAmount());
        response.setBtwRate(product.getBtwRate());
        response.setStockQuantity(product.getStockQuantity());
        response.setBarcode(product.getBarcode());
        response.setTotalPrice(product.getTotalPrice());
        response.setTotalBtwAmount(product.getTotalBtwAmount());
        response.setTotalNetPrice(product.getTotalNetPrice());
        if (product.getWarehouse() != null) {
            response.setWarehouseId(product.getWarehouse().getId());
            response.setWarehouseName(product.getWarehouse().getName());
        }
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private void applyBtwCalculations(Product product) {
        // unitPrice artık net fiyat (BTW hariç)
        BigDecimal unitPriceNet = Optional.ofNullable(product.getUnitPrice()).orElse(BigDecimal.ZERO);
        int quantity = Optional.ofNullable(product.getStockQuantity()).orElse(0);
        int rate = Optional.ofNullable(product.getBtwRate()).orElse(0);

        if (rate < 0) {
            rate = 0;
        }

        BigDecimal rateFraction = BigDecimal.valueOf(rate)
            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        // BTW net fiyat üzerinden hesaplanıyor
        BigDecimal unitBtwAmount = unitPriceNet.multiply(rateFraction).setScale(2, RoundingMode.HALF_UP);

        BigDecimal quantityValue = BigDecimal.valueOf(quantity);
        BigDecimal totalNet = unitPriceNet.multiply(quantityValue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBtwAmount = unitBtwAmount.multiply(quantityValue).setScale(2, RoundingMode.HALF_UP);
        // Brüt fiyat = Net fiyat + BTW
        BigDecimal totalGross = totalNet.add(totalBtwAmount).setScale(2, RoundingMode.HALF_UP);

        // unitPrice net fiyat olarak saklanıyor
        product.setUnitPrice(unitPriceNet.setScale(2, RoundingMode.HALF_UP));
        product.setUnitPriceBtwAmount(unitBtwAmount);
        // totalPrice brüt fiyat (net + BTW)
        product.setTotalPrice(totalGross);
        product.setTotalBtwAmount(totalBtwAmount);
        product.setTotalNetPrice(totalNet);
    }
}


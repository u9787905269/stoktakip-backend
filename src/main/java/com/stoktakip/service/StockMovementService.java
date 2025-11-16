package com.stoktakip.service;

import com.stoktakip.dto.StockMovementResponse;
import com.stoktakip.model.MovementType;
import com.stoktakip.model.Product;
import com.stoktakip.model.StockMovement;
import com.stoktakip.model.Warehouse;
import com.stoktakip.repository.ProductRepository;
import com.stoktakip.repository.StockMovementRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockMovementService {

    private static final Logger log = LoggerFactory.getLogger(StockMovementService.class);

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;

    public StockMovementService(
        StockMovementRepository stockMovementRepository,
        ProductRepository productRepository,
        WarehouseService warehouseService
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseService = warehouseService;
    }

    public StockMovement save(StockMovement movement) {
        if (movement.getProduct() == null || movement.getProduct().getId() == null) {
            throw new IllegalArgumentException("Stok hareketi için ürün bilgisi gereklidir.");
        }
        if (movement.getWarehouse() == null || movement.getWarehouse().getId() == null) {
            throw new IllegalArgumentException("Stok hareketi için depo bilgisi gereklidir.");
        }
        if (movement.getQuantity() == null || movement.getQuantity() <= 0) {
            throw new IllegalArgumentException("Geçerli bir hareket miktarı giriniz.");
        }

        Long productId = Objects.requireNonNull(movement.getProduct().getId(), "Ürün kimliği gereklidir");
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı."));

        Long warehouseId = Objects.requireNonNull(movement.getWarehouse().getId(), "Depo kimliği gereklidir");
        Warehouse warehouse = warehouseService.findById(warehouseId)
            .orElseThrow(() -> new IllegalArgumentException("Depo bulunamadı."));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int newStock = currentStock;

        if (movement.getType() == MovementType.INBOUND) {
            newStock = currentStock + movement.getQuantity();
        } else if (movement.getType() == MovementType.OUTBOUND) {
            if (currentStock < movement.getQuantity()) {
                throw new IllegalArgumentException("Yetersiz stok miktarı.");
            }
            newStock = currentStock - movement.getQuantity();
        } else {
            throw new IllegalArgumentException("Geçersiz stok hareket tipi.");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        movement.setProduct(product);
        movement.setWarehouse(warehouse);

        return stockMovementRepository.save(movement);
    }

    public void recordInitialInboundMovement(Product product) {
        if (product == null) {
            return;
        }
        Integer quantity = product.getStockQuantity();
        Warehouse warehouse = product.getWarehouse();
        if (quantity == null || quantity <= 0) {
            return;
        }
        if (warehouse == null || warehouse.getId() == null) {
            log.warn("Depo seçilmediği için ürün id={} için başlangıç stok hareketi kaydedilmedi.", product.getId());
            return;
        }
        logManualAdjustment(product, warehouse, quantity, MovementType.INBOUND, "Ürün oluşturma - başlangıç stoğu");
    }

    public void logManualAdjustment(Product product, Warehouse warehouse, int quantity, MovementType type, String note) {
        if (product == null || product.getId() == null) {
            return;
        }
        if (warehouse == null || warehouse.getId() == null) {
            log.warn("Depo bilgisi eksik olduğu için ürün id={} için stok hareketi kaydedilemedi.", product.getId());
            return;
        }
        if (type == null || quantity <= 0) {
            return;
        }
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setNote(note);
        movement.setProductNameSnapshot(product.getName());
        movement.setProductBarcodeSnapshot(product.getBarcode());
        stockMovementRepository.save(movement);
    }

    public void detachMovementsFromProduct(Product product) {
        if (product == null || product.getId() == null) {
            return;
        }
        List<StockMovement> movements = stockMovementRepository.findByProductId(product.getId());
        if (movements.isEmpty()) {
            return;
        }
        for (StockMovement movement : movements) {
            if (movement.getProductNameSnapshot() == null || movement.getProductNameSnapshot().isBlank()) {
                movement.setProductNameSnapshot(product.getName());
            }
            if (movement.getProductBarcodeSnapshot() == null || movement.getProductBarcodeSnapshot().isBlank()) {
                movement.setProductBarcodeSnapshot(product.getBarcode());
            }
            movement.setProduct(null);
        }
        stockMovementRepository.saveAll(movements);
        stockMovementRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findByWarehouseAndDateRange(Warehouse warehouse, Instant start, Instant end) {
        return stockMovementRepository.findByWarehouseAndMovementDateBetween(warehouse, start, end);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findRecentMovements(int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        return stockMovementRepository.findAllByOrderByMovementDateDesc(PageRequest.of(0, size))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setType(movement.getType());
        response.setQuantity(movement.getQuantity());
        response.setMovementDate(movement.getMovementDate());
        response.setNote(movement.getNote());
        if (movement.getProduct() != null) {
            response.setProductId(movement.getProduct().getId());
            response.setProductName(movement.getProduct().getName());
        } else {
            response.setProductName(movement.getProductNameSnapshot());
        }
        if (movement.getWarehouse() != null) {
            response.setWarehouseId(movement.getWarehouse().getId());
            response.setWarehouseName(movement.getWarehouse().getName());
        }
        return response;
    }
}


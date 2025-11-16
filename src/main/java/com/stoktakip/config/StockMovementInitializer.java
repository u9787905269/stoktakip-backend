package com.stoktakip.config;

import com.stoktakip.model.MovementType;
import com.stoktakip.model.Product;
import com.stoktakip.repository.ProductRepository;
import com.stoktakip.repository.StockMovementRepository;
import com.stoktakip.service.StockMovementService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockMovementInitializer {

    private static final Logger log = LoggerFactory.getLogger(StockMovementInitializer.class);

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementService stockMovementService;

    public StockMovementInitializer(
        ProductRepository productRepository,
        StockMovementRepository stockMovementRepository,
        StockMovementService stockMovementService
    ) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementService = stockMovementService;
    }

    @PostConstruct
    @Transactional
    public void ensureInitialMovements() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }
        for (Product product : products) {
            Integer quantity = Optional.ofNullable(product.getStockQuantity()).orElse(0);
            if (quantity <= 0 || product.getWarehouse() == null || product.getWarehouse().getId() == null) {
                continue;
            }
            if (!stockMovementRepository.findByProductId(product.getId()).isEmpty()) {
                continue;
            }
            log.info(
                "Ürün id={} için stok hareketi kaydı bulunamadı. {} adet stok başlangıç kaydı ekleniyor.",
                product.getId(),
                quantity
            );
            stockMovementService.logManualAdjustment(
                product,
                product.getWarehouse(),
                quantity,
                MovementType.INBOUND,
                "Başlangıç stoğu (geriye dönük)"
            );
        }
    }
}

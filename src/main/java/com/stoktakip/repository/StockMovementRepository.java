package com.stoktakip.repository;

import com.stoktakip.model.MovementType;
import com.stoktakip.model.StockMovement;
import com.stoktakip.model.Warehouse;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByWarehouseAndMovementDateBetween(Warehouse warehouse, Instant start, Instant end);

    List<StockMovement> findAllByOrderByMovementDateDesc(Pageable pageable);

    List<StockMovement> findByProductId(Long productId);

    long countByType(MovementType type);

    List<StockMovement> findByMovementDateBetween(Instant start, Instant end);

    List<StockMovement> findByMovementDateAfter(Instant start);

    List<StockMovement> findByMovementDateBefore(Instant end);
}


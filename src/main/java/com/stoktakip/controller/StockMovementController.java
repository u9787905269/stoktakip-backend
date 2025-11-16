package com.stoktakip.controller;

import com.stoktakip.dto.StockMovementResponse;
import com.stoktakip.model.StockMovement;
import com.stoktakip.model.Warehouse;
import com.stoktakip.service.StockMovementService;
import com.stoktakip.service.WarehouseService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/stock")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final WarehouseService warehouseService;

    public StockMovementController(StockMovementService stockMovementService, WarehouseService warehouseService) {
        this.stockMovementService = stockMovementService;
        this.warehouseService = warehouseService;
    }

    @PostMapping("/movement")
    public ResponseEntity<StockMovement> createMovement(@RequestBody StockMovement movement) {
        StockMovement saved = stockMovementService.save(movement);
        return ResponseEntity.created(Objects.requireNonNull(URI.create("/stock/movement/" + saved.getId()))).body(saved);
    }

    @GetMapping("/movement/{warehouseId}")
    public ResponseEntity<List<StockMovement>> listMovements(
        @PathVariable Long warehouseId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        Warehouse warehouse = warehouseService.findById(warehouseId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Depo bulunamadı"));
        return ResponseEntity.ok(stockMovementService.findByWarehouseAndDateRange(warehouse, start, end));
    }

    @GetMapping("/movement/recent")
    public ResponseEntity<List<StockMovementResponse>> recentMovements(
        @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(stockMovementService.findRecentMovements(limit));
    }
}


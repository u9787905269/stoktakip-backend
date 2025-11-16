package com.stoktakip.config;

import com.stoktakip.model.Warehouse;
import com.stoktakip.service.WarehouseService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final WarehouseService warehouseService;

    public DataInitializer(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostConstruct
    public void seed() {
        if (warehouseService.findAll().isEmpty()) {
            Warehouse warehouse = new Warehouse();
            warehouse.setName("Merkez Depo");
            warehouse.setDescription("Varsayılan depo");
            warehouseService.save(warehouse);
        }
    }
}


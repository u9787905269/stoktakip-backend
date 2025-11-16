package com.stoktakip.service;

import com.stoktakip.model.Warehouse;
import com.stoktakip.repository.WarehouseRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public Warehouse save(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Transactional(readOnly = true)
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Warehouse> findById(Long id) {
        Objects.requireNonNull(id, "Depo kimliği gereklidir");
        return warehouseRepository.findById(id);
    }

    public void deleteById(Long id) {
        Objects.requireNonNull(id, "Depo kimliği gereklidir");
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Depo bulunamadı"));
        if ((warehouse.getProducts() != null && !warehouse.getProducts().isEmpty())
            || (warehouse.getStockMovements() != null && !warehouse.getStockMovements().isEmpty())) {
            throw new IllegalStateException("Bu depo kayıtlı ürünler veya hareketler tarafından kullanılıyor.");
        }
        warehouseRepository.delete(warehouse);
    }
}


package com.stoktakip.repository;

import com.stoktakip.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByNameIgnoreCase(String name);
}


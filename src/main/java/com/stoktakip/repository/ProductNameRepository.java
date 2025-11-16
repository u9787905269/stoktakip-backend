package com.stoktakip.repository;

import com.stoktakip.model.ProductName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductNameRepository extends JpaRepository<ProductName, Long> {

    Optional<ProductName> findByNameIgnoreCase(String name);
}



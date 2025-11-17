package com.stoktakip.repository;

import com.stoktakip.model.InvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplate, Long> {
    
    Optional<InvoiceTemplate> findByIsDefaultTrue();
    
    Optional<InvoiceTemplate> findByName(String name);
}


package com.stoktakip.service;

import com.stoktakip.model.ProductName;
import com.stoktakip.repository.ProductNameRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductNameService {

    private final ProductNameRepository productNameRepository;

    public ProductNameService(ProductNameRepository productNameRepository) {
        this.productNameRepository = productNameRepository;
    }

    public ProductName create(String name) {
        String normalized = normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Ürün adı giriniz.");
        }
        productNameRepository.findByNameIgnoreCase(normalized).ifPresent(existing -> {
            throw new IllegalArgumentException("Ürün adı zaten mevcut.");
        });
        ProductName productName = new ProductName();
        productName.setName(normalized);
        return productNameRepository.save(productName);
    }

    public ProductName findOrCreateByName(String name) {
        String normalized = normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Ürün adı giriniz.");
        }
        Optional<ProductName> existing = productNameRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        ProductName productName = new ProductName();
        productName.setName(normalized);
        return productNameRepository.save(productName);
    }

    @Transactional(readOnly = true)
    public List<ProductName> findAll() {
        return productNameRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProductName> findById(Long id) {
        Objects.requireNonNull(id, "Ürün adı kimliği gereklidir");
        return productNameRepository.findById(id);
    }

    private String normalize(String value) {
        return value != null ? value.trim() : null;
    }

    public void deleteById(Long id) {
        Objects.requireNonNull(id, "Ürün adı kimliği gereklidir");
        ProductName productName = productNameRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ürün adı bulunamadı"));
        if (productName.getProducts() != null && !productName.getProducts().isEmpty()) {
            throw new IllegalStateException("Bu ürün adı kullanımdadır.");
        }
        productNameRepository.delete(productName);
    }
}



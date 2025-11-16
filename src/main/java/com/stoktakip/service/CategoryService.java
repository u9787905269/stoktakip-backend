package com.stoktakip.service;

import com.stoktakip.model.Category;
import com.stoktakip.repository.CategoryRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name) {
        String normalized = normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Kategori adı giriniz.");
        }
        categoryRepository.findByNameIgnoreCase(normalized).ifPresent(existing -> {
            throw new IllegalArgumentException("Kategori zaten mevcut.");
        });
        Category category = new Category();
        category.setName(normalized);
        return categoryRepository.save(category);
    }

    public Category findOrCreateByName(String name) {
        String normalized = normalize(name);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Kategori adı giriniz.");
        }
        Optional<Category> existing = categoryRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        Category category = new Category();
        category.setName(normalized);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        Objects.requireNonNull(id, "Kategori kimliği gereklidir");
        return categoryRepository.findById(id);
    }

    private String normalize(String name) {
        return name != null ? name.trim() : null;
    }

    public void deleteById(Long id) {
        Objects.requireNonNull(id, "Kategori kimliği gereklidir");
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Kategori bulunamadı"));
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("Bu kategori ürünler tarafından kullanılıyor.");
        }
        categoryRepository.delete(category);
    }
}



package com.stoktakip.service;

import com.stoktakip.model.SerialNumber;
import com.stoktakip.repository.SerialNumberRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SerialNumberService {

    private final SerialNumberRepository serialNumberRepository;

    public SerialNumberService(SerialNumberRepository serialNumberRepository) {
        this.serialNumberRepository = serialNumberRepository;
    }

    public SerialNumber create(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Seri numarası giriniz.");
        }
        serialNumberRepository.findByValueIgnoreCase(normalized).ifPresent(existing -> {
            throw new IllegalArgumentException("Seri numarası zaten mevcut.");
        });
        SerialNumber serialNumber = new SerialNumber();
        serialNumber.setValue(normalized);
        return serialNumberRepository.save(serialNumber);
    }

    public SerialNumber findOrCreate(String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Seri numarası giriniz.");
        }
        Optional<SerialNumber> existing = serialNumberRepository.findByValueIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        SerialNumber serialNumber = new SerialNumber();
        serialNumber.setValue(normalized);
        return serialNumberRepository.save(serialNumber);
    }

    @Transactional(readOnly = true)
    public List<SerialNumber> findAll() {
        return serialNumberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SerialNumber> findById(Long id) {
        Objects.requireNonNull(id, "Seri numarası kimliği gereklidir");
        return serialNumberRepository.findById(id);
    }

    private String normalize(String value) {
        return value != null ? value.trim() : null;
    }

    public void deleteById(Long id) {
        Objects.requireNonNull(id, "Seri numarası kimliği gereklidir");
        SerialNumber serialNumber = serialNumberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Seri numarası bulunamadı"));
        if (serialNumber.getProducts() != null && !serialNumber.getProducts().isEmpty()) {
            throw new IllegalStateException("Bu seri numarası ürünler tarafından kullanılıyor.");
        }
        serialNumberRepository.delete(serialNumber);
    }
}



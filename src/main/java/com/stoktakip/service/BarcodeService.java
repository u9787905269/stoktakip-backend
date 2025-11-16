package com.stoktakip.service;

import org.springframework.stereotype.Service;

@Service
public class BarcodeService {

    public String generateBarcode(String serialNumber, Long fallbackId) {
        if (serialNumber != null && !serialNumber.isBlank()) {
            return "SN-" + serialNumber.trim();
        }
        if (fallbackId == null) {
            throw new IllegalArgumentException("Barkod üretimi için kimlik gereklidir.");
        }
        return "STK-" + String.format("%06d", fallbackId);
    }

    public String generateTemporaryBarcode(String serialNumber) {
        if (serialNumber != null && !serialNumber.isBlank()) {
            return "SN-TMP-" + serialNumber.trim();
        }
        return "TMP-" + System.nanoTime();
    }
}


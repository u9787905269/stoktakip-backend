package com.stoktakip.controller;

import com.stoktakip.model.SerialNumber;
import com.stoktakip.service.SerialNumberService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/serial-numbers")
public class SerialNumberController {

    private final SerialNumberService serialNumberService;

    public SerialNumberController(SerialNumberService serialNumberService) {
        this.serialNumberService = serialNumberService;
    }

    @GetMapping
    public ResponseEntity<List<SerialNumber>> list() {
        return ResponseEntity.ok(serialNumberService.findAll());
    }

    @PostMapping
    public ResponseEntity<SerialNumber> create(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seri numarası zorunludur.");
        }
        try {
            SerialNumber created = serialNumberService.create(value);
            return ResponseEntity
                .created(Objects.requireNonNull(URI.create("/serial-numbers/" + created.getId())))
                .body(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            serialNumberService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }
}



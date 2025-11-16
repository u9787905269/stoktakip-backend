package com.stoktakip.controller;

import com.stoktakip.model.ProductName;
import com.stoktakip.service.ProductNameService;
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
@RequestMapping("/product-names")
public class ProductNameController {

    private final ProductNameService productNameService;

    public ProductNameController(ProductNameService productNameService) {
        this.productNameService = productNameService;
    }

    @GetMapping
    public ResponseEntity<List<ProductName>> list() {
        return ResponseEntity.ok(productNameService.findAll());
    }

    @PostMapping
    public ResponseEntity<ProductName> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ürün adı zorunludur.");
        }
        try {
            ProductName created = productNameService.create(name);
            return ResponseEntity
                .created(Objects.requireNonNull(URI.create("/product-names/" + created.getId())))
                .body(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            productNameService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }
}



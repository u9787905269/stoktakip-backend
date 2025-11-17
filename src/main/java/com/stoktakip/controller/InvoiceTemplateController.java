package com.stoktakip.controller;

import com.stoktakip.dto.InvoiceTemplateRequest;
import com.stoktakip.dto.InvoiceTemplateResponse;
import com.stoktakip.service.InvoiceTemplateService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/invoice-templates")
public class InvoiceTemplateController {

    private final InvoiceTemplateService invoiceTemplateService;

    public InvoiceTemplateController(InvoiceTemplateService invoiceTemplateService) {
        this.invoiceTemplateService = invoiceTemplateService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceTemplateResponse>> listTemplates() {
        return ResponseEntity.ok(invoiceTemplateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceTemplateResponse> getTemplate(@PathVariable Long id) {
        return invoiceTemplateService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/default")
    public ResponseEntity<InvoiceTemplateResponse> getDefaultTemplate() {
        return invoiceTemplateService.findDefaultTemplate()
            .map(template -> invoiceTemplateService.findById(template.getId()))
            .flatMap(opt -> opt)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvoiceTemplateResponse> createTemplate(@RequestBody @Validated InvoiceTemplateRequest request) {
        InvoiceTemplateResponse saved = invoiceTemplateService.create(request);
        return ResponseEntity.created(Objects.requireNonNull(URI.create("/invoice-templates/" + saved.getId()))).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceTemplateResponse> updateTemplate(
        @PathVariable Long id,
        @RequestBody @Validated InvoiceTemplateRequest request
    ) {
        InvoiceTemplateResponse updated = invoiceTemplateService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        invoiceTemplateService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


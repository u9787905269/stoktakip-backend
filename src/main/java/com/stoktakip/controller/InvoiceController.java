package com.stoktakip.controller;

import com.itextpdf.text.DocumentException;
import com.stoktakip.dto.InvoiceRequest;
import com.stoktakip.dto.InvoiceResponse;
import com.stoktakip.service.InvoiceExcelService;
import com.stoktakip.service.InvoicePdfService;
import com.stoktakip.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;
    private final InvoiceExcelService invoiceExcelService;

    public InvoiceController(
        InvoiceService invoiceService,
        InvoicePdfService invoicePdfService,
        InvoiceExcelService invoiceExcelService
    ) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
        this.invoiceExcelService = invoiceExcelService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> listInvoices() {
        return ResponseEntity.ok(invoiceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return invoiceService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody @Validated InvoiceRequest request) {
        InvoiceResponse saved = invoiceService.create(request);
        return ResponseEntity.created(Objects.requireNonNull(URI.create("/invoices/" + saved.getId()))).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
        @PathVariable Long id,
        @RequestBody @Validated InvoiceRequest request
    ) {
        InvoiceResponse updated = invoiceService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public void exportPdf(
        @PathVariable Long id,
        @RequestParam(required = false) Long templateId,
        HttpServletResponse response
    ) throws IOException, DocumentException {
        byte[] pdfBytes = invoicePdfService.generatePdf(id, templateId);
        
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/{id}/excel")
    public void exportExcel(
        @PathVariable Long id,
        HttpServletResponse response
    ) throws IOException {
        byte[] excelBytes = invoiceExcelService.generateExcel(id);
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".xlsx");
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/generate-number")
    public ResponseEntity<String> generateInvoiceNumber() {
        String invoiceNumber = invoiceService.generateInvoiceNumber();
        return ResponseEntity.ok(invoiceNumber);
    }
}


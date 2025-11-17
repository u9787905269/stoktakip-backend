package com.stoktakip.service;

import com.stoktakip.dto.InvoiceItemRequest;
import com.stoktakip.dto.InvoiceItemResponse;
import com.stoktakip.dto.InvoiceRequest;
import com.stoktakip.dto.InvoiceResponse;
import com.stoktakip.model.Invoice;
import com.stoktakip.model.InvoiceItem;
import com.stoktakip.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceResponse create(InvoiceRequest request) {
        // Fatura numarası kontrolü
        if (request.getInvoiceNumber() == null || request.getInvoiceNumber().isBlank()) {
            request.setInvoiceNumber(generateInvoiceNumber());
        } else {
            Optional<Invoice> existing = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Bu fatura numarası zaten kullanılıyor: " + request.getInvoiceNumber());
            }
        }

        Invoice invoice = mapRequestToEntity(request, new Invoice());
        calculateInvoiceTotals(invoice);
        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    public InvoiceResponse update(Long id, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Fatura bulunamadı"));
        
        // Fatura numarası değişmişse kontrol et
        if (!Objects.equals(invoice.getInvoiceNumber(), request.getInvoiceNumber())) {
            Optional<Invoice> existing = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("Bu fatura numarası zaten kullanılıyor: " + request.getInvoiceNumber());
            }
        }

        // Mevcut item'ları sil
        invoice.getItems().clear();
        
        mapRequestToEntity(request, invoice);
        calculateInvoiceTotals(invoice);
        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findAll() {
        try {
            List<Invoice> invoices = invoiceRepository.findAllOrderByInvoiceDateDesc();
            if (invoices == null || invoices.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return invoices.stream()
                .map(this::toResponse)
                .toList();
        } catch (Exception e) {
            // Eğer JOIN FETCH ile sorun olursa, normal findAll kullan
            List<Invoice> invoices = invoiceRepository.findAll();
            return invoices.stream()
                .sorted((a, b) -> {
                    if (a.getInvoiceDate() == null && b.getInvoiceDate() == null) return 0;
                    if (a.getInvoiceDate() == null) return 1;
                    if (b.getInvoiceDate() == null) return -1;
                    return b.getInvoiceDate().compareTo(a.getInvoiceDate());
                })
                .map(inv -> {
                    // Items'ı yükle
                    if (inv.getItems() != null) {
                        inv.getItems().size(); // Lazy load trigger
                    }
                    return this.toResponse(inv);
                })
                .toList();
        }
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceResponse> findById(Long id) {
        return invoiceRepository.findById(id)
            .map(inv -> {
                // Items'ı yükle
                if (inv.getItems() != null) {
                    inv.getItems().size(); // Lazy load trigger
                }
                return this.toResponse(inv);
            });
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findEntityById(Long id) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(id);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            // Items'ı yükle
            if (invoice.getItems() != null) {
                invoice.getItems().size(); // Lazy load trigger
            }
        }
        return invoiceOpt;
    }

    public void deleteById(Long id) {
        invoiceRepository.deleteById(id);
    }

    public String generateInvoiceNumber() {
        try {
            String prefix = "INV-";
            String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
            String prefixWithYear = prefix + year + "-";
            
            Integer maxNumber = invoiceRepository.findMaxInvoiceNumber(prefixWithYear, prefixWithYear.length());
            int nextNumber = (maxNumber != null && maxNumber > 0 ? maxNumber : 0) + 1;
            
            return prefixWithYear + String.format("%06d", nextNumber);
        } catch (Exception e) {
            // Fallback: eğer query başarısız olursa timestamp kullan
            String prefix = "INV-";
            String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
            return prefix + year + "-" + System.currentTimeMillis() % 1000000;
        }
    }

    private Invoice mapRequestToEntity(InvoiceRequest request, Invoice invoice) {
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerAddress(request.getCustomerAddress());
        invoice.setCustomerTaxNumber(request.getCustomerTaxNumber());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setCustomerPhone(request.getCustomerPhone());
        
        invoice.setSellerName(request.getSellerName());
        invoice.setSellerAddress(request.getSellerAddress());
        invoice.setSellerTaxNumber(request.getSellerTaxNumber());
        invoice.setSellerEmail(request.getSellerEmail());
        invoice.setSellerPhone(request.getSellerPhone());
        invoice.setSellerBankAccount(request.getSellerBankAccount());
        
        invoice.setTaxRate(Optional.ofNullable(request.getTaxRate()).orElse(0));
        invoice.setDiscountAmount(Optional.ofNullable(request.getDiscountAmount()).orElse(BigDecimal.ZERO));
        invoice.setNotes(request.getNotes());
        invoice.setTerms(request.getTerms());
        invoice.setStatus(Optional.ofNullable(request.getStatus()).orElse("DRAFT"));

        // Item'ları ekle
        if (request.getItems() != null) {
            List<InvoiceItemRequest> itemRequests = request.getItems();
            IntStream.range(0, itemRequests.size()).forEach(i -> {
                InvoiceItemRequest itemRequest = itemRequests.get(i);
                InvoiceItem item = mapItemRequestToEntity(itemRequest, new InvoiceItem());
                item.setInvoice(invoice);
                item.setItemNumber(item.getItemNumber() != null ? item.getItemNumber() : i + 1);
                invoice.getItems().add(item);
            });
        }

        return invoice;
    }

    private InvoiceItem mapItemRequestToEntity(InvoiceItemRequest request, InvoiceItem item) {
        item.setProductName(request.getProductName());
        item.setProductCode(request.getProductCode());
        item.setDescription(request.getDescription());
        item.setQuantity(Optional.ofNullable(request.getQuantity()).orElse(1));
        item.setUnitPrice(Optional.ofNullable(request.getUnitPrice()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        item.setTaxRate(Optional.ofNullable(request.getTaxRate()).orElse(0));
        item.setDiscountPercent(Optional.ofNullable(request.getDiscountPercent()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        
        calculateItemTotals(item);
        return item;
    }

    private void calculateItemTotals(InvoiceItem item) {
        BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
        BigDecimal unitPrice = item.getUnitPrice();
        
        // İndirim hesaplama
        BigDecimal discountPercent = item.getDiscountPercent();
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal discountedUnitPrice = unitPrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        
        // Subtotal (indirim sonrası)
        BigDecimal subtotal = discountedUnitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        
        // KDV hesaplama
        BigDecimal taxRate = BigDecimal.valueOf(item.getTaxRate()).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        
        // Total (subtotal + KDV)
        BigDecimal totalAmount = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        
        item.setSubtotal(subtotal);
        item.setTaxAmount(taxAmount);
        item.setTotalAmount(totalAmount);
    }

    private void calculateInvoiceTotals(Invoice invoice) {
        if (invoice == null) {
            return;
        }
        
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            invoice.setSubtotal(BigDecimal.ZERO);
            invoice.setTaxAmount(BigDecimal.ZERO);
            invoice.setTotalAmount(BigDecimal.ZERO);
            return;
        }
        
        BigDecimal subtotal = invoice.getItems().stream()
            .map(InvoiceItem::getSubtotal)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalTaxAmount = invoice.getItems().stream()
            .map(InvoiceItem::getTaxAmount)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal discountAmount = Optional.ofNullable(invoice.getDiscountAmount()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalAmount = subtotal.add(totalTaxAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(totalTaxAmount);
        invoice.setTotalAmount(totalAmount);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDueDate(invoice.getDueDate());
        
        response.setCustomerName(invoice.getCustomerName());
        response.setCustomerAddress(invoice.getCustomerAddress());
        response.setCustomerTaxNumber(invoice.getCustomerTaxNumber());
        response.setCustomerEmail(invoice.getCustomerEmail());
        response.setCustomerPhone(invoice.getCustomerPhone());
        
        response.setSellerName(invoice.getSellerName());
        response.setSellerAddress(invoice.getSellerAddress());
        response.setSellerTaxNumber(invoice.getSellerTaxNumber());
        response.setSellerEmail(invoice.getSellerEmail());
        response.setSellerPhone(invoice.getSellerPhone());
        response.setSellerBankAccount(invoice.getSellerBankAccount());
        
        response.setSubtotal(invoice.getSubtotal());
        response.setTaxRate(invoice.getTaxRate());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        
        response.setNotes(invoice.getNotes());
        response.setTerms(invoice.getTerms());
        response.setStatus(invoice.getStatus());
        
        if (invoice.getItems() != null) {
            response.setItems(invoice.getItems().stream()
                .map(this::toItemResponse)
                .toList());
        } else {
            response.setItems(new java.util.ArrayList<>());
        }
        
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());
        
        return response;
    }

    private InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceItemResponse response = new InvoiceItemResponse();
        response.setId(item.getId());
        response.setItemNumber(item.getItemNumber());
        response.setProductName(item.getProductName());
        response.setProductCode(item.getProductCode());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTaxRate(item.getTaxRate());
        response.setDiscountPercent(item.getDiscountPercent());
        response.setSubtotal(item.getSubtotal());
        response.setTaxAmount(item.getTaxAmount());
        response.setTotalAmount(item.getTotalAmount());
        return response;
    }
}


package com.stoktakip.service;

import com.stoktakip.dto.InvoiceTemplateRequest;
import com.stoktakip.dto.InvoiceTemplateResponse;
import com.stoktakip.model.InvoiceTemplate;
import com.stoktakip.repository.InvoiceTemplateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceTemplateService {

    private final InvoiceTemplateRepository invoiceTemplateRepository;

    public InvoiceTemplateService(InvoiceTemplateRepository invoiceTemplateRepository) {
        this.invoiceTemplateRepository = invoiceTemplateRepository;
    }

    public InvoiceTemplateResponse create(InvoiceTemplateRequest request) {
        // Eğer default olarak işaretleniyorsa, diğer template'leri güncelle
        if (request.getIsDefault() != null && request.getIsDefault()) {
            invoiceTemplateRepository.findByIsDefaultTrue()
                .ifPresent(template -> {
                    template.setIsDefault(false);
                    invoiceTemplateRepository.save(template);
                });
        }

        InvoiceTemplate template = mapRequestToEntity(request, new InvoiceTemplate());
        InvoiceTemplate saved = invoiceTemplateRepository.save(template);
        return toResponse(saved);
    }

    public InvoiceTemplateResponse update(Long id, InvoiceTemplateRequest request) {
        InvoiceTemplate template = invoiceTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Fatura şablonu bulunamadı"));

        // Eğer default olarak işaretleniyorsa, diğer template'leri güncelle
        if (request.getIsDefault() != null && request.getIsDefault()) {
            invoiceTemplateRepository.findByIsDefaultTrue()
                .filter(t -> !t.getId().equals(id))
                .ifPresent(t -> {
                    t.setIsDefault(false);
                    invoiceTemplateRepository.save(t);
                });
        }

        InvoiceTemplate updated = mapRequestToEntity(request, template);
        InvoiceTemplate saved = invoiceTemplateRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceTemplateResponse> findAll() {
        return invoiceTemplateRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceTemplateResponse> findById(Long id) {
        return invoiceTemplateRepository.findById(id)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceTemplate> findEntityById(Long id) {
        return invoiceTemplateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceTemplate> findDefaultTemplate() {
        return invoiceTemplateRepository.findByIsDefaultTrue();
    }

    public void deleteById(Long id) {
        invoiceTemplateRepository.deleteById(id);
    }

    private InvoiceTemplate mapRequestToEntity(InvoiceTemplateRequest request, InvoiceTemplate template) {
        template.setName(request.getName());
        template.setIsDefault(Optional.ofNullable(request.getIsDefault()).orElse(false));
        template.setHeaderHtml(request.getHeaderHtml());
        template.setFooterHtml(request.getFooterHtml());
        template.setLogoUrl(request.getLogoUrl());
        template.setPrimaryColor(request.getPrimaryColor());
        template.setSecondaryColor(request.getSecondaryColor());
        template.setFontFamily(request.getFontFamily());
        template.setFontSize(request.getFontSize());
        template.setShowLogo(Optional.ofNullable(request.getShowLogo()).orElse(true));
        template.setShowBorder(Optional.ofNullable(request.getShowBorder()).orElse(true));
        template.setShowQrCode(Optional.ofNullable(request.getShowQrCode()).orElse(false));
        template.setCustomFields(request.getCustomFields());
        return template;
    }

    private InvoiceTemplateResponse toResponse(InvoiceTemplate template) {
        InvoiceTemplateResponse response = new InvoiceTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setIsDefault(template.getIsDefault());
        response.setHeaderHtml(template.getHeaderHtml());
        response.setFooterHtml(template.getFooterHtml());
        response.setLogoUrl(template.getLogoUrl());
        response.setPrimaryColor(template.getPrimaryColor());
        response.setSecondaryColor(template.getSecondaryColor());
        response.setFontFamily(template.getFontFamily());
        response.setFontSize(template.getFontSize());
        response.setShowLogo(template.getShowLogo());
        response.setShowBorder(template.getShowBorder());
        response.setShowQrCode(template.getShowQrCode());
        response.setCustomFields(template.getCustomFields());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }
}


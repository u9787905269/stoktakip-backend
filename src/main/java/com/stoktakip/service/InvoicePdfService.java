package com.stoktakip.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.stoktakip.model.Invoice;
import com.stoktakip.model.InvoiceItem;
import com.stoktakip.model.InvoiceTemplate;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfService {

    private final InvoiceService invoiceService;
    private final InvoiceTemplateService invoiceTemplateService;

    public InvoicePdfService(InvoiceService invoiceService, InvoiceTemplateService invoiceTemplateService) {
        this.invoiceService = invoiceService;
        this.invoiceTemplateService = invoiceTemplateService;
    }

    public byte[] generatePdf(Long invoiceId, Long templateId) throws DocumentException {
        Invoice invoice = invoiceService.findEntityById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Fatura bulunamadı"));
        
        Optional<InvoiceTemplate> templateOpt = templateId != null 
            ? invoiceTemplateService.findEntityById(templateId)
            : invoiceTemplateService.findDefaultTemplate();
        
        InvoiceTemplate template = templateOpt.orElse(null);

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Logo ve Başlık
        if (template != null && template.getShowLogo() != null && template.getShowLogo() && template.getLogoUrl() != null) {
            // Logo eklenebilir (şimdilik atlanıyor)
        }

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        if (template != null && template.getPrimaryColor() != null) {
            try {
                BaseColor primaryColor = parseColor(template.getPrimaryColor());
                titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryColor);
            } catch (Exception e) {
                // Renk parse edilemezse varsayılan kullan
            }
        }
        
        Paragraph title = new Paragraph("FATURA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Fatura Bilgileri - Label ve Değer Yan Yana
        Font infoLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font infoValueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        // Fatura No
        Phrase invoiceNoPhrase = new Phrase("Fatura No: ", infoLabelFont);
        invoiceNoPhrase.add(new Phrase(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "-", infoValueFont));
        Paragraph invoiceNoPara = new Paragraph(invoiceNoPhrase);
        invoiceNoPara.setSpacingAfter(15);
        document.add(invoiceNoPara);
        
        // Fatura Tarihi
        Phrase invoiceDatePhrase = new Phrase("Fatura Tarihi: ", infoLabelFont);
        invoiceDatePhrase.add(new Phrase(formatDate(invoice.getInvoiceDate()), infoValueFont));
        Paragraph invoiceDatePara = new Paragraph(invoiceDatePhrase);
        invoiceDatePara.setSpacingAfter(15);
        document.add(invoiceDatePara);
        
        // Vade Tarihi
        Phrase dueDatePhrase = new Phrase("Vade Tarihi: ", infoLabelFont);
        dueDatePhrase.add(new Phrase(formatDate(invoice.getDueDate()), infoValueFont));
        Paragraph dueDatePara = new Paragraph(dueDatePhrase);
        dueDatePara.setSpacingAfter(15);
        document.add(dueDatePara);
        
        // Durum
        Phrase statusPhrase = new Phrase("Durum: ", infoLabelFont);
        statusPhrase.add(new Phrase(invoice.getStatus() != null ? invoice.getStatus() : "-", infoValueFont));
        Paragraph statusPara = new Paragraph(statusPhrase);
        statusPara.setSpacingAfter(15);
        document.add(statusPara);

        // Satıcı ve Müşteri Bilgileri
        PdfPTable partiesTable = new PdfPTable(2);
        partiesTable.setWidthPercentage(100);
        partiesTable.setSpacingAfter(20);

        // Satıcı Bilgileri - Sol Hizalı
        PdfPCell sellerCell = new PdfPCell();
        sellerCell.setBorder(PdfPCell.NO_BORDER);
        sellerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        sellerCell.setVerticalAlignment(Element.ALIGN_TOP);
        sellerCell.setPadding(5);
        
        Paragraph sellerTitle = new Paragraph("SATICI", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        sellerTitle.setAlignment(Element.ALIGN_LEFT);
        sellerCell.addElement(sellerTitle);
        
        if (invoice.getSellerName() != null) {
            Paragraph p = new Paragraph(invoice.getSellerName());
            p.setAlignment(Element.ALIGN_LEFT);
            sellerCell.addElement(p);
        }
        if (invoice.getSellerAddress() != null) {
            Paragraph p = new Paragraph(invoice.getSellerAddress());
            p.setAlignment(Element.ALIGN_LEFT);
            sellerCell.addElement(p);
        }
        if (invoice.getSellerTaxNumber() != null) {
            Paragraph p = new Paragraph(invoice.getSellerTaxNumber());
            p.setAlignment(Element.ALIGN_LEFT);
            sellerCell.addElement(p);
        }

        // Müşteri Bilgileri - Sağ Hizalı
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(PdfPCell.NO_BORDER);
        customerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        customerCell.setVerticalAlignment(Element.ALIGN_TOP);
        customerCell.setPadding(5);
        
        Paragraph customerTitle = new Paragraph("ALICI", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        customerTitle.setAlignment(Element.ALIGN_RIGHT);
        customerCell.addElement(customerTitle);
        
        if (invoice.getCustomerName() != null) {
            Paragraph p = new Paragraph(invoice.getCustomerName());
            p.setAlignment(Element.ALIGN_RIGHT);
            customerCell.addElement(p);
        }
        if (invoice.getCustomerAddress() != null) {
            Paragraph p = new Paragraph(invoice.getCustomerAddress());
            p.setAlignment(Element.ALIGN_RIGHT);
            customerCell.addElement(p);
        }
        if (invoice.getCustomerTaxNumber() != null) {
            Paragraph p = new Paragraph(invoice.getCustomerTaxNumber());
            p.setAlignment(Element.ALIGN_RIGHT);
            customerCell.addElement(p);
        }

        partiesTable.addCell(sellerCell);
        partiesTable.addCell(customerCell);
        document.add(partiesTable);

        // Fatura Kalemleri Tablosu
        PdfPTable itemsTable = new PdfPTable(8);
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingAfter(20);

        float[] columnWidths = {0.5f, 2f, 1f, 1f, 1f, 1f, 1f, 1.5f};
        itemsTable.setWidths(columnWidths);

        // Başlık satırı
        addHeaderCell(itemsTable, "Sıra", template);
        addHeaderCell(itemsTable, "Ürün Adı", template);
        addHeaderCell(itemsTable, "Ürün Kodu", template);
        addHeaderCell(itemsTable, "Miktar", template);
        addHeaderCell(itemsTable, "Birim Fiyat", template);
        addHeaderCell(itemsTable, "KDV %", template);
        addHeaderCell(itemsTable, "KDV Tutarı", template);
        addHeaderCell(itemsTable, "Toplam", template);

        // İçerik satırları
        List<InvoiceItem> items = invoice.getItems();
        for (InvoiceItem item : items) {
            addDataCell(itemsTable, String.valueOf(item.getItemNumber()), template);
            addDataCell(itemsTable, item.getProductName(), template);
            addDataCell(itemsTable, item.getProductCode() != null ? item.getProductCode() : "-", template);
            addDataCell(itemsTable, String.valueOf(item.getQuantity()), template);
            addDataCell(itemsTable, formatCurrency(item.getUnitPrice()), template);
            addDataCell(itemsTable, "%" + item.getTaxRate(), template);
            addDataCell(itemsTable, formatCurrency(item.getTaxAmount()), template);
            addDataCell(itemsTable, formatCurrency(item.getTotalAmount()), template);
        }

        document.add(itemsTable);

        // Toplam Bilgileri
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingAfter(20);

        addTotalRow(totalsTable, "Ara Toplam:", formatCurrency(invoice.getSubtotal()), template);
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "İndirim:", formatCurrency(invoice.getDiscountAmount()), template);
        }
        addTotalRow(totalsTable, "KDV Toplamı:", formatCurrency(invoice.getTaxAmount()), template);
        
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        if (template != null && template.getPrimaryColor() != null) {
            try {
                BaseColor primaryColor = parseColor(template.getPrimaryColor());
                totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, primaryColor);
            } catch (Exception e) {
                // Renk parse edilemezse varsayılan kullan
            }
        }
        addTotalRow(totalsTable, "GENEL TOPLAM:", formatCurrency(invoice.getTotalAmount()), template, totalFont);

        document.add(totalsTable);

        // Notlar
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
            Paragraph notes = new Paragraph("Notlar: " + invoice.getNotes());
            notes.setSpacingAfter(10);
            document.add(notes);
        }

        // Şartlar
        //if (invoice.getTerms() != null && !invoice.getTerms().isBlank()) {
         //   Paragraph terms = new Paragraph(" Şartlar: " + invoice.getTerms());
         //   document.add(terms);
       // }

        document.close();
        return baos.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, InvoiceTemplate template) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        BaseColor bgColor = BaseColor.LIGHT_GRAY;
        
        if (template != null && template.getPrimaryColor() != null) {
            try {
                bgColor = parseColor(template.getPrimaryColor());
            } catch (Exception e) {
                // Renk parse edilemezse varsayılan kullan
            }
        }
        
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, InvoiceTemplate template) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-"));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, InvoiceTemplate template) {
        addTotalRow(table, label, value, template, FontFactory.getFont(FontFactory.HELVETICA, 10));
    }

    private void addTotalRow(PdfPTable table, String label, String value, InvoiceTemplate template, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return "-";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0,00 ₺";
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount) + " ₺";
    }

    private BaseColor parseColor(String color) {
        if (color == null || color.isEmpty()) {
            return BaseColor.BLACK;
        }
        String hex = color.startsWith("#") ? color.substring(1) : color;
        if (hex.length() == 6) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new BaseColor(r, g, b);
        }
        return BaseColor.BLACK;
    }
}


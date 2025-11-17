package com.stoktakip.service;

import com.stoktakip.model.Invoice;
import com.stoktakip.model.InvoiceItem;
import com.stoktakip.util.InvoiceTranslations;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class InvoiceExcelService {

    private final InvoiceService invoiceService;

    public InvoiceExcelService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public byte[] generateExcel(Long invoiceId, String locale) throws IOException {
        Invoice invoice = invoiceService.findEntityById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Fatura bulunamadı"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(InvoiceTranslations.translate(locale, "invoice_title"));

        int rowNum = 0;

        // Başlık
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(InvoiceTranslations.translate(locale, "invoice_title"));
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

        // Fatura Bilgileri
        rowNum++;
        createInfoRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "invoice_number"), invoice.getInvoiceNumber());
        createInfoRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "invoice_date"), 
            invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "-");
        createInfoRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "due_date"), 
            invoice.getDueDate() != null ? invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "-");
        createInfoRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "status"), 
            invoice.getStatus() != null ? InvoiceTranslations.translateStatus(locale, invoice.getStatus()) : "-");

        rowNum++;

        // Satıcı ve Müşteri Bilgileri
        Row sellerRow = sheet.createRow(rowNum++);
        Cell sellerLabel = sellerRow.createCell(0);
        sellerLabel.setCellValue(InvoiceTranslations.translate(locale, "seller"));
        Font sellerFont = workbook.createFont();
        sellerFont.setBold(true);
        sellerFont.setFontHeightInPoints((short) 12);
        CellStyle sellerLabelStyle = workbook.createCellStyle();
        sellerLabelStyle.setFont(sellerFont);
        sellerLabel.setCellStyle(sellerLabelStyle);

        if (invoice.getSellerName() != null) createInfoRow(sheet, rowNum++, null, invoice.getSellerName());
        if (invoice.getSellerAddress() != null) createInfoRow(sheet, rowNum++, null, invoice.getSellerAddress());
        if (invoice.getSellerTaxNumber() != null) createInfoRow(sheet, rowNum++, null, InvoiceTranslations.translate(locale, "tax_number") + invoice.getSellerTaxNumber());
        if (invoice.getSellerPhone() != null) createInfoRow(sheet, rowNum++, null, "Tel: " + invoice.getSellerPhone());
        if (invoice.getSellerEmail() != null) createInfoRow(sheet, rowNum++, null, "Email: " + invoice.getSellerEmail());

        rowNum += 2;

        Row customerRow = sheet.createRow(rowNum++);
        Cell customerLabel = customerRow.createCell(4);
        customerLabel.setCellValue(InvoiceTranslations.translate(locale, "customer"));
        Font customerFont = workbook.createFont();
        customerFont.setBold(true);
        customerFont.setFontHeightInPoints((short) 12);
        CellStyle customerLabelStyle = workbook.createCellStyle();
        customerLabelStyle.setFont(customerFont);
        customerLabel.setCellStyle(customerLabelStyle);

        if (invoice.getCustomerName() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(4);
            cell.setCellValue(invoice.getCustomerName());
        }
        if (invoice.getCustomerAddress() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(4);
            cell.setCellValue(invoice.getCustomerAddress());
        }
        if (invoice.getCustomerTaxNumber() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(4);
            cell.setCellValue(InvoiceTranslations.translate(locale, "tax_number") + invoice.getCustomerTaxNumber());
        }
        if (invoice.getCustomerPhone() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(4);
            cell.setCellValue("Tel: " + invoice.getCustomerPhone());
        }
        if (invoice.getCustomerEmail() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(4);
            cell.setCellValue("Email: " + invoice.getCustomerEmail());
        }

        rowNum++;

        // Fatura Kalemleri Başlığı
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            InvoiceTranslations.translate(locale, "row"),
            InvoiceTranslations.translate(locale, "product_name"),
            InvoiceTranslations.translate(locale, "product_code"),
            InvoiceTranslations.translate(locale, "quantity"),
            InvoiceTranslations.translate(locale, "unit_price"),
            InvoiceTranslations.translate(locale, "tax_rate"),
            InvoiceTranslations.translate(locale, "tax_amount"),
            InvoiceTranslations.translate(locale, "total")
        };
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 200, (byte) 200, (byte) 200}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Fatura Kalemleri
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (InvoiceItem item : invoice.getItems()) {
            Row row = sheet.createRow(rowNum++);
            
            createCell(row, 0, String.valueOf(item.getItemNumber()), dataStyle);
            createCell(row, 1, item.getProductName(), dataStyle);
            createCell(row, 2, item.getProductCode() != null ? item.getProductCode() : "-", dataStyle);
            createCell(row, 3, String.valueOf(item.getQuantity()), numberStyle);
            createCell(row, 4, item.getUnitPrice().doubleValue(), currencyStyle);
            createCell(row, 5, "%" + item.getTaxRate(), dataStyle);
            createCell(row, 6, item.getTaxAmount().doubleValue(), currencyStyle);
            createCell(row, 7, item.getTotalAmount().doubleValue(), currencyStyle);
        }

        rowNum++;

        // Toplam Bilgileri
        CellStyle totalLabelStyle = workbook.createCellStyle();
        totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
        Font totalLabelFont = workbook.createFont();
        totalLabelFont.setBold(true);
        totalLabelStyle.setFont(totalLabelFont);

        CellStyle totalValueStyle = workbook.createCellStyle();
        totalValueStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalValueStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        Font totalValueFont = workbook.createFont();
        totalValueFont.setBold(true);
        totalValueStyle.setFont(totalValueFont);

        createTotalRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "subtotal"), invoice.getSubtotal(), totalLabelStyle, totalValueStyle);
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            createTotalRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "discount"), invoice.getDiscountAmount(), totalLabelStyle, totalValueStyle);
        }
        createTotalRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "tax_total"), invoice.getTaxAmount(), totalLabelStyle, totalValueStyle);
        
        CellStyle grandTotalStyle = workbook.createCellStyle();
        grandTotalStyle.setAlignment(HorizontalAlignment.RIGHT);
        grandTotalStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        Font grandTotalFont = workbook.createFont();
        grandTotalFont.setBold(true);
        grandTotalFont.setFontHeightInPoints((short) 12);
        grandTotalFont.setColor(new XSSFColor(new byte[]{(byte) 0, (byte) 0, (byte) 255}, null).getIndex());
        grandTotalStyle.setFont(grandTotalFont);
        
        createTotalRow(sheet, rowNum++, InvoiceTranslations.translate(locale, "grand_total"), invoice.getTotalAmount(), totalLabelStyle, grandTotalStyle);

        // Notlar
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
            rowNum++;
            Row notesRow = sheet.createRow(rowNum++);
            Cell notesCell = notesRow.createCell(0);
            notesCell.setCellValue(InvoiceTranslations.translate(locale, "notes") + invoice.getNotes());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        }

        // Şartlar
        if (invoice.getTerms() != null && !invoice.getTerms().isBlank()) {
            rowNum++;
            Row termsRow = sheet.createRow(rowNum++);
            Cell termsCell = termsRow.createCell(0);
            termsCell.setCellValue(InvoiceTranslations.translate(locale, "terms") + invoice.getTerms());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));
        }

        // Kolon genişliklerini ayarla
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 2000));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private void createInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        if (label != null) {
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(label);
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            style.setFont(font);
            labelCell.setCellStyle(style);
            
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(value != null ? value : "-");
        } else {
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(value != null ? value : "-");
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private void createTotalRow(Sheet sheet, int rowNum, String label, BigDecimal amount, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(6);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(7);
        valueCell.setCellValue(amount.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }
}


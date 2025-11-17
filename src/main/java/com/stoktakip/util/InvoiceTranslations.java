package com.stoktakip.util;

import java.util.HashMap;
import java.util.Map;

public class InvoiceTranslations {
    
    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    
    static {
        // Türkçe çeviriler
        Map<String, String> tr = new HashMap<>();
        tr.put("invoice_title", "FATURA");
        tr.put("invoice_number", "Fatura No: ");
        tr.put("invoice_date", "Fatura Tarihi: ");
        tr.put("due_date", "Vade Tarihi: ");
        tr.put("status", "Durum: ");
        tr.put("seller", "SATICI");
        tr.put("customer", "ALICI");
        tr.put("items", "Fatura Kalemleri");
        tr.put("row", "Sıra");
        tr.put("product_name", "Ürün Adı");
        tr.put("product_code", "Ürün Kodu");
        tr.put("quantity", "Miktar");
        tr.put("unit_price", "Birim Fiyat");
        tr.put("tax_rate", "KDV %");
        tr.put("tax_amount", "KDV Tutarı");
        tr.put("total", "Toplam");
        tr.put("subtotal", "Ara Toplam:");
        tr.put("discount", "İndirim:");
        tr.put("tax_total", "KDV Toplamı:");
        tr.put("grand_total", "GENEL TOPLAM:");
        tr.put("notes", "Notlar: ");
        tr.put("terms", "Şartlar: ");
        tr.put("tax_number", "Vergi No: ");
        translations.put("tr", tr);
        
        // İngilizce çeviriler
        Map<String, String> en = new HashMap<>();
        en.put("invoice_title", "INVOICE");
        en.put("invoice_number", "Invoice No: ");
        en.put("invoice_date", "Invoice Date: ");
        en.put("due_date", "Due Date: ");
        en.put("status", "Status: ");
        en.put("seller", "SELLER");
        en.put("customer", "CUSTOMER");
        en.put("items", "Invoice Items");
        en.put("row", "No");
        en.put("product_name", "Product Name");
        en.put("product_code", "Product Code");
        en.put("quantity", "Quantity");
        en.put("unit_price", "Unit Price");
        en.put("tax_rate", "VAT %");
        en.put("tax_amount", "VAT Amount");
        en.put("total", "Total");
        en.put("subtotal", "Subtotal:");
        en.put("discount", "Discount:");
        en.put("tax_total", "VAT Total:");
        en.put("grand_total", "GRAND TOTAL:");
        en.put("notes", "Notes: ");
        en.put("terms", "Terms: ");
        en.put("tax_number", "Tax No: ");
        translations.put("en", en);
        
        // Flemenkçe çeviriler
        Map<String, String> nl = new HashMap<>();
        nl.put("invoice_title", "FACTUUR");
        nl.put("invoice_number", "Factuurnummer: ");
        nl.put("invoice_date", "Factuurdatum: ");
        nl.put("due_date", "Vervaldatum: ");
        nl.put("status", "Status: ");
        nl.put("seller", "VERKOPER");
        nl.put("customer", "KLANT");
        nl.put("items", "Factuurregels");
        nl.put("row", "Nr");
        nl.put("product_name", "Productnaam");
        nl.put("product_code", "Productcode");
        nl.put("quantity", "Hoeveelheid");
        nl.put("unit_price", "Stukprijs");
        nl.put("tax_rate", "BTW %");
        nl.put("tax_amount", "BTW Bedrag");
        nl.put("total", "Totaal");
        nl.put("subtotal", "Subtotaal:");
        nl.put("discount", "Korting:");
        nl.put("tax_total", "BTW Totaal:");
        nl.put("grand_total", "TOTAAL:");
        nl.put("notes", "Notities: ");
        nl.put("terms", "Voorwaarden: ");
        nl.put("tax_number", "BTW-nummer: ");
        translations.put("nl", nl);
    }
    
    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isEmpty()) {
            return "tr";
        }
        String normalized = locale.toLowerCase();
        // "tr-TR" -> "tr", "en-US" -> "en", "nl-NL" -> "nl"
        if (normalized.contains("-")) {
            normalized = normalized.substring(0, normalized.indexOf("-"));
        }
        // Sadece "tr", "en", "nl" destekleniyor
        if (normalized.equals("tr") || normalized.equals("en") || normalized.equals("nl")) {
            return normalized;
        }
        return "tr"; // Varsayılan olarak Türkçe
    }
    
    public static String translate(String locale, String key) {
        String normalizedLocale = normalizeLocale(locale);
        Map<String, String> lang = translations.getOrDefault(normalizedLocale, translations.get("tr"));
        return lang.getOrDefault(key, key);
    }
    
    public static String translateStatus(String locale, String status) {
        if (status == null) return "";
        
        Map<String, Map<String, String>> statusTranslations = new HashMap<>();
        
        Map<String, String> trStatus = new HashMap<>();
        trStatus.put("DRAFT", "Taslak");
        trStatus.put("SENT", "Gönderildi");
        trStatus.put("PAID", "Ödendi");
        trStatus.put("CANCELLED", "İptal");
        statusTranslations.put("tr", trStatus);
        
        Map<String, String> enStatus = new HashMap<>();
        enStatus.put("DRAFT", "Draft");
        enStatus.put("SENT", "Sent");
        enStatus.put("PAID", "Paid");
        enStatus.put("CANCELLED", "Cancelled");
        statusTranslations.put("en", enStatus);
        
        Map<String, String> nlStatus = new HashMap<>();
        nlStatus.put("DRAFT", "Concept");
        nlStatus.put("SENT", "Verzonden");
        nlStatus.put("PAID", "Betaald");
        nlStatus.put("CANCELLED", "Geannuleerd");
        statusTranslations.put("nl", nlStatus);
        
        String normalizedLocale = normalizeLocale(locale);
        Map<String, String> lang = statusTranslations.getOrDefault(normalizedLocale, statusTranslations.get("tr"));
        return lang.getOrDefault(status, status);
    }
}


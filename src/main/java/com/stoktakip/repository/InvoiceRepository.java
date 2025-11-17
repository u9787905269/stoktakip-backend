package com.stoktakip.repository;

import com.stoktakip.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByStatus(String status);
    
    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.items ORDER BY i.invoiceDate DESC")
    List<Invoice> findAllOrderByInvoiceDateDesc();
    
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoice_number, :prefixLength + 1) AS INTEGER)), 0) FROM invoices i WHERE i.invoice_number LIKE :prefix || '%'", nativeQuery = true)
    Integer findMaxInvoiceNumber(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
}


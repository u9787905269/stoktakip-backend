package com.stoktakip.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DatabaseMigrationConfig - Custom migration fallback
 * 
 * NOTE: This is now DISABLED because we're using Flyway for migrations.
 * Flyway will automatically run SQL migrations from src/main/resources/db/migration/
 * 
 * If you need to disable Flyway and use this instead, uncomment this class.
 */
//@Configuration
public class DatabaseMigrationConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Skip if Flyway is enabled
        if (flywayEnabled) {
            System.out.println("ℹ Flyway is enabled, skipping custom migration config");
            return;
        }
        try {
            // Check if invoices table exists
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_name = 'invoices'";
            
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableCount == null || tableCount == 0) {
                // Table doesn't exist, let Hibernate create it
                System.out.println("⚠ invoices table does not exist. Hibernate will create it on next startup.");
                return;
            }
            
            // Check and add missing columns
            String[] columnsToCheck = {
                "invoice_date", "DATE",
                "due_date", "DATE",
                "discount_amount", "NUMERIC(12, 2) NOT NULL DEFAULT 0"
            };
            
            for (int i = 0; i < columnsToCheck.length; i += 2) {
                String columnName = columnsToCheck[i];
                String columnType = columnsToCheck[i + 1];
                
                String checkColumnSql = "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_name = 'invoices' AND column_name = ?";
                
                Integer columnCount = jdbcTemplate.queryForObject(checkColumnSql, Integer.class, columnName);
                
                if (columnCount == null || columnCount == 0) {
                    String addColumnSql = "ALTER TABLE invoices ADD COLUMN " + columnName + " " + columnType;
                    jdbcTemplate.execute(addColumnSql);
                    System.out.println("✓ Added " + columnName + " column to invoices table");
                }
            }
            
            System.out.println("✓ Database migration check completed");
        } catch (Exception e) {
            System.err.println("⚠ Error during database migration: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the application if migration fails
        }
    }
}


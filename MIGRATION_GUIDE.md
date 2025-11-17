# Database Migration Rehberi

## Flyway Migration Sistemi

Bu proje artık **Flyway** kullanarak database migration'ları yönetiyor.

## Migration Dosyaları

Migration dosyaları `src/main/resources/db/migration/` klasöründe bulunur.

### Migration Dosya Adlandırma

Flyway migration dosyaları şu formatta adlandırılmalıdır:
- `V{sürüm}__{açıklama}.sql`

Örnek:
- `V1__Initial_invoice_schema.sql`
- `V2__Add_discount_amount_column.sql`
- `V3__Add_new_feature.sql`

**Önemli:**
- Versiyon numarası sıralı olmalı (V1, V2, V3, ...)
- İki alt çizgi (`__`) zorunludur
- Açıklama alt çizgi yerine boşluk içerebilir

## Yeni Migration Ekleme

1. `src/main/resources/db/migration/` klasörüne yeni SQL dosyası oluşturun
2. Dosya adını doğru formatta verin (örn: `V3__Add_new_column.sql`)
3. SQL migration kodunuzu yazın
4. Uygulamayı başlattığınızda Flyway otomatik olarak migration'ı çalıştıracak

### Örnek Migration

```sql
-- Migration V3: Add new column
-- Flyway otomatik olarak bu dosyayı çalıştıracak

ALTER TABLE invoices 
ADD COLUMN new_column VARCHAR(255);
```

## Migration Sırası

Flyway migration'ları sırasıyla çalıştırır:
1. Mevcut migration'ları kontrol eder
2. Yeni migration dosyalarını bulur
3. Versiyon numarasına göre sırayla çalıştırır
4. Her migration sadece bir kez çalışır (versiyon tablosunda saklanır)

## Production Deploy

Production'da migration'lar otomatik olarak çalışır:
1. Uygulama başladığında Flyway aktif olur
2. Migration'lar otomatik çalıştırılır
3. Eğer bir migration başarısız olursa, uygulama başlamaz (güvenlik)

### Migration Durumunu Kontrol Etme

Production'da migration durumunu kontrol etmek için:
- Actuator endpoint'lerini kullanabilirsiniz: `/actuator/health`
- Log dosyalarında Flyway migration loglarını görebilirsiniz

## Hibernate DDL-Auto

`ddl-auto: validate` olarak ayarlandı:
- Hibernate sadece şema doğrulaması yapar
- Tablo oluşturma/güncelleme Flyway ile yapılır
- Bu sayede database şeması kontrol altında tutulur

## Eski Migration Sistemi

`DatabaseMigrationConfig.java` artık devre dışı bırakıldı.
Flyway kullanılmadığında fallback olarak kullanılabilir (şu anda yorum satırında).

## Sorun Giderme

### Migration başarısız olursa:
1. Log dosyalarını kontrol edin
2. SQL syntax hatalarını düzeltin
3. Yeni bir migration dosyası oluşturup hatayı düzeltin

### Migration'ı manuel çalıştırmak:
```bash
# Maven ile
mvn flyway:migrate

# Veya Docker container içinde
docker exec -it <container-name> flyway migrate
```

### Migration'ı geri alma (rollback):
Flyway otomatik rollback desteklemez. Manuel olarak yeni bir migration ile geri almanız gerekir:
```sql
-- V4__Rollback_V3.sql
ALTER TABLE invoices DROP COLUMN IF EXISTS new_column;
```


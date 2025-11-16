package com.stoktakip.repository;

import com.stoktakip.model.SerialNumber;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {

    Optional<SerialNumber> findByValueIgnoreCase(String value);
}



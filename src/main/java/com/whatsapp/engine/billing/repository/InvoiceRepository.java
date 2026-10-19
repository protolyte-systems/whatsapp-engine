package com.whatsapp.engine.billing.repository;

import com.whatsapp.engine.billing.Invoice;
import com.whatsapp.engine.billing.InvoiceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByOrganizationId(
            UUID organizationId,
            Pageable pageable
    );

    Page<Invoice> findByOrganizationIdAndStatus(
            UUID organizationId,
            InvoiceStatus status,
            Pageable pageable
    );

    Optional<Invoice> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    List<Invoice> findByOrganizationIdAndInvoiceDateBetweenOrderByInvoiceDateDesc(
            UUID organizationId,
            Instant startDate,
            Instant endDate
    );
}

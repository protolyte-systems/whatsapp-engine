package com.whatsapp.engine.billing.service;
import com.whatsapp.engine.billing.Invoice;
import com.whatsapp.engine.billing.InvoiceStatus;
import com.whatsapp.engine.billing.UsageSnapshot;
import com.whatsapp.engine.billing.dto.BillingSummaryResponse;
import com.whatsapp.engine.billing.dto.InvoiceResponse;
import com.whatsapp.engine.billing.dto.UsageSnapshotResponse;
import com.whatsapp.engine.billing.repository.InvoiceRepository;
import com.whatsapp.engine.billing.repository.UsageSnapshotRepository;
import com.whatsapp.engine.common.exception.ApplicationException;
import com.whatsapp.engine.subscription.Subscription;
import com.whatsapp.engine.subscription.SubscriptionStatus;
import com.whatsapp.engine.subscription.repository.SubscriptionRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final UsageSnapshotRepository usageSnapshotRepository;
    private final SubscriptionRepository subscriptionRepository;

    public BillingService(
            InvoiceRepository invoiceRepository,
            UsageSnapshotRepository usageSnapshotRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.usageSnapshotRepository = usageSnapshotRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public BillingSummaryResponse getBillingSummary(UUID organizationId) {
        List<Invoice> invoices =
                invoiceRepository.findByOrganizationIdAndInvoiceDateBetweenOrderByInvoiceDateDesc(
                        organizationId,
                        Instant.EPOCH,
                        Instant.now()
                );

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            total = total.add(invoice.getAmount());

            if (invoice.getStatus() == InvoiceStatus.PAID) {
                paid = paid.add(invoice.getAmount());
            } else if (invoice.getStatus() == InvoiceStatus.PENDING
                    || invoice.getStatus() == InvoiceStatus.OVERDUE) {
                pending = pending.add(invoice.getAmount());
            }
        }

        Subscription subscription = subscriptionRepository
                .findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organizationId,
                        SubscriptionStatus.ACTIVE
                )
                .orElse(null);

        return new BillingSummaryResponse(
                total,
                paid,
                pending,
                subscription == null ? null : subscription.getCurrentPeriodEnd(),
                subscription == null ? null : subscription.getBillingCycle().name()
        );
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(
            UUID organizationId,
            InvoiceStatus status,
            Pageable pageable
    ) {
        Page<Invoice> invoices = status == null
                ? invoiceRepository.findByOrganizationId(organizationId, pageable)
                : invoiceRepository.findByOrganizationIdAndStatus(
                        organizationId, status, pageable
                );

        return invoices.map(this::toInvoiceResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID organizationId, UUID invoiceId) {
        Invoice invoice = invoiceRepository
                .findByIdAndOrganizationId(invoiceId, organizationId)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "INVOICE_NOT_FOUND",
                        "Invoice not found"
                ));

        return toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public byte[] getInvoicePdf(UUID organizationId, UUID invoiceId) {
        Invoice invoice = invoiceRepository
                .findByIdAndOrganizationId(invoiceId, organizationId)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "INVOICE_NOT_FOUND",
                        "Invoice not found"
                ));

        byte[] pdf = invoice.getPdfData();

        if (pdf == null || pdf.length == 0) {
            throw new ApplicationException(
                    HttpStatus.NOT_FOUND,
                    "INVOICE_PDF_NOT_FOUND",
                    "Invoice PDF is not available"
            );
        }

        return pdf;
    }

    @Transactional(readOnly = true)
    public List<UsageSnapshotResponse> getUsageHistory(UUID organizationId) {
        return usageSnapshotRepository
                .findByOrganizationIdOrderByPeriodStartDesc(organizationId)
                .stream()
                .map(this::toUsageResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public byte[] downloadInvoicesZip(
            UUID organizationId,
            Instant startDate,
            Instant endDate
    ) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_INVOICE_DATE_RANGE",
                    "A valid invoice date range is required"
            );
        }

        List<Invoice> invoices =
                invoiceRepository.findByOrganizationIdAndInvoiceDateBetweenOrderByInvoiceDateDesc(
                        organizationId, startDate, endDate
                );

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {

            int included = 0;

            for (Invoice invoice : invoices) {
                byte[] pdf = invoice.getPdfData();

                if (pdf == null || pdf.length == 0) {
                    continue;
                }

                String filename = invoice.getInvoiceNumber()
                        .replaceAll("[^a-zA-Z0-9._-]", "_");

                if (filename.isBlank()) {
                    filename = invoice.getId().toString();
                }

                zip.putNextEntry(new ZipEntry(filename + ".pdf"));
                zip.write(pdf);
                zip.closeEntry();
                included++;
            }

            if (included == 0) {
                throw new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "INVOICE_PDFS_NOT_FOUND",
                        "No invoice PDFs are available for the selected date range"
                );
            }

            zip.finish();
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create invoice ZIP file", exception);
        }
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getPaidAt(),
                invoice.getSubscription() == null
                        ? null
                        : invoice.getSubscription().getId()
        );
    }

    private UsageSnapshotResponse toUsageResponse(UsageSnapshot snapshot) {
        return new UsageSnapshotResponse(
                snapshot.getId(),
                snapshot.getPeriodStart(),
                snapshot.getPeriodEnd(),
                snapshot.getMessagesUsed(),
                snapshot.getContactsUsed(),
                snapshot.getStorageUsed()
        );
    }
}

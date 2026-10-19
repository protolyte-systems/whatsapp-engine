package com.whatsapp.engine.billing.controller;

import com.whatsapp.engine.auth.User;
import com.whatsapp.engine.billing.InvoiceStatus;
import com.whatsapp.engine.billing.dto.BillingSummaryResponse;
import com.whatsapp.engine.billing.dto.InvoiceResponse;
import com.whatsapp.engine.billing.dto.UsageSnapshotResponse;
import com.whatsapp.engine.billing.service.BillingService;
import com.whatsapp.engine.common.api.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
@PreAuthorize("hasRole('ORG_ADMIN')")
@Validated
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<BillingSummaryResponse>> getBillingSummary(
            @AuthenticationPrincipal User user
    ) {
        BillingSummaryResponse response =
                billingService.getBillingSummary(user.getOrganization().getId());

        return ResponseEntity.ok(
                ApiResponse.success("Billing summary retrieved successfully", response)
        );
    }

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoices(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "invoiceDate")
        );

        Page<InvoiceResponse> response = billingService.getInvoices(
                user.getOrganization().getId(),
                status,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success("Invoices retrieved successfully", response)
        );
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @AuthenticationPrincipal User user,
            @PathVariable UUID invoiceId
    ) {
        InvoiceResponse response = billingService.getInvoice(
                user.getOrganization().getId(),
                invoiceId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Invoice retrieved successfully", response)
        );
    }

    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @AuthenticationPrincipal User user,
            @PathVariable UUID invoiceId
    ) {
        byte[] pdf = billingService.getInvoicePdf(
                user.getOrganization().getId(),
                invoiceId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("invoice-" + invoiceId + ".pdf")
                                .build()
                                .toString()
                )
                .body(pdf);
    }


    @GetMapping(value = "/invoices/download", produces = "application/zip")
    public ResponseEntity<byte[]> downloadInvoicesZip(
            @AuthenticationPrincipal User user,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate
    ) {
        byte[] zip = billingService.downloadInvoicesZip(
                user.getOrganization().getId(),
                startDate,
                endDate
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("invoices.zip")
                                .build()
                                .toString()
                )
                .body(zip);
    }
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<List<UsageSnapshotResponse>>> getUsageHistory(
            @AuthenticationPrincipal User user
    ) {
        List<UsageSnapshotResponse> response =
                billingService.getUsageHistory(user.getOrganization().getId());

        return ResponseEntity.ok(
                ApiResponse.success("Usage history retrieved successfully", response)
        );
    }
}


package com.travel.billing.repository;

import com.travel.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    Page<Invoice> findByBookingId(Long bookingId, Pageable pageable);

    Page<Invoice> findByStatus(String status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :today AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);
}

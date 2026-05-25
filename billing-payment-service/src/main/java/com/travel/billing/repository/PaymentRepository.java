package com.travel.billing.repository;

import com.travel.billing.entity.Payment;
import com.travel.billing.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    List<Payment> findByInvoiceId(Long invoiceId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.invoice.id = :invoiceId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidForInvoice(@Param("invoiceId") Long invoiceId);
}

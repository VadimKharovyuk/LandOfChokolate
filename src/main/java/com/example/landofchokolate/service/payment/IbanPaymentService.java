package com.example.landofchokolate.service.payment;
import com.example.landofchokolate.enums.PaymentStatus;
import com.example.landofchokolate.enums.PaymentType;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import com.example.landofchokolate.repository.PaymentRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IbanPaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createIbanPayment(Order order) {

        // Проверяем, нет ли уже платежа для этого заказа
        Payment existingPayment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existingPayment != null) {
            return existingPayment;
        }

        // Создаем платеж с типом IBAN и статусом PENDING
        Payment payment = Payment.builder()
                .order(order)
                .type(PaymentType.IBAN)
                .status(PaymentStatus.PENDING) // Ожидает ручной оплаты
                .amount(order.getTotalAmount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment;
    }

    /**
     * Получить банковские реквизиты для IBAN перевода
     */
    public IbanDetails getIbanDetails() {
        return IbanDetails.builder()
                .iban("UA903052992990004149123456789")
                .recipientName("ТОВ \"Land of Chocolate\"")
                .bankName("ПриватБанк")
                .swiftCode("PBANUA2X")
                .recipientAddress("м. Київ, вул. Хрещатик 1")
                .build();
    }

    /**
     * DTO для банковских реквизитов
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IbanDetails {
        private String iban;
        private String recipientName;
        private String bankName;
        private String swiftCode;
        private String recipientAddress;
    }
}

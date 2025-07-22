package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.enums.PaymentStatus;
import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import com.example.landofchokolate.service.OrderService;
import com.example.landofchokolate.service.payment.LiqPayPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/payment/privatbank")
public class PaymentController {

    @Autowired
    private LiqPayPaymentServiceImpl liqPayService;

    @Autowired
    private OrderService orderService;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/{orderId}")
    @Transactional
    public String privatbankPayment(@PathVariable Long orderId, Model model) {

        Order order = orderService.findById(orderId);
        if (order == null) {
            System.out.println("Заказ не найден!");
            return "redirect:/error";
        }


//        // URL для возврата пользователя после оплаты
        String resultUrl = baseUrl + "/payment/result?order_id=" + orderId;
        // URL для серверного callback от LiqPay
        String serverUrl = baseUrl + "/payment/callback";


        // Создаем или получаем существующий платеж
        Payment payment = liqPayService.processPayment(order, resultUrl, serverUrl);

        if (payment == null) {
            System.out.println("Не удалось создать платеж!");
            return "redirect:/error";
        }

        System.out.println("Платеж создан: " + payment.getId());

        // Получаем данные для формы LiqPay
        Map<String, String> paymentForm = liqPayService.getPaymentForm(order, resultUrl, serverUrl);


        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("data", paymentForm.get("data"));        // ← правильно
        model.addAttribute("signature", paymentForm.get("signature")); // ← правильно
        model.addAttribute("publicKey", paymentForm.get("public_key"));

        return "client/payment/privatbank";
    }

    /**
     * Обработка callback от LiqPay (серверное уведомление)
     */
    @PostMapping("/callback")
    @ResponseBody
    public ResponseEntity<String> handleCallback(@RequestParam String data,
                                                 @RequestParam String signature) {
        try {
            // Проверяем подпись
            if (!liqPayService.verifySignature(data, signature)) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            // Декодируем данные
            Map<String, Object> paymentData = liqPayService.decodeData(data);

            // Обрабатываем callback через сервис
            boolean success = liqPayService.processLiqPayCallback(paymentData);

            if (success) {
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.badRequest().body("Processing failed");
            }

        } catch (Exception e) {
            // Логируем ошибку для отладки
            System.err.println("Callback processing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing callback");
        }
    }

    /**
     * Страница результата оплаты (куда возвращается пользователь)
     */
    @GetMapping("/result")
    public String paymentResult(@RequestParam(required = false) String order_id,
                                @RequestParam(required = false) String status,
                                Model model) {
        if (order_id != null) {
            try {
                Order order = orderService.findById(Long.parseLong(order_id));
                model.addAttribute("order", order);

                // Проверяем актуальный статус платежа в БД
                if (order != null) {
                    Payment payment = liqPayService.getPaymentByOrder(order);
                    model.addAttribute("payment", payment);

                    // Определяем, какую страницу показать
                    if (payment != null) {
                        switch (payment.getStatus()) {
                            case COMPLETED:
                                return "client/payment/success";
                            case FAILED:
                                return "client/payment/failed";
                            case PROCESSING:
                                return "client/payment/processing";
                            default:
                                return "client/payment/pending";
                        }
                    }
                }
            } catch (NumberFormatException e) {
                return "redirect:/error";
            }
        }

        // По умолчанию показываем страницу ожидания
        return "client/payment/pending";
    }

    /**
     * Проверка статуса платежа (AJAX)
     */
    @GetMapping("/status/{paymentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable Long paymentId) {
        Payment payment = liqPayService.getPaymentById(paymentId);

        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = Map.of(
                "id", payment.getId(),
                "status", payment.getStatus().name(),
                "statusDisplay", payment.getStatus().getDisplayName(),
                "amount", payment.getAmount(),
                "orderId", payment.getOrder().getId(),
                "paidAt", payment.getPaidAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Проверка статуса платежа по заказу (AJAX)
     */
    @GetMapping("/order-status/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderPaymentStatus(@PathVariable Long orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Payment payment = liqPayService.getPaymentByOrder(order);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = Map.of(
                "orderId", orderId,
                "paymentId", payment.getId(),
                "status", payment.getStatus().name(),
                "statusDisplay", payment.getStatus().getDisplayName(),
                "amount", payment.getAmount(),
                "isPaid", payment.getStatus() == PaymentStatus.COMPLETED
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/test")
    public String test() {
        return "client/payment/test";
    }
}
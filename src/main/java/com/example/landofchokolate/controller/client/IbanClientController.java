package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.model.Payment;
import com.example.landofchokolate.service.OrderService;
import com.example.landofchokolate.service.payment.IbanPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
@Slf4j
@Controller
@RequestMapping("/payment/iban/")
@RequiredArgsConstructor
public class IbanClientController {
    private final OrderService orderService;
    private final IbanPaymentService ibanPaymentService;

    @GetMapping("{orderId}")
    public String iban(Model model, @PathVariable Long orderId) {

        try {
            // Получаем заказ
            Order order = orderService.findById(orderId);
            if (order == null) {
                log.error("Order not found: {}", orderId);
                return "redirect:/error";
            }

            Payment payment = ibanPaymentService.createIbanPayment(order);

            // Получаем банковские реквизиты
            IbanPaymentService.IbanDetails ibanDetails = ibanPaymentService.getIbanDetails();
            model.addAttribute("ibanDetails", ibanDetails);


            model.addAttribute("order", order);
            model.addAttribute("payment", payment);


            return "client/payment/iban";

        } catch (Exception e) {
            log.error("Error processing IBAN payment for order: {}", orderId, e);
            return "redirect:/error";
        }
    }

}

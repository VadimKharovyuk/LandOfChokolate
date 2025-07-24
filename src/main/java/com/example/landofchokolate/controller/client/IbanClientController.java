package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.model.Order;
import com.example.landofchokolate.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment/iban/")
@RequiredArgsConstructor
public class IbanClientController {
    private final OrderService orderService;

    @GetMapping("{orderId}")
    public String iban(Model model ,@PathVariable Long orderId) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            System.out.println("Заказ не найден!");
            return "redirect:/error";
        }
        model.addAttribute("order", order);
        return "client/payment/iban";
    }

}

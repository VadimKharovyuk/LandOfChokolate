package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.exception.SubscriptionNotFoundException;
import com.example.landofchokolate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/subscriptions")
public class AdminSubscriptionController {
    private final SubscriptionService subscriptionService;



    @GetMapping
    public String adminSubscriptions(Model model) {
        log.info("Admin subscriptions");
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllActiveSubscriptions();
        model.addAttribute("subscriptions", subscriptions);
        return "admin/subscriptions/list";
    }


    @PostMapping("/{id}/delete")
    public String deleteSubscription(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            subscriptionService.deleteSubscription(id);
            redirectAttributes.addFlashAttribute("successMessage", "Подписка успешно удалена");
            log.info("Subscription with id {} deleted by admin", id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении подписки");
            log.error("Error deleting subscription with id {}: {}", id, e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

//    @DeleteMapping("/{id}")
//    @ResponseBody
//    public ResponseEntity<Map<String, String>> deleteSubscription(@PathVariable Long id) {
//        try {
//            subscriptionService.deleteSubscription(id);
//            return ResponseEntity.ok(Map.of("message", "Подписка успешно удалена"));
//        } catch (SubscriptionNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body(Map.of("error", "Ошибка при удалении подписки"));
//        }
//    }
}

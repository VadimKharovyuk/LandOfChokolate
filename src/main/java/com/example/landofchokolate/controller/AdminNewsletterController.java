package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.service.NewsletterService;
import com.example.landofchokolate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/newsletter")
public class AdminNewsletterController {

    private final NewsletterService newsletterService;
    private final SubscriptionService subscriptionService;


    @GetMapping
    public String showNewsletterForm(Model model) {
        List<SubscriptionResponse> subscribers = subscriptionService.getAllActiveSubscriptions();
        model.addAttribute("subscribersCount", subscribers.size());
        return "admin/newsletter/create";
    }


    /**
     * Отправить кастомную рассылку
     */
    @PostMapping("/send")
    public String sendCustomNewsletter(
            @RequestParam String subject,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String ctaText,
            @RequestParam(required = false) String ctaUrl,
            @RequestParam(required = false) String promoCode,
            @RequestParam(required = false) String promoDescription,
            RedirectAttributes redirectAttributes) {

        try {
            // Создаем данные для рассылки
            NewsletterService.NewsletterData newsletterData = NewsletterService.NewsletterData.builder()
                    .subject(subject)
                    .title(title)
                    .content(content)
                    .imageUrl(imageUrl)
                    .ctaText(ctaText)
                    .ctaUrl(ctaUrl)
                    .build();

            // Отправляем рассылку
            newsletterService.sendNewsletterToAllSubscribers(newsletterData);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Рассылка успешно отправлена всем активным подписчикам!");

            log.info("Кастомная рассылка '{}' отправлена администратором", subject);

        } catch (Exception e) {
            log.error("Ошибка при отправке кастомной рассылки: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при отправке рассылки: " + e.getMessage());
        }

        return "redirect:/admin/newsletter";
    }

    /**
     * Предварительный просмотр рассылки
     */
    @PostMapping("/preview")
    @ResponseBody
    public String previewNewsletter(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String ctaText,
            @RequestParam(required = false) String ctaUrl) {

        try {
            NewsletterService.NewsletterData newsletterData = NewsletterService.NewsletterData.builder()
                    .title(title)
                    .content(content)
                    .imageUrl(imageUrl)
                    .ctaText(ctaText)
                    .ctaUrl(ctaUrl)
                    .build();

            // Генерируем HTML для предпросмотра
            return newsletterService.generateNewsletterHtml(newsletterData);

        } catch (Exception e) {
            log.error("Ошибка при создании предпросмотра: {}", e.getMessage());
            return "<p>Ошибка при создании предпросмотра</p>";
        }
    }

    /**
     * Быстрые шаблоны рассылок
     */
    @PostMapping("/quick-send/{type}")
    public String sendQuickNewsletter(
            @PathVariable String type,
            RedirectAttributes redirectAttributes) {

        try {
            switch (type) {
                case "new-product":
                    newsletterService.sendNewProductNotification(
                            "Новий шоколадний торт",
                            "Ексклюзивний десерт з бельгійським шоколадом",
                            "/images/new-cake.jpg",
                            "https://landofchocolate.com/new-cake"
                    );
                    break;

                case "discount":
                    newsletterService.sendSpecialOffer(
                            "Знижка 15% на всі торти",
                            "Святкуйте з нами! Отримайте знижку на найкращі торти.",
                            "CAKE15",
                            java.time.LocalDateTime.now().plusDays(7)
                    );
                    break;

                default:
                    throw new IllegalArgumentException("Неизвестный тип рассылки: " + type);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Быстрая рассылка успешно отправлена!");

        } catch (Exception e) {
            log.error("Ошибка при отправке быстрой рассылки: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при отправке: " + e.getMessage());
        }

        return "redirect:/admin/newsletter";
    }

    /**
     * Статистика рассылок
     */
    @GetMapping("/stats")
    @ResponseBody
    public Object getNewsletterStats() {
        return newsletterService.getNewsletterStats();
    }
}
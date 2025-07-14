package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.util.SubscriptionCreatedEvent;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
/**
 * Сервис для рассылки новостей подписчикам
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private final EmailService emailService;
    private final SubscriptionService subscriptionService;
    private final SpringTemplateEngine templateEngine;



    @EventListener
    @Async
    public void handleSubscriptionCreated(SubscriptionCreatedEvent event) {
        sendWelcomeEmail(event.getEmail());
    }


    /**
     * Генерация HTML для предпросмотра (добавьте в NewsletterService)
     */
    public String generateNewsletterHtml(NewsletterData newsletterData) {
        try {
            Map<String, Object> context = createNewsletterContext(newsletterData);
            Context thymeleafContext = new Context();
            context.forEach(thymeleafContext::setVariable);

            // Используем SpringTemplateEngine для генерации HTML
            return templateEngine.process("admin/email/newsletter", thymeleafContext);

        } catch (Exception e) {
            log.error("Ошибка при генерации HTML: {}", e.getMessage());
            throw new RuntimeException("Ошибка генерации HTML", e);
        }
    }

    /**
     * Отправка кастомной рассылки с дополнительными параметрами
     */
    @Async
    public void sendCustomNewsletter(String subject, String title, String content,
                                     String imageUrl, String ctaText, String ctaUrl,
                                     String promoCode, String promoDescription) {

        NewsletterData newsletterData = NewsletterData.builder()
                .subject(subject)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .ctaText(ctaText)
                .ctaUrl(ctaUrl)
                .build();

        // Добавляем промокод в контекст, если есть
        try {
            List<SubscriptionResponse> activeSubscribers = subscriptionService.getAllActiveSubscriptions();

            if (activeSubscribers.isEmpty()) {
                log.warn("Нет активных подписчиков для кастомной рассылки");
                return;
            }

            List<String> emailList = activeSubscribers.stream()
                    .map(SubscriptionResponse::getEmail)
                    .collect(Collectors.toList());

            Map<String, Object> context = createNewsletterContext(newsletterData);

            // Добавляем дополнительные данные
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                context.put("promoCode", promoCode);
                context.put("promoDescription", promoDescription);
            }

            emailService.sendBulkEmail(emailList, subject, "newsletter", context);

            log.info("Кастомная рассылка '{}' отправлена {} подписчикам", subject, emailList.size());

        } catch (Exception e) {
            log.error("Ошибка при отправке кастомной рассылки: {}", e.getMessage(), e);
        }
    }


    /**
     * DTO для данных рассылки
     */
    @Data
    @Builder
    public static class NewsletterData {
        private String subject;
        private String title;
        private String content;
        private String imageUrl;
        private String ctaText;
        private String ctaUrl;
        private String unsubscribeUrl;
    }

    /**
     * Отправка рассылки всем активным подписчикам
     *
     * @param newsletterData Данные для рассылки
     * @return Количество отправленных писем
     */
    @Async
    public void sendNewsletterToAllSubscribers(NewsletterData newsletterData) {
        try {
            log.info("Начинаем рассылку: {}", newsletterData.getSubject());

            // Получаем всех активных подписчиков
            List<SubscriptionResponse> activeSubscribers = subscriptionService.getAllActiveSubscriptions();

            if (activeSubscribers.isEmpty()) {
                log.warn("Нет активных подписчиков для рассылки");
                return;
            }

            // Извлекаем email-адреса
            List<String> emailList = activeSubscribers.stream()
                    .map(SubscriptionResponse::getEmail)
                    .collect(Collectors.toList());

            // Подготавливаем контекст для шаблона
            Map<String, Object> context = createNewsletterContext(newsletterData);

            // Отправляем массовую рассылку
            emailService.sendBulkEmail(
                    emailList,
                    newsletterData.getSubject(),
                    "admin/email/newsletter",
                    context
            );

            log.info("Рассылка '{}' отправлена {} подписчикам",
                    newsletterData.getSubject(), emailList.size());

        } catch (Exception e) {
            log.error("Ошибка при отправке рассылки: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправка приветственного письма новому подписчику
     *
     * @param email Email нового подписчика
     */
    @Async
    public void sendWelcomeEmail(String email) {
        try {
            log.info("Отправляем приветственное письмо на {}", email);

            Map<String, Object> context = createWelcomeContext(email);

            emailService.sendEmail(
                    email,
                    "Ласкаво просимо до Land of Chocolate! 🍫",
                    "admin/email/welcome",
                    context
            );

            log.info("Приветственное письмо отправлено на {}", email);

        } catch (Exception e) {
            log.error("Ошибка при отправке приветственного письма на {}: {}", email, e.getMessage());
        }
    }

    /**
     * Отправка уведомления о новом продукте
     *
     * @param productName Название нового продукта
     * @param productDescription Описание продукта
     * @param productImageUrl Ссылка на изображение
     * @param productUrl Ссылка на страницу продукта
     */
    @Async
    public void sendNewProductNotification(String productName, String productDescription,
                                           String productImageUrl, String productUrl) {
        NewsletterData productData = NewsletterData.builder()
                .subject("Новинка в Land of Chocolate: " + productName + " 🍫")
                .title("Представляємо новий десерт!")
                .content("Ми раді представити вам наш новий вишуканий десерт: " + productName + ". " + productDescription)
                .imageUrl(productImageUrl)
                .ctaText("Замовити зараз")
                .ctaUrl(productUrl)
                .build();

        sendNewsletterToAllSubscribers(productData);
    }

    /**
     * Отправка специального предложения
     *
     * @param offerTitle Заголовок предложения
     * @param offerDescription Описание предложения
     * @param discountCode Промокод
     * @param validUntil Действительно до
     */
    @Async
    public void sendSpecialOffer(String offerTitle, String offerDescription,
                                 String discountCode, LocalDateTime validUntil) {
        NewsletterData offerData = NewsletterData.builder()
                .subject("Спеціальна пропозиція: " + offerTitle + " 🎁")
                .title("Ексклюзивна пропозиція для вас!")
                .content(offerDescription + " Використовуйте промокод: " + discountCode +
                        " до " + validUntil.toLocalDate())
                .ctaText("Скористатися пропозицією")
                .ctaUrl("https://landofchocolate.com/catalog?promo=" + discountCode)
                .build();

        sendNewsletterToAllSubscribers(offerData);
    }

    /**
     * Создание контекста для шаблона рассылки
     */
    private Map<String, Object> createNewsletterContext(NewsletterData data) {
        Map<String, Object> context = new HashMap<>();
        context.put("title", data.getTitle());
        context.put("content", data.getContent());
        context.put("imageUrl", data.getImageUrl());
        context.put("ctaText", data.getCtaText());
        context.put("ctaUrl", data.getCtaUrl());
        context.put("currentYear", LocalDateTime.now().getYear());
        context.put("companyName", "Land of Chocolate");
        context.put("unsubscribeUrl", "https://landofchocolate.com/unsubscribe");
        context.put("websiteUrl", "https://landofchocolate.com");
        return context;
    }

    /**
     * Создание контекста для приветственного письма
     */
    private Map<String, Object> createWelcomeContext(String email) {
        Map<String, Object> context = new HashMap<>();
        context.put("email", email);
        context.put("currentYear", LocalDateTime.now().getYear());
        context.put("companyName", "Land of Chocolate");
        context.put("websiteUrl", "https://landofchocolate.com"); // ✅ Полный URL
        context.put("catalogUrl", "https://landofchocolate.com/catalog");
        context.put("unsubscribeUrl", "https://landofchocolate.com/unsubscribe");
        return context;
    }

    /**
     * Получение статистики рассылок
     */
    public Map<String, Object> getNewsletterStats() {
        Map<String, Object> stats = new HashMap<>();

        List<SubscriptionResponse> subscribers = subscriptionService.getAllActiveSubscriptions();
        stats.put("totalSubscribers", subscribers.size());
        stats.put("emailServiceAvailable", emailService.isServiceAvailable());
        stats.put("lastUpdate", LocalDateTime.now());

        return stats;
    }
}
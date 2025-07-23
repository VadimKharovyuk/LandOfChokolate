package com.example.landofchokolate.service.serviceImpl;
import com.example.landofchokolate.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Реализация сервиса отправки email с использованием очереди
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailQueueServiceImpl implements EmailService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final BlockingQueue<EmailTask> emailQueue = new LinkedBlockingQueue<>();
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private boolean serviceAvailable = false;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Класс для задачи отправки email
     */
    @Data
    @Builder
    private static class EmailTask {
        private String to;
        private String subject;
        private String templateName;
        private Map<String, Object> context;
        private Map<String, byte[]> attachments;
    }

    /**
     * Инициализация сервиса и запуск потока обработки очереди
     */
    @PostConstruct
    public void init() {
        try {
            executorService.submit(() -> {
                try {
                    log.info("Запуск потока обработки email-очереди");
                    serviceAvailable = true;

                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            EmailTask task = emailQueue.take();
                            try {
                                processEmailTask(task);
                            } catch (Exception e) {
                                log.error("Ошибка при выполнении задачи отправки email: {}", e.getMessage());
                            }
                            TimeUnit.SECONDS.sleep(5); // Задержка между отправками для предотвращения ошибок лимита
                        } catch (InterruptedException e) {
                            log.warn("Прерывание потока обработки email-очереди");
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } finally {
                    serviceAvailable = false;
                    log.info("Поток обработки email-очереди завершен");
                }
            });
            log.info("Сервис email-очереди успешно инициализирован");
        } catch (Exception e) {
            serviceAvailable = false;
            log.error("Ошибка при инициализации сервиса email-очереди: {}", e.getMessage());
        }
    }

    /**
     * Обработка задачи отправки email
     *
     * @param task Задача отправки email
     */
    private void processEmailTask(EmailTask task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            Context thymeleafContext = new Context();
            task.getContext().forEach(thymeleafContext::setVariable);

            // ✅ ПОЛНОСТЬЮ ПЕРЕПИСАННАЯ логика
            String templatePath = task.getTemplateName();

            // Если передано только имя - добавляем префикс
            if (!templatePath.contains("/")) {
                templatePath = "admin/email/" + templatePath;
            }

            log.debug("Обрабатываем шаблон: {}", templatePath);
            String htmlBody = templateEngine.process(templatePath, thymeleafContext);

            helper.setFrom(fromEmail);
            helper.setTo(task.getTo());
            helper.setSubject(task.getSubject());
            helper.setText(htmlBody, true);

            // Добавление вложений...
            if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
                for (Map.Entry<String, byte[]> entry : task.getAttachments().entrySet()) {
                    String fileName = entry.getKey();
                    byte[] fileData = entry.getValue();
                    String mimeType = determineMimeType(fileName);
                    helper.addAttachment(fileName, new ByteArrayDataSource(fileData, mimeType));
                }
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке email на адрес {}: {}", task.getTo(), e.getMessage());
        }
    }

    /**
     * Определение MIME-типа файла по его расширению
     *
     * @param fileName Имя файла
     * @return MIME-тип
     */
    private String determineMimeType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Добавление задачи в очередь отправки email
     *
     * @param to Email получателя
     * @param subject Тема письма
     * @param templateName Имя шаблона
     * @param context Контекст данных для шаблона
     */
    @Override
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> context) {
        EmailTask task = EmailTask.builder()
                .to(to)
                .subject(subject)
                .templateName(templateName)
                .context(context)
                .build();

        if (!emailQueue.offer(task)) {
            log.warn("Не удалось добавить задачу в очередь email-рассылки для {}", to);
        } else {
            log.debug("Задача отправки email для {} успешно добавлена в очередь", to);
        }
    }



    /**
     * Массовая отправка email нескольким получателям
     *
     * @param recipients Список email-адресов получателей
     * @param subject Тема письма
     * @param templateName Имя шаблона
     * @param context Общий контекст данных для шаблона
     */
    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String templateName, Map<String, Object> context) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("Попытка массовой отправки на пустой список получателей");
            return;
        }

        for (String recipient : recipients) {
            Map<String, Object> personalizedContext = new HashMap<>(context);
            personalizedContext.put("recipientEmail", recipient);
            sendEmail(recipient, subject, templateName, personalizedContext);
        }
    }

    /**
     * Проверка статуса сервиса отправки писем
     *
     * @return true, если сервис доступен
     */
    @Override
    public boolean isServiceAvailable() {
        return serviceAvailable;
    }

    /**
     * Корректное завершение работы сервиса
     */
    @PreDestroy
    public void shutdown() {
        log.info("Завершение службы email-очереди");
        executorService.shutdownNow();
        serviceAvailable = false;
    }


}

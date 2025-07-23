package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.supportRequestDto.CreateSupportRequestDto;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import com.example.landofchokolate.enums.SupportTopic;
import com.example.landofchokolate.model.SupportRequest;
import com.example.landofchokolate.service.SupportRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/support")
public class SupportRequestClientController {

    private final SupportRequestService supportRequestService;

    @GetMapping
    public String support(Model model) {

        model.addAttribute("support", new CreateSupportRequestDto());

        model.addAttribute("supportTopics", SupportTopic.values());

        return "client/support/form";
    }


    @PostMapping("/create")
    public String create(Model model,
                         @Valid @ModelAttribute("support") CreateSupportRequestDto createSupportRequestDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        log.info("Processing support request creation for email: {}", createSupportRequestDto.getEmail());

        // ДОБАВЛЕНО: проверка ошибок валидации
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in support request: {}", bindingResult.getAllErrors());

            // Возвращаем форму с ошибками
            model.addAttribute("supportTopics", SupportTopic.values());
            return "client/support/form";
        }

        try {
            // Создание запроса
            SupportRequestResponseDto responseDto = supportRequestService.createSupport(createSupportRequestDto);

            log.info("Support request created successfully with ID: {}", responseDto.getId());

            // ДОБАВЛЕНО: flash сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ваш запрос успешно отправлен! Номер обращения: " + responseDto.getId());

            // УЛУЧШЕНО: redirect после POST для предотвращения повторной отправки
            return "redirect:/support";

        } catch (Exception e) {
            log.error("Error creating support request", e);

            // ДОБАВЛЕНО: обработка ошибок
            model.addAttribute("errorMessage", "Произошла ошибка при отправке запроса. Попробуйте еще раз.");
            model.addAttribute("supportTopics", SupportTopic.values());

            return "client/support/form";
        }


    }

}

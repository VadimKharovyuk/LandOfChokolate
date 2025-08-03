package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.storeReview.CreateStoreReviewDTO;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import com.example.landofchokolate.service.StoreReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/store-review")
public class StoreReviewController {
    private final StoreReviewService storeReviewService;

    @GetMapping
    public String storeReviewList(
            Model model,
            @PageableDefault(size = 10, sort = "created", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Запрос списка отзывов: страница {}, размер {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Форма для создания нового отзыва
        model.addAttribute("form", new CreateStoreReviewDTO());

        // Получаем пагинированные отзывы
        PagedResponse<StoreReviewResponseDTO> pagedResponse = storeReviewService.getAllReviews(pageable);
        model.addAttribute("pagedResponse", pagedResponse);

        // Дополнительные атрибуты для удобства в шаблоне
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("pageSize", pageable.getPageSize());

        return "client/store-review/list";
    }

    @PostMapping
    public String createReview(
            @Valid @ModelAttribute("form") CreateStoreReviewDTO form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @PageableDefault(size = 10, sort = "created", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        // Если есть ошибки валидации - возвращаем форму
        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при создании отзыва: {}", bindingResult.getAllErrors());

            // Загружаем отзывы заново для отображения
            PagedResponse<StoreReviewResponseDTO> pagedResponse = storeReviewService.getAllReviews(pageable);
            model.addAttribute("pagedResponse", pagedResponse);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("pageSize", pageable.getPageSize());

            return "client/store-review/list";
        }

        try {
            storeReviewService.createReview(form);
            log.info("Отзыв успешно создан от пользователя: {}", form.getName());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Спасибо за ваш отзыв! Он успешно добавлен.");

        } catch (Exception e) {
            log.error("Ошибка при создании отзыва: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при добавлении отзыва. Попробуйте еще раз.");
        }

        return "redirect:/store-review";
    }


}
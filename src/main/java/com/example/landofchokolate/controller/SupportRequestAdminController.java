package com.example.landofchokolate.controller;

import com.cloudinary.api.ApiResponse;
import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.supportRequestDto.SupportRequestResponseDto;
import com.example.landofchokolate.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.landofchokolate.enums.SupportTopic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/support")
public class SupportRequestAdminController {

    private final SupportRequestService supportRequestService;

    @GetMapping
    public String supportRequestList(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                     @RequestParam(defaultValue = "desc") String sortDir,
                                     @RequestParam(required = false) SupportTopic topic,
                                     @RequestParam(required = false) String email) {


        // Создание Pageable с сортировкой
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Получение данных с пагинацией
        PagedResponse<SupportRequestResponseDto> pagedResponse =
                supportRequestService.getAllSupport(pageable);

        // ДОБАВЛЕНО: передаем весь PagedResponse, а не только content
        model.addAttribute("supportRequests", pagedResponse);

        // ДОБАВЛЕНО: метаданные для удобства использования в шаблоне
        model.addAttribute("content", pagedResponse.getContent());
        model.addAttribute("metadata", pagedResponse.getMetadata());

        // ДОБАВЛЕНО: параметры для формы фильтрации и сортировки
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        model.addAttribute("currentTopic", topic);
        model.addAttribute("currentEmail", email);

        // ДОБАВЛЕНО: данные для фильтров
        model.addAttribute("supportTopics", SupportTopic.values());
        model.addAttribute("pageSizes", new int[]{5, 10, 20, 50});

        return "admin/support/list";
    }

    @GetMapping("/{id}")
    public String supportRequestDetail(Model model, @PathVariable Long id) {

        try {
            SupportRequestResponseDto dto = supportRequestService.getById(id);
            model.addAttribute("supportRequest", dto);
            return "admin/support/detail";

        } catch (Exception e) {
            log.error("Error loading support request with ID: {}", id, e);
            model.addAttribute("errorMessage", "Запрос поддержки не найден");
            return "redirect:/admin/support";
        }
    }


    @PostMapping("/{id}/delete")
    public String deleteSupportRequest(@PathVariable Long id) {
        log.info("Deleting support request with ID: {}", id);

        try {
            supportRequestService.deleteById(id);
            return "redirect:/admin/support?deleted=true";

        } catch (Exception e) {
            log.error("Error deleting support request with ID: {}", id, e);
            return "redirect:/admin/support?error=delete";
        }
    }

}

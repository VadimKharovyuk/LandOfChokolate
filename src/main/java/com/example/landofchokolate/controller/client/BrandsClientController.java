package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.brend.BrandClientDto;
import com.example.landofchokolate.dto.brend.BrandPageResponseDto;
import com.example.landofchokolate.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequiredArgsConstructor
@RequestMapping("/brands")
public class BrandsClientController {
    private final BrandService brandService;

    private static final int DEFAULT_PAGE_SIZE = 12;

    @GetMapping
    public String listBrands(Model model, Pageable pageable) {
        // Устанавливаем размер страницы по умолчанию если не указан
        if (pageable.getPageSize() > 20) {
            pageable = PageRequest.of(pageable.getPageNumber(), DEFAULT_PAGE_SIZE, pageable.getSort());
        }

        BrandPageResponseDto pageResponseDto = brandService.getBrandsForClient(pageable);

        // Добавляем бренды
        model.addAttribute("brands", pageResponseDto.getBrands());

        // Добавляем информацию о пагинации
        model.addAttribute("currentPage", pageResponseDto.getCurrentPage());
        model.addAttribute("totalPages", pageResponseDto.getTotalPages());
        model.addAttribute("totalElements", pageResponseDto.getTotalElements());
        model.addAttribute("pageSize", pageResponseDto.getPageSize());
        model.addAttribute("hasNext", pageResponseDto.isHasNext());
        model.addAttribute("hasPrevious", pageResponseDto.isHasPrevious());

        // Или можно добавить весь объект сразу
        model.addAttribute("pagination", pageResponseDto);

        return "client/brands/list";
    }

    // Дополнительный метод для отображения конкретного бренда
    @GetMapping("/{slug}")
    public String brandDetail(@PathVariable String slug, Model model) {
        BrandClientDto brand = brandService.getBrandBySlug(slug);
        model.addAttribute("brand", brand);
        return "client/brands/detail";
    }
}

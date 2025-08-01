package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.brend.BrandClientDto;
import com.example.landofchokolate.dto.brend.BrandPageResponseDto;
import com.example.landofchokolate.dto.brend.BrandProductsPageResponseDto;
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
    private static final int DEFAULT_PAGE_SIZE_PRODUCT = 12;

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

    // Метод для отображения конкретного бренда с продуктами
    @GetMapping("/{slug}")
    public String brandDetail(@PathVariable String slug, Model model, Pageable pageable) {
        // Устанавливаем размер страницы для продуктов
        if (pageable.getPageSize() > 10) {
            pageable = PageRequest.of(pageable.getPageNumber(), DEFAULT_PAGE_SIZE_PRODUCT, pageable.getSort());
        }


        // Получаем информацию о бренде
        BrandClientDto brand = brandService.getBrandBySlug(slug);
        model.addAttribute("brand", brand);

        // Получаем продукты бренда с пагинацией
        BrandProductsPageResponseDto productsPage = brandService.getBrandDetailBySlug(slug, pageable);

        // Добавляем продукты
        model.addAttribute("products", productsPage.getProducts());

        // Добавляем полную информацию о пагинации
        model.addAttribute("currentPage", productsPage.getCurrentPage());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalElements", productsPage.getTotalElements());
        model.addAttribute("pageSize", productsPage.getPageSize());
        model.addAttribute("hasNext", productsPage.isHasNext());
        model.addAttribute("hasPrevious", productsPage.isHasPrevious());

        // Добавляем весь объект пагинации для удобства
        model.addAttribute("productsPagination", productsPage);

        return "client/brands/detail";
    }
}
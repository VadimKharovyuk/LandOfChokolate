package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.product.ProductListResponseDto;
import com.example.landofchokolate.service.ClientProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product")
public class ClientProductController {
    private final ClientProductService clientProductService;
    @GetMapping("/all")
    public String allProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Model model) {

        log.info("Getting all products page: {}, sortBy: {}, sortDirection: {}", page, sortBy, sortDirection);

        // Создаем объект сортировки
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Создаем объект Pageable (размер страницы уже установлен в сервисе)
        Pageable pageable = PageRequest.of(page, 20, sort);

        // Получаем данные через сервис
        ProductListResponseDto response = clientProductService.getAllProducts(pageable);

        // Добавляем данные в модель для отображения в шаблоне
        model.addAttribute("products", response.getProducts());
        model.addAttribute("totalCount", response.getTotalCount());
        model.addAttribute("hasNext", response.getHasNext());
        model.addAttribute("hasPrevious", response.getHasPrevious());
        model.addAttribute("currentPage", response.getCurrentPage());
        model.addAttribute("pageSize", response.getPageSize());

        // Добавляем новые атрибуты для навигации:
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("nextPage", response.getNextPage());
        model.addAttribute("previousPage", response.getPreviousPage());
        model.addAttribute("pageNumbers", response.getPageNumbers());

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);

        model.addAttribute("cartCount", 0);
        model.addAttribute("favoritesCount", 0);

        return "client/products/list";
    }
}
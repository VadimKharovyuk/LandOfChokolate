package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.brend.BrandFilterDto;
import com.example.landofchokolate.dto.brend.BrandProjection;
import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.product.ProductDetailDto;
import com.example.landofchokolate.dto.product.ProductFilterDto;
import com.example.landofchokolate.dto.product.ProductListResponseDto;
import com.example.landofchokolate.dto.product.ProductResponseDto;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ClientProductService;
import com.example.landofchokolate.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product")
public class ClientProductController {
    private final ClientProductService clientProductService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;



    @GetMapping("/{slug}")
    public String getProductDetail(@PathVariable String slug, Model model) {
        ProductDetailDto product = productService.getProductBySlug(slug);
        model.addAttribute("product", product);
        return "client/products/detail";
    }



    @GetMapping("/all")
    public String allProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            // Параметры фильтрации
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) String stockStatus,
            Model model) {

        log.info("Getting products page: {}, filters: searchName={}, minPrice={}, maxPrice={}",
                page, searchName, minPrice, maxPrice);

        // Создаем объект сортировки
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Создаем объект Pageable
        Pageable pageable = PageRequest.of(page, 20, sort);

        // Получаем данные через сервис с фильтрами
        ProductListResponseDto response = clientProductService.getAllProductsWithFilters(
                pageable, searchName, minPrice, maxPrice, category, brand, stockStatus);


        //для фильтра категорий
        List<CategoryResponseDto> cat = categoryService.getAllCategories();
        model.addAttribute("categories", cat);

        // Данные для фильтров
        List<BrandProjection> brandInfos = brandService.getBrandsForFilters();
        model.addAttribute("brands", brandInfos);

        // Добавляем данные в модель
        model.addAttribute("products", response.getProducts());
        model.addAttribute("totalCount", response.getTotalCount());
        model.addAttribute("hasNext", response.getHasNext());
        model.addAttribute("hasPrevious", response.getHasPrevious());
        model.addAttribute("currentPage", response.getCurrentPage());
        model.addAttribute("pageSize", response.getPageSize());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("nextPage", response.getNextPage());
        model.addAttribute("previousPage", response.getPreviousPage());
        model.addAttribute("pageNumbers", response.getPageNumbers());

        // Сортировка
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);

        // Фильтры (для сохранения состояния в форме)
        model.addAttribute("searchName", searchName);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("category", category);
        model.addAttribute("brand", brand);
        model.addAttribute("stockStatus", stockStatus);

        model.addAttribute("cartCount", 0);
        model.addAttribute("favoritesCount", 0);

        return "client/products/list";
    }

}
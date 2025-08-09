package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.brend.BrandFilterDto;
import com.example.landofchokolate.dto.brend.BrandProjection;
import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.product.*;
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
import java.util.Collections;
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

        // Создаем объект сортировки
        Sort sort;
        try {
            if ("desc".equalsIgnoreCase(sortDirection)) {
                sort = Sort.by(Sort.Direction.DESC, sortBy);
            } else {
                sort = Sort.by(Sort.Direction.ASC, sortBy);
            }
            log.debug("Created sort: {}", sort);
        } catch (Exception e) {
            log.warn("Error creating sort with sortBy={}, sortDirection={}. Using default sort.", sortBy, sortDirection, e);
            sort = Sort.by("name").ascending();
        }

        // Создаем Pageable с dummy размером - реальный размер будет установлен в сервисе
        Pageable pageable = PageRequest.of(page, 50, sort); // Передаем большой размер, сервис ограничит
        log.debug("Created pageable: page={}, sort={}", page, sort);

        try {
            // Получаем данные через сервис с фильтрами
            ProductListResponseDto response = clientProductService.getAllProductsWithFilters(
                    pageable, searchName, minPrice, maxPrice, category, brand, stockStatus);

            // Загружаем данные для фильтров
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandProjection> brands = brandService.getBrandsForFilters();

            // Добавляем данные в модель
            addSuccessDataToModel(model, response, categories, brands,
                    sortBy, sortDirection, searchName, minPrice, maxPrice,
                    category, brand, stockStatus);


        } catch (Exception e) {
            log.error("❌ Ошибка при получении товаров: {}", e.getMessage(), e);

            // В случае ошибки используем размер по умолчанию из сервиса (12)
            addErrorDataToModel(model, 12, sortBy, sortDirection,
                    searchName, minPrice, maxPrice, category, brand, stockStatus);
        }

        return "client/products/list";
    }

    /**
     * Добавление данных в модель при успешном выполнении
     */
    private void addSuccessDataToModel(Model model, ProductListResponseDto response,
                                       List<CategoryResponseDto> categories, List<BrandProjection> brands,
                                       String sortBy, String sortDirection, String searchName,
                                       BigDecimal minPrice, BigDecimal maxPrice, Long category,
                                       Long brand, String stockStatus) {

        // Данные товаров - размер берем из response (он будет установлен сервисом)
        model.addAttribute("products", response.getProducts());
        model.addAttribute("totalCount", response.getTotalCount());
        model.addAttribute("hasNext", response.getHasNext());
        model.addAttribute("hasPrevious", response.getHasPrevious());
        model.addAttribute("currentPage", response.getCurrentPage());
        model.addAttribute("pageSize", response.getPageSize()); // ✅ Реальный размер из сервиса
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("nextPage", response.getNextPage());
        model.addAttribute("previousPage", response.getPreviousPage());
        model.addAttribute("pageNumbers", response.getPageNumbers());

        // Данные для фильтров
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);

        // Параметры сортировки и фильтрации
        addCommonAttributes(model, sortBy, sortDirection, searchName, minPrice,
                maxPrice, category, brand, stockStatus);

        // Счетчики корзины и избранного
        model.addAttribute("cartCount", 0);
        model.addAttribute("favoritesCount", 0);
    }

    /**
     * Добавление данных в модель при ошибке
     */
    private void addErrorDataToModel(Model model, int pageSize, String sortBy, String sortDirection,
                                     String searchName, BigDecimal minPrice, BigDecimal maxPrice,
                                     Long category, Long brand, String stockStatus) {

        // Пустые данные с размером из сервиса
        model.addAttribute("products", Collections.emptyList());
        model.addAttribute("totalCount", 0);
        model.addAttribute("hasNext", false);
        model.addAttribute("hasPrevious", false);
        model.addAttribute("currentPage", 1);
        model.addAttribute("pageSize", pageSize); // Размер из сервиса (12)
        model.addAttribute("totalPages", 0);
        model.addAttribute("pageNumbers", Collections.emptyList());

        // Загружаем категории и бренды для фильтров (с обработкой ошибок)
        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandProjection> brands = brandService.getBrandsForFilters();
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
        } catch (Exception ex) {
            log.error("❌ Ошибка загрузки категорий/брендов: {}", ex.getMessage());
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("brands", Collections.emptyList());
        }

        // Параметры сортировки и фильтрации
        addCommonAttributes(model, sortBy, sortDirection, searchName, minPrice,
                maxPrice, category, brand, stockStatus);

        model.addAttribute("baseUrl", "https://landofchokolate.onrender.com");
        model.addAttribute("cartCount", 0);
        model.addAttribute("favoritesCount", 0);
    }

    /**
     * Общие атрибуты для сортировки и фильтрации
     */
    private void addCommonAttributes(Model model, String sortBy, String sortDirection,
                                     String searchName, BigDecimal minPrice, BigDecimal maxPrice,
                                     Long category, Long brand, String stockStatus) {

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
    }

//    @GetMapping("/all")
//    public String allProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "name") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDirection,
//            // Параметры фильтрации
//            @RequestParam(required = false) String searchName,
//            @RequestParam(required = false) BigDecimal minPrice,
//            @RequestParam(required = false) BigDecimal maxPrice,
//            @RequestParam(required = false) Long category,
//            @RequestParam(required = false) Long brand,
//            @RequestParam(required = false) String stockStatus,
//            Model model) {
//
//        // ИСПРАВЛЕНО: Создаем объект сортировки правильно
//        Sort sort;
//        try {
//            if ("desc".equalsIgnoreCase(sortDirection)) {
//                sort = Sort.by(Sort.Direction.DESC, sortBy);
//            } else {
//                sort = Sort.by(Sort.Direction.ASC, sortBy);
//            }
//            log.debug("Created sort: {}", sort);
//        } catch (Exception e) {
//            log.warn("Error creating sort with sortBy={}, sortDirection={}. Using default sort.", sortBy, sortDirection, e);
//            sort = Sort.by("name").ascending();
//        }
//
//        // Создаем объект Pageable с правильным размером
//        Pageable pageable = PageRequest.of(page, 30, sort);
//        log.debug("Created pageable: {}", pageable);
//
//        try {
//            // Получаем данные через сервис с фильтрами
//            ProductListResponseDto response = clientProductService.getAllProductsWithFilters(
//                    pageable, searchName, minPrice, maxPrice, category, brand, stockStatus);
//
//            //для фильтра категорий
//            List<CategoryResponseDto> cat = categoryService.getAllCategories();
//            model.addAttribute("categories", cat);
//
//            // Данные для фильтров
//            List<BrandProjection> brandInfos = brandService.getBrandsForFilters();
//            model.addAttribute("brands", brandInfos);
//
//            // Добавляем данные в модель
//            model.addAttribute("products", response.getProducts());
//            model.addAttribute("totalCount", response.getTotalCount());
//            model.addAttribute("hasNext", response.getHasNext());
//            model.addAttribute("hasPrevious", response.getHasPrevious());
//            model.addAttribute("currentPage", response.getCurrentPage());
//            model.addAttribute("pageSize", response.getPageSize());
//            model.addAttribute("totalPages", response.getTotalPages());
//            model.addAttribute("nextPage", response.getNextPage());
//            model.addAttribute("previousPage", response.getPreviousPage());
//            model.addAttribute("pageNumbers", response.getPageNumbers());
//
//            // Сортировка
//            model.addAttribute("sortBy", sortBy);
//            model.addAttribute("sortDirection", sortDirection);
//
//            // Фильтры (для сохранения состояния в форме)
//            model.addAttribute("searchName", searchName);
//            model.addAttribute("minPrice", minPrice);
//            model.addAttribute("maxPrice", maxPrice);
//            model.addAttribute("category", category);
//            model.addAttribute("brand", brand);
//            model.addAttribute("stockStatus", stockStatus);
//
//            model.addAttribute("cartCount", 0);
//            model.addAttribute("favoritesCount", 0);
//
//        } catch (Exception e) {
//            log.error("Error getting products", e);
//
//            // В случае ошибки показываем пустой результат
//            model.addAttribute("products", Collections.emptyList());
//            model.addAttribute("totalCount", 0);
//            model.addAttribute("hasNext", false);
//            model.addAttribute("hasPrevious", false);
//            model.addAttribute("currentPage", 1);
//            model.addAttribute("pageSize", 6);
//            model.addAttribute("totalPages", 0);
//            model.addAttribute("pageNumbers", Collections.emptyList());
//
//            // Сохраняем параметры
//            model.addAttribute("sortBy", sortBy);
//            model.addAttribute("sortDirection", sortDirection);
//            model.addAttribute("searchName", searchName);
//            model.addAttribute("minPrice", minPrice);
//            model.addAttribute("maxPrice", maxPrice);
//            model.addAttribute("category", category);
//            model.addAttribute("brand", brand);
//            model.addAttribute("stockStatus", stockStatus);
//
//            model.addAttribute("baseUrl", "https://landofchokolate.onrender.com");
//
//            // Загружаем категории и бренды для фильтров
//            try {
//                List<CategoryResponseDto> cat = categoryService.getAllCategories();
//                model.addAttribute("categories", cat);
//                List<BrandProjection> brandInfos = brandService.getBrandsForFilters();
//                model.addAttribute("brands", brandInfos);
//            } catch (Exception ex) {
//                log.error("Error loading categories/brands", ex);
//                model.addAttribute("categories", Collections.emptyList());
//                model.addAttribute("brands", Collections.emptyList());
//            }
//
//            model.addAttribute("cartCount", 0);
//            model.addAttribute("favoritesCount", 0);
//        }
//
//
//        return "client/products/list";
//    }



    @GetMapping("/{slug}")
    public String getProductDetail(@PathVariable String slug, Model model) {

        ProductDetailDto product = productService.getProductBySlug(slug);
        model.addAttribute("product", product);

        ///похожие товары
        List<RelatedProductDto> relatedProducts = productService.getRelatedProducts(slug, 8);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("baseUrl", "https://landofchokolate.onrender.com");

        return "client/products/detail";
    }



}
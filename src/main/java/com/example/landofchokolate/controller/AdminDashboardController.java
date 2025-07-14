package com.example.landofchokolate.controller;

import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping
    public String adminDashboard(Model model) {
        log.info("Loading admin dashboard");

        try {
            // Получаем статистику продуктов
            ProductService.ProductStatistics productStats = productService.getProductStatistics();

            // Основные метрики продуктов
            model.addAttribute("totalProducts", productStats.getTotalProducts());
            model.addAttribute("inStockProducts", productStats.getInStockProducts());
            model.addAttribute("outOfStockProducts", productStats.getOutOfStockProducts());
            model.addAttribute("lowStockProducts", productStats.getLowStockProducts());
            model.addAttribute("totalInventoryValue", productStats.getTotalInventoryValue());

            // Рассчитываем среднюю цену товара
            BigDecimal averagePrice = BigDecimal.ZERO;
            if (productStats.getTotalProducts() > 0 && productStats.getTotalInventoryValue().compareTo(BigDecimal.ZERO) > 0) {
                averagePrice = productStats.getTotalInventoryValue()
                        .divide(BigDecimal.valueOf(productStats.getTotalProducts()), 2, RoundingMode.HALF_UP);
            }
            model.addAttribute("averagePrice", averagePrice);

            // Получаем статистику категорий
            long totalCategories = categoryService.getAllCategories().size();
            model.addAttribute("totalCategories", totalCategories);

            // Последняя добавленная категория (если есть)
            String lastCategory = "Нет";
            try {
                var categories = categoryService.getAllCategories();
                if (!categories.isEmpty()) {
                    lastCategory = categories.get(categories.size() - 1).getName();
                }
            } catch (Exception e) {
                log.warn("Could not get last category: {}", e.getMessage());
            }
            model.addAttribute("lastCategory", lastCategory);

            // Получаем статистику брендов
            long totalBrands = brandService.getAllBrands().size();
            model.addAttribute("totalBrands", totalBrands);

            // Количество брендов с логотипами
            long brandsWithLogos = brandService.getAllBrands().stream()
                    .mapToLong(brand -> (brand.getImageUrl() != null && !brand.getImageUrl().isEmpty()) ? 1 : 0)
                    .sum();
            model.addAttribute("brandsWithLogos", brandsWithLogos);

            log.info("Admin dashboard loaded successfully - Products: {}, Categories: {}, Brands: {}",
                    productStats.getTotalProducts(), totalCategories, totalBrands);

        } catch (Exception e) {
            log.error("Error loading admin dashboard statistics: {}", e.getMessage(), e);

            // Устанавливаем значения по умолчанию при ошибке
            model.addAttribute("totalProducts", 0L);
            model.addAttribute("inStockProducts", 0L);
            model.addAttribute("outOfStockProducts", 0L);
            model.addAttribute("lowStockProducts", 0L);
            model.addAttribute("totalInventoryValue", BigDecimal.ZERO);
            model.addAttribute("averagePrice", BigDecimal.ZERO);
            model.addAttribute("totalCategories", 0L);
            model.addAttribute("lastCategory", "Ошибка загрузки");
            model.addAttribute("totalBrands", 0L);
            model.addAttribute("brandsWithLogos", 0L);

            model.addAttribute("errorMessage", "Ошибка при загрузке статистики: " + e.getMessage());
        }

        return "admin/admin-dashboard";
    }

}
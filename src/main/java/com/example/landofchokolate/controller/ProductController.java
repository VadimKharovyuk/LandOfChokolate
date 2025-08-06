package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.product.*;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;



    @GetMapping
    public String productCreateForm(Model model) {
        model.addAttribute("product", new CreateProductDto());
        loadFormData(model);
        return "admin/product/product-form";
    }

    @PostMapping
    public String productCreate(@ModelAttribute("product") @Valid CreateProductDto createProductDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred while creating product: {}", bindingResult.getAllErrors());
            loadFormData(model);
            return "admin/product/product-form";
        }

        try {
            ProductResponseDto createdProduct = productService.createProduct(createProductDto);
            log.info("Product created successfully with id: {}", createdProduct.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Продукт '" + createdProduct.getName() + "' успешно создан!");

        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании продукта: " + e.getMessage());
        }

        return "redirect:/admin/product/list";
    }

    @GetMapping("/list")
    public String productList(Model model,
                              @RequestParam(name = "category", required = false) Long categoryId,
                              @RequestParam(name = "brand", required = false) Long brandId,
                              @RequestParam(name = "search", required = false) String searchTerm,
                              @RequestParam(name = "status", required = false) String status) {

        List<ProductListDto> products;
        String filterDescription = "Все продукты";

        try {
            // Применяем фильтры
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                products = productService.searchProductsByName(searchTerm.trim());
                filterDescription = "Поиск по: \"" + searchTerm + "\"";
            } else if (categoryId != null) {
                products = productService.getProductsByCategory(categoryId);
                CategoryResponseDto category = categoryService.getCategoryById(categoryId);
                filterDescription = "Категория: " + category.getName();
            } else if (brandId != null) {
                products = productService.getProductsByBrand(brandId);
                BrandResponseDto brand = brandService.getBrandById(brandId);
                filterDescription = "Бренд: " + brand.getName();
            } else if ("in-stock".equals(status)) {
                products = productService.getProductsInStock();
                filterDescription = "В наличии";
            } else if ("low-stock".equals(status)) {
                products = productService.getProductsWithLowStock();
                filterDescription = "Заканчивается";
            } else if ("out-of-stock".equals(status)) {
                products = productService.getOutOfStockProducts();
                filterDescription = "Нет в наличии";
            } else {
                products = productService.getAllProducts();
            }

            model.addAttribute("products", products);
            model.addAttribute("filterDescription", filterDescription);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("selectedCategory", categoryId);
            model.addAttribute("selectedBrand", brandId);
            model.addAttribute("selectedStatus", status);

            // Данные для фильтров
            loadFilterData(model);

        } catch (Exception e) {
            log.error("Error loading products: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Ошибка при загрузке продуктов: " + e.getMessage());
            model.addAttribute("products", List.of());
            loadFilterData(model);
        }

        return "admin/product/product-list";
    }



    @GetMapping("/edit/{id}")
    public String productEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto product = productService.getProductById(id);

            UpdateProductDto editDto = new UpdateProductDto();
            editDto.setName(product.getName());
            editDto.setPrice(product.getPrice());
            editDto.setStockQuantity(product.getStockQuantity());
            editDto.setCategoryId(product.getCategory().getId());
            editDto.setBrandId(product.getBrand().getId());
            editDto.setIsRecommendation(product.getIsRecommendation());

            editDto.setDescription(product.getDescription());
            editDto.setMetaTitle(product.getMetaTitle());
            editDto.setMetaDescription(product.getMetaDescription());


            model.addAttribute("product", editDto);
            model.addAttribute("productId", id);
            model.addAttribute("currentImageUrl", product.getImageUrl());
            model.addAttribute("isEdit", true);
            loadFormData(model);

            return "admin/product/product-form";

        } catch (Exception e) {
            log.error("Error loading product for edit with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Продукт с ID " + id + " не найден!");
            return "redirect:/admin/product/list";
        }
    }

    @PostMapping(value = "/edit/{id}", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public String productUpdate(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam BigDecimal price,
                                @RequestParam Integer stockQuantity,
                                @RequestParam Long categoryId,
                                @RequestParam Long brandId,
                                @RequestParam(required = false, defaultValue = "false") Boolean isRecommendation,
                                @RequestParam(required = false) String metaTitle,
                                @RequestParam(required = false) String metaDescription,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) MultipartFile image, // Необязательно!
                                @RequestParam(required = false, defaultValue = "false") Boolean removeCurrentImage,
                                RedirectAttributes redirectAttributes) {

        // Создаем DTO вручную
        UpdateProductDto updateProductDto = new UpdateProductDto();
        updateProductDto.setName(name);
        updateProductDto.setPrice(price);
        updateProductDto.setStockQuantity(stockQuantity);
        updateProductDto.setCategoryId(categoryId);
        updateProductDto.setBrandId(brandId);
        updateProductDto.setIsRecommendation(isRecommendation);
        updateProductDto.setMetaTitle(metaTitle);
        updateProductDto.setMetaDescription(metaDescription);
        updateProductDto.setDescription(description);

        // Устанавливаем изображение только если оно есть
        if (image != null && !image.isEmpty()) {
            updateProductDto.setImage(image);
        }
        updateProductDto.setRemoveCurrentImage(removeCurrentImage);

        try {
            ProductResponseDto updatedProduct = productService.updateProduct(id, updateProductDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Продукт '" + updatedProduct.getName() + "' успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении продукта: " + e.getMessage());
        }


        return "redirect:/admin/product/list";
    }


    @GetMapping("/delete/{id}")
    public String productDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto product = productService.getProductById(id);
            productService.deleteProduct(id);

            log.info("Product deleted successfully with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Продукт '" + product.getName() + "' успешно удален!");

        } catch (Exception e) {
            log.error("Error deleting product with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении продукта: " + e.getMessage());
        }

        return "redirect:/admin/product/list";
    }

    @GetMapping("/view/{id}")
    public String productView(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto product = productService.getProductById(id);
            model.addAttribute("product", product);
            return "admin/product/product-view";

        } catch (Exception e) {
            log.error("Error loading product view with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Продукт с ID " + id + " не найден!");
            return "redirect:/admin/product/list";
        }
    }

    @PostMapping("/stock/update/{id}")
    public String updateStock(@PathVariable Long id,
                              @RequestParam("quantity") Integer quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.updateStock(id, quantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Количество товара успешно обновлено!");

        } catch (Exception e) {
            log.error("Error updating stock for product {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении количества: " + e.getMessage());
        }

        return "redirect:/admin/product/view/" + id;
    }

    @GetMapping("/statistics")
    public String productStatistics(Model model) {
        try {
            ProductService.ProductStatistics stats = productService.getProductStatistics();
            model.addAttribute("statistics", stats);

            // Дополнительные данные для дашборда
            List<ProductListDto> lowStockProducts = productService.getProductsWithLowStock();
            List<ProductListDto> outOfStockProducts = productService.getOutOfStockProducts();

            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);

            return "admin/product/product-statistics";

        } catch (Exception e) {
            log.error("Error loading product statistics: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Ошибка при загрузке статистики: " + e.getMessage());
            return "admin/product/product-statistics";
        }
    }

    @GetMapping("/click")
    public String click(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ProductListClickDto> clickDtoPagedResponse =
                productService.getProductsClick(pageable);

        model.addAttribute("clickDtoPagedResponse", clickDtoPagedResponse);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        return "admin/product/product-click";
    }

    /**
     * Загружает данные для форм (категории и бренды)
     */
    private void loadFormData(Model model) {
        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();

            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
        } catch (Exception e) {
            log.error("Error loading form data: {}", e.getMessage(), e);
            model.addAttribute("categories", List.of());
            model.addAttribute("brands", List.of());
        }
    }

    /**
     * Загружает данные для фильтров
     */
    private void loadFilterData(Model model) {
        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();

            model.addAttribute("filterCategories", categories);
            model.addAttribute("filterBrands", brands);
        } catch (Exception e) {
            log.error("Error loading filter data: {}", e.getMessage(), e);
            model.addAttribute("filterCategories", List.of());
            model.addAttribute("filterBrands", List.of());
        }
    }



//    // Добавить в контроллер кнопку или вызвать один раз:
//    @PostConstruct
//    public void init() {
//        productService.generateMissingSlugForAllProducts();
//    }
}
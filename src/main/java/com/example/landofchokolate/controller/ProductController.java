package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.category.CategoryResponseDto;
import com.example.landofchokolate.dto.product.*;
import com.example.landofchokolate.enums.PriceUnit;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
        model.addAttribute("priceUnits", PriceUnit.values());

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
            model.addAttribute("priceUnits", PriceUnit.values());
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


    @GetMapping("/edit/{id}")
    public String productEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto product = productService.getProductById(id);

            UpdateProductDto editDto = new UpdateProductDto();
            editDto.setName(product.getName());
            editDto.setPrice(product.getPrice());
            editDto.setPriceUnit(product.getPriceUnit());
            editDto.setStockQuantity(product.getStockQuantity());
            editDto.setCategoryId(product.getCategory().getId());
            editDto.setBrandId(product.getBrand().getId());
            editDto.setIsRecommendation(product.getIsRecommendation());

            // 🆕 Добавили недостающие поля:
            editDto.setMetaTitle(product.getMetaTitle());
            editDto.setMetaDescription(product.getMetaDescription());
            editDto.setDescription(product.getDescription());

            model.addAttribute("product", editDto);
            model.addAttribute("productId", id);
            model.addAttribute("priceUnits", PriceUnit.values());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("brands", brandService.findAll());

            // ✅ ИСПОЛЬЗУЕМ ОТДЕЛЬНУЮ ФОРМУ
            return "admin/product/product-edit-form";

        } catch (Exception e) {
            log.error("Error loading product for edit with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Продукт не найден!");
            return "redirect:/admin/product/list";
        }
    }


    // ✅ ПРОСТОЙ POST метод БЕЗ multipart
    @PostMapping("/edit/{id}")
    public String productUpdate(@PathVariable Long id,
                                @ModelAttribute("product") @Valid UpdateProductDto updateProductDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("priceUnits", PriceUnit.values());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("brands", brandService.findAll());
            return "admin/product/product-edit-form";
        }

        try {
            ProductResponseDto updatedProduct = productService.updateProduct(id, updateProductDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Продукт '" + updatedProduct.getName() + "' успешно обновлен!");
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении продукта: " + e.getMessage());
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
                    "Ошибка при удалению продукта: " + e.getMessage());
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

    // 🆕 НОВЫЕ МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ ИЗОБРАЖЕНИЯМИ

    /**
     * Страница управления изображениями продукта
     */
    @GetMapping("/{id}/images")
    public String manageProductImages(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto product = productService.getProductById(id);
            model.addAttribute("product", product);
            return "admin/product/product-images";

        } catch (Exception e) {
            log.error("Error loading product images for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Продукт с ID " + id + " не найден!");
            return "redirect:/admin/product/list";
        }
    }

    /**
     * Добавить изображение к продукту
     */
    @PostMapping("/{id}/images/add")
    public String addProductImage(@PathVariable Long id,
                                  @RequestParam("image") MultipartFile imageFile,
                                  @RequestParam(value = "altText", required = false) String altText,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Выберите файл изображения!");
                return "redirect:/admin/product/" + id + "/images";
            }

            productService.addProductImage(id, imageFile, altText);
            redirectAttributes.addFlashAttribute("successMessage", "Изображение успешно добавлено!");

        } catch (Exception e) {
            log.error("Error adding image to product {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при добавлении изображения: " + e.getMessage());
        }

        return "redirect:/admin/product/" + id + "/images";
    }

    /**
     * Удалить изображение продукта
     */
    @PostMapping("/{productId}/images/{imageId}/delete")
    public String removeProductImage(@PathVariable Long productId,
                                     @PathVariable Long imageId,
                                     RedirectAttributes redirectAttributes) {
        try {
            productService.removeProductImage(productId, imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Изображение успешно удалено!");

        } catch (Exception e) {
            log.error("Error removing image {} from product {}: {}", imageId, productId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении изображения: " + e.getMessage());
        }

        return "redirect:/admin/product/" + productId + "/images";
    }

    /**
     * Установить главное изображение
     */
    @PostMapping("/{productId}/images/{imageId}/set-main")
    public String setMainImage(@PathVariable Long productId,
                               @PathVariable Long imageId,
                               RedirectAttributes redirectAttributes) {
        try {
            productService.setMainImage(productId, imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Главное изображение установлено!");

        } catch (Exception e) {
            log.error("Error setting main image {} for product {}: {}", imageId, productId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при установке главного изображения: " + e.getMessage());
        }

        return "redirect:/admin/product/" + productId + "/images";
    }

    /**
     * AJAX endpoint для удаления изображения
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseBody
    public ResponseEntity<?> removeProductImageAjax(@PathVariable Long productId,
                                                    @PathVariable Long imageId) {
        try {
            productService.removeProductImage(productId, imageId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error removing image {} from product {}: {}", imageId, productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при удалении изображения: " + e.getMessage());
        }
    }

    /**
     * AJAX endpoint для установки главного изображения
     */
    @PutMapping("/{productId}/images/{imageId}/set-main")
    @ResponseBody
    public ResponseEntity<?> setMainImageAjax(@PathVariable Long productId,
                                              @PathVariable Long imageId) {
        try {
            productService.setMainImage(productId, imageId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error setting main image {} for product {}: {}", imageId, productId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при установке главного изображения: " + e.getMessage());
        }
    }

    // ОСТАЛЬНЫЕ МЕТОДЫ БЕЗ ИЗМЕНЕНИЙ

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

//    @PostConstruct
//    public void init() {
//        productService.generateMissingSlugForAllProducts();
//    }
}
package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.category.CategoryListPublicDto;
import com.example.landofchokolate.dto.category.CategoryPublicDto;
import com.example.landofchokolate.exception.CategoryNotFoundException;
import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class ClientCategoryController {
    private final CategoryService categoryService;
    private final ProductService productService;


    @GetMapping()
    public String categoriesList(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size,
                                 Model model) {

        CategoryListPublicDto categoryList = categoryService.getPublicCategories(page, size);
        model.addAttribute("categoryList", categoryList);
        return "client/categories/list";
    }


    @GetMapping("/{categorySlug}")
    public String categoryProducts(@PathVariable String categorySlug,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   Model model) {

        Category category = categoryService.findBySlug(categorySlug);
        if (category == null) {
            throw new CategoryNotFoundException("Category not found: " + categorySlug);
        }

        Page<Product> productPage = productService.getProductsByCategoryPage(category.getId(), page, size);

        model.addAttribute("category", category);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "client/categories/products";
    }

}

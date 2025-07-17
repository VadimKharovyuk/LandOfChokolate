package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.category.CategoryListPublicDto;
import com.example.landofchokolate.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class ClientCategoryController {
    private final CategoryService categoryService;


    @GetMapping()
    public String categoriesList(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size,
                                 Model model) {

        CategoryListPublicDto categoryList = categoryService.getPublicCategories(page, size);
        model.addAttribute("categoryList", categoryList);
        return "client/categories/list";
    }

}

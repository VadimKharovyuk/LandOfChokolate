package com.example.landofchokolate.controller;
import com.example.landofchokolate.dto.brend.BrandClientDto;
import com.example.landofchokolate.dto.category.CategoryPublicDto;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping
    public String home(Model model) {
        final int BRANDS_LIMIT = 12;
        final int CATEGORIES_LIMIT = 12;

        List<BrandClientDto> topBrands = brandService.getBrandByLimit(BRANDS_LIMIT);
        model.addAttribute("brands", topBrands);

        List<CategoryPublicDto> topCategories = categoryService.getTopCategories(CATEGORIES_LIMIT);
        model.addAttribute("topCategories", topCategories);

        return "homeV1";
    }
}

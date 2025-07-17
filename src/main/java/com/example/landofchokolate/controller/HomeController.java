package com.example.landofchokolate.controller;
import com.example.landofchokolate.dto.category.CategoryPublicDto;
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

    @GetMapping
    public String home(Model model) {

        // Получаем топ  категорий для главной
        List<CategoryPublicDto> topCategories = categoryService.getTopCategories(12);
        model.addAttribute("topCategories", topCategories);
        return "homeV1";
    }
}

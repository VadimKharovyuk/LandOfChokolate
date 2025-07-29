package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.product.ProductListDto;
import com.example.landofchokolate.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchPageController {
    private final ProductService productService;

    @GetMapping
    public String searchPage(Model model,
                             @RequestParam(required = false) String searchTerm) {

        // Добавляем результаты поиска только если есть поисковый запрос
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<ProductListDto> productListDtos = productService.searchProductsByName(searchTerm.trim());
            model.addAttribute("productListDtos", productListDtos);
        }

        return "client/search/index";
    }

}

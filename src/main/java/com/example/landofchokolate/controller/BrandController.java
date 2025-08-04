package com.example.landofchokolate.controller;

import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.brend.CreateBrandDto;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.service.BrandService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/brand")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public String brandCreateForm(Model model) {
        model.addAttribute("brand", new CreateBrandDto());
        return "admin/brand/brand-form";
    }

    @PostMapping
    public String brandCreate(@ModelAttribute("brand") @Valid CreateBrandDto createBrandDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred while creating brand: {}", bindingResult.getAllErrors());
            return "admin/brand/brand-form";
        }

        try {
            BrandResponseDto createdBrand = brandService.createBrand(createBrandDto);
            log.info("Brand created successfully with id: {}", createdBrand.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Бренд '" + createdBrand.getName() + "' успешно создан!");

        } catch (Exception e) {
            log.error("Error creating brand: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при создании бренда: " + e.getMessage());
        }

        return "redirect:/admin/brand/list";
    }

    @GetMapping("/list")
    public String brandList(Model model) {
        List<BrandResponseDto> brands = brandService.getAllBrands();
        model.addAttribute("brands", brands);
        return "admin/brand/brand-list";
    }

    @GetMapping("/edit/{id}")
    public String brandEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BrandResponseDto brand = brandService.getBrandById(id);
            CreateBrandDto editDto = new CreateBrandDto();
            editDto.setName(brand.getName());
            editDto.setDescription(brand.getDescription());
            editDto.setShortDescription(brand.getShortDescription());


            model.addAttribute("brand", editDto);
            model.addAttribute("brandId", id);
            model.addAttribute("currentImageUrl", brand.getImageUrl()); // Для отображения текущего изображения
            model.addAttribute("isEdit", true);

            return "admin/brand/brand-form";

        } catch (Exception e) {
            log.error("Error loading brand for edit with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Бренд с ID " + id + " не найден!");
            return "redirect:/admin/brand/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String brandUpdate(@PathVariable Long id,
                              @ModelAttribute("brand") @Valid CreateBrandDto updateBrandDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred while updating brand with id: {}", id);

            // Получаем текущее изображение для отображения при ошибке валидации
            try {
                BrandResponseDto currentBrand = brandService.getBrandById(id);
                model.addAttribute("currentImageUrl", currentBrand.getImageUrl());
            } catch (Exception e) {
                log.warn("Could not load current image for brand {}", id);
            }

            model.addAttribute("brandId", id);
            model.addAttribute("isEdit", true);
            return "admin/brand/brand-form";
        }

        try {
            BrandResponseDto updatedBrand = brandService.updateBrand(id, updateBrandDto);
            log.info("Brand updated successfully with id: {}", updatedBrand.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Бренд '" + updatedBrand.getName() + "' успешно обновлен!");

        } catch (Exception e) {
            log.error("Error updating brand with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении бренда: " + e.getMessage());
        }

        return "redirect:/admin/brand/list";
    }

    @GetMapping("/delete/{id}")
    public String brandDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BrandResponseDto brand = brandService.getBrandById(id);
            brandService.deleteBrand(id);

            log.info("Brand deleted successfully with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Бренд '" + brand.getName() + "' успешно удален!");

        } catch (Exception e) {
            log.error("Error deleting brand with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении бренда: " + e.getMessage());
        }

        return "redirect:/admin/brand/list";
    }


//        @PostConstruct
//    public void init() {
//        brandService.generateMissingSlugForBrands();
//    }
}
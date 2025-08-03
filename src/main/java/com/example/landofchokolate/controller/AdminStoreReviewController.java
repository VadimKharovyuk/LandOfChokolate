package com.example.landofchokolate.controller;
import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import com.example.landofchokolate.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/reviews")
public class AdminStoreReviewController {

    private final StoreReviewService storeReviewService;

    @GetMapping
    public String adminReviewsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
        PagedResponse<StoreReviewResponseDTO> reviews = storeReviewService.getAllReviews(pageable);

        model.addAttribute("pagedResponse", reviews);
        return "admin/reviews/list";
    }


    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            storeReviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Відгук успішно видалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення відгуку");
        }
        return "redirect:/admin/reviews";
    }
}

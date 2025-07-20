package com.example.landofchokolate.controller.client;
import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public String wishlistPage(HttpSession session, Model model) {
        try {
            WishlistDto wishlist = wishlistService.getWishlistDto(session);
            model.addAttribute("wishlist", wishlist);
            model.addAttribute("pageTitle", "Избранное");
            return "client/wishlist/wishlist";
        } catch (Exception e) {
            log.error("Ошибка при загрузке страницы избранного", e);
            model.addAttribute("error", "Произошла ошибка при загрузке избранного");
            return "error/error";
        }
    }

    /**
     * API: Добавить товар в избранное
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "unknown") String addedFromPage,
            HttpSession session) {


        log.info("Получен запрос на добавление в избранное: productId={}, addedFromPage={}", productId, addedFromPage);


        Map<String, Object> response = new HashMap<>();

        try {
            wishlistService.addProduct(session, productId, addedFromPage);

            response.put("success", true);
            response.put("message", "Товар добавлен в избранное");
            response.put("itemCount", wishlistService.getWishlistItemCount(session));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка при добавлении товара {} в избранное", productId, e);

            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Удалить товар из избранного
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeProduct(
            @RequestParam Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            wishlistService.removeProduct(session, productId);

            response.put("success", true);
            response.put("message", "Товар удален из избранного");
            response.put("itemCount", wishlistService.getWishlistItemCount(session));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка при удалении товара {} из избранного", productId, e);

            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Переключить товар в избранном
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleProduct(
            @RequestParam Long productId,
            @RequestParam(required = false, defaultValue = "unknown") String addedFromPage,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean wasInWishlist = wishlistService.isProductInWishlist(session, productId);
            wishlistService.toggleProduct(session, productId, addedFromPage);
            boolean isInWishlist = wishlistService.isProductInWishlist(session, productId);

            response.put("success", true);
            response.put("isInWishlist", isInWishlist);
            response.put("message", isInWishlist ? "Товар добавлен в избранное" : "Товар удален из избранного");
            response.put("itemCount", wishlistService.getWishlistItemCount(session));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ошибка при переключении товара {} в избранном", productId, e);

            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Проверить, находится ли товар в избранном
     */
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkProduct(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isInWishlist = wishlistService.isProductInWishlist(session, productId);

            response.put("success", true);
            response.put("isInWishlist", isInWishlist);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при проверке товара {} в избранном", productId, e);

            response.put("success", false);
            response.put("isInWishlist", false);

            return ResponseEntity.ok(response);
        }
    }

    /**
     * API: Проверить несколько товаров в избранном
     */
    @PostMapping("/check-multiple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkMultipleProducts(
            @RequestBody List<Long> productIds,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<Long, Boolean> results = wishlistService.checkProductsInWishlist(session, productIds);

            response.put("success", true);
            response.put("results", results);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при проверке товаров в избранном", e);

            response.put("success", false);
            response.put("results", new HashMap<>());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * API: Очистить избранное
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearWishlist(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            wishlistService.clearWishlist(session);

            response.put("success", true);
            response.put("message", "Избранное очищено");
            response.put("itemCount", 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при очистке избранного", e);

            response.put("success", false);
            response.put("message", "Произошла ошибка при очистке избранного");

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Получить количество товаров в избранном
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            int itemCount = wishlistService.getWishlistItemCount(session);

            response.put("success", true);
            response.put("itemCount", itemCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при получении количества товаров в избранном", e);

            response.put("success", false);
            response.put("itemCount", 0);

            return ResponseEntity.ok(response);
        }
    }

    /**
     * API: Получить данные избранного для AJAX
     */
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<WishlistDto> getWishlistData(HttpSession session) {
        try {
            WishlistDto wishlist = wishlistService.getWishlistDto(session);
            return ResponseEntity.ok(wishlist);

        } catch (Exception e) {
            log.error("Ошибка при получении данных избранного", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

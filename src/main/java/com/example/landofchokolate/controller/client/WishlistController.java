package com.example.landofchokolate.controller.client;
import com.example.landofchokolate.dto.wishlis.WishlistDto;
import com.example.landofchokolate.model.Wishlist;
import com.example.landofchokolate.repository.WishlistRepository;
import com.example.landofchokolate.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final WishlistRepository wishlistRepository;

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
            HttpSession session) {


        Map<String, Object> response = new HashMap<>();

        try {
            wishlistService.addProduct(session, productId);

            response.put("success", true);
            response.put("message", "Товар додано до обраного");
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
            response.put("message", "Товар видалено з обраного");
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
            response.put("message", isInWishlist ? "Товар додано до обраного" : "Товар видалено з обраного");
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
            log.error("Помилка при перевірці товару {} в обраному", productId, e);

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
            response.put("message", "Обране очищено");
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









    @GetMapping("/debug")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDebugInfo(HttpSession session) {
        try {
            Map<String, Object> debugInfo = wishlistService.getDebugInfo(session);

            // Добавляем информацию о сессии
            debugInfo.put("sessionId", session.getId());
            debugInfo.put("sessionCreationTime", new Date(session.getCreationTime()));
            debugInfo.put("sessionLastAccessedTime", new Date(session.getLastAccessedTime()));
            debugInfo.put("sessionMaxInactiveInterval", session.getMaxInactiveInterval());

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            log.error("Ошибка при получении отладочной информации", e);

            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("sessionId", session.getId());

            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * API: Расширенная отладочная информация
     */
    @GetMapping("/debug/enhanced")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEnhancedDebugInfo(HttpSession session) {
        try {
            Map<String, Object> debugInfo = wishlistService.getEnhancedDebugInfo(session);

            // Добавляем информацию о сессии
            debugInfo.put("sessionId", session.getId());
            debugInfo.put("sessionCreationTime", new Date(session.getCreationTime()));
            debugInfo.put("sessionLastAccessedTime", new Date(session.getLastAccessedTime()));

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            log.error("Ошибка при получении расширенной отладочной информации", e);

            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(errorInfo);
        }
    }

    /**
     * API: Принудительное пересоздание wishlist
     */
    @PostMapping("/debug/recreate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> forceRecreateWishlist(HttpSession session) {
        try {
            Map<String, Object> result = wishlistService.forceRecreateWishlist(session);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Ошибка при пересоздании wishlist", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());

            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * API: Поиск потерянных wishlist
     */
    @GetMapping("/debug/orphaned")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> findOrphanedWishlists() {
        try {
            List<Map<String, Object>> orphaned = wishlistService.findOrphanedWishlists();
            return ResponseEntity.ok(orphaned);

        } catch (Exception e) {
            log.error("Ошибка при поиске потерянных wishlist", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * API: Проверка конкретного UUID в базе данных
     */
    @GetMapping("/debug/check-uuid/{uuid}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUuidInDatabase(@PathVariable String uuid) {
        try {
            Map<String, Object> result = new HashMap<>();

            // Проверяем в БД
            Optional<Wishlist> wishlist = wishlistRepository.findByWishlistUuid(uuid);

            if (wishlist.isPresent()) {
                Wishlist w = wishlist.get();
                result.put("found", true);
                result.put("status", w.getStatus().toString());
                result.put("createdAt", w.getCreatedAt());
                result.put("expiresAt", w.getExpiresAt());
                result.put("itemCount", w.getItems() != null ? w.getItems().size() : 0);
                result.put("expired", w.getExpiresAt() != null && LocalDateTime.now().isAfter(w.getExpiresAt()));

                // Показываем товары
                if (w.getItems() != null && !w.getItems().isEmpty()) {
                    List<Map<String, Object>> items = w.getItems().stream()
                            .map(item -> {
                                Map<String, Object> itemInfo = new HashMap<>();
                                itemInfo.put("productId", item.getProduct() != null ? item.getProduct().getId() : null);
                                itemInfo.put("addedAt", item.getAddedAt());
                                return itemInfo;
                            })
                            .collect(Collectors.toList());
                    result.put("items", items);
                }
            } else {
                result.put("found", false);
                result.put("message", "Wishlist с UUID " + uuid + " не найден в базе данных");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Ошибка при проверке UUID в БД", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());

            return ResponseEntity.ok(errorResult);
        }
    }

    @PostMapping("/sync-cookie")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> syncCookieWithSession(HttpSession session) {
        try {
            Map<String, Object> result = wishlistService.syncCookieWithSession(session);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Ошибка при синхронизации cookie", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());

            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * API: Обновление cookie для текущего wishlist
     */
    @PostMapping("/update-cookie")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWishlistCookie(HttpSession session) {
        try {
            wishlistService.updateWishlistCookie(session);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cookie обновлен");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Ошибка при обновлении cookie", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());

            return ResponseEntity.ok(errorResult);
        }
    }
}

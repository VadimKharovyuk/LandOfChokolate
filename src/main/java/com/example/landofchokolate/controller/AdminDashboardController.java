package com.example.landofchokolate.controller;
import com.example.landofchokolate.enums.AdminType;
import com.example.landofchokolate.model.User;
import com.example.landofchokolate.repository.UserRepository;
import com.example.landofchokolate.service.AdminOrderService;
import com.example.landofchokolate.service.BrandService;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import com.example.landofchokolate.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminOrderService adminOrderService;




    // === АУТЕНТИФИКАЦИЯ ===

    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/admin/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null && passwordEncoder.matches(password, user.getPassword())
                    && user.getAdminType() != AdminType.NONE) {

                session.setAttribute("adminUser", user);
                log.info("Admin logged in: {} with role: {}", username, user.getAdminType());
                return "redirect:/admin";
            } else {
                redirectAttributes.addFlashAttribute("error", "Неверные учетные данные или недостаточно прав");
                return "redirect:/admin/login";
            }
        } catch (Exception e) {
            log.error("Login error for user: {}", username, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка входа в систему");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Вы успешно вышли из системы");
        return "redirect:/admin/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/admin/access-denied";
    }

    // === ОСНОВНОЙ ДАШБОРД ===

    @GetMapping
    public String adminDashboard(Model model, HttpSession session) {
        log.info("Loading admin dashboard");

        // Получаем текущего пользователя из сессии
        User currentUser = (User) session.getAttribute("adminUser");
        model.addAttribute("currentUser", currentUser);

        try {
            // Получаем статистику продуктов
            ProductService.ProductStatistics productStats = productService.getProductStatistics();

            // Основные метрики продуктов
            model.addAttribute("totalProducts", productStats.getTotalProducts());
            model.addAttribute("inStockProducts", productStats.getInStockProducts());
            model.addAttribute("outOfStockProducts", productStats.getOutOfStockProducts());
            model.addAttribute("lowStockProducts", productStats.getLowStockProducts());
            model.addAttribute("totalInventoryValue", productStats.getTotalInventoryValue());

            // Рассчитываем среднюю цену товара
            BigDecimal averagePrice = BigDecimal.ZERO;
            if (productStats.getTotalProducts() > 0 && productStats.getTotalInventoryValue().compareTo(BigDecimal.ZERO) > 0) {
                averagePrice = productStats.getTotalInventoryValue()
                        .divide(BigDecimal.valueOf(productStats.getTotalProducts()), 2, RoundingMode.HALF_UP);
            }
            model.addAttribute("averagePrice", averagePrice);

            // Получаем статистику категорий
            long totalCategories = categoryService.getAllCategories().size();
            model.addAttribute("totalCategories", totalCategories);

            // Получаем статистику заказов
            Long totalOrders = adminOrderService.getTotalOrdersCount();
            model.addAttribute("totalOrders", totalOrders);

            Long todayOrder = adminOrderService.getTodayOrdersCount();
            model.addAttribute("todayOrder", todayOrder);


            // Последняя добавленная категория (если есть)
            String lastCategory = "Нет";
            try {
                var categories = categoryService.getAllCategories();
                if (!categories.isEmpty()) {
                    lastCategory = categories.get(categories.size() - 1).getName();
                }
            } catch (Exception e) {
                log.warn("Could not get last category: {}", e.getMessage());
            }
            model.addAttribute("lastCategory", lastCategory);

            // Получаем статистику брендов
            long totalBrands = brandService.getAllBrands().size();
            model.addAttribute("totalBrands", totalBrands);

            // Количество брендов с логотипами
            long brandsWithLogos = brandService.getAllBrands().stream()
                    .mapToLong(brand -> (brand.getImageUrl() != null && !brand.getImageUrl().isEmpty()) ? 1 : 0)
                    .sum();
            model.addAttribute("brandsWithLogos", brandsWithLogos);

            // Статистика пользователей
            model.addAttribute("totalUsers", userRepository.count());
            model.addAttribute("totalAdmins", userRepository.countByAdminType(AdminType.SUPER_ADMIN)
                    + userRepository.countByAdminType(AdminType.EDITOR)
                    + userRepository.countByAdminType(AdminType.VIEWER));

            log.info("Admin dashboard loaded successfully - Products: {}, Categories: {}, Brands: {}",
                    productStats.getTotalProducts(), totalCategories, totalBrands);

        } catch (Exception e) {
            log.error("Error loading admin dashboard statistics: {}", e.getMessage(), e);

            // Устанавливаем значения по умолчанию при ошибке
            model.addAttribute("totalProducts", 0L);
            model.addAttribute("inStockProducts", 0L);
            model.addAttribute("outOfStockProducts", 0L);
            model.addAttribute("lowStockProducts", 0L);
            model.addAttribute("totalInventoryValue", BigDecimal.ZERO);
            model.addAttribute("averagePrice", BigDecimal.ZERO);
            model.addAttribute("totalCategories", 0L);
            model.addAttribute("lastCategory", "Ошибка загрузки");
            model.addAttribute("totalBrands", 0L);
            model.addAttribute("brandsWithLogos", 0L);
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("totalAdmins", 0L);

            model.addAttribute("errorMessage", "Ошибка при загрузке статистики: " + e.getMessage());
        }

        return "admin/admin-dashboard";
    }

    // === УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===

    @GetMapping("/users")
    public String showUsers(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("adminUser");

        // VIEWER и выше могут просматривать пользователей
        if (currentUser.getAdminType() == AdminType.NONE) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для просмотра пользователей");
            return "redirect:/admin";
        }

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentUser", currentUser);
        return "admin/admin/users";
    }

    @GetMapping("/create-user")
    public String showCreateUserForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("adminUser");

        // Только SUPER_ADMIN может создавать пользователей
        if (currentUser.getAdminType() != AdminType.SUPER_ADMIN) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для создания пользователей");
            return "redirect:/admin";
        }

        model.addAttribute("user", new User());
        model.addAttribute("adminTypes", AdminType.values());
        model.addAttribute("currentUser", currentUser);
        return "admin/admin/create-user";
    }

    @PostMapping("/create-user")
    public String createUser(@ModelAttribute User user,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("adminUser");

            // Проверка прав доступа
            if (currentUser.getAdminType() != AdminType.SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для создания пользователей");
                return "redirect:/admin";
            }

            // Валидация данных
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Имя пользователя не может быть пустым");
                return "redirect:/admin/create-user";
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
                return "redirect:/admin/create-user";
            }

            // Проверка на существование пользователя
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Пользователь с таким именем уже существует");
                return "redirect:/admin/create-user";
            }

            // Установка значений по умолчанию
            if (user.getAdminType() == null) {
                user.setAdminType(AdminType.VIEWER);
            }

            // Шифрование пароля
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);
            log.info("New user created: {} with role: {} by admin: {}",
                    user.getUsername(), user.getAdminType(), currentUser.getUsername());

            redirectAttributes.addFlashAttribute("success", "Пользователь успешно создан");
            return "redirect:/admin/users";

        } catch (Exception e) {
            log.error("Error creating user: {}", user.getUsername(), e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании пользователя");
            return "redirect:/admin/create-user";
        }
    }

    @PostMapping("/user/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("adminUser");

            // Только SUPER_ADMIN может удалять пользователей
            if (currentUser.getAdminType() != AdminType.SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для удаления пользователей");
                return "redirect:/admin/users";
            }

            User userToDelete = userRepository.findById(id).orElse(null);
            if (userToDelete == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
                return "redirect:/admin/users";
            }

            // Нельзя удалить самого себя
            if (userToDelete.getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Нельзя удалить самого себя");
                return "redirect:/admin/users";
            }

            String deletedUsername = userToDelete.getUsername();
            userRepository.deleteById(id);

            log.info("User deleted: {} by admin: {}", deletedUsername, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно удален");

        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении пользователя");
        }

        return "redirect:/admin/users";
    }

    // GET метод - показ формы редактирования
    @GetMapping("/user/{id}/edit")
    public String showEditUserForm(@PathVariable Long id,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("adminUser");

            // Только SUPER_ADMIN и EDITOR могут редактировать
            if (currentUser.getAdminType() == AdminType.VIEWER || currentUser.getAdminType() == AdminType.NONE) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для редактирования пользователей");
                return "redirect:/admin/users";
            }

            User userToEdit = userRepository.findById(id).orElse(null);
            if (userToEdit == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
                return "redirect:/admin/users";
            }

            model.addAttribute("user", userToEdit);
            model.addAttribute("adminTypes", AdminType.values());
            model.addAttribute("currentUser", currentUser);

            return "admin/admin/edit-user";

        } catch (Exception e) {
            log.error("Error loading edit form for user id: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке формы редактирования");
            return "redirect:/admin/users";
        }
    }

    // POST метод - обновление пользователя
    @PostMapping("/user/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User updatedUser,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("adminUser");

            // Проверка прав доступа
            if (currentUser.getAdminType() == AdminType.VIEWER || currentUser.getAdminType() == AdminType.NONE) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для редактирования пользователей");
                return "redirect:/admin/users";
            }

            User existingUser = userRepository.findById(id).orElse(null);
            if (existingUser == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
                return "redirect:/admin/users";
            }

            // Валидация данных
            if (updatedUser.getUsername() == null || updatedUser.getUsername().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Имя пользователя не может быть пустым");
                return "redirect:/admin/user/" + id + "/edit";
            }

            if (updatedUser.getUsername().length() < 3) {
                redirectAttributes.addFlashAttribute("error", "Имя пользователя должно содержать минимум 3 символа");
                return "redirect:/admin/user/" + id + "/edit";
            }

            // Проверка на существование пользователя с таким именем (кроме текущего)
            User existingByUsername = userRepository.findByUsername(updatedUser.getUsername()).orElse(null);
            if (existingByUsername != null && !existingByUsername.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Пользователь с таким именем уже существует");
                return "redirect:/admin/user/" + id + "/edit";
            }

            // Обновляем основные поля
            existingUser.setUsername(updatedUser.getUsername());

            // Обновляем роль (только SUPER_ADMIN может изменять роли)
            if (currentUser.getAdminType() == AdminType.SUPER_ADMIN) {
                existingUser.setAdminType(updatedUser.getAdminType());
            }

            // Обновляем пароль только если он был введен
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                if (updatedUser.getPassword().length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
                    return "redirect:/admin/user/" + id + "/edit";
                }
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            userRepository.save(existingUser);

            log.info("User updated: {} by admin: {}", existingUser.getUsername(), currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно обновлен");

            return "redirect:/admin/users";

        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении пользователя");
            return "redirect:/admin/user/" + id + "/edit";
        }
    }


}
package com.example.landofchokolate.controller;

import com.example.landofchokolate.model.Category;
import com.example.landofchokolate.model.Product;
import com.example.landofchokolate.service.CategoryService;
import com.example.landofchokolate.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SitemapController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml; charset=UTF-8")
    @ResponseBody
//    @Cacheable(value = "sitemap", key = "'sitemap_xml'")
    public String getSitemap(HttpServletRequest request) {
        log.info("Generating dynamic sitemap.xml");

        String baseUrl = getBaseUrl(request);
        LocalDate today = LocalDate.now();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"");
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        sb.append(" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 ");
        sb.append("http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n\n");

        // Статические страницы
        addStaticPages(sb, baseUrl, today);

        // Динамические категории
        addCategories(sb, baseUrl, today);

        // Динамические продукты
        addProducts(sb, baseUrl, today);

        sb.append("</urlset>");

        log.info("Sitemap generated successfully");
        return sb.toString();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();

        // Добавляем порт если он не стандартный
        int port = request.getServerPort();
        if ((request.getScheme().equals("http") && port != 80) ||
                (request.getScheme().equals("https") && port != 443)) {
            baseUrl += ":" + port;
        }

        return baseUrl;
    }

    private void addStaticPages(StringBuilder sb, String baseUrl, LocalDate today) {
        sb.append("    <!-- Статические страницы -->\n");

        // Главная страница - самый высокий приоритет
        sb.append(buildUrlEntry(baseUrl + "/", today, "daily", "1.0"));

        // Основные разделы
        sb.append(buildUrlEntry(baseUrl + "/categories", today, "weekly", "0.9"));
        sb.append(buildUrlEntry(baseUrl + "/product/all", today, "daily", "0.8"));
        sb.append(buildUrlEntry(baseUrl + "/about", today, "monthly", "0.5"));

        // Если есть другие статические страницы
        sb.append(buildUrlEntry(baseUrl + "/contact", today, "monthly", "0.4"));
        sb.append(buildUrlEntry(baseUrl + "/delivery", today, "monthly", "0.4"));

        sb.append("\n");
    }

    private void addCategories(StringBuilder sb, String baseUrl, LocalDate today) {
        sb.append("    <!-- Категории товаров -->\n");

        try {
            List<Category> categories = categoryService.findAllActiveCategories();

            for (Category category : categories) {
                if (category.getSlug() == null || category.getSlug().isEmpty()) {
                    continue;
                }

                String categoryUrl = baseUrl + "/categories/" + category.getSlug();

                // Дата последнего изменения категории или сегодня
                LocalDate lastMod = (category.getUpdatedAt() != null)
                        ? category.getUpdatedAt().toLocalDate()
                        : today;

                sb.append(buildUrlEntry(categoryUrl, lastMod, "weekly", "0.7"));
            }
        } catch (Exception e) {
            log.error("Error adding categories to sitemap", e);
        }

        sb.append("\n");
    }

    private void addProducts(StringBuilder sb, String baseUrl, LocalDate today) {
        sb.append("    <!-- Товары -->\n");

        try {
            List<Product> products = productService.findAllActiveProducts();

            for (Product product : products) {
                // Пропускаем неактивные товары
                if (!Boolean.TRUE.equals(product.getIsActive())) {
                    continue;
                }

                String slugOrId = (product.getSlug() != null && !product.getSlug().isEmpty())
                        ? product.getSlug()
                        : String.valueOf(product.getId());

                String productUrl = baseUrl + "/product/" + slugOrId;

                // Используем дату обновления товара
                LocalDate lastMod = (product.getUpdatedAt() != null)
                        ? product.getUpdatedAt().toLocalDate()
                        : today;

                // Приоритет зависит от популярности товара
                String priority = calculateProductPriority(product);

                sb.append(buildUrlEntry(productUrl, lastMod, "weekly", priority));
            }
        } catch (Exception e) {
            log.error("Error adding products to sitemap", e);
        }

        sb.append("\n");
    }

    private String calculateProductPriority(Product product) {
        // Рекомендуемые товары имеют высокий приоритет
        if (Boolean.TRUE.equals(product.getIsRecommendation())) {
            return "0.8";
        }

        // Товары с высокой популярностью (много кликов)
        if (product.getClickCount() != null && product.getClickCount() > 100) {
            return "0.7";
        }

        // Товары в наличии
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            return "0.6";
        }

        // Товары без остатка - низкий приоритет
        return "0.3";
    }

    private String buildUrlEntry(String loc, LocalDate lastmod, String changefreq, String priority) {
        return new StringBuilder()
                .append("    <url>\n")
                .append("        <loc>").append(escapeXml(loc)).append("</loc>\n")
                .append("        <lastmod>").append(lastmod).append("</lastmod>\n")
                .append("        <changefreq>").append(changefreq).append("</changefreq>\n")
                .append("        <priority>").append(priority).append("</priority>\n")
                .append("    </url>\n")
                .toString();
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
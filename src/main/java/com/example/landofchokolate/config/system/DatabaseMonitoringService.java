package com.example.landofchokolate.config.system;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class DatabaseMonitoringService {

    @Autowired
    private DataSource dataSource;

    private final DecimalFormat df;
    private final AtomicLong totalQueriesProcessed = new AtomicLong(0);
    private final AtomicLong slowQueriesCount = new AtomicLong(0);

    // Пороги для анализа
    private static final int OPTIMAL_POOL_SIZE = 10; // Для небольших приложений
    private static final double HIGH_USAGE_THRESHOLD = 80.0; // 80% использования пула
    private static final double CRITICAL_USAGE_THRESHOLD = 95.0; // 95% использования пула

    public DatabaseMonitoringService() {
        this.df = new DecimalFormat("#.##");
        this.df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
    }

    /**
     * Основной мониторинг БД каждые 5 минут
     */
    @Scheduled(fixedRate = 300000)
    public void logDatabaseStatistics() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

            int activeConnections = poolMXBean.getActiveConnections();
            int idleConnections = poolMXBean.getIdleConnections();
            int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
            int totalConnections = poolMXBean.getTotalConnections();

            log.info("🗄️ === СТАТИСТИКА БД ПУЛА ===");
            log.info("🔗 СОЕДИНЕНИЯ: Активные: {}, Простаивающие: {}, Ожидающие: {}, Общее: {}",
                    activeConnections, idleConnections, threadsAwaitingConnection, totalConnections);

            // Расширенная статистика HikariCP
            logDetailedHikariStats(hikari, poolMXBean);

            // Анализ эффективности пула
            analyzeConnectionPoolEfficiency(activeConnections, idleConnections,
                    threadsAwaitingConnection, totalConnections);

            // Рекомендации по оптимизации
            provideOptimizationRecommendations(hikari, activeConnections,
                    idleConnections, totalConnections);
        } else {
            log.warn("⚠️ DataSource не является HikariDataSource. Используется: {}",
                    dataSource.getClass().getSimpleName());
        }
    }

    /**
     * Детальная статистика HikariCP
     */
    private void logDetailedHikariStats(HikariDataSource hikari, HikariPoolMXBean poolMXBean) {
        try {
            // Конфигурация пула
            int maxPoolSize = hikari.getMaximumPoolSize();
            int minPoolSize = hikari.getMinimumIdle();
            long connectionTimeout = hikari.getConnectionTimeout();
            long idleTimeout = hikari.getIdleTimeout();
            long maxLifetime = hikari.getMaxLifetime();

            log.info("⚙️ КОНФИГУРАЦИЯ ПУЛА:");
            log.info("   📏 Размер: {} (мин) - {} (макс)", minPoolSize, maxPoolSize);
            log.info("   ⏱️ Таймауты: Соединение: {} мс, Простой: {} мс, Жизнь: {} мс",
                    connectionTimeout, idleTimeout, maxLifetime);

            // Дополнительная статистика из HikariCP
            log.info("📊 ДЕТАЛЬНАЯ СТАТИСТИКА:");

            // Эти методы могут быть недоступны в некоторых версиях HikariCP
            try {
                log.info("   🔄 Соединения в использовании: {}/{}",
                        poolMXBean.getActiveConnections(), maxPoolSize);

                double usagePercent = ((double) poolMXBean.getActiveConnections() / maxPoolSize) * 100;
                log.info("   📈 Процент использования пула: {}%", df.format(usagePercent));

            } catch (Exception e) {
                log.debug("Некоторые метрики HikariCP недоступны: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.debug("Ошибка при получении детальной статистики HikariCP: {}", e.getMessage());
        }
    }

    /**
     * Анализ эффективности пула соединений
     */
    private void analyzeConnectionPoolEfficiency(int active, int idle, int awaiting, int total) {
        log.info("🔍 === АНАЛИЗ ЭФФЕКТИВНОСТИ ПУЛА ===");

        // Анализ текущего состояния
        if (awaiting > 0) {
            log.error("🚨 ПРОБЛЕМА: {} потоков ожидают соединения! Пул перегружен!", awaiting);
            log.error("💡 СРОЧНО: Увеличьте размер пула или оптимизируйте запросы");
        } else if (active == 0 && idle > 0) {
            log.info("😴 ПРОСТОЙ: БД не используется, все соединения простаивают");
        } else if (active > 0) {
            double activePercent = ((double) active / total) * 100;

            if (activePercent > CRITICAL_USAGE_THRESHOLD) {
                log.error("🔥 КРИТИЧЕСКОЕ использование пула: {}% ({}/{})",
                        df.format(activePercent), active, total);
            } else if (activePercent > HIGH_USAGE_THRESHOLD) {
                log.warn("⚠️ ВЫСОКОЕ использование пула: {}% ({}/{})",
                        df.format(activePercent), active, total);
            } else {
                log.info("✅ НОРМАЛЬНОЕ использование пула: {}% ({}/{})",
                        df.format(activePercent), active, total);
            }
        }

        // Анализ соотношения активных/простаивающих
        if (idle > active * 3 && active > 0) {
            log.info("💡 ОПТИМИЗАЦИЯ: Много простаивающих соединений. Можно уменьшить минимальный размер пула");
        } else if (idle == 0 && active > 0) {
            log.warn("⚠️ ВНИМАНИЕ: Нет резервных соединений. Рассмотрите увеличение пула");
        }
    }

    /**
     * Рекомендации по оптимизации для Render
     */
    private void provideOptimizationRecommendations(HikariDataSource hikari, int active, int idle, int total) {
        log.info("💡 === РЕКОМЕНДАЦИИ ДЛЯ RENDER ===");

        int maxPoolSize = hikari.getMaximumPoolSize();
        int minPoolSize = hikari.getMinimumIdle();

        // Рекомендации по размеру пула для Render (512 MB памяти)
        if (maxPoolSize > 15) {
            log.warn("⚠️ Пул слишком большой для Render! Рекомендуется максимум 10-12 соединений");
            log.warn("   💾 Каждое соединение PostgreSQL ≈ 5-10 MB памяти");
            log.warn("   🔧 Установите: spring.datasource.hikari.maximum-pool-size=10");
        }

        if (minPoolSize > 5) {
            log.warn("⚠️ Минимальный пул можно уменьшить для экономии памяти");
            log.warn("   🔧 Установите: spring.datasource.hikari.minimum-idle=2");
        }

        // Анализ текущего использования
        if (active == 0 && total > 5) {
            log.info("💰 ЭКОНОМИЯ: БД не используется, можно уменьшить пул до 2-5 соединений");
        } else if (active > 7) {
            log.info("📈 НАГРУЗКА: Высокое использование БД, текущий размер пула оптимален");
        }

        // Рекомендации по таймаутам
        long connectionTimeout = hikari.getConnectionTimeout();
        if (connectionTimeout > 10000) {
            log.info("⏱️ Можно уменьшить connection-timeout до 5000ms для быстрого обнаружения проблем");
        }

        // Специфичные рекомендации для PostgreSQL на Render
        log.info("🎯 ОПТИМАЛЬНЫЕ НАСТРОЙКИ ДЛЯ RENDER:");
        log.info("   spring.datasource.hikari.maximum-pool-size=10");
        log.info("   spring.datasource.hikari.minimum-idle=2");
        log.info("   spring.datasource.hikari.connection-timeout=5000");
        log.info("   spring.datasource.hikari.idle-timeout=300000");
        log.info("   spring.datasource.hikari.max-lifetime=1200000");
    }

    /**
     * Критический мониторинг каждую минуту при проблемах
     */
    @Scheduled(fixedRate = 60000) // каждую минуту
    public void criticalDatabaseCheck() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

            int awaiting = poolMXBean.getThreadsAwaitingConnection();
            int active = poolMXBean.getActiveConnections();
            int total = poolMXBean.getTotalConnections();

            // Критические ситуации
            if (awaiting > 0) {
                log.error("🚨 КРИТИЧНО! {} потоков ожидают БД соединения уже минуту!", awaiting);
            }

            if (active >= total && total > 0) {
                log.error("🚨 КРИТИЧНО! Все соединения БД заняты! Новые запросы будут ждать!");
            }

            // Логируем только при проблемах
            if (awaiting > 0 || active >= total) {
                log.error("🔍 Состояние пула: Активные: {}, Общее: {}, Ожидающие: {}",
                        active, total, awaiting);
            }
        }
    }

    /**
     * Получить информацию о состоянии БД пула
     */
    public DatabaseInfo getDatabaseInfo() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

            int activeConnections = poolMXBean.getActiveConnections();
            int idleConnections = poolMXBean.getIdleConnections();
            int threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
            int totalConnections = poolMXBean.getTotalConnections();

            int maxPoolSize = hikari.getMaximumPoolSize();
            int minPoolSize = hikari.getMinimumIdle();

            double usagePercent = totalConnections > 0 ?
                    ((double) activeConnections / totalConnections) * 100 : 0;

            String status = getDatabaseStatus(activeConnections, threadsAwaitingConnection,
                    totalConnections, maxPoolSize);
            String recommendation = getDatabaseRecommendation(activeConnections, idleConnections,
                    maxPoolSize, threadsAwaitingConnection);

            return new DatabaseInfo(activeConnections, idleConnections, threadsAwaitingConnection,
                    totalConnections, maxPoolSize, minPoolSize, usagePercent, status, recommendation);
        }

        return new DatabaseInfo(0, 0, 0, 0, 0, 0, 0, "UNKNOWN",
                "DataSource не является HikariDataSource");
    }

    private String getDatabaseStatus(int active, int awaiting, int total, int maxPool) {
        if (awaiting > 0) return "CRITICAL";
        if (active >= total && total >= maxPool) return "CRITICAL";
        if (active > total * 0.8) return "WARNING";
        if (active == 0) return "IDLE";
        return "GOOD";
    }

    private String getDatabaseRecommendation(int active, int idle, int maxPool, int awaiting) {
        if (awaiting > 0) {
            return "Увеличьте размер пула БД или оптимизируйте медленные запросы";
        }
        if (maxPool > 15) {
            return "Уменьшите размер пула до 10 для экономии памяти на Render";
        }
        if (idle > active * 3 && active > 0) {
            return "Можно уменьшить минимальный размер пула для экономии ресурсов";
        }
        if (active == 0) {
            return "БД не используется. Рассмотрите уменьшение пула до 2-5 соединений";
        }
        return "Пул БД настроен оптимально";
    }

    /**
     * Информация о состоянии БД
     */
    @Getter
    public static class DatabaseInfo {
        private final int activeConnections;
        private final int idleConnections;
        private final int threadsAwaitingConnection;
        private final int totalConnections;
        private final int maxPoolSize;
        private final int minPoolSize;
        private final double usagePercent;
        private final String status;
        private final String recommendation;
        private final String timestamp;

        public DatabaseInfo(int activeConnections, int idleConnections, int threadsAwaitingConnection,
                            int totalConnections, int maxPoolSize, int minPoolSize, double usagePercent,
                            String status, String recommendation) {
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.threadsAwaitingConnection = threadsAwaitingConnection;
            this.totalConnections = totalConnections;
            this.maxPoolSize = maxPoolSize;
            this.minPoolSize = minPoolSize;
            this.usagePercent = usagePercent;
            this.status = status;
            this.recommendation = recommendation;
            this.timestamp = LocalDateTime.now().toString();
        }

        public String getUsagePercentFormatted() {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));
            return df.format(usagePercent);
        }

        public String getStatusIcon() {
            switch (status) {
                case "CRITICAL": return "🔴";
                case "WARNING": return "🟡";
                case "IDLE": return "😴";
                case "GOOD": return "🟢";
                default: return "❓";
            }
        }

        public String getStatusText() {
            switch (status) {
                case "CRITICAL": return "Критичний стан";
                case "WARNING": return "Попередження";
                case "IDLE": return "Простій";
                case "GOOD": return "Нормальний стан";
                default: return "Невідомо";
            }
        }
    }
}
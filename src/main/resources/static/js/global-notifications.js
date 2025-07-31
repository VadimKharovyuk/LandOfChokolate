// global-notifications.js - подключить ПЕРВЫМ в layout
(function() {
    'use strict';

    // Глобальная функция показа уведомлений
    window.showNotification = function(message, type = 'success', autoHideTime = 3000) {
        // Удаляем существующие уведомления
        const existingNotifications = document.querySelectorAll('.notification');
        existingNotifications.forEach(notification => notification.remove());

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;

        let icon, bgColor;
        switch(type) {
            case 'success':
                icon = 'check-circle';
                bgColor = '#28a745';
                autoHideTime = autoHideTime || 3000;
                break;
            case 'error':
                icon = 'exclamation-triangle';
                bgColor = '#dc3545';
                autoHideTime = autoHideTime || 5000; // Ошибки показываем дольше
                break;
            case 'warning':
                icon = 'exclamation-triangle';
                bgColor = '#ffc107';
                autoHideTime = autoHideTime || 4000;
                break;
            case 'info':
                icon = 'info-circle';
                bgColor = '#17a2b8';
                autoHideTime = autoHideTime || 2000; // Инфо быстро исчезает
                break;
            default:
                icon = 'check-circle';
                bgColor = '#28a745';
        }

        notification.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <span>${message}</span>
            <button class="notification-close" onclick="this.parentElement.remove()" 
                    style="margin-left: auto; background: none; border: none; color: inherit; font-size: 18px; cursor: pointer; padding: 0; width: 20px; height: 20px;">
                ×
            </button>
        `;

        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            max-width: 400px;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            color: ${type === 'warning' ? '#000' : '#FFFFFF'};
            font-weight: 500;
            background: ${bgColor};
            display: flex;
            align-items: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideInRight 0.3s ease;
            cursor: pointer;
        `;

        document.body.appendChild(notification);

        // Автоматическое закрытие
        if (autoHideTime > 0) {
            setTimeout(() => {
                hideNotification(notification);
            }, autoHideTime);
        }

        return notification;
    };

    // Функция скрытия уведомления
    function hideNotification(notification) {
        if (notification && notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }
    }

    // 🎯 ГЛАВНАЯ ФУНКЦИЯ - Глобальный обработчик ответов сервера
    window.handleServerResponse = function(data, options = {}) {
        const settings = {
            showNotification: true,
            updateCounters: true,
            playSound: false,
            redirectOnSuccess: null,
            customSuccessHandler: null,
            customErrorHandler: null,
            ...options
        };

        if (data.success) {
            // Показываем уведомление об успехе
            if (settings.showNotification && data.message) {
                showNotification(data.message, 'success');
            }

            // Обновляем счетчики
            if (settings.updateCounters) {
                if (data.cartItemCount !== undefined) {
                    updateCartCounter(data.cartItemCount);
                }
                if (data.wishlistItemCount !== undefined || data.itemCount !== undefined) {
                    updateWishlistCounter(data.wishlistItemCount || data.itemCount);
                }
            }

            // Кастомный обработчик успеха
            if (settings.customSuccessHandler && typeof settings.customSuccessHandler === 'function') {
                settings.customSuccessHandler(data);
            }

            // Редирект при успехе
            if (settings.redirectOnSuccess) {
                setTimeout(() => {
                    window.location.href = settings.redirectOnSuccess;
                }, 1500);
            }

        } else {
            // Показываем уведомление об ошибке
            if (settings.showNotification && data.message) {
                showNotification(data.message, 'error');
            }

            // Кастомный обработчик ошибки
            if (settings.customErrorHandler && typeof settings.customErrorHandler === 'function') {
                settings.customErrorHandler(data);
            }
        }

        // Звуковые эффекты
        if (settings.playSound) {
            playNotificationSound(data.success ? 'success' : 'error');
        }

        return data;
    };

    // Функция обновления счетчика корзины
    function updateCartCounter(count) {
        const cartCounters = document.querySelectorAll('.cart-count, .nav-cart-count, [data-cart-count]');
        cartCounters.forEach(counter => {
            counter.textContent = count;
            counter.style.display = count > 0 ? 'inline-block' : 'none';
        });
    }

    // Функция обновления счетчика избранного
    function updateWishlistCounter(count) {
        const wishlistCounters = document.querySelectorAll('.wishlist-count, .nav-wishlist-count, [data-wishlist-count]');
        wishlistCounters.forEach(counter => {
            counter.textContent = count;
            counter.style.display = count > 0 ? 'inline-block' : 'none';
        });
    }

    // Звуковые эффекты (опционально)
    function playNotificationSound(type) {
        try {
            const audio = new Audio();
            audio.volume = 0.2;
            audio.src = type === 'success' ? '/sounds/success.mp3' : '/sounds/error.mp3';
            audio.play().catch(() => {}); // Игнорируем ошибки
        } catch (e) {
            // Звук отключен
        }
    }

    // CSS стили для анимаций
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOutRight {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }

})();
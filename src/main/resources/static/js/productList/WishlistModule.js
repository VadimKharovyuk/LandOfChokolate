/**
 * Модуль для работы с избранным
 */
const WishlistModule = {
    // Конфигурация
    config: {
        endpoints: {
            toggle: '/wishlist/toggle',
            check: '/wishlist/check',
            checkMultiple: '/wishlist/check-multiple',
            count: '/wishlist/count'
        },
        selectors: {
            wishlistBtn: '.wishlist-btn',
            wishlistCounter: '.wishlist-counter'
        }
    },

    /**
     * Инициализация модуля
     */
    init() {
        this.bindEvents();
        this.checkMultipleProducts();
        this.updateWishlistCounter();
    },

    /**
     * Привязка событий
     */
    bindEvents() {
        // События теперь обрабатываются через onclick в HTML
        // Этот метод оставлен для совместимости и будущих обработчиков
    },

    /**
     * Переключение товара в избранном
     */
    async toggleProduct(button) {
        const productId = button.dataset.productId;
        if (!productId) return;

        // Показываем состояние загрузки
        this.setButtonLoading(button, true);

        try {
            const response = await fetch(this.config.endpoints.toggle, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `productId=${productId}&addedFromPage=product-list`
            });

            const data = await response.json();

            if (data.success) {
                // Обновляем состояние кнопки
                this.updateButtonState(button, data.isInWishlist);

                // Показываем анимацию
                this.animateButton(button);

                // Обновляем счетчик
                this.updateWishlistCounter(data.itemCount);

                // Показываем уведомление
                this.showNotification(data.message, 'success');
            } else {
                this.showNotification(data.message || 'Произошла ошибка', 'error');
            }

        } catch (error) {
            console.error('Ошибка при работе с избранным:', error);
            this.showNotification('Произошла ошибка при работе с избранным', 'error');
        } finally {
            this.setButtonLoading(button, false);
        }
    },

    /**
     * Проверка статуса множественных товаров
     */
    async checkMultipleProducts() {
        const buttons = document.querySelectorAll(this.config.selectors.wishlistBtn);
        if (buttons.length === 0) return;

        const productIds = Array.from(buttons).map(btn => btn.dataset.productId).filter(Boolean);

        if (productIds.length === 0) return;

        try {
            const response = await fetch(this.config.endpoints.checkMultiple, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(productIds)
            });

            const data = await response.json();

            if (data.success && data.results) {
                // Обновляем состояние всех кнопок
                buttons.forEach(button => {
                    const productId = button.dataset.productId;
                    if (productId && data.results[productId] !== undefined) {
                        this.updateButtonState(button, data.results[productId]);
                    }
                });
            }

        } catch (error) {
            console.error('Ошибка при проверке товаров в избранном:', error);
        }
    },

    /**
     * Обновление состояния кнопки
     */
    updateButtonState(button, isInWishlist) {
        button.dataset.inWishlist = isInWishlist.toString();

        const icon = button.querySelector('i');
        if (icon) {
            if (isInWishlist) {
                icon.className = 'fas fa-heart';
                button.title = 'Видалити з обраного';
            } else {
                icon.className = 'far fa-heart';
                button.title = 'Додати в обране';
            }
        }
    },

    /**
     * Установка состояния загрузки для кнопки
     */
    setButtonLoading(button, loading) {
        if (loading) {
            button.classList.add('loading');
            button.disabled = true;
        } else {
            button.classList.remove('loading');
            button.disabled = false;
        }
    },

    /**
     * Анимация кнопки при действии
     */
    animateButton(button) {
        button.classList.add('adding');
        setTimeout(() => {
            button.classList.remove('adding');
        }, 300);
    },

    /**
     * Обновление счетчика избранного
     */
    async updateWishlistCounter(count = null) {
        if (count === null) {
            try {
                const response = await fetch(this.config.endpoints.count);
                const data = await response.json();
                count = data.success ? data.itemCount : 0;
            } catch (error) {
                console.error('Ошибка при получении счетчика избранного:', error);
                return;
            }
        }

        // Обновляем все счетчики на странице
        const counters = document.querySelectorAll(this.config.selectors.wishlistCounter);
        counters.forEach(counter => {
            counter.textContent = count;

            // Показываем/скрываем счетчик в зависимости от количества
            if (count > 0) {
                counter.style.display = 'inline';
            } else {
                counter.style.display = 'none';
            }
        });
    },

    /**
     * Показ уведомления
     */
    showNotification(message, type = 'info') {
        // Простая реализация уведомления
        // Можно заменить на более сложную систему уведомлений

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;

        // Стили для уведомления
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '12px 20px',
            borderRadius: '6px',
            color: 'white',
            zIndex: '10000',
            fontSize: '14px',
            maxWidth: '300px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            transform: 'translateX(100%)',
            transition: 'transform 0.3s ease'
        });

        // Цвета в зависимости от типа
        const colors = {
            success: '#27ae60',
            error: '#e74c3c',
            info: '#3498db'
        };

        notification.style.backgroundColor = colors[type] || colors.info;

        document.body.appendChild(notification);

        // Анимация появления
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 100);

        // Удаление через 3 секунды
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    },

    /**
     * Получение статуса одного товара
     */
    async checkSingleProduct(productId) {
        try {
            const response = await fetch(`${this.config.endpoints.check}/${productId}`);
            const data = await response.json();
            return data.success ? data.isInWishlist : false;
        } catch (error) {
            console.error('Ошибка при проверке товара в избранном:', error);
            return false;
        }
    }
};

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', () => {
    WishlistModule.init();
});

// Глобальная функция для обработки клика на кнопку избранного
window.handleWishlistClick = function(event, button) {
    event.stopPropagation();
    event.preventDefault();
    event.stopImmediatePropagation();

    if (window.WishlistModule) {
        WishlistModule.toggleProduct(button);
    }

    return false;
};

// Экспорт для использования в других скриптах
window.WishlistModule = WishlistModule;
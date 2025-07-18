/**
 * Cart JavaScript Functions
 * Простые и легкие методы для работы с корзиной
 */

// Конфигурация
const CART_CONFIG = {
    baseUrl: '/cart',
    timeout: 10000,
    retryAttempts: 3
};

// Утилитарные функции
const CartUtils = {
    /**
     * Выполнить AJAX запрос
     */
    async makeRequest(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            timeout: CART_CONFIG.timeout
        };

        const config = { ...defaultOptions, ...options };

        try {
            const response = await fetch(url, config);
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Cart request error:', error);
            throw new Error('Ошибка соединения с сервером');
        }
    },

    /**
     * Создать FormData для отправки
     */
    createFormData(params) {
        const formData = new FormData();
        Object.keys(params).forEach(key => {
            formData.append(key, params[key]);
        });
        return formData;
    },

    /**
     * Форматировать цену
     */
    formatPrice(price) {
        return `${price} грн`;
    },

    /**
     * Обновить счетчик корзины в хедере
     */
    updateCartCounter(count) {
        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
        cartCounters.forEach(counter => {
            counter.textContent = count || '0';
            // Показать/скрыть счетчик
            if (count > 0) {
                counter.style.display = 'inline';
                counter.classList.remove('d-none');
            } else {
                counter.style.display = 'none';
                counter.classList.add('d-none');
            }
        });
    },

    /**
     * Обновить общую сумму корзины
     */
    updateCartTotal(total) {
        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
        cartTotals.forEach(totalElement => {
            totalElement.textContent = this.formatPrice(total);
        });
    }
};

// Уведомления
const CartNotifications = {
    /**
     * Показать уведомление
     */
    show(message, type = 'success') {
        // Удалить предыдущие уведомления
        this.remove();

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.id = 'cart-notification';

        const icon = type === 'success' ? 'check-circle' : 'exclamation-triangle';
        notification.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <span>${message}</span>
            <button type="button" class="notification-close" onclick="CartNotifications.remove()">
                <i class="fas fa-times"></i>
            </button>
        `;

        document.body.appendChild(notification);

        // Автоматическое удаление через 4 секунды
        setTimeout(() => {
            this.remove();
        }, 4000);
    },

    /**
     * Удалить уведомление
     */
    remove() {
        const notification = document.getElementById('cart-notification');
        if (notification) {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }
};

// Основные функции корзины
const Cart = {
    /**
     * Добавить товар в корзину
     */
    async addProduct(productId, quantity = 1) {
        const button = event?.target?.closest('.btn-add-to-cart');

        // Показать загрузку на кнопке
        if (button) {
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Добавляем...';
            button.disabled = true;
        }

        try {
            const formData = CartUtils.createFormData({ productId, quantity });

            const data = await CartUtils.makeRequest(`${CART_CONFIG.baseUrl}/add`, {
                method: 'POST',
                body: formData
            });

            if (data.success) {
                CartNotifications.show(data.message || 'Товар добавлен в корзину!', 'success');

                // Обновить счетчики
                CartUtils.updateCartCounter(data.cartItemCount);
                CartUtils.updateCartTotal(data.cartTotal);

                // Можно добавить анимацию или другие эффекты
                this.onProductAdded(productId, quantity, data);

            } else {
                CartNotifications.show(data.message || 'Ошибка при добавлении товара', 'error');
            }

        } catch (error) {
            CartNotifications.show(error.message, 'error');
        } finally {
            // Восстановить кнопку
            if (button) {
                const icon = button.querySelector('i');
                const text = button.dataset.originalText || 'В корзину';
                button.innerHTML = `<i class="fas fa-shopping-cart"></i> ${text}`;
                button.disabled = false;
            }
        }
    },

    /**
     * Обновить количество товара
     */
    async updateQuantity(productId, quantity) {
        try {
            const formData = CartUtils.createFormData({ productId, quantity });

            const data = await CartUtils.makeRequest(`${CART_CONFIG.baseUrl}/update`, {
                method: 'POST',
                body: formData
            });

            if (data.success) {
                CartNotifications.show('Количество обновлено', 'success');

                // Обновить счетчики
                CartUtils.updateCartCounter(data.cartItemCount);
                CartUtils.updateCartTotal(data.cartTotal);

                // Обновить стоимость товара
                this.updateItemTotal(productId, data.itemTotal);

                this.onQuantityUpdated(productId, quantity, data);

            } else {
                CartNotifications.show(data.message || 'Ошибка при обновлении количества', 'error');
            }

        } catch (error) {
            CartNotifications.show(error.message, 'error');
        }
    },

    /**
     * Удалить товар из корзины
     */
    async removeProduct(productId) {
        // Подтверждение удаления
        if (!confirm('Удалить товар из корзины?')) {
            return;
        }

        try {
            const formData = CartUtils.createFormData({ productId });

            const data = await CartUtils.makeRequest(`${CART_CONFIG.baseUrl}/remove`, {
                method: 'POST',
                body: formData
            });

            if (data.success) {
                CartNotifications.show('Товар удален из корзины', 'success');

                // Обновить счетчики
                CartUtils.updateCartCounter(data.cartItemCount);
                CartUtils.updateCartTotal(data.cartTotal);

                // Удалить элемент из DOM
                this.removeItemFromDOM(productId);

                // Показать пустую корзину если нужно
                if (data.isEmpty) {
                    this.showEmptyCart();
                }

                this.onProductRemoved(productId, data);

            } else {
                CartNotifications.show(data.message || 'Ошибка при удалении товара', 'error');
            }

        } catch (error) {
            CartNotifications.show(error.message, 'error');
        }
    },

    /**
     * Очистить корзину
     */
    async clearCart() {
        // Подтверждение очистки
        if (!confirm('Очистить всю корзину?')) {
            return;
        }

        try {
            const data = await CartUtils.makeRequest(`${CART_CONFIG.baseUrl}/clear`, {
                method: 'POST'
            });

            if (data.success) {
                CartNotifications.show('Корзина очищена', 'success');

                // Обновить счетчики
                CartUtils.updateCartCounter(0);
                CartUtils.updateCartTotal(0);

                // Показать пустую корзину
                this.showEmptyCart();

                this.onCartCleared(data);

            } else {
                CartNotifications.show(data.message || 'Ошибка при очистке корзины', 'error');
            }

        } catch (error) {
            CartNotifications.show(error.message, 'error');
        }
    },

    /**
     * Получить информацию о корзине
     */
    async getCartInfo() {
        try {
            const data = await CartUtils.makeRequest(`${CART_CONFIG.baseUrl}/info`);

            if (data.success) {
                CartUtils.updateCartCounter(data.cartItemCount);
                CartUtils.updateCartTotal(data.cartTotal);
                return data;
            }

        } catch (error) {
            console.error('Ошибка получения информации о корзине:', error);
        }
    },

    // Вспомогательные методы для обновления DOM
    updateItemTotal(productId, total) {
        const itemTotalElement = document.querySelector(`[data-item-total="${productId}"]`);
        if (itemTotalElement) {
            itemTotalElement.textContent = CartUtils.formatPrice(total);
        }
    },

    removeItemFromDOM(productId) {
        const cartItem = document.querySelector(`[data-cart-item="${productId}"]`);
        if (cartItem) {
            cartItem.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => {
                cartItem.remove();
            }, 300);
        }
    },

    showEmptyCart() {
        const cartContainer = document.querySelector('.cart-items, .cart-container');
        if (cartContainer) {
            cartContainer.innerHTML = `
                <div class="empty-cart">
                    <div class="empty-icon">
                        <i class="fas fa-shopping-cart"></i>
                    </div>
                    <h3>Ваша корзина пуста</h3>
                    <p>Добавьте товары в корзину, чтобы продолжить покупки</p>
                    <a href="/categories" class="btn btn-primary">
                        <i class="fas fa-arrow-left"></i>
                        Продолжить покупки
                    </a>
                </div>
            `;
        }
    },

    // События (можно переопределить для кастомной логики)
    onProductAdded(productId, quantity, data) {
        console.log('Product added:', productId, quantity, data);
    },

    onQuantityUpdated(productId, quantity, data) {
        console.log('Quantity updated:', productId, quantity, data);
    },

    onProductRemoved(productId, data) {
        console.log('Product removed:', productId, data);
    },

    onCartCleared(data) {
        console.log('Cart cleared:', data);
    }
};

// Глобальные функции для использования в HTML
window.addToCart = Cart.addProduct.bind(Cart);
window.updateCartQuantity = Cart.updateQuantity.bind(Cart);
window.removeFromCart = Cart.removeProduct.bind(Cart);
window.clearCart = Cart.clearCart.bind(Cart);

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Обновить информацию о корзине
    Cart.getCartInfo();

    console.log('Cart JavaScript initialized');
});
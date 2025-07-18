/**
 * Cart Update Functions
 * Функции для обновления количества товаров в корзине
 */

const CartUpdate = {
    /**
     * Обновить количество товара в корзине
     */
    async updateQuantity(productId, quantity) {
        // Валидация
        if (!productId || quantity < 0) {
            console.error('Некорректные параметры для обновления корзины');
            return;
        }

        // Если количество 0, удаляем товар
        if (quantity === 0) {
            if (typeof CartRemove !== 'undefined') {
                CartRemove.removeProduct(productId);
            } else {
                console.warn('CartRemove не загружен, не могу удалить товар');
            }
            return;
        }

        try {
            // Показать загрузку на элементе количества
            this.showQuantityLoading(productId, true);

            const formData = new FormData();
            formData.append('productId', productId);
            formData.append('quantity', quantity);

            const response = await fetch('/cart/update', {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                // Обновить UI
                this.updateCartUI(productId, quantity, data);

                // Показать уведомление
                this.showNotification('Количество обновлено', 'success');

                console.log('Количество товара обновлено:', productId, quantity);
            } else {
                // Восстановить предыдущее значение
                this.revertQuantityInput(productId);
                this.showNotification(data.message || 'Ошибка при обновлении количества', 'error');
            }

        } catch (error) {
            console.error('Ошибка обновления корзины:', error);
            this.revertQuantityInput(productId);
            this.showNotification('Ошибка соединения с сервером', 'error');
        } finally {
            this.showQuantityLoading(productId, false);
        }
    },

    /**
     * Обновить UI после изменения количества
     */
    updateCartUI(productId, quantity, data) {
        // Обновить поле количества
        const quantityInput = document.querySelector(`[data-cart-item="${productId}"] .quantity-input`);
        if (quantityInput) {
            quantityInput.value = quantity;
        }

        // Обновить общую стоимость товара
        const itemTotal = document.querySelector(`[data-item-total="${productId}"]`);
        if (itemTotal && data.itemTotal) {
            itemTotal.textContent = `${data.itemTotal} грн`;
        }

        // Обновить общие счетчики корзины
        this.updateCartCounters(data.cartItemCount, data.cartTotal);

        // Обновить состояние кнопок +/-
        this.updateQuantityButtons(productId, quantity, data);
    },

    /**
     * Обновить состояние кнопок количества
     */
    updateQuantityButtons(productId, quantity, data) {
        const cartItem = document.querySelector(`[data-cart-item="${productId}"]`);
        if (!cartItem) return;

        const minusBtn = cartItem.querySelector('.quantity-btn[onclick*="- 1"]');
        const plusBtn = cartItem.querySelector('.quantity-btn[onclick*="+ 1"]');

        // Кнопка минус
        if (minusBtn) {
            minusBtn.disabled = quantity <= 1;
        }

        // Кнопка плюс (проверяем наличие на складе)
        if (plusBtn && data.itemTotal) {
            // Здесь можно добавить проверку максимального количества на складе
            // plusBtn.disabled = quantity >= maxStock;
        }
    },

    /**
     * Показать/скрыть загрузку на элементе количества
     */
    showQuantityLoading(productId, loading) {
        const cartItem = document.querySelector(`[data-cart-item="${productId}"]`);
        if (!cartItem) return;

        const quantitySelector = cartItem.querySelector('.quantity-selector');
        const quantityButtons = cartItem.querySelectorAll('.quantity-btn');
        const quantityInput = cartItem.querySelector('.quantity-input');

        if (loading) {
            quantitySelector.classList.add('loading');
            quantityButtons.forEach(btn => btn.disabled = true);
            quantityInput.disabled = true;
        } else {
            quantitySelector.classList.remove('loading');
            quantityButtons.forEach(btn => btn.disabled = false);
            quantityInput.disabled = false;
        }
    },

    /**
     * Восстановить предыдущее значение в поле количества
     */
    revertQuantityInput(productId) {
        const quantityInput = document.querySelector(`[data-cart-item="${productId}"] .quantity-input`);
        if (quantityInput && quantityInput.dataset.previousValue) {
            quantityInput.value = quantityInput.dataset.previousValue;
        }
    },

    /**
     * Обновить общие счетчики корзины
     */
    updateCartCounters(itemCount, total) {
        // Обновить счетчик товаров
        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
        cartCounters.forEach(counter => {
            counter.textContent = itemCount || '0';
            if (itemCount > 0) {
                counter.style.display = 'inline';
                counter.classList.remove('d-none');
            }
        });

        // Обновить общую сумму
        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total], .summary-value');
        cartTotals.forEach(totalElement => {
            if (totalElement.hasAttribute('data-cart-total') ||
                totalElement.classList.contains('cart-total')) {
                totalElement.textContent = `${total} грн`;
            }
        });

        // Обновить итоговую сумму в сводке
        const summaryTotalElements = document.querySelectorAll('.summary-total .summary-value');
        summaryTotalElements.forEach(element => {
            element.textContent = `${total} грн`;
        });
    },

    /**
     * Показать уведомление
     */
    showNotification(message, type = 'success') {
        // Проверить, есть ли система уведомлений
        if (typeof CartNotifications !== 'undefined') {
            CartNotifications.show(message, type);
        } else {
            // Простое уведомление
            console.log(`${type.toUpperCase()}: ${message}`);

            // Можно добавить простой toast
            const toast = document.createElement('div');
            toast.className = `simple-toast ${type}`;
            toast.textContent = message;
            toast.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 9999;
                padding: 1rem 1.5rem;
                border-radius: 4px;
                color: white;
                font-weight: 500;
                background: ${type === 'success' ? '#28a745' : '#dc3545'};
                animation: slideIn 0.3s ease;
            `;

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.remove();
            }, 3000);
        }
    },

    /**
     * Инициализация обработчиков событий
     */
    init() {
        // Сохранить предыдущие значения для отката
        const quantityInputs = document.querySelectorAll('.quantity-input');
        quantityInputs.forEach(input => {
            input.addEventListener('focus', function() {
                this.dataset.previousValue = this.value;
            });
        });

        console.log('CartUpdate initialized');
    }
};

// Глобальная функция для использования в HTML
window.updateCartQuantity = function(productId, quantity) {
    CartUpdate.updateQuantity(productId, parseInt(quantity));
};

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    CartUpdate.init();
});

// Экспорт для модульного использования
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CartUpdate;
}

window.CartUpdate = CartUpdate;
/**
 * Cart Clear Functions
 * Функции для очистки корзины
 */

const CartClear = {
    /**
     * Очистить всю корзину
     */
    async clearCart(showConfirm = true) {
        // Подтверждение очистки
        if (showConfirm && !confirm('Очистить всю корзину? Все товары будут удалены.')) {
            return;
        }

        try {
            // Показать загрузку
            this.showClearLoading(true);

            const response = await fetch('/cart/clear', {
                method: 'POST'
            });

            const data = await response.json();

            if (data.success) {
                // Очистить UI
                this.clearCartUI();

                // Обновить счетчики
                this.updateCartCounters(0, 0);

                // Показать пустую корзину
                this.showEmptyCart();

                // Показать уведомление
                this.showNotification('Корзина очищена', 'success');

                console.log('Корзина очищена');
            } else {
                this.showNotification(data.message || 'Ошибка при очистке корзины', 'error');
            }

        } catch (error) {
            console.error('Ошибка очистки корзины:', error);
            this.showNotification('Ошибка соединения с сервером', 'error');
        } finally {
            this.showClearLoading(false);
        }
    },

    /**
     * Показать/скрыть загрузку для кнопки очистки
     */
    showClearLoading(loading) {
        const clearButton = document.querySelector('.btn-clear');
        if (!clearButton) return;

        if (loading) {
            clearButton.disabled = true;
            clearButton.innerHTML = `
                <i class="fas fa-spinner fa-spin"></i>
                Очищаем...
            `;
            clearButton.classList.add('loading');
        } else {
            clearButton.disabled = false;
            clearButton.innerHTML = `
                <i class="fas fa-trash"></i>
                Очистить корзину
            `;
            clearButton.classList.remove('loading');
        }
    },

    /**
     * Очистить UI корзины
     */
    clearCartUI() {
        // Удалить все товары с анимацией
        const cartItems = document.querySelectorAll('[data-cart-item]');

        cartItems.forEach((item, index) => {
            setTimeout(() => {
                item.style.transition = 'all 0.3s ease';
                item.style.transform = 'translateX(-100%)';
                item.style.opacity = '0';

                setTimeout(() => {
                    item.remove();
                }, 300);
            }, index * 100); // Постепенное удаление
        });

        // Скрыть секцию корзины через время
        setTimeout(() => {
            const cartSection = document.querySelector('.cart-section');
            if (cartSection) {
                cartSection.style.display = 'none';
            }
        }, cartItems.length * 100 + 400);
    },

    /**
     * Показать состояние пустой корзины
     */
    showEmptyCart() {
        setTimeout(() => {
            // Скрыть секцию корзины
            const cartSection = document.querySelector('.cart-section');
            if (cartSection) {
                cartSection.style.display = 'none';
            }

            // Показать блок пустой корзины
            let emptyCartSection = document.querySelector('.empty-cart');

            if (!emptyCartSection) {
                // Создать блок пустой корзины если его нет
                emptyCartSection = document.createElement('section');
                emptyCartSection.className = 'empty-cart';
                emptyCartSection.innerHTML = `
                    <div class="empty-icon">
                        <i class="fas fa-shopping-cart"></i>
                    </div>
                    <h2 class="empty-title">Ваша корзина пуста</h2>
                    <p class="empty-description">
                        Добавьте товары в корзину, чтобы продолжить покупки.
                        У нас есть множество вкусных шоколадных изделий!
                    </p>
                    <a href="/categories" class="back-button">
                        <i class="fas fa-arrow-left"></i>
                        Начать покупки
                    </a>
                `;

                // Добавить после заголовка корзины
                const cartHeader = document.querySelector('.cart-header');
                if (cartHeader) {
                    cartHeader.insertAdjacentElement('afterend', emptyCartSection);
                }
            }

            emptyCartSection.style.display = 'block';
            emptyCartSection.style.opacity = '0';
            emptyCartSection.style.transition = 'opacity 0.5s ease';

            // Плавное появление
            setTimeout(() => {
                emptyCartSection.style.opacity = '1';
            }, 100);

            // Обновить заголовок
            const cartSubtitle = document.querySelector('.cart-subtitle');
            if (cartSubtitle) {
                cartSubtitle.style.display = 'none';
            }
        }, 500);
    },

    /**
     * Обновить счетчики корзины
     */
    updateCartCounters(itemCount, total) {
        // Обновить счетчик товаров в навигации
        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
        cartCounters.forEach(counter => {
            counter.textContent = itemCount || '0';
            if (itemCount === 0) {
                counter.style.display = 'none';
                counter.classList.add('d-none');
            }
        });

        // Обновить общую сумму
        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
        cartTotals.forEach(totalElement => {
            totalElement.textContent = `${total || 0} грн`;
        });

        // Обновить количество в заголовке корзины
        const cartSubtitle = document.querySelector('.cart-subtitle span');
        if (cartSubtitle) {
            cartSubtitle.textContent = itemCount || '0';
        }

        // Обновить все поля в сводке
        const summaryValues = document.querySelectorAll('.summary-value');
        summaryValues.forEach((value, index) => {
            if (index === 0) { // Количество товаров
                value.textContent = itemCount || '0';
            } else if (value.textContent.includes('грн')) { // Суммы
                value.textContent = `${total || 0} грн`;
            }
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
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            `;

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.style.animation = 'slideOut 0.3s ease';
                setTimeout(() => {
                    toast.remove();
                }, 300);
            }, 3000);
        }
    },

    /**
     * Показать диалог подтверждения с дополнительной информацией
     */
    showAdvancedConfirm() {
        const cartItems = document.querySelectorAll('[data-cart-item]');
        const itemCount = cartItems.length;

        if (itemCount === 0) {
            this.showNotification('Корзина уже пуста', 'info');
            return false;
        }

        const message = `Вы действительно хотите очистить корзину?\n\nБудет удалено: ${itemCount} товар(ов)\n\nЭто действие нельзя отменить.`;

        return confirm(message);
    },

    /**
     * Инициализация
     */
    init() {
        // Можно добавить дополнительные обработчики событий

        // Обработчик для Esc - отмена действий
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                // Можно добавить логику отмены операций
            }
        });

        console.log('CartClear initialized');
    }
};

// Глобальная функция для использования в HTML
window.clearCart = function(showConfirm = true) {
    CartClear.clearCart(showConfirm);
};

// Альтернативная функция с расширенным подтверждением
window.clearCartAdvanced = function() {
    if (CartClear.showAdvancedConfirm()) {
        CartClear.clearCart(false); // Уже показали свое подтверждение
    }
};

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    CartClear.init();
});

// Экспорт для модульного использования
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CartClear;
}

window.CartClear = CartClear;
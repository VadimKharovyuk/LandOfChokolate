/**
 * Cart Remove Functions
 * Функции для удаления товаров из корзины
 */

const CartRemove = {
    /**
     * Видалити товар з кошика
     */
    async removeProduct(productId, showConfirm = true) {
        // Підтвердження видалення
        if (showConfirm && !confirm('Видалити товар з кошика?')) {
            return;
        }

        if (!productId) {
            console.error('ID товару не вказано');
            return;
        }

        try {
            // Показати завантаження на елементі товару
            this.showItemLoading(productId, true);

            const formData = new FormData();
            formData.append('productId', productId);

            const response = await fetch('/cart/remove', {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                // Видалити елемент з DOM
                this.removeItemFromDOM(productId);

                // Оновити лічильники кошика
                this.updateCartCounters(data.cartItemCount, data.cartTotal);

                // Показати порожній кошик, якщо потрібно
                if (data.isEmpty) {
                    this.showEmptyCart();
                }

                // Показати сповіщення
                this.showNotification('Товар видалено з кошика', 'success');

                console.log('Товар видалено з кошика:', productId);
            } else {
                this.showNotification(data.message || 'Помилка при видаленні товару', 'error');
            }

        } catch (error) {
            console.error('Помилка видалення товару:', error);
            this.showNotification('Помилка з’єднання з сервером', 'error');
        } finally {
            this.showItemLoading(productId, false);
        }
    },

    /**
     * Показать/скрыть загрузку на элементе товара
     */
    showItemLoading(productId, loading) {
        const cartItem = document.querySelector(`[data-cart-item="${productId}"]`);
        if (!cartItem) return;

        if (loading) {
            cartItem.classList.add('removing');
            cartItem.style.opacity = '0.5';
            cartItem.style.pointerEvents = 'none';

            // Заблокировать кнопки
            const buttons = cartItem.querySelectorAll('button');
            buttons.forEach(btn => btn.disabled = true);
        } else {
            cartItem.classList.remove('removing');
            cartItem.style.opacity = '1';
            cartItem.style.pointerEvents = 'auto';

            // Разблокировать кнопки
            const buttons = cartItem.querySelectorAll('button');
            buttons.forEach(btn => btn.disabled = false);
        }
    },

    /**
     * Удалить элемент товара из DOM с анимацией
     */
    removeItemFromDOM(productId) {
        const cartItem = document.querySelector(`[data-cart-item="${productId}"]`);
        if (!cartItem) return;

        // Анимация удаления
        cartItem.style.transition = 'all 0.3s ease';
        cartItem.style.transform = 'translateX(-100%)';
        cartItem.style.opacity = '0';
        cartItem.style.maxHeight = cartItem.offsetHeight + 'px';

        setTimeout(() => {
            cartItem.style.maxHeight = '0';
            cartItem.style.padding = '0';
            cartItem.style.margin = '0';
            cartItem.style.border = 'none';
        }, 150);

        setTimeout(() => {
            cartItem.remove();

            // Проверить, остались ли товары
            const remainingItems = document.querySelectorAll('[data-cart-item]');
            if (remainingItems.length === 0) {
                this.showEmptyCart();
            }
        }, 300);
    },

    /**
     * Показать состояние пустой корзины
     */
    showEmptyCart() {
        const cartItemsContainer = document.querySelector('.cart-items');
        const cartSummary = document.querySelector('.cart-summary');
        const cartSection = document.querySelector('.cart-section');

        // Скрыть секцию корзины
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
               <h2 class="empty-title">Ваш кошик порожній</h2>
               <p class="empty-description">
                Додайте товари до кошика, щоб продовжити покупки.
                У нас є багато смачних шоколадних виробів!
                 </p>
                <a href="/categories" class="back-button">
                    <i class="fas fa-arrow-left"></i>
                     Розпочати покупки
                </a>
            `;

            // Добавить после заголовка корзины
            const cartHeader = document.querySelector('.cart-header');
            if (cartHeader) {
                cartHeader.insertAdjacentElement('afterend', emptyCartSection);
            }
        }

        emptyCartSection.style.display = 'block';

        // Обновить заголовок
        const cartSubtitle = document.querySelector('.cart-subtitle');
        if (cartSubtitle) {
            cartSubtitle.style.display = 'none';
        }
    },

    /**
     * Обновить счетчики корзины
     */
    updateCartCounters(itemCount, total) {
        // Обновить счетчик товаров
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

        // Обновить количество в заголовке
        const cartSubtitle = document.querySelector('.cart-subtitle span');
        if (cartSubtitle) {
            cartSubtitle.textContent = itemCount || '0';
        }
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
            `;

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.remove();
            }, 3000);
        }
    },

    /**
     * Инициализация
     */
    init() {
        console.log('CartRemove initialized');
    }
};

// Глобальная функция для использования в HTML
window.removeFromCart = function(productId) {
    CartRemove.removeProduct(productId);
};

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    CartRemove.init();
});

// Экспорт для модульного использования
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CartRemove;
}

window.CartRemove = CartRemove;
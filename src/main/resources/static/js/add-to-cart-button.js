
/**
 * Простая версия Add to Cart Button - без спиннера
 */
class AddToCartButton {
    constructor(element) {
        this.element = element;
        this.productId = this.getProductId();
        this.isLoading = false;

        this.init();
    }

    init() {
        this.element.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.handleClick();
        });
    }

    getProductId() {
        return this.element.dataset.productId ||
            this.element.dataset.id ||
            this.element.getAttribute('data-product-id') ||
            this.element.getAttribute('data-id');
    }

    getQuantity() {
        return parseInt(this.element.dataset.quantity) || 1;
    }

    async handleClick() {
        if (this.isLoading || !this.productId) {
            return;
        }

        const quantity = this.getQuantity();

        try {
            this.isLoading = true;
            await this.addToCart(this.productId, quantity);
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            this.showNotification('Помилка при додаванні товару', 'error');
        } finally {
            this.isLoading = false;
        }
    }

    async addToCart(productId, quantity) {
        const formData = new FormData();
        formData.append('productId', productId);
        formData.append('quantity', quantity);

        const response = await fetch('/cart/add', {
            method: 'POST',
            body: formData,
            credentials: 'same-origin',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        });

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const data = await response.json();

        if (data.success) {
            this.showNotification('Товар додано в кошик!', 'success');
            this.updateCartCounters(data.cartItemCount, data.cartTotal);
        } else {
            this.showNotification(data.message || 'Помилка при додаванні товару', 'error');
        }
    }

    showNotification(message, type) {
        // Удаляем существующие уведомления
        const existingNotifications = document.querySelectorAll('.cart-notification');
        existingNotifications.forEach(notification => notification.remove());

        // Создаем новое уведомление
        const notification = document.createElement('div');
        notification.className = `cart-notification ${type}`;
        notification.textContent = message;

        // Простые стили
        notification.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            z-index: 9999;
            padding: 16px 24px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            font-size: 14px;
            background: ${type === 'success' ? '#27ae60' : '#e74c3c'};
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            transform: translateX(100%);
            transition: transform 0.3s ease;
            max-width: 300px;
        `;

        document.body.appendChild(notification);

        // Показываем
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 10);

        // Скрываем через 3 секунды
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }, 3000);
    }

    updateCartCounters(count, total) {
        // Обновляем счетчики корзины
        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count], .mobile-nav-badge');
        cartCounters.forEach(counter => {
            counter.textContent = count || '0';
            if (count > 0) {
                counter.style.display = 'inline';
            }
        });

        // Обновляем общую сумму
        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
        cartTotals.forEach(totalElement => {
            totalElement.textContent = `${total} ₴`;
        });
    }

    static initAll() {
        const buttons = document.querySelectorAll('.add-to-cart-btn, [data-product-id]');
        const instances = [];

        buttons.forEach(button => {
            if (!button.hasAttribute('data-cart-initialized')) {
                const instance = new AddToCartButton(button);
                instances.push(instance);
                button.setAttribute('data-cart-initialized', 'true');
            }
        });

        return instances;
    }
}

// Автоматическая инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    AddToCartButton.initAll();
});

// Экспорт для глобального использования
window.AddToCartButton = AddToCartButton;
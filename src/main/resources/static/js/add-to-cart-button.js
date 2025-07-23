// /**
//  * Universal Add to Cart Button Component
//  * Универсальная кнопка добавления в корзину для использования на любых страницах
//  */
//
// class AddToCartButton {
//     constructor(element) {
//         this.element = element;
//         this.productId = this.getProductId();
//         this.isLoading = false;
//         this.originalContent = element.innerHTML;
//
//         this.init();
//     }
//
//     /**
//      * Инициализация кнопки
//      */
//     init() {
//         // Добавить обработчик клика
//         this.element.addEventListener('click', (e) => {
//             e.preventDefault();
//             e.stopPropagation();
//             this.handleClick();
//         });
//
//         // Добавить data-атрибут для идентификации
//         this.element.setAttribute('data-cart-button', this.productId);
//
//         // Сохранить оригинальный текст
//         this.element.setAttribute('data-original-text', this.originalContent);
//     }
//
//     /**
//      * Получить ID продукта из кнопки
//      */
//     getProductId() {
//         // Попробовать разные способы получения ID
//         return this.element.dataset.productId ||
//             this.element.dataset.id ||
//             this.element.getAttribute('data-product-id') ||
//             this.element.getAttribute('data-id') ||
//             this.extractIdFromOnclick();
//     }
//
//     /**
//      * Извлечь ID из onclick атрибута (для совместимости с Thymeleaf)
//      */
//     extractIdFromOnclick() {
//         const onclick = this.element.getAttribute('onclick');
//         if (onclick) {
//             const match = onclick.match(/addToCart\((\d+)\)/);
//             return match ? match[1] : null;
//         }
//         return null;
//     }
//
//     /**
//      * Получить количество для добавления
//      */
//     getQuantity() {
//         // Попробовать найти количество рядом с кнопкой
//         const quantityInput = this.findQuantityInput();
//         if (quantityInput) {
//             return parseInt(quantityInput.value) || 1;
//         }
//
//         // Из data-атрибута кнопки
//         return parseInt(this.element.dataset.quantity) || 1;
//     }
//
//     /**
//      * Найти поле количества рядом с кнопкой
//      */
//     findQuantityInput() {
//         const container = this.element.closest('.product-card, .product-details, .cart-form, .product-item');
//         if (container) {
//             return container.querySelector('input[name="quantity"], .quantity-input, input[type="number"]');
//         }
//         return null;
//     }
//
//     /**
//      * Обработка клика по кнопке
//      */
//     async handleClick() {
//         if (this.isLoading || !this.productId) {
//             return;
//         }
//
//         const quantity = this.getQuantity();
//
//         try {
//             this.setLoadingState(true);
//             await this.addToCart(this.productId, quantity);
//         } catch (error) {
//             console.error('Add to cart error:', error);
//         } finally {
//             this.setLoadingState(false);
//         }
//     }
//
//     /**
//      * Добавить товар в корзину
//      */
//     async addToCart(productId, quantity) {
//         const formData = new FormData();
//         formData.append('productId', productId);
//         formData.append('quantity', quantity);
//
//         const response = await fetch('/cart/add', {
//             method: 'POST',
//             body: formData
//         });
//
//         const data = await response.json();
//
//         if (data.success) {
//             // Показать успешное уведомление
//             this.showSuccess(data.message || 'Товар добавлен в корзину!');
//
//             // Обновить счетчики корзины
//             this.updateCartCounters(data.cartItemCount, data.cartTotal);
//
//             // Вызвать событие для кастомной логики
//             this.dispatchAddedEvent(productId, quantity, data);
//
//             // Анимация успеха
//             this.animateSuccess();
//
//         } else {
//             this.showError(data.message || 'Ошибка при добавлении товара');
//         }
//     }
//
//     /**
//      * Установить состояние загрузки с сохранением структуры кнопки
//      */
//     setLoadingState(loading) {
//         this.isLoading = loading;
//
//         if (loading) {
//             this.element.disabled = true;
//
//             // Проверить, есть ли вложенная структура (.btn-text)
//             const btnText = this.element.querySelector('.btn-text');
//             if (btnText) {
//                 // Сохранить оригинальную структуру с .btn-text
//                 btnText.innerHTML = `
//                     <i class="fas fa-spinner fa-spin"></i>
//                     <span>Добавляем...</span>
//                 `;
//             } else {
//                 // Обычная структура
//                 this.element.innerHTML = `
//                     <i class="fas fa-spinner fa-spin"></i>
//                     <span>Добавляем...</span>
//                 `;
//             }
//             this.element.classList.add('loading');
//         } else {
//             this.element.disabled = false;
//             this.element.innerHTML = this.originalContent;
//             this.element.classList.remove('loading');
//         }
//     }
//
//     /**
//      * Анимация успешного добавления
//      */
//     animateSuccess() {
//         // Временно показать галочку
//         const originalContent = this.element.innerHTML;
//         this.element.innerHTML = `
//             <i class="fas fa-check"></i>
//             <span>Добавлено!</span>
//         `;
//         this.element.classList.add('success');
//
//         setTimeout(() => {
//             this.element.innerHTML = originalContent;
//             this.element.classList.remove('success');
//         }, 1500);
//     }
//
//     /**
//      * Показать уведомление об успехе
//      */
//     showSuccess(message) {
//         if (typeof CartNotifications !== 'undefined') {
//             CartNotifications.show(message, 'success');
//         } else {
//             this.showSimpleNotification(message, 'success');
//         }
//     }
//
//     /**
//      * Показать уведомление об ошибке
//      */
//     showError(message) {
//         if (typeof CartNotifications !== 'undefined') {
//             CartNotifications.show(message, 'error');
//         } else {
//             this.showSimpleNotification(message, 'error');
//         }
//     }
//
//     /**
//      * Простое уведомление (если основной класс недоступен)
//      */
//     showSimpleNotification(message, type) {
//         const notification = document.createElement('div');
//         notification.className = `simple-notification ${type}`;
//         notification.textContent = message;
//         notification.style.cssText = `
//             position: fixed;
//             top: 20px;
//             right: 20px;
//             z-index: 9999;
//             padding: 1rem 1.5rem;
//             border-radius: 4px;
//             color: white;
//             font-weight: 500;
//             background: ${type === 'success' ? '#28a745' : '#dc3545'};
//             animation: slideIn 0.3s ease;
//         `;
//
//         document.body.appendChild(notification);
//
//         setTimeout(() => {
//             notification.remove();
//         }, 3000);
//     }
//
//     /**
//      * Обновить счетчики корзины
//      */
//     updateCartCounters(count, total) {
//         // Обновить счетчики
//         const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
//         cartCounters.forEach(counter => {
//             counter.textContent = count || '0';
//             if (count > 0) {
//                 counter.style.display = 'inline';
//                 counter.classList.remove('d-none');
//             }
//         });
//
//         // Обновить общую сумму
//         const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
//         cartTotals.forEach(totalElement => {
//             totalElement.textContent = `${total} грн`;
//         });
//     }
//
//     /**
//      * Отправить событие о добавлении товара
//      */
//     dispatchAddedEvent(productId, quantity, data) {
//         const event = new CustomEvent('product-added-to-cart', {
//             detail: {
//                 productId: productId,
//                 quantity: quantity,
//                 cartData: data,
//                 button: this.element
//             }
//         });
//
//         document.dispatchEvent(event);
//     }
//
//     /**
//      * Статический метод для инициализации всех кнопок на странице
//      */
//     static initAll(selector = '.add-to-cart, .btn-add-to-cart, .btn-related-cart, .add-to-cart-btn, [data-add-to-cart]') {
//         const buttons = document.querySelectorAll(selector);
//         const instances = [];
//
//         buttons.forEach(button => {
//             if (!button.hasAttribute('data-cart-initialized')) {
//                 const instance = new AddToCartButton(button);
//                 instances.push(instance);
//                 button.setAttribute('data-cart-initialized', 'true');
//             }
//         });
//
//         return instances;
//     }
//
//     /**
//      * Статический метод для инициализации конкретной кнопки
//      */
//     static init(element) {
//         if (element && !element.hasAttribute('data-cart-initialized')) {
//             const instance = new AddToCartButton(element);
//             element.setAttribute('data-cart-initialized', 'true');
//             return instance;
//         }
//         return null;
//     }
// }
//
// // Глобальные функции для обратной совместимости
// window.addToCart = function(productId, quantity = 1) {
//     const button = event?.target?.closest('button, .btn');
//     if (button) {
//         button.dataset.productId = productId;
//         button.dataset.quantity = quantity;
//         const instance = new AddToCartButton(button);
//         instance.handleClick();
//     }
// };
//
// // Автоматическая инициализация при загрузке DOM
// document.addEventListener('DOMContentLoaded', function() {
//     AddToCartButton.initAll();
//     console.log('Add to Cart buttons initialized');
// });
//
// // Инициализация новых кнопок при динамическом добавлении контента
// const observer = new MutationObserver(function(mutations) {
//     mutations.forEach(function(mutation) {
//         mutation.addedNodes.forEach(function(node) {
//             if (node.nodeType === 1) { // Element node
//                 // Проверить добавленный элемент
//                 if (node.matches && node.matches('.add-to-cart, .btn-add-to-cart, .btn-related-cart, .add-to-cart-btn, [data-add-to-cart]')) {
//                     AddToCartButton.init(node);
//                 }
//
//                 // Проверить дочерние элементы
//                 const buttons = node.querySelectorAll && node.querySelectorAll('.add-to-cart, .btn-add-to-cart, .btn-related-cart, .add-to-cart-btn, [data-add-to-cart]');
//                 if (buttons) {
//                     buttons.forEach(button => AddToCartButton.init(button));
//                 }
//             }
//         });
//     });
// });
//
// observer.observe(document.body, {
//     childList: true,
//     subtree: true
// });
//
// // Экспорт для модульного использования
// if (typeof module !== 'undefined' && module.exports) {
//     module.exports = AddToCartButton;
// }
//
// // Добавить в глобальную область
// window.AddToCartButton = AddToCartButton;


/**
 * Add to Cart Button для гибридной системы (сессии + UUID куки)
 */
class AddToCartButton {
    constructor(element) {
        this.element = element;
        this.productId = this.getProductId();
        this.isLoading = false;
        this.originalContent = element.innerHTML;

        // Название куки как в вашем сервисе
        this.CART_COOKIE_NAME = 'cart_id';

        this.init();
    }

    init() {
        this.element.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.handleClick();
        });

        this.element.setAttribute('data-cart-button', this.productId);
        this.element.setAttribute('data-original-text', this.originalContent);
    }

    getProductId() {
        return this.element.dataset.productId ||
            this.element.dataset.id ||
            this.element.getAttribute('data-product-id') ||
            this.element.getAttribute('data-id') ||
            this.extractIdFromOnclick();
    }

    extractIdFromOnclick() {
        const onclick = this.element.getAttribute('onclick');
        if (onclick) {
            const match = onclick.match(/addToCart\((\d+)\)/);
            return match ? match[1] : null;
        }
        return null;
    }

    getQuantity() {
        const quantityInput = this.findQuantityInput();
        if (quantityInput) {
            return parseInt(quantityInput.value) || 1;
        }
        return parseInt(this.element.dataset.quantity) || 1;
    }

    findQuantityInput() {
        const container = this.element.closest('.product-card, .product-details, .cart-form, .product-item');
        if (container) {
            return container.querySelector('input[name="quantity"], .quantity-input, input[type="number"]');
        }
        return null;
    }

    async handleClick() {
        if (this.isLoading || !this.productId) {
            console.warn('Кнопка заблокирована или отсутствует productId:', {
                isLoading: this.isLoading,
                productId: this.productId
            });
            return;
        }

        const quantity = this.getQuantity();
        console.log('=== ДОБАВЛЕНИЕ В КОРЗИНУ ===');
        console.log('ProductId:', this.productId, 'Quantity:', quantity);

        // Отладка куки
        this.debugCookies();

        try {
            this.setLoadingState(true);
            await this.addToCart(this.productId, quantity);
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
        } finally {
            this.setLoadingState(false);
        }
    }

    /**
     * Отладка куки
     */
    debugCookies() {
        console.log('Все куки:', document.cookie);

        const cartCookie = this.getCookieValue(this.CART_COOKIE_NAME);
        console.log(`Кука ${this.CART_COOKIE_NAME}:`, cartCookie);

        // Проверяем JSESSIONID
        const sessionCookie = this.getCookieValue('JSESSIONID');
        console.log('JSESSIONID:', sessionCookie);
    }

    /**
     * Получить значение куки
     */
    getCookieValue(name) {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [cookieName, cookieValue] = cookie.trim().split('=');
            if (cookieName === name) {
                return cookieValue;
            }
        }
        return null;
    }

    /**
     * Добавить товар в корзину
     */
    async addToCart(productId, quantity) {
        const formData = new FormData();
        formData.append('productId', productId);
        formData.append('quantity', quantity);

        console.log('Отправляем POST запрос на /cart/add');
        console.log('FormData:', {
            productId: formData.get('productId'),
            quantity: formData.get('quantity')
        });

        try {
            const response = await fetch('/cart/add', {
                method: 'POST',
                body: formData,
                credentials: 'same-origin', // КРИТИЧЕСКИ ВАЖНО для сессий
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            console.log('Статус ответа:', response.status);
            console.log('Заголовки ответа:');
            for (let [key, value] of response.headers.entries()) {
                console.log(`  ${key}: ${value}`);
            }

            if (!response.ok) {
                const errorText = await response.text();
                console.error('HTTP ошибка:', response.status, errorText);
                throw new Error(`Ошибка сервера: ${response.status} - ${errorText}`);
            }

            const data = await response.json();
            console.log('Ответ сервера:', data);

            if (data.success) {
                this.showSuccess(data.message || 'Товар добавлен в корзину!');
                this.updateCartCounters(data.cartItemCount, data.cartTotal);
                this.dispatchAddedEvent(productId, quantity, data);
                this.animateSuccess();

                // Отладка куки после запроса
                console.log('Куки после запроса:', document.cookie);
            } else {
                console.error('Сервер вернул ошибку:', data.message);
                this.showError(data.message || 'Ошибка при добавлении товара');
            }

        } catch (error) {
            console.error('Ошибка сети/парсинга:', error);
            this.showError('Помилка мережі: ' + error.message);
            throw error;
        }
    }

    setLoadingState(loading) {
        this.isLoading = loading;

        if (loading) {
            this.element.disabled = true;

            const btnText = this.element.querySelector('.btn-text');
            if (btnText) {
                btnText.innerHTML = `
                    <i class="fas fa-spinner fa-spin"></i>
                    <span>Добавляем...</span>
                `;
            } else {
                this.element.innerHTML = `
                    <i class="fas fa-spinner fa-spin"></i>
                    <span>Добавляем...</span>
                `;
            }
            this.element.classList.add('loading');
        } else {
            this.element.disabled = false;
            this.element.innerHTML = this.originalContent;
            this.element.classList.remove('loading');
        }
    }

    animateSuccess() {
        const originalContent = this.element.innerHTML;

        const btnText = this.element.querySelector('.btn-text');
        if (btnText) {
            btnText.innerHTML = `
                <i class="fas fa-check"></i>
                <span>Добавлено!</span>
            `;
        } else {
            this.element.innerHTML = `
                <i class="fas fa-check"></i>
                <span>Добавлено!</span>
            `;
        }

        this.element.classList.add('success');

        setTimeout(() => {
            this.element.innerHTML = originalContent;
            this.element.classList.remove('success');
        }, 1500);
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type) {
        // Попробуем использовать существующий toast
        const existingToast = document.getElementById('toast');
        if (existingToast) {
            existingToast.textContent = message;
            existingToast.className = `toast ${type}`;
            existingToast.classList.add('show');

            setTimeout(() => {
                existingToast.classList.remove('show');
            }, 3000);
            return;
        }

        // Создаем простое уведомление
        const notification = document.createElement('div');
        notification.className = `cart-notification ${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            background: ${type === 'success' ? '#28a745' : '#dc3545'};
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            transform: translateX(100%);
            transition: transform 0.3s ease;
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 10);

        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }

    updateCartCounters(count, total) {
        console.log('Обновляем счетчики корзины:', { count, total });

        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
        cartCounters.forEach(counter => {
            counter.textContent = count || '0';
            if (count > 0) {
                counter.style.display = 'inline';
                counter.classList.remove('d-none', 'hidden');
            }
        });

        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
        cartTotals.forEach(totalElement => {
            totalElement.textContent = `${total} ₴`;
        });
    }

    dispatchAddedEvent(productId, quantity, data) {
        const event = new CustomEvent('product-added-to-cart', {
            detail: {
                productId: productId,
                quantity: quantity,
                cartData: data,
                button: this.element
            }
        });

        document.dispatchEvent(event);
    }

    static initAll(selector = '.add-to-cart, .btn-add-to-cart, .add-to-cart-btn, [data-add-to-cart]') {
        const buttons = document.querySelectorAll(selector);
        const instances = [];

        console.log(`Инициализируем ${buttons.length} кнопок корзины`);

        buttons.forEach(button => {
            if (!button.hasAttribute('data-cart-initialized')) {
                const instance = new AddToCartButton(button);
                instances.push(instance);
                button.setAttribute('data-cart-initialized', 'true');
            }
        });

        return instances;
    }

    static init(element) {
        if (element && !element.hasAttribute('data-cart-initialized')) {
            const instance = new AddToCartButton(element);
            element.setAttribute('data-cart-initialized', 'true');
            return instance;
        }
        return null;
    }
}

// Глобальная функция для обратной совместимости
window.addToCart = function(productId, quantity = 1) {
    const button = event?.target?.closest('button, .btn');
    if (button) {
        button.dataset.productId = productId;
        button.dataset.quantity = quantity;
        const instance = new AddToCartButton(button);
        instance.handleClick();
    }
};

// Автоматическая инициализация
document.addEventListener('DOMContentLoaded', function() {
    console.log('Инициализация кнопок Add to Cart...');
    const instances = AddToCartButton.initAll();
    console.log(`Инициализировано ${instances.length} кнопок корзины`);
});

// Наблюдатель за динамически добавляемыми кнопками
const observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        mutation.addedNodes.forEach(function(node) {
            if (node.nodeType === 1) {
                if (node.matches && node.matches('.add-to-cart, .btn-add-to-cart, .add-to-cart-btn, [data-add-to-cart]')) {
                    AddToCartButton.init(node);
                }

                const buttons = node.querySelectorAll && node.querySelectorAll('.add-to-cart, .btn-add-to-cart, .add-to-cart-btn, [data-add-to-cart]');
                if (buttons) {
                    buttons.forEach(button => AddToCartButton.init(button));
                }
            }
        });
    });
});

observer.observe(document.body, {
    childList: true,
    subtree: true
});

window.AddToCartButton = AddToCartButton;
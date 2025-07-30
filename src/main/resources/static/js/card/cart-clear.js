// /**
//  * Cart Clear Functions
//  * Функции для очистки корзины
//  */
//
// const CartClear = {
//     /**
//      * Очистить всю корзину
//      */
//     async clearCart(showConfirm = true) {
//         // Подтверждение очистки
//         if (showConfirm && !confirm('Очистить всю корзину? Все товары будут удалены.')) {
//             return;
//         }
//
//         try {
//             // Показать загрузку
//             this.showClearLoading(true);
//
//             const response = await fetch('/cart/clear', {
//                 method: 'POST'
//             });
//
//             const data = await response.json();
//
//             if (data.success) {
//                 // Очистить UI
//                 this.clearCartUI();
//
//                 // Обновить счетчики
//                 this.updateCartCounters(0, 0);
//
//                 // Показать пустую корзину
//                 this.showEmptyCart();
//
//                 // Показать уведомление
//                 this.showNotification('Корзина очищена', 'success');
//
//                 console.log('Корзина очищена');
//             } else {
//                 this.showNotification(data.message || 'Ошибка при очистке корзины', 'error');
//             }
//
//         } catch (error) {
//             console.error('Ошибка очистки корзины:', error);
//             this.showNotification('Ошибка соединения с сервером', 'error');
//         } finally {
//             this.showClearLoading(false);
//         }
//     },
//
//     /**
//      * Показать/скрыть загрузку для кнопки очистки
//      */
//     showClearLoading(loading) {
//         const clearButton = document.querySelector('.btn-clear');
//         if (!clearButton) return;
//
//         if (loading) {
//             clearButton.disabled = true;
//             clearButton.innerHTML = `
//                 <i class="fas fa-spinner fa-spin"></i>
//                 Очищаем...
//             `;
//             clearButton.classList.add('loading');
//         } else {
//             clearButton.disabled = false;
//             clearButton.innerHTML = `
//                 <i class="fas fa-trash"></i>
//                 Очистить корзину
//             `;
//             clearButton.classList.remove('loading');
//         }
//     },
//
//     /**
//      * Очистить UI корзины
//      */
//     clearCartUI() {
//         // Удалить все товары с анимацией
//         const cartItems = document.querySelectorAll('[data-cart-item]');
//
//         cartItems.forEach((item, index) => {
//             setTimeout(() => {
//                 item.style.transition = 'all 0.3s ease';
//                 item.style.transform = 'translateX(-100%)';
//                 item.style.opacity = '0';
//
//                 setTimeout(() => {
//                     item.remove();
//                 }, 300);
//             }, index * 100); // Постепенное удаление
//         });
//
//         // Скрыть секцию корзины через время
//         setTimeout(() => {
//             const cartSection = document.querySelector('.cart-section');
//             if (cartSection) {
//                 cartSection.style.display = 'none';
//             }
//         }, cartItems.length * 100 + 400);
//     },
//
//     /**
//      * Показать состояние пустой корзины
//      */
//     showEmptyCart() {
//         setTimeout(() => {
//             // Скрыть секцию корзины
//             const cartSection = document.querySelector('.cart-section');
//             if (cartSection) {
//                 cartSection.style.display = 'none';
//             }
//
//             // Показать блок пустой корзины
//             let emptyCartSection = document.querySelector('.empty-cart');
//
//             if (!emptyCartSection) {
//                 // Створити блок порожнього кошика, якщо його немає
//                 emptyCartSection = document.createElement('section');
//                 emptyCartSection.className = 'empty-cart';
//                 emptyCartSection.innerHTML = `
//         <div class="empty-icon">
//             <i class="fas fa-shopping-cart"></i>
//         </div>
//         <h2 class="empty-title">Ваш кошик порожній</h2>
//         <p class="empty-description">
//             Додайте товари до кошика, щоб продовжити покупки.
//             У нас багато смачних шоколадних виробів!
//         </p>
//         <a href="/categories" class="back-button">
//             <i class="fas fa-arrow-left"></i>
//             Почати покупки
//         </a>
//     `;
//
//                 // Додати після заголовка кошика
//                 const cartHeader = document.querySelector('.cart-header');
//                 if (cartHeader) {
//                     cartHeader.insertAdjacentElement('afterend', emptyCartSection);
//                 }
//             }
//
//             emptyCartSection.style.display = 'block';
//             emptyCartSection.style.opacity = '0';
//             emptyCartSection.style.transition = 'opacity 0.5s ease';
//
//             // Плавное появление
//             setTimeout(() => {
//                 emptyCartSection.style.opacity = '1';
//             }, 100);
//
//             // Обновить заголовок
//             const cartSubtitle = document.querySelector('.cart-subtitle');
//             if (cartSubtitle) {
//                 cartSubtitle.style.display = 'none';
//             }
//         }, 500);
//     },
//
//     /**
//      * Обновить счетчики корзины
//      */
//     updateCartCounters(itemCount, total) {
//         // Обновить счетчик товаров в навигации
//         const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count]');
//         cartCounters.forEach(counter => {
//             counter.textContent = itemCount || '0';
//             if (itemCount === 0) {
//                 counter.style.display = 'none';
//                 counter.classList.add('d-none');
//             }
//         });
//
//         // Обновить общую сумму
//         const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
//         cartTotals.forEach(totalElement => {
//             totalElement.textContent = `${total || 0} грн`;
//         });
//
//         // Обновить количество в заголовке корзины
//         const cartSubtitle = document.querySelector('.cart-subtitle span');
//         if (cartSubtitle) {
//             cartSubtitle.textContent = itemCount || '0';
//         }
//
//         // Обновить все поля в сводке
//         const summaryValues = document.querySelectorAll('.summary-value');
//         summaryValues.forEach((value, index) => {
//             if (index === 0) { // Количество товаров
//                 value.textContent = itemCount || '0';
//             } else if (value.textContent.includes('грн')) { // Суммы
//                 value.textContent = `${total || 0} грн`;
//             }
//         });
//     },
//
//     /**
//      * Показать уведомление
//      */
//     showNotification(message, type = 'success') {
//         // Проверить, есть ли система уведомлений
//         if (typeof CartNotifications !== 'undefined') {
//             CartNotifications.show(message, type);
//         } else {
//             // Простое уведомление
//             console.log(`${type.toUpperCase()}: ${message}`);
//
//             const toast = document.createElement('div');
//             toast.className = `simple-toast ${type}`;
//             toast.textContent = message;
//             toast.style.cssText = `
//                 position: fixed;
//                 top: 20px;
//                 right: 20px;
//                 z-index: 9999;
//                 padding: 1rem 1.5rem;
//                 border-radius: 4px;
//                 color: white;
//                 font-weight: 500;
//                 background: ${type === 'success' ? '#28a745' : '#dc3545'};
//                 animation: slideIn 0.3s ease;
//                 box-shadow: 0 4px 12px rgba(0,0,0,0.15);
//             `;
//
//             document.body.appendChild(toast);
//
//             setTimeout(() => {
//                 toast.style.animation = 'slideOut 0.3s ease';
//                 setTimeout(() => {
//                     toast.remove();
//                 }, 300);
//             }, 3000);
//         }
//     },
//
//     /**
//      * Показать диалог подтверждения с дополнительной информацией
//      */
//     showAdvancedConfirm() {
//         const cartItems = document.querySelectorAll('[data-cart-item]');
//         const itemCount = cartItems.length;
//
//         if (itemCount === 0) {
//             this.showNotification('Корзина уже пуста', 'info');
//             return false;
//         }
//
//         const message = `Вы действительно хотите очистить корзину?\n\nБудет удалено: ${itemCount} товар(ов)\n\nЭто действие нельзя отменить.`;
//
//         return confirm(message);
//     },
//
//     /**
//      * Инициализация
//      */
//     init() {
//         // Можно добавить дополнительные обработчики событий
//
//         // Обработчик для Esc - отмена действий
//         document.addEventListener('keydown', (e) => {
//             if (e.key === 'Escape') {
//                 // Можно добавить логику отмены операций
//             }
//         });
//
//         console.log('CartClear initialized');
//     }
// };
//
// // Глобальная функция для использования в HTML
// window.clearCart = function(showConfirm = true) {
//     CartClear.clearCart(showConfirm);
// };
//
// // Альтернативная функция с расширенным подтверждением
// window.clearCartAdvanced = function() {
//     if (CartClear.showAdvancedConfirm()) {
//         CartClear.clearCart(false); // Уже показали свое подтверждение
//     }
// };
//
// // Инициализация при загрузке DOM
// document.addEventListener('DOMContentLoaded', function() {
//     CartClear.init();
// });
//
// // Экспорт для модульного использования
// if (typeof module !== 'undefined' && module.exports) {
//     module.exports = CartClear;
// }
//
// window.CartClear = CartClear;
/**
 * Cart Clear Functions
 * Функції для очищення кошика
 */

const CartClear = {
    /**
     * Очистити весь кошик
     */
    async clearCart(showConfirm = true) {
        // Підтвердження очищення
        if (showConfirm && !confirm('Очистити весь кошик? Усі товари будуть видалені.')) {
            return;
        }

        try {
            // Показати завантаження
            this.showClearLoading(true);

            const response = await fetch('/cart/clear', {
                method: 'POST'
            });

            const data = await response.json();

            if (data.success) {
                // Очистити UI
                this.clearCartUI();

                // Оновити лічильники
                this.updateCartCounters(0, 0);

                // Показати порожній кошик
                this.showEmptyCart();

                // Показати сповіщення
                this.showNotification('Кошик очищено', 'success');

                console.log('Кошик очищено');
            } else {
                this.showNotification(data.message || 'Помилка при очищенні кошика', 'error');
            }

        } catch (error) {
            console.error('Помилка очищення кошика:', error);
            this.showNotification('Помилка з\'єднання з сервером', 'error');
        } finally {
            this.showClearLoading(false);
        }
    },

    /**
     * Показати/приховати завантаження для кнопки очищення
     */
    showClearLoading(loading) {
        const clearButton = document.querySelector('.btn-clear');
        if (!clearButton) return;

        if (loading) {
            clearButton.disabled = true;
            clearButton.innerHTML = `
                <i class="fas fa-spinner fa-spin"></i>
                Очищаємо...
            `;
            clearButton.classList.add('loading');
        } else {
            clearButton.disabled = false;
            clearButton.innerHTML = `
                <i class="fas fa-trash"></i>
                Очистити кошик
            `;
            clearButton.classList.remove('loading');
        }
    },

    /**
     * Очистити UI кошика
     */
    clearCartUI() {
        // Видалити всі товари з анімацією
        const cartItems = document.querySelectorAll('[data-cart-item]');

        cartItems.forEach((item, index) => {
            setTimeout(() => {
                item.style.transition = 'all 0.3s ease';
                item.style.transform = 'translateX(-100%)';
                item.style.opacity = '0';

                setTimeout(() => {
                    item.remove();
                }, 300);
            }, index * 100); // Поступове видалення
        });

        // Приховати секцію кошика через час
        setTimeout(() => {
            const cartSection = document.querySelector('.cart-section');
            if (cartSection) {
                cartSection.style.display = 'none';
            }
        }, cartItems.length * 100 + 400);
    },

    /**
     * Показати стан порожнього кошика
     */
    showEmptyCart() {
        setTimeout(() => {
            // Приховати секцію кошика
            const cartSection = document.querySelector('.cart-section');
            if (cartSection) {
                cartSection.style.display = 'none';
            }

            // Показати блок порожнього кошика
            let emptyCartSection = document.querySelector('.empty-cart');

            if (!emptyCartSection) {
                // Створити блок порожнього кошика, якщо його немає
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

                // Додати після заголовка кошика
                const cartHeader = document.querySelector('.cart-header');
                if (cartHeader) {
                    cartHeader.insertAdjacentElement('afterend', emptyCartSection);
                }
            }

            emptyCartSection.style.display = 'block';
            emptyCartSection.style.opacity = '0';
            emptyCartSection.style.transition = 'opacity 0.5s ease';

            // Плавна поява
            setTimeout(() => {
                emptyCartSection.style.opacity = '1';
            }, 100);

            // Оновити заголовок
            const cartSubtitle = document.querySelector('.cart-subtitle');
            if (cartSubtitle) {
                cartSubtitle.style.display = 'none';
            }
        }, 500);
    },

    /**
     * Оновити лічильники кошика
     */
    updateCartCounters(itemCount, total) {
        // Оновити лічильник товарів у навігації
        const cartCounters = document.querySelectorAll('.cart-counter, .cart-count, [data-cart-count], .mobile-nav-badge');
        cartCounters.forEach(counter => {
            counter.textContent = itemCount || '0';
            if (itemCount === 0) {
                counter.style.display = 'none';
                counter.classList.add('d-none');
            }
        });

        // Оновити загальну суму
        const cartTotals = document.querySelectorAll('.cart-total, [data-cart-total]');
        cartTotals.forEach(totalElement => {
            totalElement.textContent = `${total || 0} ₴`;
        });

        // Оновити кількість у заголовку кошика
        const cartSubtitle = document.querySelector('.cart-subtitle span');
        if (cartSubtitle) {
            cartSubtitle.textContent = itemCount || '0';
        }

        // Оновити всі поля в зведенні
        const summaryValues = document.querySelectorAll('.summary-value');
        summaryValues.forEach((value, index) => {
            if (index === 0) { // Кількість товарів
                value.textContent = itemCount || '0';
            } else if (value.textContent.includes('₴')) { // Суми
                value.textContent = `${total || 0} ₴`;
            }
        });
    },

    /**
     * Показати сповіщення
     */
    showNotification(message, type = 'success') {
        // Видалити існуючі сповіщення
        const existingNotifications = document.querySelectorAll('.cart-notification, .simple-toast');
        existingNotifications.forEach(notification => notification.remove());

        // Створити нове сповіщення
        const toast = document.createElement('div');
        toast.className = `cart-notification ${type}`;
        toast.textContent = message;

        // Стилі для правильного позиціонування
        toast.style.cssText = `
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

        document.body.appendChild(toast);

        // Показати сповіщення
        setTimeout(() => {
            toast.style.transform = 'translateX(0)';
        }, 10);

        // Приховати через 3 секунди
        setTimeout(() => {
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.remove();
                }
            }, 300);
        }, 3000);
    },

    /**
     * Показати діалог підтвердження з додатковою інформацією
     */
    showAdvancedConfirm() {
        const cartItems = document.querySelectorAll('[data-cart-item]');
        const itemCount = cartItems.length;

        if (itemCount === 0) {
            this.showNotification('Кошик уже порожній', 'info');
            return false;
        }

        const message = `Ви дійсно хочете очистити кошик?\n\nБуде видалено: ${itemCount} товар(ів)\n\nЦю дію неможливо скасувати.`;

        return confirm(message);
    },

    /**
     * Ініціалізація
     */
    init() {
        // Можна додати додаткові обробники подій

        // Обробник для Esc - скасування дій
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                // Можна додати логіку скасування операцій
            }
        });

        console.log('CartClear ініціалізовано');
    }
};

// Глобальна функція для використання в HTML
window.clearCart = function(showConfirm = true) {
    CartClear.clearCart(showConfirm);
};

// Альтернативна функція з розширеним підтвердженням
window.clearCartAdvanced = function() {
    if (CartClear.showAdvancedConfirm()) {
        CartClear.clearCart(false); // Уже показали своє підтвердження
    }
};

// Ініціалізація при завантаженні DOM
document.addEventListener('DOMContentLoaded', function() {
    CartClear.init();
});

// Експорт для модульного використання
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CartClear;
}

window.CartClear = CartClear;
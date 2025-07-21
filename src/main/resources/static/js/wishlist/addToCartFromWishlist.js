// Функции для работы с избранным и корзиной
(function() {
    'use strict';

    // Функция добавления в корзину из избранного
    window.addToCartFromWishlist = function(button) {
        const productId = button.getAttribute('data-product-id');

        if (!productId || isNaN(productId)) {
            console.error('Некорректный ID товара:', productId);
            return;
        }

        // Блокируем кнопку
        const originalText = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Добавление...';

        fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: `productId=${encodeURIComponent(productId)}&quantity=1`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    updateCartCounter(data.cartItemCount);

                    button.innerHTML = '<i class="fas fa-check"></i> Добавлено!';
                    setTimeout(() => {
                        button.innerHTML = originalText;
                        button.disabled = false;
                    }, 2000);
                } else {
                    throw new Error(data.message || 'Ошибка при добавлении в корзину');
                }
            })
            .catch(error => {
                console.error('Ошибка при добавлении в корзину:', error);
                showNotification('Ошибка при добавлении в корзину: ' + error.message, 'error');
                button.innerHTML = originalText;
                button.disabled = false;
            });
    };

    // Функция удаления из избранного
    window.removeFromWishlist = function(button) {
        const productId = button.getAttribute('data-product-id');

        if (!productId || isNaN(productId)) {
            console.error('Некорректный ID товара:', productId);
            return;
        }

        if (!confirm('Удалить товар из избранного?')) {
            return;
        }

        const originalText = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

        fetch('/wishlist/remove', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: `productId=${encodeURIComponent(productId)}`
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    updateWishlistCounter(data.itemCount);

                    // Удаляем элемент из DOM с анимацией
                    const wishlistItem = button.closest('.wishlist-item');
                    if (wishlistItem) {
                        wishlistItem.style.transition = 'all 0.3s ease';
                        wishlistItem.style.opacity = '0';
                        wishlistItem.style.transform = 'translateX(100%)';

                        setTimeout(() => {
                            wishlistItem.remove();

                            // Проверяем, остались ли товары
                            const remainingItems = document.querySelectorAll('.wishlist-item');
                            if (remainingItems.length === 0) {
                                window.location.reload();
                            }
                        }, 300);
                    }
                } else {
                    throw new Error(data.message || 'Ошибка при удалении из избранного');
                }
            })
            .catch(error => {
                console.error('Ошибка при удалении из избранного:', error);
                showNotification('Ошибка при удалении из избранного: ' + error.message, 'error');
                button.innerHTML = originalText;
                button.disabled = false;
            });
    };

    // Функция очистки избранного
    window.clearWishlist = function() {
        if (!confirm('Очистить все избранное? Это действие нельзя отменить.')) {
            return;
        }

        fetch('/wishlist/clear', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    updateWishlistCounter(0);

                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    throw new Error(data.message || 'Ошибка при очистке избранного');
                }
            })
            .catch(error => {
                console.error('Ошибка при очистке избранного:', error);
                showNotification('Ошибка при очистке избранного: ' + error.message, 'error');
            });
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

    // Функция показа уведомлений
    function showNotification(message, type = 'success') {
        const existingNotifications = document.querySelectorAll('.notification');
        existingNotifications.forEach(notification => notification.remove());

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;

        const icon = type === 'success' ? 'check-circle' : 'exclamation-triangle';
        const bgColor = type === 'success' ? '#28a745' : '#dc3545';

        notification.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <span>${message}</span>
        `;

        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            max-width: 400px;
            padding: 1rem 1.5rem;
            border-radius: 4px;
            color: #FFFFFF;
            font-weight: 500;
            background: ${bgColor};
            display: flex;
            align-items: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideInRight 0.3s ease;
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            if (notification.parentNode) {
                notification.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 3000);
    }

    // CSS анимации
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
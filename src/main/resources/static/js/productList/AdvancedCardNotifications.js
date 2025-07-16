// ============================================================================
// ПРОДВИНУТЫЙ МОДУЛЬ: Уведомления в карточках
// ============================================================================
const AdvancedCardNotifications = {
    showCardNotification(productCard, message, type = 'success', position = 'center') {
        // Удаляем существующее уведомление если есть
        const existingNotification = productCard.querySelector('.card-notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // Получаем правильную иконку и цвет
        const notificationConfig = this.getNotificationConfig(type);

        // Создаем элемент уведомления
        const notification = document.createElement('div');
        notification.className = `card-notification ${type} position-${position}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="${notificationConfig.icon}"></i>
                <span class="notification-text">${message}</span>
            </div>
        `;

        // Добавляем в карточку
        productCard.style.position = 'relative';
        productCard.appendChild(notification);

        // Добавляем класс для затемнения карточки
        productCard.classList.add('notification-active');

        // Показываем с анимацией
        setTimeout(() => {
            notification.classList.add('show');
        }, 50);

        // Убираем через время в зависимости от типа
        const displayTime = type === 'favorite' ? 1500 : 2000;
        setTimeout(() => {
            this.hideNotification(notification, productCard);
        }, displayTime);
    },

    hideNotification(notification, productCard) {
        notification.classList.remove('show');
        productCard.classList.remove('notification-active');

        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    },

    getNotificationConfig(type) {
        const configs = {
            success: {
                icon: 'fas fa-shopping-cart',
                color: '#27ae60'
            },
            favorite: {
                icon: 'fas fa-heart',
                color: '#e74c3c'
            },
            remove: {
                icon: 'far fa-heart',
                color: '#95a5a6'
            },
            error: {
                icon: 'fas fa-exclamation-triangle',
                color: '#e74c3c'
            }
        };

        return configs[type] || configs.success;
    },

    // Специальные типы уведомлений
    showCartSuccess(productCard, quantity = 1) {
        const message = quantity > 1 ? `Додано ${quantity} шт. в кошик!` : 'Додано в кошик!';
        this.showCardNotification(productCard, message, 'success');
    },

    showFavoriteAdded(productCard) {
        this.showCardNotification(productCard, 'Додано в обране!', 'favorite');
    },

    showFavoriteRemoved(productCard) {
        this.showCardNotification(productCard, 'Видалено з обраного', 'remove');
    },

    showOutOfStock(productCard) {
        this.showCardNotification(productCard, 'Товар відсутній!', 'error');
    }
};

// ============================================================================
// ОБНОВЛЕННЫЙ МОДУЛЬ: Корзина и избранное (с продвинутыми уведомлениями)
// ============================================================================
const CartAndFavoritesAdvanced = {
    cart: [],
    favorites: [],

    addToCart(productId) {
        const button = event.target.closest('.add-to-cart-btn');
        const productCard = button.closest('.product-card');

        // Проверяем наличие товара
        if (button.disabled) {
            AdvancedCardNotifications.showOutOfStock(productCard);
            return;
        }

        const existingItem = this.cart.find(item => item.id === productId);
        let quantity = 1;

        if (existingItem) {
            existingItem.quantity += 1;
            quantity = existingItem.quantity;
        } else {
            this.cart.push({id: productId, quantity: 1});
        }

        // Показываем уведомление в карточке
        AdvancedCardNotifications.showCartSuccess(productCard, quantity);

        // Анимация кнопки
        this.animateButton(button, 'scale(0.95)');
    },

    toggleFavorite(button, productId) {
        const icon = button.querySelector('i');
        const isCurrentlyFavorited = this.favorites.includes(productId);
        const productCard = button.closest('.product-card');

        if (isCurrentlyFavorited) {
            this.favorites = this.favorites.filter(id => id !== productId);
            icon.className = 'far fa-heart';
            button.classList.remove('favorited');
            AdvancedCardNotifications.showFavoriteRemoved(productCard);
        } else {
            this.favorites.push(productId);
            icon.className = 'fas fa-heart';
            button.classList.add('favorited');
            AdvancedCardNotifications.showFavoriteAdded(productCard);
        }

        // Анимация кнопки
        this.animateButton(button, 'scale(1.2)');
    },

    animateButton(button, transform) {
        button.style.transform = transform;
        setTimeout(() => {
            button.style.transform = 'scale(1)';
        }, 200);
    },

    initializeFavorites() {
        this.favorites.forEach(productId => {
            const favoriteBtn = document.querySelector(`[onclick*="toggleFavorite(this, ${productId})"]`);
            if (favoriteBtn) {
                favoriteBtn.classList.add('favorited');
                favoriteBtn.querySelector('i').className = 'fas fa-heart';
            }
        });
    }
};
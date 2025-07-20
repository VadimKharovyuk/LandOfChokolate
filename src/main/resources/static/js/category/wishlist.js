// Отдельный скрипт для работы с избранным
(function() {
    'use strict';

    // Инициализируем Set для активных запросов, если его нет
    if (typeof window.activeRequests === 'undefined') {
        window.activeRequests = new Set();
    }

    // Улучшенная функция для работы с избранным
    function toggleFavorite(productId = null, buttonElement = null) {
        // Получаем кнопку избранного
        let button = buttonElement;
        if (!button && typeof event !== 'undefined' && event) {
            button = event.target.closest('.btn-favorite, .btn-related-favorite');
        }
        if (!button) {
            button = document.querySelector('.btn-favorite, .btn-related-favorite');
        }

        if (!button) {
            console.error('Кнопка избранного не найдена');
            return;
        }

        // Получаем ID товара из кнопки или параметра
        if (!productId) {
            productId = button.getAttribute('data-product-id');
            if (!productId) {
                console.error('ID товара не найден в атрибуте data-product-id');
                console.log('Кнопка:', button);
                return;
            }
        }

        console.log('Работаем с товаром ID:', productId);

        // Проверяем, нет ли уже активного запроса для этого товара
        const requestKey = `wishlist-${productId}`;
        if (window.activeRequests.has(requestKey)) {
            console.log('Запрос избранного уже выполняется...');
            return;
        }

        const icon = button.querySelector('i');
        const textSpan = button.querySelector('span');
        const isCurrentlyInWishlist = button.classList.contains('active');

        // Блокируем кнопку на время запроса
        button.disabled = true;
        window.activeRequests.add(requestKey);

        // Показываем состояние загрузки
        const originalIconClass = icon.className;
        const originalText = textSpan ? textSpan.textContent : '';
        icon.className = 'fas fa-spinner fa-spin';
        if (textSpan) {
            textSpan.textContent = isCurrentlyInWishlist ? 'Удаление...' : 'Добавление...';
        }

        // Определяем действие (добавить или удалить)
        const action = isCurrentlyInWishlist ? 'remove' : 'add';
        const url = `/wishlist/${action}`;

        // Определяем источник добавления
        const currentPage = window.location.pathname;
        let addedFromPage = 'unknown';

        if (currentPage.includes('/products/')) {
            addedFromPage = 'product-detail';
        } else if (currentPage.includes('/categories')) {
            addedFromPage = 'catalog';
        } else if (currentPage === '/') {
            addedFromPage = 'homepage';
        }

        // Подготавливаем данные для отправки
        const formData = new FormData();
        formData.append('productId', productId);
        if (action === 'add') {
            formData.append('addedFromPage', addedFromPage);
        }

        // Выполняем запрос
        fetch(url, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    // Обновляем визуальное состояние кнопки
                    if (action === 'add') {
                        button.classList.add('active');
                        icon.className = 'fas fa-heart';
                        button.title = 'Удалить из избранного';
                        if (textSpan) {
                            textSpan.textContent = 'В избранном';
                        }
                    } else {
                        button.classList.remove('active');
                        icon.className = 'far fa-heart';
                        button.title = 'В избранное';
                        if (textSpan) {
                            textSpan.textContent = 'В избранное';
                        }
                    }

                    // Обновляем счетчик избранного в навигации
                    updateWishlistCounter(data.itemCount);

                    // Показываем уведомление
                    showWishlistNotification(data.message, 'success');

                    // Анимация сердечка
                    if (action === 'add') {
                        icon.style.animation = 'heartbeat 0.6s ease';
                        setTimeout(() => {
                            icon.style.animation = '';
                        }, 600);
                    }

                } else {
                    // В случае ошибки возвращаем исходное состояние
                    icon.className = originalIconClass;
                    if (textSpan) {
                        textSpan.textContent = originalText;
                    }
                    showWishlistNotification(data.message || 'Произошла ошибка', 'error');
                }
            })
            .catch(error => {
                console.error('Ошибка при работе с избранным:', error);

                // Возвращаем исходное состояние
                icon.className = originalIconClass;
                if (textSpan) {
                    textSpan.textContent = originalText;
                }
                showWishlistNotification('Произошла ошибка. Попробуйте позже.', 'error');
            })
            .finally(() => {
                // Разблокируем кнопку
                button.disabled = false;
                window.activeRequests.delete(requestKey);
            });
    }

    // Функция для обновления счетчика избранного в навигации
    function updateWishlistCounter(count) {
        const wishlistCounters = document.querySelectorAll('.wishlist-count, .nav-wishlist-count, [data-wishlist-count]');

        wishlistCounters.forEach(counter => {
            counter.textContent = count;

            if (count > 0) {
                counter.style.display = 'inline-block';
                counter.style.opacity = '1';
            } else {
                counter.style.display = 'none';
            }
        });
    }

    // Функция для проверки статуса товара в избранном при загрузке страницы
    function checkWishlistStatus() {
        const wishlistButtons = document.querySelectorAll('.btn-favorite[data-product-id], .btn-related-favorite[data-product-id]');
        if (wishlistButtons.length === 0) return;

        // Собираем все ID товаров на странице
        const productIds = [];
        wishlistButtons.forEach(button => {
            const productId = button.getAttribute('data-product-id');
            if (productId) {
                productIds.push(parseInt(productId));
            }
        });

        if (productIds.length === 0) return;

        // Проверяем статус всех товаров одним запросом
        fetch('/wishlist/check-multiple', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(productIds)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success && data.results) {
                    // Обновляем состояние всех кнопок
                    Object.keys(data.results).forEach(productId => {
                        const isInWishlist = data.results[productId];
                        const button = document.querySelector(`[data-product-id="${productId}"].btn-favorite, [data-product-id="${productId}"].btn-related-favorite`);

                        if (button) {
                            const icon = button.querySelector('i');
                            const textSpan = button.querySelector('span');

                            if (isInWishlist) {
                                button.classList.add('active');
                                if (icon) {
                                    icon.className = 'fas fa-heart';
                                }
                                button.title = 'Удалить из избранного';
                                if (textSpan) {
                                    textSpan.textContent = 'В избранном';
                                }
                            } else {
                                button.classList.remove('active');
                                if (icon) {
                                    icon.className = 'far fa-heart';
                                }
                                button.title = 'В избранное';
                                if (textSpan) {
                                    textSpan.textContent = 'В избранное';
                                }
                            }
                        }
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка при проверке статуса избранного:', error);
            });
    }

    // Функция для получения текущего количества товаров в избранном
    function getWishlistCount() {
        fetch('/wishlist/count', {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    updateWishlistCounter(data.itemCount);
                }
            })
            .catch(error => {
                console.error('Ошибка при получении количества товаров в избранном:', error);
            });
    }

    // Функция показа уведомлений для избранного
    function showWishlistNotification(message, type = 'success') {
        // Удаляем существующие уведомления избранного
        const existingNotifications = document.querySelectorAll('.wishlist-notification');
        existingNotifications.forEach(notification => notification.remove());

        const notification = document.createElement('div');
        notification.className = `wishlist-notification notification ${type}`;

        const icon = type === 'success' ? 'heart' : 'exclamation-triangle';

        notification.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <span>${message}</span>
        `;

        // Добавляем стили для уведомления
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
            animation: slideIn 0.3s ease;
            background: ${type === 'success' ? '#e91e63' : '#dc3545'};
            display: flex;
            align-items: center;
            gap: 0.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        `;

        document.body.appendChild(notification);

        // Автоматически удаляем уведомление через 4 секунды
        setTimeout(() => {
            if (notification.parentNode) {
                notification.style.animation = 'slideOut 0.3s ease';
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 4000);
    }

    // Инициализация при загрузке страницы
    function initWishlist() {
        console.log('Инициализация wishlist...');

        // Инициализация кнопок избранного
        const wishlistButtons = document.querySelectorAll('.btn-favorite[data-product-id], .btn-related-favorite[data-product-id]');
        console.log('Найдено кнопок избранного:', wishlistButtons.length);

        wishlistButtons.forEach((button, index) => {
            const productId = button.getAttribute('data-product-id');
            console.log(`Кнопка ${index + 1}: ID=${productId}, класс=${button.className}`);

            // Удаляем старые обработчики если есть
            button.removeEventListener('click', handleWishlistClick);

            // Добавляем новый обработчик
            button.addEventListener('click', handleWishlistClick);
        });

        // Проверяем статус товаров в избранном
        checkWishlistStatus();

        // Получаем актуальное количество товаров в избранном
        getWishlistCount();

        // Добавляем CSS анимации если их нет
        if (!document.querySelector('#wishlist-styles')) {
            const style = document.createElement('style');
            style.id = 'wishlist-styles';
            style.textContent = `
                @keyframes slideIn {
                    from {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                    to {
                        transform: translateX(0);
                        opacity: 1;
                    }
                }

                @keyframes slideOut {
                    from {
                        transform: translateX(0);
                        opacity: 1;
                    }
                    to {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                }

                @keyframes heartbeat {
                    0% { transform: scale(1); }
                    25% { transform: scale(1.2); }
                    50% { transform: scale(1); }
                    75% { transform: scale(1.1); }
                    100% { transform: scale(1); }
                }

                .btn-favorite, .btn-related-favorite {
                    transition: all 0.2s ease;
                    background: none;
                    border: 1px solid #E8E8E8;
                    border-radius: 4px;
                    padding: 0.75rem 1.5rem;
                    color: #757575;
                    font-size: 0.9rem;
                    font-weight: 500;
                    cursor: pointer;
                    display: inline-flex;
                    align-items: center;
                    gap: 0.5rem;
                    text-decoration: none;
                    min-height: 44px;
                }

                .btn-favorite:hover, .btn-related-favorite:hover {
                    border-color: #e91e63;
                    color: #e91e63;
                    transform: translateY(-1px);
                    box-shadow: 0 2px 8px rgba(233, 30, 99, 0.2);
                }

                .btn-favorite:disabled, .btn-related-favorite:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                    transform: none;
                    box-shadow: none;
                }

                .btn-favorite.active, .btn-related-favorite.active {
                    background: #e91e63;
                    border-color: #e91e63;
                    color: white;
                }

                .btn-favorite.active:hover, .btn-related-favorite.active:hover {
                    background: #c2185b;
                    border-color: #c2185b;
                }

                .btn-favorite.active i, .btn-related-favorite.active i {
                    color: white;
                }

                .btn-favorite i, .btn-related-favorite i {
                    font-size: 1.1rem;
                    transition: all 0.2s ease;
                }

                .btn-favorite:hover i, .btn-related-favorite:hover i {
                    transform: scale(1.1);
                }

                .btn-favorite .fa-spinner, .btn-related-favorite .fa-spinner {
                    animation: spin 1s linear infinite;
                }

                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(style);
        }
    }

    // Обработчик клика по кнопке избранного
    function handleWishlistClick(e) {
        e.preventDefault();
        e.stopPropagation();

        const productId = parseInt(this.getAttribute('data-product-id'));
        console.log('Клик по кнопке избранного, ID товара:', productId);

        if (productId && !isNaN(productId)) {
            toggleFavorite(productId, this);
        } else {
            console.error('Некорректный ID товара:', this.getAttribute('data-product-id'));
        }
    }

    // Экспортируем функции в глобальную область
    window.wishlistFunctions = window.wishlistFunctions || {};
    Object.assign(window.wishlistFunctions, {
        toggleFavorite,
        updateWishlistCounter,
        checkWishlistStatus,
        getWishlistCount,
        showWishlistNotification,
        initWishlist
    });

    // Автоматическая инициализация при загрузке DOM
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWishlist);
    } else {
        initWishlist();
    }

    // Реинициализация при динамическом добавлении контента
    window.addEventListener('wishlist:reinit', initWishlist);

})();
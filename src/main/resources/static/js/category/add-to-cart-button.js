// Глобальная переменная для отслеживания активных запросов
let activeRequests = new Set();

/**
 * Добавление товара в корзину
 * @param {number} productId - ID товара
 * @param {number} quantity - количество товара (по умолчанию 1)
 */
function addToCart(productId, quantity = 1) {
    // Проверяем, нет ли уже активного запроса для этого товара
    const requestKey = `add-${productId}`;
    if (activeRequests.has(requestKey)) {
        console.log('Запрос уже выполняется...');
        return;
    }

    // Находим кнопку для данного товара
    const button = document.querySelector(`button[data-product-id="${productId}"]`);
    if (!button) {
        console.error('Кнопка не найдена для товара:', productId);
        return;
    }

    // Блокируем кнопку и добавляем в активные запросы
    activeRequests.add(requestKey);
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Добавление...';

    // Выполняем AJAX запрос
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: `productId=${productId}&quantity=${quantity}`
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // Показываем уведомление об успехе
                showNotification(data.message || 'Товар добавлен в корзину', 'success');

                // Временно меняем текст кнопки
                button.innerHTML = '<i class="fas fa-check"></i> Добавлено!';
                button.style.backgroundColor = '#28a745';
                button.style.borderColor = '#28a745';

                // Возвращаем оригинальный вид кнопки через 2 секунды
                setTimeout(() => {
                    button.innerHTML = originalText;
                    button.style.backgroundColor = '';
                    button.style.borderColor = '';
                }, 2000);

            } else {
                // Показываем ошибку
                showNotification(data.message || 'Произошла ошибка при добавлении товара', 'error');
            }
        })
        .catch(error => {
            console.error('Ошибка при добавлении товара в корзину:', error);
            showNotification('Произошла ошибка. Попробуйте позже.', 'error');
        })
        .finally(() => {
            // Разблокируем кнопку и удаляем из активных запросов
            button.disabled = false;
            activeRequests.delete(requestKey);
        });
}

/**
 * Функция показа уведомлений
 * @param {string} message - текст сообщения
 * @param {string} type - тип уведомления (success, error)
 */
function showNotification(message, type = 'success') {
    // Удаляем существующие уведомления
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    const icon = type === 'success' ? 'check-circle' : 'exclamation-triangle';

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
        background: ${type === 'success' ? '#28a745' : '#dc3545'};
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
document.addEventListener('DOMContentLoaded', function() {
    // Добавляем обработчики для кнопок с data-cart-action="add"
    const addToCartButtons = document.querySelectorAll('button[data-cart-action="add"]');

    addToCartButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopImmediatePropagation();

            const productId = parseInt(this.getAttribute('data-product-id'));
            if (productId && !this.disabled) {
                addToCart(productId);
            }
        });
    });

    // Добавляем CSS анимации если их нет
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
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
        `;
        document.head.appendChild(style);
    }
});
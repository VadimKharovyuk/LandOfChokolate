
window.OrderNotifications = {

    /**
     * Показать уведомление пользователю
     * @param {string} message - Текст сообщения
     * @param {string} type - Тип: 'info', 'error', 'success', 'warning'
     */
    show: function(message, type = 'info') {
        console.log(`Notification: ${type} - ${message}`);

        // Создаем элемент уведомления
        const notification = document.createElement('div');
        notification.className = `alert alert-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'info'}`;
        notification.innerHTML = `
            <i class="fas fa-${this.getIcon(type)}"></i>
            <span>${message}</span>
            <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>
        `;

        // Добавляем в начало формы
        const form = document.querySelector('.order-form-body');
        if (form) {
            form.insertBefore(notification, form.firstChild);

            // Прокручиваем к уведомлению
            notification.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

            // Убираем через 7 секунд
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.remove();
                }
            }, 7000);
        } else {
            // Fallback - обычный alert
            alert(message);
        }
    },

    /**
     * Получить иконку для типа уведомления
     */
    getIcon: function(type) {
        const icons = {
            'error': 'exclamation-triangle',
            'success': 'check-circle',
            'warning': 'exclamation-circle',
            'info': 'info-circle'
        };
        return icons[type] || 'info-circle';
    },

    /**
     * Показать ошибку
     */
    error: function(message) {
        this.show(message, 'error');
    },

    /**
     * Показать успех
     */
    success: function(message) {
        this.show(message, 'success');
    },

    /**
     * Показать предупреждение
     */
    warning: function(message) {
        this.show(message, 'warning');
    }
};

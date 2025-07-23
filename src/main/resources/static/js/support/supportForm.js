// Character counter for message field
function updateCharCounter() {
    const messageField = document.getElementById('message');
    const charCount = document.getElementById('charCount');
    const counter = document.querySelector('.char-counter');
    const currentLength = messageField.value.length;

    charCount.textContent = currentLength;

    // Remove all classes
    counter.classList.remove('success', 'warning', 'danger');

    // Add appropriate class based on character count
    if (currentLength < 6) {
        counter.classList.add('danger');
    } else if (currentLength > 900) {
        counter.classList.add('warning');
    } else {
        counter.classList.add('success');
    }
}

// Show notification
function showNotification(message, type = 'success', title = null) {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    const iconClass = type === 'success' ? 'check-circle' : 'exclamation-triangle';
    const defaultTitle = type === 'success' ? 'Успіх' : 'Помилка';

    notification.innerHTML = `
            <div class="notification-header">
                <i class="fas fa-${iconClass} notification-icon ${type}"></i>
                <div class="notification-title">${title || defaultTitle}</div>
            </div>
            <div class="notification-message">${message}</div>
        `;

    document.body.appendChild(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOut 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 400);
        }
    }, 5000);
}

// Form validation enhancement
function enhanceFormValidation() {
    const form = document.querySelector('form');
    const submitBtn = form.querySelector('.btn-submit');

    form.addEventListener('submit', function(e) {
        // Change button state
        const originalContent = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Відправляємо...';
        submitBtn.disabled = true;

        // If there are client-side validation errors, restore button
        setTimeout(() => {
            if (form.querySelector('.is-invalid')) {
                submitBtn.innerHTML = originalContent;
                submitBtn.disabled = false;
            }
        }, 100);
    });
}

// Phone number formatting
function formatPhoneNumber() {
    const phoneField = document.getElementById('phoneNumber');

    phoneField.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');

        if (value.startsWith('380')) {
            value = '+380' + value.substring(3);
        } else if (value.startsWith('80')) {
            value = '+3' + value;
        } else if (value.startsWith('0')) {
            value = '+38' + value;
        } else if (value.length > 0 && !value.startsWith('38')) {
            value = '+38' + value;
        }

        // Limit length
        if (value.length > 13) {
            value = value.substring(0, 13);
        }

        e.target.value = value;
    });
}

// Auto-hide alerts
function autoHideAlerts() {
    const alerts = document.querySelectorAll('.alert-custom');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-20px)';
            alert.style.transition = 'all 0.3s ease';
            setTimeout(() => alert.remove(), 300);
        }, 8000);
    });
}

// Initialize everything
document.addEventListener('DOMContentLoaded', function() {
    updateCharCounter();
    enhanceFormValidation();
    formatPhoneNumber();
    autoHideAlerts();

    // Focus first field if no alerts
    if (!document.querySelector('.alert-custom')) {
        document.getElementById('email').focus();
    }
});
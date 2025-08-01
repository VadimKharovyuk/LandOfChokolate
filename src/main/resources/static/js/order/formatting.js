/**
 * Модуль форматирования полей
 */
window.OrderFormatting = {

    /**
     * Форматирование номера телефона
     */
    formatPhone: function(input) {
        if (!input) return;

        input.addEventListener('input', function(e) {
            // Убираем все не-цифры
            let value = e.target.value.replace(/\D/g, '');

            // Ограничиваем длину до 10 цифр
            if (value.length > 10) {
                value = value.substring(0, 10);
            }

            // Форматируем номер: 066 842 0005
            if (value.length >= 6) {
                value = value.substring(0, 3) + ' ' + value.substring(3, 6) + ' ' + value.substring(6);
            } else if (value.length >= 3) {
                value = value.substring(0, 3) + ' ' + value.substring(3);
            }

            e.target.value = value;
        });
    },

    /**
     * Подготовить номер телефона для отправки
     */
    preparePhoneForSubmit: function(form, phoneInput) {
        if (!phoneInput) return;

        const phoneValue = phoneInput.value.replace(/\D/g, '');

        if (phoneValue.length > 0 && !phoneValue.startsWith('38')) {
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'phoneNumber';
            hiddenInput.value = '+38' + phoneValue;

            phoneInput.disabled = true;
            form.appendChild(hiddenInput);
        }
    }
};

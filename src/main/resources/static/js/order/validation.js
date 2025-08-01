/**
 * Модуль валидации формы
 */
window.OrderValidation = {

    /**
     * Валидация формы заказа
     */
    validateForm: function(form) {
        console.log('Validation: Проверяем форму...');

        const deliveryMethod = document.querySelector('input[name="deliveryMethod"]:checked');

        // Проверяем Nova Poshta
        if (deliveryMethod && deliveryMethod.value === 'NOVA_POSHTA') {
            if (!window.NovaPoshta.validate()) {
                return false;
            }
        }

        // Подготавливаем номер телефона
        const phoneInput = document.getElementById('phoneNumber');
        window.OrderFormatting.preparePhoneForSubmit(form, phoneInput);

        console.log('Validation: ✅ Форма валидна');
        return true;
    }
};
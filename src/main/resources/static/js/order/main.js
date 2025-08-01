
window.OrderMain = {

    /**
     * Инициализация
     */
    init: function() {
        console.log('OrderMain: Инициализация...');

        // Ждем загрузки DOM
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setupEventListeners());
        } else {
            this.setupEventListeners();
        }
    },

    /**
     * Настройка обработчиков событий
     */
    setupEventListeners: function() {
        console.log('OrderMain: Настройка обработчиков событий...');

        // Инициализация выбранного метода доставки
        this.initializeSelectedDelivery();

        // Форматирование телефона
        const phoneInput = document.getElementById('phoneNumber');
        window.OrderFormatting.formatPhone(phoneInput);

        // Обработчик изменения города
        this.setupCityChangeHandler();

        // Обработчик отправки формы
        this.setupFormSubmitHandler();
    },

    /**
     * Выбор способа доставки
     */
    selectDelivery: function(method) {
        console.log('OrderMain: Выбран метод доставки:', method);

        // Убираем активный класс со всех карточек
        document.querySelectorAll('.delivery-option').forEach(option => {
            option.classList.remove('selected');
        });

        // Добавляем активный класс к выбранной карточке
        event.currentTarget.classList.add('selected');

        // Отмечаем соответствующий radio button
        const radio = document.querySelector(`input[value="${method}"]`);
        if (radio) radio.checked = true;

        // Управляем полями Nova Poshta
        window.NovaPoshta.toggleFields(method === 'NOVA_POSHTA');
    },

    /**
     * Инициализация выбранного способа доставки
     */
    initializeSelectedDelivery: function() {
        const checkedRadio = document.querySelector('input[name="deliveryMethod"]:checked');
        if (checkedRadio) {
            const option = checkedRadio.closest('.delivery-option');
            if (option) {
                option.classList.add('selected');
            }
        }
    },

    /**
     * Настройка обработчика изменения города
     */
    setupCityChangeHandler: function() {
        const citySelect = document.getElementById('citySelect');
        if (citySelect) {
            citySelect.addEventListener('change', function() {
                window.NovaPoshta.loadDepartments(this.value);
            });
        }
    },

    /**
     * Настройка обработчика отправки формы
     */
    setupFormSubmitHandler: function() {
        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', function(e) {
                console.log('OrderMain: Отправка формы...');

                if (!window.OrderValidation.validateForm(this)) {
                    e.preventDefault();
                    return false;
                }

                console.log('OrderMain: Форма отправляется...');
            });
        }
    }
};

// Глобальная функция для кнопок (совместимость)
function selectDelivery(method) {
    window.OrderMain.selectDelivery(method);
}

// Автоматическая инициализация
window.OrderMain.init();
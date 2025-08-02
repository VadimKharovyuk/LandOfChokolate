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

        // Ждем готовности Nova Poshta модуля
        this.ensureNovaPoshtaReady().then(() => {
            // Инициализация выбранного метода доставки
            this.initializeSelectedDelivery();
        });

        // Форматирование телефона
        const phoneInput = document.getElementById('phoneNumber');
        if (window.OrderFormatting) {
            window.OrderFormatting.formatPhone(phoneInput);
        }

        // Обработчик изменения города (для старого select - совместимость)
        this.setupCityChangeHandler();

        // Обработчик отправки формы
        this.setupFormSubmitHandler();
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Проверка готовности Nova Poshta модуля
     */
    ensureNovaPoshtaReady: function() {
        return new Promise((resolve) => {
            if (window.NovaPoshta && typeof window.NovaPoshta.toggleFields === 'function') {
                console.log('✅ Nova Poshta module is ready');
                resolve(true);
                return;
            }

            console.log('⏳ Waiting for Nova Poshta module...');

            // Ждем загрузки модуля максимум 3 секунды
            let attempts = 0;
            const maxAttempts = 30; // 30 * 100ms = 3 секунды

            const checkInterval = setInterval(() => {
                attempts++;

                if (window.NovaPoshta && typeof window.NovaPoshta.toggleFields === 'function') {
                    clearInterval(checkInterval);
                    console.log('✅ Nova Poshta module loaded after', attempts * 100, 'ms');
                    resolve(true);
                } else if (attempts >= maxAttempts) {
                    clearInterval(checkInterval);
                    console.error('❌ Nova Poshta module failed to load within 3 seconds');
                    resolve(false);
                }
            }, 100);
        });
    },

    /**
     * ✅ ОБНОВЛЕННЫЙ: Выбор способа доставки
     */
    selectDelivery: function(method) {
        console.log('OrderMain: Выбран метод доставки:', method);

        // Убираем активный класс со всех карточек
        document.querySelectorAll('.delivery-option').forEach(option => {
            option.classList.remove('selected');
        });

        // Добавляем активный класс к выбранной карточке
        const selectedOption = document.getElementById(method.toLowerCase() + '-option') ||
            document.getElementById(method.toLowerCase().replace('_', '') + '-option');
        if (selectedOption) {
            selectedOption.classList.add('selected');
        }

        // Отмечаем соответствующий radio button
        const radio = document.querySelector(`input[value="${method}"]`);
        if (radio) {
            radio.checked = true;
        }

        // ✅ УЛУЧШЕННОЕ управление полями Nova Poshta с проверками
        if (method === 'NOVA_POSHTA') {
            if (window.NovaPoshta && typeof window.NovaPoshta.toggleFields === 'function') {
                console.log('OrderMain: Показываем поля Nova Poshta');
                window.NovaPoshta.toggleFields(true);
            } else {
                console.warn('OrderMain: Nova Poshta module not ready, showing fields manually');

                // Показываем поля вручную если модуль не готов
                const novaPoshtaFields = document.getElementById('novaPoshtaFields');
                if (novaPoshtaFields) {
                    novaPoshtaFields.style.display = 'block';
                }

                // Пытаемся инициализировать через задержку
                setTimeout(() => {
                    if (window.NovaPoshta && typeof window.NovaPoshta.initializeFields === 'function') {
                        window.NovaPoshta.initializeFields();
                    }
                }, 100);
            }
        } else {
            // Скрываем поля Nova Poshta для других методов доставки
            if (window.NovaPoshta && typeof window.NovaPoshta.toggleFields === 'function') {
                console.log('OrderMain: Скрываем поля Nova Poshta');
                window.NovaPoshta.toggleFields(false);
            } else {
                // Скрываем поля вручную если модуль не готов
                const novaPoshtaFields = document.getElementById('novaPoshtaFields');
                if (novaPoshtaFields) {
                    novaPoshtaFields.style.display = 'none';
                }
            }
        }
    },

    /**
     * ✅ ОБНОВЛЕННАЯ: Инициализация выбранного способа доставки
     */
    initializeSelectedDelivery: function() {
        console.log('OrderMain: Инициализация выбранного способа доставки...');

        const checkedRadio = document.querySelector('input[name="deliveryMethod"]:checked');
        if (checkedRadio) {
            const method = checkedRadio.value;
            console.log('OrderMain: Найден предвыбранный метод:', method);

            // Выделяем опцию визуально
            const option = checkedRadio.closest('.delivery-option');
            if (option) {
                option.classList.add('selected');
            }

            // Показываем поля Nova Poshta если они выбраны
            if (method === 'NOVA_POSHTA') {
                console.log('OrderMain: Nova Poshta уже выбрана, показываем поля');

                if (window.NovaPoshta && typeof window.NovaPoshta.toggleFields === 'function') {
                    window.NovaPoshta.toggleFields(true);
                } else {
                    // Показываем поля вручную
                    const novaPoshtaFields = document.getElementById('novaPoshtaFields');
                    if (novaPoshtaFields) {
                        novaPoshtaFields.style.display = 'block';
                    }
                }
            }
        } else {
            console.log('OrderMain: Метод доставки не выбран');
        }
    },

    /**
     * Настройка обработчика изменения города (для совместимости со старым select)
     */
    setupCityChangeHandler: function() {
        const citySelect = document.getElementById('citySelect');
        if (citySelect) {
            console.log('OrderMain: Настройка обработчика старого citySelect');
            citySelect.addEventListener('change', function() {
                if (window.NovaPoshta && typeof window.NovaPoshta.loadDepartments === 'function') {
                    window.NovaPoshta.loadDepartments(this.value);
                }
            });
        }
    },

    /**
     * ✅ УЛУЧШЕННАЯ: Настройка обработчика отправки формы
     */
    setupFormSubmitHandler: function() {
        const form = document.querySelector('form');
        if (form) {
            console.log('OrderMain: Настройка обработчика отправки формы');

            form.addEventListener('submit', function(e) {
                console.log('OrderMain: Отправка формы...');

                // Валидация общей формы
                if (window.OrderValidation && !window.OrderValidation.validateForm(this)) {
                    console.log('OrderMain: Основная валидация не прошла');
                    e.preventDefault();
                    return false;
                }

                // ✅ ДОБАВЛЕНА: Специальная валидация Nova Poshta
                const selectedMethod = document.querySelector('input[name="deliveryMethod"]:checked');
                if (selectedMethod && selectedMethod.value === 'NOVA_POSHTA') {
                    console.log('OrderMain: Проверяем поля Nova Poshta...');

                    if (window.NovaPoshta && typeof window.NovaPoshta.validate === 'function') {
                        if (!window.NovaPoshta.validate()) {
                            console.log('OrderMain: Валидация Nova Poshta не прошла');
                            e.preventDefault();
                            return false;
                        }
                    } else {
                        console.warn('OrderMain: Nova Poshta validation not available');
                    }
                }

                console.log('OrderMain: ✅ Все проверки пройдены, форма отправляется...');
            });
        }
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Получить информацию о текущем выборе
     */
    getCurrentDeliveryInfo: function() {
        const selectedMethod = document.querySelector('input[name="deliveryMethod"]:checked');
        const info = {
            method: selectedMethod ? selectedMethod.value : null,
            isNovaPoshtaSelected: selectedMethod && selectedMethod.value === 'NOVA_POSHTA',
            novaPoshtaReady: window.NovaPoshta && typeof window.NovaPoshta.validate === 'function'
        };

        if (info.isNovaPoshtaSelected && info.novaPoshtaReady) {
            info.selectedCity = window.NovaPoshta.getSelectedCity ? window.NovaPoshta.getSelectedCity() : null;
        }

        return info;
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Диагностика для отладки
     */
    diagnose: function() {
        console.log('=== OrderMain Диагностика ===');

        const info = this.getCurrentDeliveryInfo();
        console.log('Текущий метод доставки:', info.method);
        console.log('Nova Poshta выбрана:', info.isNovaPoshtaSelected);
        console.log('Nova Poshta модуль готов:', info.novaPoshtaReady);

        if (info.isNovaPoshtaSelected) {
            console.log('Выбранный город:', info.selectedCity);

            const cityRef = document.getElementById('cityRef');
            const departmentSelect = document.getElementById('departmentSelect');

            console.log('City Ref:', cityRef ? cityRef.value : 'не найден');
            console.log('Department Selected:', departmentSelect ? departmentSelect.value : 'не найден');
        }

        console.log('===============================');
        return info;
    }
};

// ✅ ОБНОВЛЕННАЯ глобальная функция для кнопок (совместимость)
function selectDelivery(method) {
    console.log('Global selectDelivery called with:', method);

    if (window.OrderMain && typeof window.OrderMain.selectDelivery === 'function') {
        window.OrderMain.selectDelivery(method);
    } else {
        console.error('OrderMain not available');
    }
}

// ✅ ОБНОВЛЕННАЯ автоматическая инициализация
console.log('Loading OrderMain module...');

// Инициализируем сразу при загрузке скрипта
window.OrderMain.init();

// Дополнительная проверка через 1 секунду (для медленных соединений)
setTimeout(() => {
    if (document.readyState === 'complete') {
        console.log('OrderMain: Дополнительная проверка инициализации...');

        // Проверяем что Nova Poshta поля корректно показаны/скрыты
        const deliveryInfo = window.OrderMain.getCurrentDeliveryInfo();
        if (deliveryInfo.isNovaPoshtaSelected) {
            const novaPoshtaFields = document.getElementById('novaPoshtaFields');
            if (novaPoshtaFields && novaPoshtaFields.style.display === 'none') {
                console.log('OrderMain: Исправляем отображение Nova Poshta полей');
                window.OrderMain.selectDelivery('NOVA_POSHTA');
            }
        }
    }
}, 1000);
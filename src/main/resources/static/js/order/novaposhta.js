// Файл: /js/order/novaposhta.js

document.addEventListener('DOMContentLoaded', function() {
    console.log('Nova Poshta unified module loaded');

    // ✅ Основные элементы формы
    const cityInput = document.getElementById('cityInput');
    const cityDropdown = document.getElementById('cityDropdown');
    const cityLoading = document.getElementById('cityLoading');
    const cityRef = document.getElementById('cityRef');
    const cityName = document.getElementById('cityName');
    const departmentSelect = document.getElementById('departmentSelect');
    const departmentName = document.getElementById('departmentName');

    // ✅ Состояние модуля
    let allCities = [];
    let currentCities = [];
    let searchTimeout = null;
    let selectedCity = null;
    let highlightedIndex = -1;

    // ✅ Инициализация при загрузке
    init();

    function init() {
        console.log('Initializing Nova Poshta module...');

        // Восстанавливаем значения при загрузке страницы
        if (cityName && cityName.value && cityRef && cityRef.value) {
            console.log('Restoring saved city:', cityName.value);
            cityInput.value = cityName.value;
            loadDepartments(cityRef.value);
        }

        // Загружаем все города для быстрого поиска
        loadAllCities();

        // Настраиваем обработчики событий
        setupEventListeners();
    }

    // ✅ 1. Исправьте loadAllCities функцию:
    function loadAllCities() {
        if (allCities.length > 0) {
            console.log(`Cities already loaded: ${allCities.length} cities`);
            return Promise.resolve(allCities);
        }

        console.log('Loading all cities for search...');

        return fetch('/api/novaposhta/cities/search?query=all') // ← Добавьте параметр
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(result => {
                // ✅ ИСПРАВЛЕНО: обрабатываем обернутый ответ
                if (result.success && Array.isArray(result.data)) {
                    allCities = result.data.filter(city =>
                        city && city.ref && city.description &&
                        city.ref.length > 0 && city.description.length > 0
                    );
                    console.log(`✅ Loaded ${allCities.length} cities for search`);
                } else {
                    console.warn('Invalid cities response:', result);
                    allCities = [];
                }
                return allCities;
            })
            .catch(error => {
                console.error('Error loading all cities:', error);
                allCities = [];
                return [];
            });
    }

    // ✅ Настройка обработчиков событий
    function setupEventListeners() {
        if (!cityInput) {
            console.error('cityInput not found');
            return;
        }

        // Поиск с задержкой
        cityInput.addEventListener('input', handleCityInput);

        // Навигация клавишами
        cityInput.addEventListener('keydown', handleKeyNavigation);

        // Скрытие dropdown при клике вне
        document.addEventListener('click', handleOutsideClick);

        // Валидация формы
        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', handleFormSubmit);
        }
    }

    // ✅ Обработчик ввода в поле города
    function handleCityInput(event) {
        const query = event.target.value.trim();

        // Очищаем предыдущий таймаут
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        highlightedIndex = -1;

        if (query.length < 2) {
            hideCityDropdown();
            clearSelectedCity();
            return;
        }

        // Показываем индикатор загрузки
        showCityLoading();

        // Устанавливаем задержку для уменьшения количества запросов
        searchTimeout = setTimeout(() => {
            searchCities(query);
        }, 300);
    }

    // ✅ Навигация клавишами
    function handleKeyNavigation(event) {
        const items = cityDropdown.querySelectorAll('.city-dropdown-item');

        if (event.key === 'ArrowDown') {
            event.preventDefault();
            highlightedIndex = Math.min(highlightedIndex + 1, items.length - 1);
            updateHighlight(items);
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            highlightedIndex = Math.max(highlightedIndex - 1, -1);
            updateHighlight(items);
        } else if (event.key === 'Enter') {
            event.preventDefault();
            if (highlightedIndex >= 0 && items[highlightedIndex]) {
                const cityData = JSON.parse(items[highlightedIndex].dataset.city);
                selectCity(cityData);
            }
        } else if (event.key === 'Escape') {
            hideCityDropdown();
        }
    }

    // ✅ Клик вне dropdown
    function handleOutsideClick(event) {
        if (!event.target.closest('.city-search-container')) {
            hideCityDropdown();
        }
    }

    // ✅ Валидация формы
    function handleFormSubmit(event) {
        const deliveryMethod = document.querySelector('input[name="deliveryMethod"]:checked');

        if (deliveryMethod && deliveryMethod.value === 'NOVA_POSHTA') {
            if (!validate()) {
                event.preventDefault();
                return false;
            }
        }
    }

    // ✅ Поиск городов
    function searchCities(query) {
        console.log('Searching cities for:', query);

        if (allCities.length > 0) {
            // Поиск среди загруженных городов
            const results = performLocalSearch(query);
            displayCities(results, query);
            hideCityLoading();
        } else {
            // Поиск через API
            performAPISearch(query);
        }
    }

    // 3. Исправьте функцию performLocalSearch (для совместимости с API ответом):
    function performLocalSearch(query) {
        const searchQuery = query.toLowerCase().trim();

        return allCities.filter(city => {
            // ✅ ДОБАВЛЕНА поддержка разных форматов полей (заглавные и строчные буквы)
            const description = (city.description || city.Description || '').toLowerCase();
            const descriptionRu = (city.descriptionRu || city.DescriptionRu || '').toLowerCase();

            // Основной поиск
            if (description.includes(searchQuery) || descriptionRu.includes(searchQuery)) {
                return true;
            }

            // Поиск по частям для составных названий
            const queryParts = searchQuery.split(' ').filter(part => part.length > 1);
            if (queryParts.length > 0) {
                return queryParts.every(part =>
                    description.includes(part) || descriptionRu.includes(part)
                );
            }

            return false;
        }).slice(0, 10);
    }

    // ✅ 2. Исправьте performAPISearch функцию:
    function performAPISearch(query) {
        fetch(`/api/novaposhta/cities/search?query=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(result => {
                // ✅ ИСПРАВЛЕНО: обрабатываем обернутый ответ
                console.log('API response:', result);

                if (result.success && Array.isArray(result.data)) {
                    currentCities = result.data;
                    console.log('Found cities via API:', currentCities.length);
                } else {
                    console.log('No cities found or error:', result.message);
                    currentCities = [];
                }

                displayCities(currentCities, query);
                hideCityLoading();
            })
            .catch(error => {
                console.error('Error searching cities via API:', error);
                hideCityLoading();
                showCityError('Помилка пошуку міст');
            });
    }

    // ✅ Отображение результатов поиска

// 2. Исправьте функцию displayCities (строка ~250):
    function displayCities(cities, query) {
        cityDropdown.innerHTML = '';

        if (cities.length === 0) {
            cityDropdown.innerHTML = `
            <div class="city-dropdown-item no-results">
                <i class="fas fa-search"></i>
                Не знайдено міст за запитом "${query}"
            </div>
        `;
        } else {
            cityDropdown.innerHTML = cities.map(city => {
                // ✅ ДОБАВЛЕНЫ проверки на существование полей
                const cityDescription = city.description || city.Description || '';
                const cityDescriptionRu = city.descriptionRu || city.DescriptionRu || '';
                const settlementType = city.settlementTypeDescription || city.SettlementTypeDescription || '';

                const displayName = highlightMatch(cityDescription, query);
                let additionalInfo = '';

                if (cityDescriptionRu) {
                    additionalInfo += cityDescriptionRu;
                }
                if (settlementType) {
                    additionalInfo += (additionalInfo ? ' • ' : '') + settlementType;
                }

                // ✅ ИСПРАВЛЕНА структура данных города для JSON.stringify
                const cityData = {
                    ref: city.ref || city.Ref || '',
                    description: cityDescription,
                    descriptionRu: cityDescriptionRu,
                    settlementTypeDescription: settlementType
                };

                return `
                <div class="city-dropdown-item" data-city='${JSON.stringify(cityData)}'>
                    <div class="city-name">${displayName}</div>
                    ${additionalInfo ? `<div class="city-info">${additionalInfo}</div>` : ''}
                </div>
            `;
            }).join('');

            // Добавляем обработчики клика
            cityDropdown.querySelectorAll('.city-dropdown-item').forEach(item => {
                if (item.dataset.city) {
                    item.addEventListener('click', () => {
                        try {
                            const cityData = JSON.parse(item.dataset.city);
                            selectCity(cityData);
                        } catch (e) {
                            console.error('Error parsing city data:', e);
                        }
                    });
                }
            });
        }

        showCityDropdown();
    }

    // 1. Исправьте функцию highlightMatch (строка 291):
    function highlightMatch(text, query) {
        // ✅ ДОБАВЛЕНА проверка на существование text
        if (!text || !query || query.length < 2) return text || '';

        const regex = new RegExp(`(${escapeRegex(query)})`, 'gi');
        return text.replace(regex, '<strong>$1</strong>');
    }

    // ✅ Экранирование для regex
    function escapeRegex(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // ✅ Обновление подсветки элементов
    function updateHighlight(items) {
        items.forEach((item, index) => {
            item.classList.toggle('highlighted', index === highlightedIndex);
        });

        // Прокручиваем к выделенному элементу
        if (highlightedIndex >= 0 && items[highlightedIndex]) {
            items[highlightedIndex].scrollIntoView({ block: 'nearest' });
        }
    }

    // 4. Исправьте функцию selectCity:
    // ✅ ИСПРАВЛЕННАЯ функция selectCity
    function selectCity(city) {
        console.log('Selected city:', city);

        selectedCity = city;

        // ✅ ИСПРАВЛЕНО: используем правильное поле description
        const cityDescription = city.description || city.Description || '';
        const cityRefValue = city.ref || city.Ref || '';

        cityInput.value = cityDescription;

        // ✅ КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: правильная работа с полями
        if (cityRef) cityRef.value = cityRefValue;  // cityRef - это элемент, а не значение!
        if (cityName) cityName.value = cityDescription;

        hideCityDropdown();
        showSelectedCityInfo(city);

        // Очищаем и загружаем отделения
        resetDepartmentSelect();

        // ✅ ВАЖНО: передаем именно значение cityRef, а не объект city
        loadDepartments(cityRefValue);
    }

    // ✅ Показать информацию о выбранном городе
    function showSelectedCityInfo(city) {
        // Удаляем предыдущую информацию
        const existingInfo = document.querySelector('.city-selected-info');
        if (existingInfo) {
            existingInfo.remove();
        }

        const container = document.querySelector('.city-search-container');
        if (!container) return;

        const info = document.createElement('div');
        info.className = 'city-selected-info';

        let infoText = `✓ ${city.description}`;
        if (city.descriptionRu) infoText += ` (${city.descriptionRu})`;
        if (city.settlementTypeDescription) infoText += ` • ${city.settlementTypeDescription}`;

        info.innerHTML = `<i class="fas fa-check-circle"></i> ${infoText}`;
        container.appendChild(info);
    }

    // ✅ Очистка выбранного города
    function clearSelectedCity() {
        selectedCity = null;

        if (cityRef) cityRef.value = '';
        if (cityName) cityName.value = '';

        const existingInfo = document.querySelector('.city-selected-info');
        if (existingInfo) {
            existingInfo.remove();
        }

        resetDepartmentSelect();
    }

    // ✅ Сброс поля отделений
    function resetDepartmentSelect() {
        if (departmentSelect) {
            departmentSelect.innerHTML = '<option value="">Спочатку оберіть місто</option>';
            departmentSelect.disabled = true;
        }
        if (departmentName) {
            departmentName.value = '';
        }
    }

    // ✅ ПОЛНАЯ функция loadDepartments с подробной отладкой структуры данных
    function loadDepartments(cityRefValue) {
        console.log('=== loadDepartments DEBUG ===');
        console.log('Input cityRefValue:', cityRefValue, typeof cityRefValue);

        if (!cityRefValue || !departmentSelect) {
            console.error('❌ Missing required parameters:');
            console.log('- cityRefValue:', cityRefValue);
            console.log('- departmentSelect:', departmentSelect);
            return;
        }

        // Проверяем что cityRefValue это действительно строка с UUID
        if (typeof cityRefValue !== 'string' || cityRefValue.length < 10) {
            console.error('❌ Invalid cityRefValue format:', cityRefValue);
            return;
        }

        console.log('✅ Loading departments for city:', cityRefValue);

        departmentSelect.innerHTML = '<option value="">Завантаження відділень...</option>';
        departmentSelect.disabled = true;

        const url = `/api/novaposhta/departments/${encodeURIComponent(cityRefValue)}`;
        console.log('🌐 Fetching URL:', url);

        fetch(url)
            .then(response => {
                console.log('📡 Response status:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }
                return response.json();
            })
            .then(result => {
                console.log('📦 Full API response:', result);

                let departments = [];
                if (result.success && Array.isArray(result.data)) {
                    departments = result.data;
                    console.log('✅ Found departments:', departments.length);

                    // ✅ РАСШИРЕННАЯ ОТЛАДКА для изучения структуры данных
                    if (departments.length > 0) {
                        console.log('🔍 DEBUGGING DEPARTMENT STRUCTURE:');
                        console.log('First department full object:', departments[0]);
                        console.log('All keys in first department:', Object.keys(departments[0]));

                        // Попробуем найти поля с номером и описанием
                        const dept = departments[0];

                        console.log('Possible number fields:');
                        Object.keys(dept).forEach(key => {
                            if (key.toLowerCase().includes('number') || key.toLowerCase().includes('num')) {
                                console.log(`  ${key}:`, dept[key]);
                            }
                        });

                        console.log('Possible description fields:');
                        Object.keys(dept).forEach(key => {
                            if (key.toLowerCase().includes('description') || key.toLowerCase().includes('name') || key.toLowerCase().includes('address')) {
                                console.log(`  ${key}:`, dept[key]);
                            }
                        });

                        // ✅ ДОПОЛНИТЕЛЬНАЯ ОТЛАДКА: показываем все поля и их значения
                        console.log('🔍 ALL FIELDS AND VALUES:');
                        Object.entries(dept).forEach(([key, value]) => {
                            console.log(`  ${key}: (${typeof value}) "${value}"`);
                        });

                        // ✅ ПОИСК ПОЛЕЙ ПО СОДЕРЖИМОМУ
                        console.log('🔍 SEARCHING BY CONTENT:');
                        Object.entries(dept).forEach(([key, value]) => {
                            // Ищем поля которые могут быть номером (числа или строки с числами)
                            if (typeof value === 'number' || (typeof value === 'string' && /^\d+$/.test(value))) {
                                console.log(`  📊 POTENTIAL NUMBER FIELD: ${key} = "${value}"`);
                            }
                            // Ищем поля которые могут быть описанием (длинные строки)
                            if (typeof value === 'string' && value.length > 5 && value.length < 200) {
                                console.log(`  📝 POTENTIAL DESCRIPTION FIELD: ${key} = "${value}"`);
                            }
                        });
                    }

                    // Показываем первые несколько отделений для отладки (старый формат)
                    if (departments.length > 0) {
                        console.log('📋 Sample departments (old format):');
                        departments.slice(0, 3).forEach((dept, index) => {
                            console.log(`  ${index + 1}. ${dept.number} - ${dept.description}`);
                        });
                    }
                } else {
                    console.warn('⚠️ No departments in response:', result);
                }

                displayDepartments(departments);
            })
            .catch(error => {
                console.error('💥 Error loading departments:', error);
                departmentSelect.innerHTML = '<option value="">Помилка завантаження відділень</option>';
            });
    }

    // ✅ УЛУЧШЕННАЯ функция displayDepartments с поддержкой разных форматов
    function displayDepartments(departments) {
        if (!departmentSelect) return;

        departmentSelect.innerHTML = '';

        if (!Array.isArray(departments) || departments.length === 0) {
            const option = document.createElement('option');
            option.value = '';
            option.textContent = 'Відділення не знайдено';
            departmentSelect.appendChild(option);
            return;
        }

        // Добавляем пустую опцию
        const emptyOption = document.createElement('option');
        emptyOption.value = '';
        emptyOption.textContent = 'Оберіть відділення';
        departmentSelect.appendChild(emptyOption);

        // ✅ ФУНКЦИЯ для извлечения значения поля с разными вариантами названий
        function getFieldValue(obj, ...possibleKeys) {
            for (const key of possibleKeys) {
                if (obj[key] !== undefined && obj[key] !== null) {
                    return obj[key];
                }
            }
            return '';
        }

        // Обрабатываем и добавляем отделения
        departments.forEach((dept, index) => {
            console.log(`Processing department ${index + 1}:`, dept);

            // ✅ Пробуем разные варианты названий полей
            const number = getFieldValue(dept,
                'number', 'Number', 'num', 'Num',
                'departmentNumber', 'DepartmentNumber',
                'branchNumber', 'BranchNumber'
            );

            const description = getFieldValue(dept,
                'description', 'Description',
                'name', 'Name',
                'address', 'Address',
                'departmentDescription', 'DepartmentDescription',
                'title', 'Title'
            );

            const ref = getFieldValue(dept,
                'ref', 'Ref', 'id', 'Id', 'uuid', 'UUID'
            );

            console.log(`  Extracted: number="${number}", description="${description}", ref="${ref}"`);

            // Создаем опцию
            const option = document.createElement('option');
            option.value = ref || `dept_${index}`;

            // Формируем текст опции
            let optionText = '';
            if (number) {
                optionText += `№${number}`;
            }
            if (description) {
                optionText += (optionText ? ' - ' : '') + description;
            }
            if (!optionText) {
                optionText = `Відділення ${index + 1}`;
            }

            option.textContent = optionText;
            departmentSelect.appendChild(option);
        });

        // Сортировка (если удалось извлечь номера)
        const options = Array.from(departmentSelect.options).slice(1); // исключаем первую пустую опцию
        options.sort((a, b) => {
            const aNum = parseInt(a.textContent.match(/№(\d+)/)?.[1]) || 999999;
            const bNum = parseInt(b.textContent.match(/№(\d+)/)?.[1]) || 999999;
            return aNum - bNum;
        });

        // Очищаем и добавляем отсортированные опции
        departmentSelect.innerHTML = '';
        departmentSelect.appendChild(emptyOption);
        options.forEach(option => departmentSelect.appendChild(option));

        // Обработчик изменения отделения
        departmentSelect.onchange = function() {
            const selectedOption = this.options[this.selectedIndex];
            if (departmentName && selectedOption && selectedOption.value) {
                departmentName.value = selectedOption.textContent;
                console.log('Selected department:', selectedOption.textContent);
            } else if (departmentName) {
                departmentName.value = '';
            }
        };

        departmentSelect.disabled = false;
        console.log(`✅ Loaded ${departments.length} departments`);
    }

    // ✅ Функции управления UI
    function showCityDropdown() {
        if (cityDropdown) cityDropdown.style.display = 'block';
    }

    function hideCityDropdown() {
        if (cityDropdown) cityDropdown.style.display = 'none';
    }

    function showCityLoading() {
        if (cityLoading) cityLoading.style.display = 'block';
        hideCityDropdown();
    }

    function hideCityLoading() {
        if (cityLoading) cityLoading.style.display = 'none';
    }

    function showCityError(message) {
        if (cityDropdown) {
            cityDropdown.innerHTML = `
                <div class="city-dropdown-item error">
                    <i class="fas fa-exclamation-triangle"></i>
                    ${message}
                </div>
            `;
            showCityDropdown();
        }
    }

    // ✅ Валидация Nova Poshta полей
    function validate() {
        if (!cityRef || !cityRef.value) {
            alert('Оберіть місто для доставки Новою Поштою');
            if (cityInput) cityInput.focus();
            return false;
        }

        if (!departmentSelect || !departmentSelect.value) {
            alert('Оберіть відділення Нової Пошти');
            if (departmentSelect) departmentSelect.focus();
            return false;
        }

        console.log('✅ Nova Poshta validation passed');
        return true;
    }

    // Добавьте эти функции в ваш novaposhta.js в конце файла, перед window.NovaPoshta
// 5. Добавьте функцию toggleFields в конец файла (перед window.NovaPoshta):
    function toggleFields(show) {
        const novaPoshtaFields = document.getElementById('novaPoshtaFields');
        if (!novaPoshtaFields) {
            console.log('Nova Poshta fields container not found');
            return;
        }

        if (show) {
            console.log('Nova Poshta: Showing fields');
            novaPoshtaFields.style.display = 'block';

            // Если города еще не загружены - загружаем
            if (allCities.length === 0) {
                loadAllCities();
            }
        } else {
            console.log('Nova Poshta: Hiding fields');
            novaPoshtaFields.style.display = 'none';

            // Очищаем поля при скрытии
            clearSelectedCity();
            if (cityInput) cityInput.value = '';
        }
    }

    // ✅ НОВАЯ ФУНКЦИЯ: Инициализация для внешнего вызова
    function initializeFields() {
        if (!cityInput) {
            console.error('Nova Poshta: cityInput not found during initialization');
            return false;
        }

        // Запускаем настройку обработчиков если еще не настроены
        setupEventListeners();

        // Загружаем города если еще не загружены
        if (allCities.length === 0) {
            loadAllCities();
        }

        return true;
    }

    // ✅ ОБНОВЛЕННЫЙ публичный API
    window.NovaPoshta = {
        validate: validate,
        clearSelectedCity: clearSelectedCity,
        loadDepartments: loadDepartments,
        getSelectedCity: () => selectedCity,
        toggleFields: toggleFields,              // ✅ ВАЖНО: для показа/скрытия полей
        initializeFields: setupEventListeners,   // ✅ ВАЖНО: для инициализации
        isReady: () => allCities.length > 0      // ✅ ВАЖНО: для проверки готовности
    };

    console.log('Nova Poshta unified module initialized');
});
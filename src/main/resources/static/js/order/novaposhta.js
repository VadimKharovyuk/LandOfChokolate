window.NovaPoshta = {

    // ✅ НОВЫЕ СВОЙСТВА для поиска
    allCities: [],
    selectedCity: null,


    /**
     * ✅ УЛУЧШЕННАЯ загрузка всех городов с отладкой
     */
    loadAllCities: function() {
        console.log('NovaPoshta: Загружаем ВСЕ города для поиска...');

        if (this.allCities.length > 0) {
            console.log(`NovaPoshta: Города уже загружены из кэша (${this.allCities.length} городов)`);
            return Promise.resolve(this.allCities);
        }

        return fetch('/api/novaposhta/cities')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Помилка завантаження міст');
                }
                return response.json();
            })
            .then(result => {
                console.log('NovaPoshta: Ответ API:', {
                    success: result.success,
                    count: result.count,
                    hasStatistics: !!result.statistics
                });

                if (result.success && result.data && Array.isArray(result.data)) {
                    // Фильтруем только валидные города
                    this.allCities = result.data.filter(city => {
                        return city && city.Ref && city.Description &&
                            city.Ref.length > 0 && city.Description.length > 0;
                    });

                    console.log(`NovaPoshta: ✅ Загружено ${this.allCities.length} городов для поиска`);

                    // ✅ ПОКАЗЫВАЕМ СТАТИСТИКУ
                    if (result.statistics) {
                        console.log('Статистика по типам:', result.statistics.byType);
                    }

                    // ✅ ТЕСТ ПОИСКА для отладки
                    const testQuery = 'кегичівка';
                    const testResults = this.allCities.filter(city =>
                        (city.Description || '').toLowerCase().includes(testQuery) ||
                        (city.DescriptionRu || '').toLowerCase().includes(testQuery)
                    );
                    console.log(`Тест поиска "${testQuery}": найдено ${testResults.length} результатов`);
                    if (testResults.length > 0) {
                        console.log('Найденные города:', testResults.map(c => c.Description));
                    }

                    return this.allCities;
                } else {
                    throw new Error(result.message || 'Помилка отримання міст');
                }
            })
            .catch(error => {
                console.error('NovaPoshta: Ошибка загрузки городов:', error);
                window.OrderNotifications.error('Помилка завантаження міст для пошуку');
                return [];
            });
    },

    /**
     * ✅ УЛУЧШЕННЫЙ МЕТОД: Поиск городов по названию с отладкой
     */
    searchCities: function(query) {
        if (!query || query.length < 2) {
            return [];
        }

        const searchQuery = query.toLowerCase().trim();
        console.log(`NovaPoshta: Поиск по запросу "${searchQuery}" среди ${this.allCities.length} городов`);

        const results = this.allCities.filter(city => {
            const description = (city.Description || '').toLowerCase();
            const descriptionRu = (city.DescriptionRu || '').toLowerCase();

            // ✅ РАСШИРЕННЫЙ ПОИСК
            const matchesUa = description.includes(searchQuery);
            const matchesRu = descriptionRu.includes(searchQuery);

            // ✅ ПОИСК ПО ЧАСТЯМ (для составных названий)
            const queryParts = searchQuery.split(' ').filter(part => part.length > 1);
            const matchesParts = queryParts.length > 0 && queryParts.every(part =>
                description.includes(part) || descriptionRu.includes(part)
            );

            return matchesUa || matchesRu || matchesParts;
        });

        console.log(`NovaPoshta: Найдено ${results.length} результатов для "${searchQuery}"`);

        // ✅ ОТЛАДКА: показываем первые несколько результатов
        if (results.length > 0) {
            console.log('Первые результаты:', results.slice(0, 3).map(c => ({
                Description: c.Description,
                DescriptionRu: c.DescriptionRu,
                SettlementTypeDescription: c.SettlementTypeDescription
            })));
        }

        return results.slice(0, 10); // Ограничиваем до 10 результатов
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Инициализация поля поиска городов
     */
    initCitySearch: function() {
        const cityInput = document.getElementById('cityInput');
        const cityRefInput = document.getElementById('cityRef');
        const cityDropdown = document.getElementById('cityDropdown');
        const cityLoading = document.getElementById('cityLoading');

        if (!cityInput || !cityDropdown) {
            console.error('NovaPoshta: Элементы поиска городов не найдены');
            return;
        }

        let searchTimeout;
        let highlightedIndex = -1;

        // Обработчик ввода
        cityInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();

            clearTimeout(searchTimeout);
            highlightedIndex = -1;

            if (query.length < 2) {
                this.hideCityDropdown();
                this.clearSelectedCity();
                return;
            }

            // Показываем загрузку
            if (cityLoading) cityLoading.style.display = 'block';

            searchTimeout = setTimeout(() => {
                this.loadAllCities().then(() => {
                    const results = this.searchCities(query);
                    this.showCityResults(results, query);
                    if (cityLoading) cityLoading.style.display = 'none';
                });
            }, 300);
        });

        // Обработчик клавиш для навигации
        cityInput.addEventListener('keydown', (e) => {
            const items = cityDropdown.querySelectorAll('.city-dropdown-item');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                highlightedIndex = Math.min(highlightedIndex + 1, items.length - 1);
                this.updateHighlight(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                highlightedIndex = Math.max(highlightedIndex - 1, -1);
                this.updateHighlight(items);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (highlightedIndex >= 0 && items[highlightedIndex]) {
                    const cityData = JSON.parse(items[highlightedIndex].dataset.city);
                    this.selectCity(cityData);
                }
            } else if (e.key === 'Escape') {
                this.hideCityDropdown();
            }
        });

        // Скрываем dropdown при клике вне
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.city-search-container')) {
                this.hideCityDropdown();
            }
        });
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Показать результаты поиска
     */
    showCityResults: function(cities, query) {
        const cityDropdown = document.getElementById('cityDropdown');

        if (cities.length === 0) {
            cityDropdown.innerHTML = `
                <div class="city-no-results">
                    Не знайдено міст за запитом "${query}"
                </div>
            `;
        } else {
            cityDropdown.innerHTML = cities.map(city => {
                const deliveryInfo = this.getDeliveryInfo(city);

                return `
                    <div class="city-dropdown-item" data-city='${JSON.stringify(city)}'>
                        <div class="city-name">${this.highlightMatch(city.Description, query)}</div>
                        <div class="city-info">
                            ${city.DescriptionRu ? city.DescriptionRu + ' • ' : ''}
                            ${city.SettlementTypeDescription || 'Населений пункт'}
                            ${deliveryInfo}
                        </div>
                    </div>
                `;
            }).join('');

            // Добавляем обработчики клика
            cityDropdown.querySelectorAll('.city-dropdown-item').forEach(item => {
                item.addEventListener('click', () => {
                    const cityData = JSON.parse(item.dataset.city);
                    this.selectCity(cityData);
                });
            });
        }

        cityDropdown.style.display = 'block';
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Подсветка совпадений в тексте
     */
    highlightMatch: function(text, query) {
        const regex = new RegExp(`(${query})`, 'gi');
        return text.replace(regex, '<strong>$1</strong>');
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Информация о доставке
     */
    getDeliveryInfo: function(city) {
        let info = '';
        if (city.IsBranch === '1') {
            info += ' • Є відділення';
        }
        if (city.Delivery1 === '1') {
            info += ' • Доставка';
        }
        return info;
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Обновить подсветку элементов
     */
    updateHighlight: function(items) {
        items.forEach((item, index) => {
            item.classList.toggle('highlighted', index === highlightedIndex);
        });
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Выбрать город
     */
    selectCity: function(city) {
        const cityInput = document.getElementById('cityInput');
        const cityRefInput = document.getElementById('cityRef');

        this.selectedCity = city;
        if (cityInput) cityInput.value = city.Description;
        if (cityRefInput) cityRefInput.value = city.Ref;

        this.hideCityDropdown();

        // Показываем информацию о выбранном городе
        this.showSelectedCityInfo(city);

        // Загружаем отделения
        this.loadDepartments(city.Ref);

        console.log('NovaPoshta: Выбран город:', city.Description);
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Показать информацию о выбранном городе
     */
    showSelectedCityInfo: function(city) {
        let existingInfo = document.querySelector('.city-selected-info');
        if (existingInfo) {
            existingInfo.remove();
        }

        const container = document.querySelector('.city-search-container');
        if (!container) return;

        const info = document.createElement('div');
        info.className = 'city-selected-info';

        let infoText = `✓ ${city.Description}`;
        if (city.DescriptionRu) infoText += ` (${city.DescriptionRu})`;
        if (city.SettlementTypeDescription) infoText += ` • ${city.SettlementTypeDescription}`;

        info.innerHTML = infoText;
        container.appendChild(info);
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Очистить выбранный город
     */
    clearSelectedCity: function() {
        this.selectedCity = null;
        const cityRefInput = document.getElementById('cityRef');
        if (cityRefInput) cityRefInput.value = '';

        const existingInfo = document.querySelector('.city-selected-info');
        if (existingInfo) {
            existingInfo.remove();
        }

        // Очищаем отделения
        const departmentSelect = document.getElementById('departmentSelect');
        if (departmentSelect) {
            departmentSelect.innerHTML = '<option value="">Спочатку оберіть місто</option>';
            departmentSelect.disabled = true;
        }
    },

    /**
     * ✅ НОВЫЙ МЕТОД: Скрыть dropdown
     */
    hideCityDropdown: function() {
        const cityDropdown = document.getElementById('cityDropdown');
        if (cityDropdown) {
            cityDropdown.style.display = 'none';
        }
    },

    /**
     * СТАРЫЙ МЕТОД: Загрузить города (для совместимости)
     */
    loadCities: function() {
        // Если используем поиск, просто инициализируем его
        if (document.getElementById('cityInput')) {
            this.initCitySearch();
            return;
        }

        // Иначе работаем со старым select
        console.log('NovaPoshta: Загружаем города...');
        const citySelect = document.getElementById('citySelect');

        if (!citySelect) {
            console.error('NovaPoshta: citySelect не найден');
            return;
        }

        // Показываем индикатор загрузки
        citySelect.innerHTML = '<option value="">Завантаження міст...</option>';
        citySelect.disabled = true;

        this.loadAllCities().then(cities => {
            this.processCities(cities, citySelect);
        }).catch(error => {
            console.error('NovaPoshta: Помилка завантаження міст:', error);
            citySelect.innerHTML = '<option value="">Помилка завантаження міст</option>';
            window.OrderNotifications.error('Помилка завантаження міст. Спробуйте пізніше.');
        });
    },

    /**
     * ОБНОВЛЕННЫЙ МЕТОД: Обработать список городов (для старого select)
     */
    processCities: function(cities, citySelect) {
        console.log('NovaPoshta: Получено населенных пунктов:', cities.length);

        // ✅ ОСНОВНАЯ ФИЛЬТРАЦИЯ: только города и крупные населенные пункты
        const actualCities = cities.filter(city => {
            // 1. Города с отделениями Nova Poshta
            if (city.IsBranch === '1') {
                return true;
            }

            // 2. Проверяем тип населенного пункта
            const settlementType = (city.SettlementTypeDescription || '').toLowerCase();
            const description = (city.Description || '').toLowerCase();

            // Включаем города
            if (settlementType.includes('місто') || settlementType.includes('город')) {
                return true;
            }

            // 3. Крупные известные населенные пункты
            const majorPlaces = [
                'київ', 'харків', 'одеса', 'дніпро', 'запоріжжя', 'львів',
                'кривий ріг', 'миколаїв', 'маріуполь', 'вінниця', 'херсон',
                'полтава', 'чернігів', 'черкаси', 'житомир', 'суми', 'хмельницький',
                'рівне', 'івано-франківськ', 'тернопіль', 'луцьк', 'ужгород',
                'чернівці', 'кропивницький', 'кременчук', 'білая церква', 'нікополь'
            ];

            const isMajorPlace = majorPlaces.some(major => description.includes(major));
            if (isMajorPlace) {
                return true;
            }

            return false;
        });

        console.log(`NovaPoshta: Отфильтровано ${actualCities.length} городов и крупных населенных пунктов`);

        citySelect.innerHTML = '<option value="">Оберіть місто...</option>';

        // ✅ СОРТИРОВКА с приоритетом
        actualCities.sort((a, b) => {
            try {
                const aDesc = a.Description.toString().trim();
                const bDesc = b.Description.toString().trim();

                // Приоритет 1: Города с отделениями
                const aHasBranch = a.IsBranch === '1';
                const bHasBranch = b.IsBranch === '1';

                if (aHasBranch && !bHasBranch) return -1;
                if (!aHasBranch && bHasBranch) return 1;

                // Приоритет 2: Крупные города
                const majorCities = ['київ', 'харків', 'одеса', 'дніпро', 'запоріжжя', 'львів'];
                const aIsMajor = majorCities.some(major => aDesc.toLowerCase().includes(major));
                const bIsMajor = majorCities.some(major => bDesc.toLowerCase().includes(major));

                if (aIsMajor && !bIsMajor) return -1;
                if (!aIsMajor && bIsMajor) return 1;

                // В остальных случаях - по алфавиту
                return aDesc.localeCompare(bDesc, 'uk', {
                    sensitivity: 'base',
                    numeric: true
                });
            } catch (error) {
                console.error('NovaPoshta: Ошибка сортировки:', error);
                return 0;
            }
        });

        // ✅ Добавляем в select
        let addedCount = 0;
        actualCities.forEach((city, index) => {
            try {
                const option = document.createElement('option');
                option.value = city.Ref;
                option.textContent = city.Description;

                // ✅ Дополнительная информация в title
                let titleInfo = city.Description;
                if (city.DescriptionRu) {
                    titleInfo += ` / ${city.DescriptionRu}`;
                }
                if (city.SettlementTypeDescription) {
                    titleInfo += ` (${city.SettlementTypeDescription})`;
                }
                if (city.IsBranch === '1') {
                    titleInfo += ' [Є відділення]';
                }
                option.title = titleInfo;

                citySelect.appendChild(option);
                addedCount++;
            } catch (error) {
                console.error(`NovaPoshta: Ошибка добавления города ${index}:`, error, city);
            }
        });

        citySelect.disabled = false;
        console.log(`NovaPoshta: ✅ Завантажено ${addedCount} міст у випадаючий список`);
    },

    /**
     * ✅ ОБНОВЛЕННАЯ детальная статистика
     */
    logDetailedStatistics: function(allItems, cities, finalList) {
        console.log('=== СТАТИСТИКА НАСЕЛЕННЫХ ПУНКТОВ ===');

        // Общая статистика
        console.log(`Всего получено: ${allItems.length}`);
        console.log(`Отфильтровано городов: ${cities.length}`);
        console.log(`Итого в списке: ${finalList.length}`);

        // По типам
        const byType = {};
        allItems.forEach(item => {
            const type = item.SettlementTypeDescription || 'Невідомо';
            byType[type] = (byType[type] || 0) + 1;
        });
        console.log('По типам населенных пунктов:', byType);

        // С отделениями
        const withBranches = allItems.filter(c => c.IsBranch === '1').length;
        console.log(`С отделениями Nova Poshta: ${withBranches}`);

        // С доставкой
        const withDelivery = allItems.filter(c => c.Delivery1 === '1' || c.Delivery3 === '1').length;
        console.log(`С активной доставкой: ${withDelivery}`);

        console.log('=======================================');
    },

    /**
     * Загрузить отделения по городу
     */
    loadDepartments: function(cityRef) {
        console.log('NovaPoshta: Загружаем отделения для города:', cityRef);

        const departmentSelect = document.getElementById('departmentSelect');
        if (!departmentSelect) {
            console.error('NovaPoshta: departmentSelect не найден');
            return;
        }

        if (!cityRef) {
            departmentSelect.innerHTML = '<option value="">Спочатку оберіть місто</option>';
            departmentSelect.disabled = true;
            return;
        }

        // Показываем индикатор загрузки
        departmentSelect.innerHTML = '<option value="">Завантаження відділень...</option>';
        departmentSelect.disabled = true;

        fetch(`/api/novaposhta/departments/${cityRef}`)
            .then(response => {
                console.log('NovaPoshta: Ответ сервера для отделений:', response.status);
                if (!response.ok) {
                    throw new Error('Помилка завантаження відділень');
                }
                return response.json();
            })
            .then(result => {
                console.log('NovaPoshta: Полный ответ API для отделений:', result);

                if (result.success && result.data && Array.isArray(result.data)) {
                    this.processDepartments(result.data, departmentSelect);
                } else {
                    console.error('NovaPoshta: API вернул ошибку для отделений:', result);
                    departmentSelect.innerHTML = '<option value="">Помилка завантаження відділень</option>';

                    const errorMessage = result.message || 'Невідома помилка при завантаженні відділень';
                    window. OrderNotifications.error(errorMessage);
                }
            })
            .catch(error => {
                console.error('NovaPoshta: Помилка завантаження відділень:', error);
                departmentSelect.innerHTML = '<option value="">Помилка завантаження відділень</option>';
                window.OrderNotifications.error('Помилка завантаження відділень. Спробуйте пізніше.');
            });
    },

    /**
     * ✅ ИСПРАВЛЕННЫЙ МЕТОД: Обработать список отделений с правильными полями
     */
    processDepartments: function(departments, departmentSelect) {
        console.log('NovaPoshta: Получено отделений:', departments.length);

        departmentSelect.innerHTML = '<option value="">Оберіть відділення...</option>';

        // ✅ Фильтруем только активные отделения с правильными полями
        const activeDepartments = departments.filter(dept => {
            // ✅ ВАЖНО: используем поля с заглавными буквами
            if (!dept.Ref || !dept.Description) {
                return false;
            }

            // Фильтруем неактивные отделения
            if (dept.WarehouseStatus && dept.WarehouseStatus !== 'Working') {
                return false;
            }

            // Исключаем отделения с запретом выбора
            if (dept.DenyToSelect === '1') {
                return false;
            }

            return true;
        });

        console.log(`NovaPoshta: Отфильтровано ${activeDepartments.length} активных отделений из ${departments.length}`);

        // Сортируем отделения по номеру
        activeDepartments.sort((a, b) => {
            try {
                const getNumber = (dept) => {
                    // ✅ ВАЖНО: используем поле с заглавной буквой
                    if (dept.Number && !isNaN(parseInt(dept.Number))) {
                        return parseInt(dept.Number);
                    }

                    const desc = (dept.Description || '').toString();
                    const match = desc.match(/№?(\d+)/);
                    return match ? parseInt(match[1]) : 999999;
                };

                const aNum = getNumber(a);
                const bNum = getNumber(b);

                if (aNum !== bNum) {
                    return aNum - bNum;
                }

                // Если номера одинаковые, сортируем по описанию
                const aDesc = (a.Description || '').toString().trim();
                const bDesc = (b.Description || '').toString().trim();

                return aDesc.localeCompare(bDesc, 'uk', {
                    sensitivity: 'base',
                    numeric: true
                });

            } catch (error) {
                console.error('NovaPoshta: Ошибка сортировки отделений:', error);
                return 0;
            }
        });

        // ✅ ИСПРАВЛЕННОЕ добавление отделений с правильными полями
        let addedCount = 0;
        activeDepartments.forEach((dept, index) => {
            try {
                const option = document.createElement('option');
                option.value = dept.Ref || '';

                // ✅ Улучшенное отображение с правильными полями
                let displayName = '';
                if (dept.Number && dept.ShortAddress) {
                    displayName = `№${dept.Number}: ${dept.ShortAddress}`;
                } else if (dept.Number && dept.Description) {
                    displayName = `№${dept.Number}: ${dept.Description}`;
                } else if (dept.Description) {
                    displayName = dept.Description;
                } else {
                    displayName = `Відділення ${index + 1}`;
                }

                option.textContent = displayName;

                // ✅ Дополнительная информация в title с правильными полями
                let titleInfo = displayName;
                if (dept.Phone) titleInfo += ` | Тел: ${dept.Phone}`;
                if (dept.Schedule && dept.Schedule.Monday) titleInfo += ` | Пн: ${dept.Schedule.Monday}`;
                option.title = titleInfo;

                departmentSelect.appendChild(option);
                addedCount++;

            } catch (error) {
                console.error(`NovaPoshta: Ошибка добавления отделения ${index}:`, error, dept);
            }
        });

        departmentSelect.disabled = false;
        console.log(`NovaPoshta: ✅ Завантажено ${addedCount} активних відділень`);
    },

    /**
     * ✅ ОБНОВЛЕННЫЙ МЕТОД: Показать или скрыть поля Nova Poshta
     */
    toggleFields: function(show) {
        const novaPoshtaFields = document.getElementById('novaPoshtaFields');
        if (!novaPoshtaFields) return;

        if (show) {
            console.log('NovaPoshta: Показываем поля поиска');
            novaPoshtaFields.style.display = 'block';

            // Проверяем какой тип поля используется
            if (document.getElementById('cityInput')) {
                this.initCitySearch(); // Инициализируем поиск
            } else {
                this.loadCities(); // Загружаем в select
            }
        } else {
            console.log('NovaPoshta: Скрываем поля');
            novaPoshtaFields.style.display = 'none';

            // Очищаем поля в зависимости от типа
            if (document.getElementById('cityInput')) {
                this.clearSelectedCity();
            } else {
                // Очищаем старый select
                const citySelect = document.getElementById('citySelect');
                const departmentSelect = document.getElementById('departmentSelect');

                if (citySelect) {
                    citySelect.innerHTML = '<option value="">Оберіть місто...</option>';
                }
                if (departmentSelect) {
                    departmentSelect.innerHTML = '<option value="">Спочатку оберіть місто</option>';
                }
            }
        }
    },

    /**
     * ✅ ОБНОВЛЕННАЯ МЕТОД: Валидация Nova Poshta полей
     */
    validate: function() {
        // Проверяем какой тип поля используется
        const cityInput = document.getElementById('cityInput');
        const citySelect = document.getElementById('citySelect');

        let cityRef, departmentRef;

        if (cityInput) {
            // Новый поиск
            cityRef = document.getElementById('cityRef')?.value;
            departmentRef = document.getElementById('departmentSelect')?.value;

            if (!cityRef) {
                window.OrderNotifications.error('Будь ласка, оберіть місто для доставки Новою Поштою');
                cityInput.focus();
                return false;
            }
        } else if (citySelect) {
            // Старый select
            cityRef = citySelect.value;
            departmentRef = document.getElementById('departmentSelect')?.value;

            if (!cityRef) {
                window.OrderNotifications.error('Будь ласка, оберіть місто для доставки Новою Поштою');
                citySelect.scrollIntoView({ behavior: 'smooth' });
                return false;
            }
        }

        console.log('NovaPoshta: Валидация:', { cityRef, departmentRef });

        if (!departmentRef) {
            window.OrderNotifications.error('Будь ласка, оберіть відділення Нової Пошти');
            document.getElementById('departmentSelect')?.scrollIntoView({ behavior: 'smooth' });
            return false;
        }

        console.log('NovaPoshta: ✅ Валидация пройдена успешно');
        return true;
    }
};
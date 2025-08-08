/**
 * Модуль галереи изображений с переключением по клику
 * @version 1.0.0
 */
class ClickableGallery {
    constructor(images, options = {}) {
        this.productImages = images || [];
        this.currentIndex = 0;

        // Настройки по умолчанию
        this.options = {
            fadeDelay: 150,
            fadeOpacity: 0.7,
            enableConsoleLog: true,
            ...options
        };

        // DOM элементы
        this.productImageEl = document.getElementById('productImage');
        this.currentIndexEl = document.getElementById('currentImageIndex');

        this.init();
    }

    init() {
        // Проверяем, есть ли несколько изображений
        if (!this.productImages || this.productImages.length <= 1) {
            if (this.options.enableConsoleLog) {
                console.log('ClickableGallery: Недостаточно изображений для галереи');
            }
            return;
        }

        if (!this.productImageEl) {
            console.error('ClickableGallery: Элемент productImage не найден');
            return;
        }

        this.bindEvents();

        if (this.options.enableConsoleLog) {
            console.log(`ClickableGallery: Инициализирована галерея для ${this.productImages.length} изображений`);
        }
    }

    bindEvents() {
        // Переключение по клику на изображение
        this.productImageEl.addEventListener('click', (e) => {
            e.preventDefault();
            this.nextImage();
        });

        // Добавляем курсор pointer для показа кликабельности
        this.productImageEl.style.cursor = 'pointer';

        // Очистка при выгрузке страницы
        window.addEventListener('beforeunload', () => {
            this.destroy();
        });
    }

    changeImage(index) {
        if (index < 0) index = this.productImages.length - 1;
        if (index >= this.productImages.length) index = 0;

        this.currentIndex = index;
        const image = this.productImages[this.currentIndex];

        // Плавная смена изображения с сохранением всех CSS свойств
        const currentOpacity = this.productImageEl.style.opacity || '1';

        this.productImageEl.style.opacity = this.options.fadeOpacity;
        this.productImageEl.style.transition = `opacity ${this.options.fadeDelay * 2}ms ease`;

        setTimeout(() => {
            this.productImageEl.src = image.imageUrl;
            this.productImageEl.alt = image.altText || 'Зображення товару';
            this.productImageEl.style.opacity = '1';
        }, this.options.fadeDelay);

        // Обновляем счетчик
        if (this.currentIndexEl) {
            this.currentIndexEl.textContent = this.currentIndex + 1;
        }

        if (this.options.enableConsoleLog) {
            console.log(`ClickableGallery: Переключено на изображение ${this.currentIndex + 1}/${this.productImages.length}`);
        }
    }

    nextImage() {
        this.changeImage(this.currentIndex + 1);
    }

    prevImage() {
        this.changeImage(this.currentIndex - 1);
    }

    goToImage(index) {
        if (index >= 0 && index < this.productImages.length) {
            this.changeImage(index);
        }
    }

    getCurrentIndex() {
        return this.currentIndex;
    }

    getTotalImages() {
        return this.productImages.length;
    }

    destroy() {
        // Удаляем обработчики событий
        if (this.productImageEl) {
            this.productImageEl.removeEventListener('click', this.nextImage);
            this.productImageEl.style.cursor = '';
        }

        if (this.options.enableConsoleLog) {
            console.log('ClickableGallery: Галерея уничтожена');
        }
    }
}

// Глобальная функция для инициализации галереи
window.initClickableGallery = function(images, options) {
    if (typeof images === 'undefined') {
        console.warn('ClickableGallery: Изображения не переданы');
        return null;
    }

    return new ClickableGallery(images, options);
};

// Автоматическая инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    // Проверяем, есть ли данные для инициализации на странице
    if (typeof window.productImagesData !== 'undefined') {
        window.clickableGallery = new ClickableGallery(window.productImagesData);
    }
});
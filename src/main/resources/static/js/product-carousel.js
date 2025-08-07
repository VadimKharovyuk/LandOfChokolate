/**
 * Модуль автоматической карусели изображений продукта
 * @version 1.0.0
 */
class ProductImageCarousel {
    constructor(images, options = {}) {
        this.productImages = images || [];
        this.currentIndex = 0;
        this.autoplayInterval = null;
        this.isAutoplay = true;

        // Настройки по умолчанию
        this.options = {
            autoplayDelay: 3000,
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
                console.log('ProductCarousel: Недостаточно изображений для карусели');
            }
            return;
        }

        if (!this.productImageEl) {
            console.error('ProductCarousel: Элемент productImage не найден');
            return;
        }

        this.bindEvents();
        this.startAutoplay();

        if (this.options.enableConsoleLog) {
            console.log(`ProductCarousel: Запущена карусель для ${this.productImages.length} изображений`);
        }
    }

    bindEvents() {
        // Пауза при наведении мыши
        this.productImageEl.addEventListener('mouseenter', () => {
            if (this.isAutoplay) this.stopAutoplay();
        });

        this.productImageEl.addEventListener('mouseleave', () => {
            if (!this.isAutoplay) this.startAutoplay();
        });

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

        // Плавная смена изображения
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
    }

    nextImage() {
        this.changeImage(this.currentIndex + 1);
    }

    prevImage() {
        this.changeImage(this.currentIndex - 1);
    }

    startAutoplay() {
        if (this.autoplayInterval) {
            clearInterval(this.autoplayInterval);
        }

        this.autoplayInterval = setInterval(() => {
            this.nextImage();
        }, this.options.autoplayDelay);

        this.isAutoplay = true;
    }

    stopAutoplay() {
        if (this.autoplayInterval) {
            clearInterval(this.autoplayInterval);
            this.autoplayInterval = null;
        }

        this.isAutoplay = false;
    }

    destroy() {
        this.stopAutoplay();

        // Удаляем обработчики событий
        if (this.productImageEl) {
            this.productImageEl.removeEventListener('mouseenter', this.stopAutoplay);
            this.productImageEl.removeEventListener('mouseleave', this.startAutoplay);
        }

        if (this.options.enableConsoleLog) {
            console.log('ProductCarousel: Карусель уничтожена');
        }
    }

    // Публичные методы для внешнего управления
    pause() {
        this.stopAutoplay();
    }

    resume() {
        this.startAutoplay();
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
}

// Глобальная функция для инициализации карусели
window.initProductCarousel = function(images, options) {
    if (typeof images === 'undefined') {
        console.warn('ProductCarousel: Изображения не переданы');
        return null;
    }

    return new ProductImageCarousel(images, options);
};

// Автоматическая инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    // Проверяем, есть ли данные для инициализации на странице
    if (typeof window.productImagesData !== 'undefined') {
        window.productCarousel = new ProductImageCarousel(window.productImagesData);
    }
});
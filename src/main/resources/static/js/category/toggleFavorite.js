
    // Главный класс для управления избранным
    class WishlistManager {
    constructor() {
    this.baseUrl = '/wishlist';
    this.init();
}

    async init() {
    this.bindEvents();
    await this.loadInitialState();
}

    bindEvents() {
    // Обработчик для всех кнопок избранного
    document.addEventListener('click', (e) => {
    if (e.target.closest('.btn-favorite')) {
    e.preventDefault();
    const button = e.target.closest('.btn-favorite');
    const productId = button.dataset.productId;
    this.toggleWishlist(productId, button);
}
});
}

    async loadInitialState() {
    try {
    // Загружаем количество товаров в избранном
    await this.updateWishlistCounter();

    // Проверяем состояние всех кнопок на странице
    const buttons = document.querySelectorAll('.btn-favorite[data-product-id]');
    const productIds = Array.from(buttons).map(btn => parseInt(btn.dataset.productId));

    if (productIds.length > 0) {
    await this.checkMultipleProducts(productIds);
}
} catch (error) {
    console.error('Ошибка при загрузке начального состояния:', error);
}
}

    async toggleWishlist(productId, button) {
    if (!productId || button.classList.contains('loading')) {
    return;
}

    this.setButtonLoading(button, true);

    try {
    const response = await fetch(`${this.baseUrl}/toggle`, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
},
    body: `productId=${productId}&addedFromPage=${window.location.pathname}`
});

    const data = await response.json();

    if (data.success) {
    // Обновляем состояние кнопки
    this.updateButtonState(button, data.isInWishlist);

    // Обновляем счетчик
    this.updateCounterDisplay(data.itemCount);

    // Показываем уведомление
    this.showNotification(data.message, 'success');

    // Пользовательское событие для интеграции с другими компонентами
    document.dispatchEvent(new CustomEvent('wishlistUpdated', {
    detail: {
    productId: parseInt(productId),
    isInWishlist: data.isInWishlist,
    itemCount: data.itemCount
}
}));
} else {
    this.showNotification(data.message || 'Произошла ошибка', 'error');
}
} catch (error) {
    console.error('Ошибка при переключении избранного:', error);
    this.showNotification('Произошла ошибка при добавлении в избранное', 'error');
} finally {
    this.setButtonLoading(button, false);
}
}

    async checkMultipleProducts(productIds) {
    try {
    const response = await fetch(`${this.baseUrl}/check-multiple`, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
},
    body: JSON.stringify(productIds)
});

    const data = await response.json();

    if (data.success) {
    // Обновляем состояние всех кнопок
    Object.entries(data.results).forEach(([productId, isInWishlist]) => {
    const button = document.querySelector(`.btn-favorite[data-product-id="${productId}"]`);
    if (button) {
    this.updateButtonState(button, isInWishlist);
}
});
}
} catch (error) {
    console.error('Ошибка при проверке товаров в избранном:', error);
}
}

    async updateWishlistCounter() {
    try {
    const response = await fetch(`${this.baseUrl}/count`);
    const data = await response.json();

    if (data.success) {
    this.updateCounterDisplay(data.itemCount);
}
} catch (error) {
    console.error('Ошибка при получении счетчика избранного:', error);
}
}

    updateButtonState(button, isInWishlist) {
    const icon = button.querySelector('.fa-heart');

    if (isInWishlist) {
    button.classList.add('active');
    icon.classList.remove('far');
    icon.classList.add('fas');
    button.title = button.title.replace('Добавить', 'Удалить из избранного');
} else {
    button.classList.remove('active');
    icon.classList.remove('fas');
    icon.classList.add('far');
    button.title = button.title.replace('Удалить из избранного', 'Добавить в избранное');
}
}

    setButtonLoading(button, loading) {
    if (loading) {
    button.classList.add('loading');
} else {
    button.classList.remove('loading');
}
}

    updateCounterDisplay(count) {
    // Обновляем все счетчики на странице
    const counters = document.querySelectorAll('.wishlist-counter .badge, #wishlist-counter');
    counters.forEach(counter => {
    counter.textContent = count;
    // Скрываем счетчик если 0
    if (count === 0) {
    counter.style.display = 'none';
} else {
    counter.style.display = 'flex';
}
});
}

    showNotification(message, type = 'success') {
    // Создаем элемент уведомления если его нет
    let notification = document.getElementById('notification');
    if (!notification) {
    notification = document.createElement('div');
    notification.id = 'notification';
    notification.className = 'notification';
    document.body.appendChild(notification);
}

    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.classList.add('show');

    // Автоматически скрываем через 3 секунды
    setTimeout(() => {
    notification.classList.remove('show');
}, 3000);
}
}

    // Инициализируем менеджер избранного при загрузке страницы
    document.addEventListener('DOMContentLoaded', () => {
    window.wishlistManager = new WishlistManager();
});

    // Глобальная функция для обратной совместимости
    function toggleFavorite(productId, button) {
    if (window.wishlistManager) {
    if (!productId && button) {
    productId = button.dataset.productId;
}
    if (!button && productId) {
    button = document.querySelector(`.btn-favorite[data-product-id="${productId}"]`);
}

    if (productId && button) {
    window.wishlistManager.toggleWishlist(productId, button);
}
}
}

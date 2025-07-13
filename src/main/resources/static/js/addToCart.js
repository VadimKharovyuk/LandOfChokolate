// Функция добавления в корзину
function addToCart(button) {
    const productCard = button.closest('.product-card');
    const productTitle = productCard.querySelector('.product-title').textContent;

    // Анимация кнопки
    const originalText = button.innerHTML;
    button.innerHTML = 'Добавлено!';
    button.style.background = '#f1a680';
    button.style.color = '#fff';

    // Обновляем счетчик корзины
    const cartBadge = document.querySelector('.cart-badge');
    let currentCount = parseInt(cartBadge.textContent);
    cartBadge.textContent = currentCount + 1;

    // Анимация значка корзины
    cartBadge.style.transform = 'scale(1.5)';
    setTimeout(() => {
        cartBadge.style.transform = 'scale(1)';
    }, 300);

    // Возвращаем кнопку в исходное состояние
    setTimeout(() => {
        button.innerHTML = originalText;
        button.style.background = '';
        button.style.color = '';
    }, 2000);

    console.log(`Товар "${productTitle}" добавлен в корзину`);
}
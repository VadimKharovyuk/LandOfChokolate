// Функция для добавлние из списка  избранного
function toggleFavorite(button) {
    const productCard = button.closest('.product-card');
    const productId = productCard.querySelector('.add-to-cart')?.getAttribute('data-product-id') ||
        productCard.querySelector('.notify-availability-btn')?.getAttribute('data-product-id');

    // Переключаем класс active для кнопки
    button.classList.toggle('active');

    // Меняем иконку
    const icon = button.querySelector('i');
    if (button.classList.contains('active')) {
        icon.className = 'fas fa-heart';
    } else {
        icon.className = 'far fa-heart';
    }

    // Отправляем запрос на сервер
    fetch('/wishlist/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || ''
        },
        body: `productId=${encodeURIComponent(productId)}`
    })
        .then(response => response.json())
        .catch(error => {
            console.error('Ошибка при обновлении избранного:', error);
        });
}
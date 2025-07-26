document.addEventListener("DOMContentLoaded", function () {
    // Создаём кнопку
    const btn = document.createElement("button");
    btn.id = "scrollTopBtn";
    btn.title = "Наверх";
    btn.innerText = "⬆";

    // Добавляем стили
    btn.style.position = "fixed";
    btn.style.bottom = "20px";
    btn.style.right = "20px";
    btn.style.zIndex = "99";
    btn.style.backgroundColor = "#555";
    btn.style.color = "white";
    btn.style.border = "none";
    btn.style.outline = "none";
    btn.style.padding = "10px 15px";
    btn.style.borderRadius = "8px";
    btn.style.cursor = "pointer";
    btn.style.fontSize = "18px";
    btn.style.opacity = "0.7";
    btn.style.transition = "opacity 0.3s ease";
    btn.style.display = "none";

    // Поведение при наведении
    btn.addEventListener("mouseenter", () => btn.style.opacity = "1");
    btn.addEventListener("mouseleave", () => btn.style.opacity = "0.7");

    // Добавляем в тело документа
    document.body.appendChild(btn);

    // Показ при прокрутке
    window.addEventListener("scroll", function () {
        if (document.documentElement.scrollTop > 100) {
            btn.style.display = "block";
        } else {
            btn.style.display = "none";
        }
    });

    // Прокрутка вверх
    btn.addEventListener("click", function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
});
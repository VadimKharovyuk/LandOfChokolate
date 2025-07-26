document.addEventListener("DOMContentLoaded", function () {
    // Создаём кнопку
    const btn = document.createElement("button");
    btn.id = "scrollTopBtn";
    btn.title = "Наверх";
    btn.innerText = "⬆";


    const style = document.createElement('style');
    style.textContent = `
        /* Стили для кнопки "Наверх" */
        #scrollTopBtn {
            position: fixed !important;
            right: 20px !important;
            z-index: 99999 !important; /* Выше мобильной навигации */
            
            /* Базовые стили */
            background-color: var(--accent-gold, #c9a96e) !important;
            color: white !important;
            border: none !important;
            outline: none !important;
            cursor: pointer !important;
            
            /* Размеры и форма */
            width: 50px !important;
            height: 50px !important;
            border-radius: 50% !important;
            
            /* Текст */
            font-size: 18px !important;
            font-weight: bold !important;
            
            /* Отображение */
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            
            /* Эффекты */
            box-shadow: 0 4px 15px rgba(201, 169, 110, 0.3) !important;
            opacity: 0.85 !important;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
            
            /* По умолчанию на десктопе */
            bottom: 20px !important;
        }

        /* Hover эффекты */
        #scrollTopBtn:hover {
            opacity: 1 !important;
            transform: translateY(-3px) scale(1.05) !important;
            box-shadow: 0 6px 20px rgba(201, 169, 110, 0.4) !important;
        }

        /* Активное состояние */
        #scrollTopBtn:active {
            transform: translateY(-1px) scale(0.98) !important;
        }

        /* Позиционирование на мобильных устройствах */
        @media screen and (max-width: 991px) {
            #scrollTopBtn {
                bottom: 100px !important; /* Выше мобильной навигации */
                right: 15px !important;
                width: 45px !important;
                height: 45px !important;
                font-size: 16px !important;
                
                /* Более мягкая тень на мобильных */
                box-shadow: 0 3px 12px rgba(201, 169, 110, 0.25) !important;
            }
        }

        /* Для очень маленьких экранов */
        @media screen and (max-width: 576px) {
            #scrollTopBtn {
                bottom: 95px !important;
                right: 10px !important;
                width: 40px !important;
                height: 40px !important;
                font-size: 14px !important;
            }
        }

        /* Для landscape ориентации на мобильных */
        @media screen and (max-width: 991px) and (orientation: landscape) and (max-height: 500px) {
            #scrollTopBtn {
                bottom: 80px !important;
                right: 10px !important;
                width: 35px !important;
                height: 35px !important;
                font-size: 12px !important;
            }
        }
    `;

    // Добавляем стили в head
    document.head.appendChild(style);

    // Изначально скрываем кнопку
    btn.style.display = "none";

    // Добавляем кнопку в body
    document.body.appendChild(btn);

    // Показ при прокрутке
    window.addEventListener("scroll", function () {
        if (document.documentElement.scrollTop > 100) {
            btn.style.display = "flex";
        } else {
            btn.style.display = "none";
        }
    });

    // Прокрутка вверх
    btn.addEventListener("click", function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
});
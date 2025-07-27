// document.addEventListener("DOMContentLoaded", function () {
//     // Создаём кнопку
//     const btn = document.createElement("button");
//     btn.id = "scrollTopBtn";
//     btn.title = "Наверх";
//     btn.innerText = "⬆";
//
//
//     const style = document.createElement('style');
//     style.textContent = `
//         /* Стили для кнопки "Наверх" */
//         #scrollTopBtn {
//             position: fixed !important;
//             right: 20px !important;
//             z-index: 99999 !important; /* Выше мобильной навигации */
//
//             /* Базовые стили */
//             background-color: var(--accent-gold, #c9a96e) !important;
//             color: white !important;
//             border: none !important;
//             outline: none !important;
//             cursor: pointer !important;
//
//             /* Размеры и форма */
//             width: 50px !important;
//             height: 50px !important;
//             border-radius: 50% !important;
//
//             /* Текст */
//             font-size: 18px !important;
//             font-weight: bold !important;
//
//             /* Отображение */
//             display: flex !important;
//             align-items: center !important;
//             justify-content: center !important;
//
//             /* Эффекты */
//             box-shadow: 0 4px 15px rgba(201, 169, 110, 0.3) !important;
//             opacity: 0.85 !important;
//             transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
//
//             /* По умолчанию на десктопе */
//             bottom: 20px !important;
//         }
//
//         /* Hover эффекты */
//         #scrollTopBtn:hover {
//             opacity: 1 !important;
//             transform: translateY(-3px) scale(1.05) !important;
//             box-shadow: 0 6px 20px rgba(201, 169, 110, 0.4) !important;
//         }
//
//         /* Активное состояние */
//         #scrollTopBtn:active {
//             transform: translateY(-1px) scale(0.98) !important;
//         }
//
//         /* Позиционирование на мобильных устройствах */
//         @media screen and (max-width: 991px) {
//             #scrollTopBtn {
//                 bottom: 100px !important; /* Выше мобильной навигации */
//                 right: 15px !important;
//                 width: 45px !important;
//                 height: 45px !important;
//                 font-size: 16px !important;
//
//                 /* Более мягкая тень на мобильных */
//                 box-shadow: 0 3px 12px rgba(201, 169, 110, 0.25) !important;
//             }
//         }
//
//         /* Для очень маленьких экранов */
//         @media screen and (max-width: 576px) {
//             #scrollTopBtn {
//                 bottom: 95px !important;
//                 right: 10px !important;
//                 width: 40px !important;
//                 height: 40px !important;
//                 font-size: 14px !important;
//             }
//         }
//
//         /* Для landscape ориентации на мобильных */
//         @media screen and (max-width: 991px) and (orientation: landscape) and (max-height: 500px) {
//             #scrollTopBtn {
//                 bottom: 80px !important;
//                 right: 10px !important;
//                 width: 35px !important;
//                 height: 35px !important;
//                 font-size: 12px !important;
//             }
//         }
//     `;
//
//     // Добавляем стили в head
//     document.head.appendChild(style);
//
//     // Изначально скрываем кнопку
//     btn.style.display = "none";
//
//     // Добавляем кнопку в body
//     document.body.appendChild(btn);
//
//     // Показ при прокрутке
//     window.addEventListener("scroll", function () {
//         if (document.documentElement.scrollTop > 100) {
//             btn.style.display = "flex";
//         } else {
//             btn.style.display = "none";
//         }
//     });
//
//     // Прокрутка вверх
//     btn.addEventListener("click", function () {
//         window.scrollTo({
//             top: 0,
//             behavior: 'smooth'
//         });
//     });
// });

document.addEventListener("DOMContentLoaded", function () {
    // Создаём кнопку
    const btn = document.createElement("button");
    btn.id = "scrollTopBtn";
    btn.title = "Наверх";
    btn.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m18 15-6-6-6 6"/>
        </svg>
    `;

    const style = document.createElement('style');
    style.textContent = `
        /* Luxury Minimalism Scroll Button */
        #scrollTopBtn {
            position: fixed !important;
            right: 24px !important;
            bottom: 24px !important;
            z-index: 99999 !important;
            
            /* Современная форма и размеры */
            width: 56px !important;
            height: 56px !important;
            border-radius: 16px !important;
            
            /* Luxury цветовая схема */
            background: rgba(255, 255, 255, 0.95) !important;
            backdrop-filter: blur(20px) !important;
            -webkit-backdrop-filter: blur(20px) !important;
            border: 1px solid rgba(0, 0, 0, 0.08) !important;
            color: #1a1a1a !important;
            
            /* Убираем стандартные стили */
            outline: none !important;
            cursor: pointer !important;
            
            /* Центрирование контента */
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            
            /* Премиальные тени и эффекты */
            box-shadow: 
                0 8px 32px rgba(0, 0, 0, 0.08),
                0 1px 2px rgba(0, 0, 0, 0.05) !important;
            
            /* Плавные переходы */
            transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1) !important;
            transform: translateY(0) scale(1) !important;
            opacity: 0 !important;
            visibility: hidden !important;
        }
        
        /* Показ кнопки */
        #scrollTopBtn.show {
            opacity: 1 !important;
            visibility: visible !important;
        }
        
        /* Hover эффекты */
        #scrollTopBtn:hover {
            transform: translateY(-2px) scale(1.02) !important;
            box-shadow: 
                0 12px 40px rgba(0, 0, 0, 0.12),
                0 2px 8px rgba(0, 0, 0, 0.08) !important;
            background: rgba(255, 255, 255, 1) !important;
            border-color: rgba(0, 0, 0, 0.12) !important;
        }
        
        /* Активное состояние */
        #scrollTopBtn:active {
            transform: translateY(-1px) scale(0.98) !important;
            transition: all 0.15s ease !important;
        }
        
        /* SVG иконка */
        #scrollTopBtn svg {
            transition: transform 0.3s ease !important;
        }
        
        #scrollTopBtn:hover svg {
            transform: translateY(-1px) !important;
        }
        
        /* Темная тема (опционально) */
        @media (prefers-color-scheme: dark) {
            #scrollTopBtn {
                background: rgba(26, 26, 26, 0.95) !important;
                border-color: rgba(255, 255, 255, 0.12) !important;
                color: #f5f5f5 !important;
                box-shadow: 
                    0 8px 32px rgba(0, 0, 0, 0.3),
                    0 1px 2px rgba(0, 0, 0, 0.2) !important;
            }
            
            #scrollTopBtn:hover {
                background: rgba(26, 26, 26, 1) !important;
                border-color: rgba(255, 255, 255, 0.2) !important;
                box-shadow: 
                    0 12px 40px rgba(0, 0, 0, 0.4),
                    0 2px 8px rgba(0, 0, 0, 0.3) !important;
            }
        }
        
        /* Адаптив для планшетов */
        @media screen and (max-width: 1024px) {
            #scrollTopBtn {
                right: 20px !important;
                bottom: 20px !important;
                width: 52px !important;
                height: 52px !important;
                border-radius: 14px !important;
            }
        }
        
        /* Мобильные устройства */
        @media screen and (max-width: 768px) {
            #scrollTopBtn {
                right: 16px !important;
                bottom: 90px !important; /* Выше мобильной навигации */
                width: 48px !important;
                height: 48px !important;
                border-radius: 12px !important;
            }
            
            #scrollTopBtn svg {
                width: 18px !important;
                height: 18px !important;
            }
        }
        
        /* Маленькие экраны */
        @media screen and (max-width: 480px) {
            #scrollTopBtn {
                right: 12px !important;
                bottom: 85px !important;
                width: 44px !important;
                height: 44px !important;
                border-radius: 10px !important;
            }
            
            #scrollTopBtn svg {
                width: 16px !important;
                height: 16px !important;
            }
        }
        
        /* Landscape на мобильных */
        @media screen and (max-width: 768px) and (orientation: landscape) and (max-height: 500px) {
            #scrollTopBtn {
                right: 12px !important;
                bottom: 70px !important;
                width: 40px !important;
                height: 40px !important;
                border-radius: 8px !important;
            }
            
            #scrollTopBtn svg {
                width: 14px !important;
                height: 14px !important;
            }
        }
        
        /* Уменьшение motion для пользователей с чувствительностью */
        @media (prefers-reduced-motion: reduce) {
            #scrollTopBtn {
                transition: opacity 0.3s ease !important;
            }
            
            #scrollTopBtn:hover {
                transform: none !important;
            }
            
            #scrollTopBtn svg,
            #scrollTopBtn:hover svg {
                transform: none !important;
            }
        }
    `;

    // Добавляем стили в head
    document.head.appendChild(style);

    // Изначально скрываем кнопку
    btn.classList.remove("show");

    // Добавляем кнопку в body
    document.body.appendChild(btn);

    // Показ/скрытие при прокрутке с debounce для производительности
    let scrollTimeout;
    window.addEventListener("scroll", function () {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(() => {
            if (document.documentElement.scrollTop > 300) {
                btn.classList.add("show");
            } else {
                btn.classList.remove("show");
            }
        }, 10);
    });

    // Плавная прокрутка вверх
    btn.addEventListener("click", function () {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
});

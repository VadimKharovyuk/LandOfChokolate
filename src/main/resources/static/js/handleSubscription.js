document.addEventListener('DOMContentLoaded', function() {
    const newsletterForm = document.getElementById('newsletterForm');
    const emailInput = document.getElementById('emailInput');
    const subscribeBtn = document.getElementById('subscribeBtn');
    const btnText = subscribeBtn.querySelector('.btn-text');
    const loadingSpinner = subscribeBtn.querySelector('.loading-spinner');
    const btnIcon = subscribeBtn.querySelector('i');
    const successMessage = document.getElementById('successMessage');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');


    // Email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // Function to show message
    function showMessage(element, duration = 5000) {
        element.classList.add('show');
        setTimeout(() => {
            element.classList.remove('show');
        }, duration);
    }

    // Function to hide all messages
    function hideMessages() {
        successMessage.classList.remove('show');
        errorMessage.classList.remove('show');
    }

    // Function to set loading state
    function setLoadingState(isLoading) {
        if (isLoading) {
            subscribeBtn.disabled = true;
            btnText.style.display = 'none';
            btnIcon.style.display = 'none';
            loadingSpinner.style.display = 'block';
        } else {
            subscribeBtn.disabled = false;
            btnText.style.display = 'inline';
            btnIcon.style.display = 'inline';
            loadingSpinner.style.display = 'none';
        }
    }

    // Function to validate email
    function validateEmail(email) {
        return emailRegex.test(email);
    }

    // Function to handle subscription
    async function handleSubscription(email) {
        try {
            const response = await fetch('/api/subscriptions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: email })
            });

            if (response.ok) {
                // 200/201 - Успешная подписка
                emailInput.value = '';
                showMessage(successMessage);

                // Analytics tracking (optional)
                if (typeof gtag !== 'undefined') {
                    gtag('event', 'newsletter_signup', {
                        'method': 'footer_form'
                    });
                }
            } else {
                // Обработка ошибок на основе HTTP статуса
                let errorData;
                try {
                    errorData = await response.json();
                } catch (parseError) {
                    errorData = { message: 'Помилка сервера' };
                }

                if (response.status === 409) {
                    // 409 Conflict - пользователь уже подписан
                    errorText.textContent = 'Ви вже підписані на нашу розсилку! 😊 Дякуємо за інтерес!';
                } else if (response.status === 400) {
                    // 400 Bad Request - ошибка валидации
                    errorText.textContent = errorData.message || 'Некоректні дані. Перевірте email адресу.';
                } else if (response.status === 500) {
                    // 500 Internal Server Error
                    errorText.textContent = 'Помилка сервера. Спробуйте пізніше або зверніться до підтримки.';
                } else {
                    // Другие ошибки
                    errorText.textContent = errorData.message || `Помилка ${response.status}. Спробуйте ще раз.`;
                }

                showMessage(errorMessage);
            }
        } catch (error) {
            console.error('Subscription error:', error);

            // Обработка сетевых ошибок
            if (error.name === 'TypeError') {
                if (error.message.includes('fetch') || error.message.includes('Failed to fetch')) {
                    errorText.textContent = 'Помилка з\'єднання. Перевірте інтернет-з\'єднання та спробуйте ще раз.';
                } else {
                    errorText.textContent = 'Помилка мережі. Спробуйте ще раз.';
                }
            } else {
                errorText.textContent = 'Сталася неочікувана помилка. Спробуйте ще раз.';
            }

            showMessage(errorMessage);
        }
    }

    // Form submit handler
    newsletterForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const email = emailInput.value.trim();

        // Hide previous messages
        hideMessages();

        // Validate email
        if (!email) {
            errorText.textContent = 'Будь ласка, введіть email адресу.';
            showMessage(errorMessage);
            emailInput.focus();
            return;
        }

        if (!validateEmail(email)) {
            errorText.textContent = 'Будь ласка, введіть коректну email адресу.';
            showMessage(errorMessage);
            emailInput.focus();
            return;
        }

        // Set loading state
        setLoadingState(true);

        // Handle subscription
        await handleSubscription(email);

        // Remove loading state
        setLoadingState(false);
    });

    // Real-time email validation
    emailInput.addEventListener('input', function() {
        const email = this.value.trim();

        if (email && !validateEmail(email)) {
            this.style.borderColor = '#dc3545';
        } else {
            this.style.borderColor = '';
        }
    });

    // Clear validation on focus
    emailInput.addEventListener('focus', function() {
        hideMessages();
        this.style.borderColor = '';
    });

    // Social links tracking (optional)
    document.querySelectorAll('.social-link').forEach(link => {
        link.addEventListener('click', function() {
            const platform = this.classList.contains('instagram') ? 'instagram' :
                this.classList.contains('telegram') ? 'telegram' : 'tiktok';

            // Analytics tracking (optional)
            if (typeof gtag !== 'undefined') {
                gtag('event', 'social_click', {
                    'platform': platform,
                    'location': 'footer'
                });
            }

            console.log(`Clicked ${platform} social link`);
        });
    });

    // Smooth scroll for footer links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                const offsetTop = target.offsetTop - 100;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
});
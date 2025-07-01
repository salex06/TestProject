document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');

    // Сброс ошибок
    usernameError.textContent = '';
    passwordError.textContent = '';

    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username,
                password
            }),
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.description || 'Ошибка регистрации');
        }

        showPopup('Успешная регистрация! Перенаправляем...', 'success');
        // Перенаправление через 2 секунды
        setTimeout(() => {
            window.location.href = '/';
        }, 2000);
    } catch (error) {
        if (error.message.includes("имя")) {
            usernameError.textContent = error.message;
        } else {
            showPopup(error.description, 'error');
        }
    }
});

function showPopup(message, type) {
    const popup = document.createElement('div');
    popup.className = `popup ${type}`;
    popup.textContent = message;

    document.body.appendChild(popup);

    setTimeout(() => {
        popup.classList.add('fade-out');
        popup.addEventListener('animationend', () => popup.remove());
    }, 3000);
}
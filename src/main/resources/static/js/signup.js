document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const usernameError = document.getElementById('usernameError');

    const password = document.getElementById('password').value;
    const name = document.getElementById('name').value;
    const surname = document.getElementById('surname').value;
    const photo = document.getElementById('photo');
    const about = document.getElementById('about').value;

    usernameError.textContent = '';

    try {
        const formData = new FormData(e.target);

        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            body: formData
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
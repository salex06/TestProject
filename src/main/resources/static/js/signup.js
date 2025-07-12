document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
        const formData = new FormData(e.target);
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            credentials: 'include',
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
        showPopup(error.message, 'error');
    }
});
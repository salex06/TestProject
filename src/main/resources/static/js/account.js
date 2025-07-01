document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/api/account', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            throw new Error('Требуется авторизация');
        }

        if (!response.ok) {
            throw new Error('Ошибка при загрузке данных');
        }

        const userData = await response.json();
        updateUserGreeting(userData.username);
    } catch (error) {
        console.error('Ошибка:', error.message);
        redirectToLogin();
    }
});

document.getElementById('logoutBtn').addEventListener('click', async () => {
    const response = await fetch('/api/account/quit', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(response.ok){
        window.location.href = '/signin';
    }
});

function updateUserGreeting(username) {
    const greetingElement = document.getElementById('account-username');
    if (greetingElement) {
        greetingElement.textContent = `${username}`;
    }
}

function redirectToLogin() {
    window.location.href = '/signin';
}
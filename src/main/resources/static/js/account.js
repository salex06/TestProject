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
        updateUserInfo(userData);
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

function updateUserInfo(data) {
    const greetingElement = document.getElementById('account-username');
    if (greetingElement) {
        greetingElement.textContent = `${data.username}`;
    }

    const name = document.getElementById('first-name');
    if(name){
        name.textContent = `${data.name}`;
    }

    const surname = document.getElementById('last-name');
    if(surname){
        surname.textContent = `${data.surname}`;
    }

    const about = document.getElementById('bio');
    if(about){
        about.textContent = `${data.about}`;
    }

    loadUserAvatar(data.photoPath);
}

async function loadUserAvatar(filename) {
    try {
        const response = await fetch(`/api/images/${filename}`, {
            method: 'GET',
            credentials: 'include',
        });
        if (response.ok) {
            const blob = await response.blob();
            const imageUrl = URL.createObjectURL(blob);
            document.getElementById('avatar-preview').src = imageUrl;
        }
    } catch (error) {
        console.error('Ошибка загрузки аватара:', error);
    }
}

function redirectToLogin() {
    window.location.href = '/signin';
}
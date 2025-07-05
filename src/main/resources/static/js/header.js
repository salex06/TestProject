document.addEventListener('DOMContentLoaded', loadPhotoFromServer);

document.addEventListener("avatarWasChanged", function(event) {
    loadPhotoFromServer();
});

async function loadPhotoFromServer() {
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
        updateHeaderInfo(userData);
    } catch (error) {
        console.error('Ошибка:', error.message);
        redirectToLogin();
    }
}

function updateHeaderInfo(data) {
    const greetingElement = document.getElementById('header-username');
    if (greetingElement) {
        greetingElement.textContent = `${data.username}`;
    }

    loadUserAvatarForHeader(data.photoPath);
}

async function loadUserAvatarForHeader(filename) {
    try {
        const response = await fetch(`/api/images/${filename}`, {
            method: 'GET',
            credentials: 'include',
        });
        if (response.ok) {
            const blob = await response.blob();
            const imageUrl = URL.createObjectURL(blob);
            document.getElementById('header-avatar').src = imageUrl;
        }
    } catch (error) {
        console.error('Ошибка загрузки аватара:', error);
    }
}
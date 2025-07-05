//Получение информации о профиле при загрузке страницы
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

//Обновление информации профиля
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

//Загрузка фото профиля
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

            let event = new Event("avatarWasChanged", {bubbles: true});
            document.dispatchEvent(event);
        }
    } catch (error) {
        console.error('Ошибка загрузки аватара:', error);
    }
}

//Выход из аккаунта
async function quit() {
    const response = await fetch('/api/account/quit', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(response.ok){
        redirectToLogin();
    }
}
document.getElementById('logoutBtn').addEventListener('click', quit);
document.getElementById('headerLogoutBtn').addEventListener('click', quit);

function redirectToLogin() {
    window.location.href = '/signin';
}

// Обработчик для кнопки редактирования имени
document.querySelector('[data-field="first-name"]').addEventListener('click', function() {
    const currentName = document.getElementById('first-name').textContent;
    openNameModal(currentName, 'nameModal', 'newName', 'nameError');
});

//Обработчик кнопки редактирования фамилии
document.querySelector('[data-field="last-name"]').addEventListener('click', function() {
    const currentData = document.getElementById('last-name').textContent;
    openNameModal(currentData, 'surnameModal', 'newSurname', 'surnameError');
});

//Обработчик кнопки редактирования раздела "о себе"
document.querySelector('[data-field="bio"]').addEventListener('click', function() {
    const currentData = document.getElementById('bio').textContent;
    openNameModal(currentData, 'aboutModal', 'newAbout', 'aboutError');
});

// Функция открытия модального окна
function openNameModal(currentData, modalName, inputName, errorName) {
    const modal = document.getElementById(modalName);
    const newDataInput = document.getElementById(inputName);
    const errorSpan = document.getElementById(errorName);

    newDataInput.value = currentData;
    errorSpan.textContent = '';
    modal.style.display = 'block';

    // Фокус на поле ввода
    newDataInput.focus();
}

// Закрытие модального окна
document.querySelector('.close').addEventListener('click', function() {
    document.getElementById('nameModal').style.display = 'none';
    document.getElementById('surnameModal').style.display = 'none';
    document.getElementById('aboutModal').style.display = 'none';
});

// Обработчик сохранения имени
document.getElementById('saveNameBtn').addEventListener('click', function() {
    const newName = document.getElementById('newName').value.trim();
    const errorSpan = document.getElementById('nameError');

    if (!newName) {
        errorSpan.textContent = 'Имя не может быть пустым';
        return;
    }

    updateUserName(newName);
});

// Обработчик сохранения фамилии
document.getElementById('saveSurnameBtn').addEventListener('click', function() {
    const newSurname = document.getElementById('newSurname').value.trim();
    const errorSpan = document.getElementById('surnameError');

    if (!newSurname) {
        errorSpan.textContent = 'Фамилия не может быть пустой';
        return;
    }

    updateUserSurname(newSurname);
});

// Обработчик сохранения раздела "о себе"
document.getElementById('saveAboutBtn').addEventListener('click', function() {
    const newAbout = document.getElementById('newAbout').value.trim();
    const errorSpan = document.getElementById('AboutError');

    if (!newAbout) {
        errorSpan.textContent = 'Вы не ввели ничего!';
        return;
    }

    updateUserAbout(newAbout);
});

//Обработчик обновления изображения
document.getElementById('avatar-upload').addEventListener('change', function(e) {
    const file = e.target.files[0];

    if (!file) return;

    // Валидация изображения
    if (!file.type.match('image.*')) {
        showPopup('Пожалуйста, выберите изображение', 'error');
        return;
    }
    if (file.size > 5 * 1024 * 1024) { // 5MB
        showPopup('Изображение должно быть меньше 5MB', 'error');
        return;
    }

    // Отправляем на сервер
    uploadAvatar(file);
});

// Функция отправки PATCH-запроса
async function updateUserName(newName) {
    const modal = document.getElementById('nameModal');
    const errorSpan = document.getElementById('nameError');

    try {
        const response = await fetch('/api/account/name', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ newName: newName })
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('first-name').textContent = data.updatedName;
            modal.style.display = 'none';

            showPopup('Имя успешно изменено', 'success');
        } else {
            const error = await response.json();
            errorSpan.textContent = error.message || 'Ошибка при сохранении';
        }
    } catch (err) {
        errorSpan.textContent = 'Ошибка сети';
        console.error('Ошибка при обновлении имени:', err);
    }
}

async function updateUserSurname(newSurname) {
    const modal = document.getElementById('surnameModal');
    const errorSpan = document.getElementById('surnameError');

    try {
        const response = await fetch('/api/account/surname', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ newSurname: newSurname })
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('last-name').textContent = data.updatedSurname;
            modal.style.display = 'none';

            showPopup('Фамилия успешно изменена', 'success');
        } else {
            const error = await response.json();
            errorSpan.textContent = error.message || 'Ошибка при сохранении';
        }
    } catch (err) {
        errorSpan.textContent = 'Ошибка сети';
        console.error('Ошибка при обновлении фамилии:', err);
    }
}

async function updateUserAbout(newAbout) {
    const modal = document.getElementById('aboutModal');
    const errorSpan = document.getElementById('aboutError');

    try {
        const response = await fetch('/api/account/about', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ newAbout: newAbout })
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('bio').textContent = data.updatedAbout;
            modal.style.display = 'none';

            showPopup('Данные успешно изменены', 'success');
        } else {
            const error = await response.json();
            errorSpan.textContent = error.message || 'Ошибка при сохранении';
        }
    } catch (err) {
        errorSpan.textContent = 'Ошибка сети';
        console.error('Ошибка при обновлении данных:', err);
    }
}

async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('newPhoto', file);

    try {
        const response = await fetch('/api/account/photo', {
            method: 'PATCH',
            body: formData,
        });

        if (response.ok) {
            showPopup('Аватар успешно обновлен', 'success');

            const data = await response.json();
            loadUserAvatar(data.updatedPhoto);
        } else {
            const error = await response.json();
            showPopup(error.message || 'Ошибка обновления', 'error');
        }
    } catch (err) {
        console.error('Ошибка загрузки:', err);
        showPopup('Ошибка сети', 'error');
    }
}


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
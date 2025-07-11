//Получение информации о профиле при загрузке страницы
const username = window.location.pathname.split('/profile/')[1];
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch(`/api/profile/${username}`, {
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
        if(await isContact(username)){
            document.getElementById('addToContactBtn').classList.add('hidden');
            document.getElementById('removeFromContactsBtn').classList.remove('hidden');
        }
    } catch (error) {
        console.error('Ошибка:', error.message);
        redirectToMainPage();
    }
});

async function isContact(username){
    const response = await fetch(`/api/contacts/check?owner=${await getUsername()}&contact=${username}`, {
        method: "GET",
        credentials: "include",
        headers : {
            "Accept" : "application/json"
        }
    });

    if(response.status == 401){
        window.location.href = "/signin";
    }

    const data = await response.text();
    if(response.ok && data == "true"){
        return true;
    }
    return false;
}

//Загрузка информации о пользователе
function updateUserInfo(data) {
    const username = document.getElementById('profile-username');
    if(username){
        username.textContent = `${data.username}`;
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

    if(data.photoPath)
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

//Обработчик нажатия на кнопку перехода к чату
document.getElementById("goToChatBtn").addEventListener("click", (e) => {
    window.location.href = `/chats?receiverUsername=${username}`;
});

//Обработчик добавления пользователя в контакты
document.getElementById("addToContactBtn").addEventListener("click", async (e) =>{
    let ownerUsername = await getUsername();
    const response = await fetch("/api/contacts", {
        method: 'POST',
        credentials: "include",
        headers: {
            "Accept" : "application/json",
            "Content-Type" : "application/json"
        },
        body: JSON.stringify({
              'owner' : ownerUsername,
              'contact' : username
        })
    });

    if(response.status == 401){
        window.location.href = "/signin";
    }

    if(response.ok){
        document.getElementById('addToContactBtn').classList.add('hidden');
        document.getElementById('removeFromContactsBtn').classList.remove('hidden');
    }
});

//Обработчик удаления пользователя из контактов
document.getElementById("removeFromContactsBtn").addEventListener("click", async (e) => {
    let ownerUsername = await getUsername();
    const response = await fetch("/api/contacts", {
        method: 'DELETE',
        credentials: "include",
        headers: {
            "Accept" : "application/json",
            "Content-Type" : "application/json"
        },
        body: JSON.stringify({
              'owner' : ownerUsername,
              'contact' : username
        })
    });

    if(response.status == 401){
        window.location.href = "/signin";
    }

    if(response.ok){
        document.getElementById('addToContactBtn').classList.remove('hidden');
        document.getElementById('removeFromContactsBtn').classList.add('hidden');
    }
});

function redirectToMainPage() {
    window.location.href = '/';
}

async function getUsername(){
    const response = await fetch('/api/account/username', {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(response.status == 401){
        window.location.href = "/signin";
    }

    let data = await response.json();
    return data.username;
}
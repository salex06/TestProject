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

//TODO: API изменилось - поправить
async function getImage(filename){
    try {
        const response = await fetch(`/api/images/${filename}`, {
            method: 'GET',
            credentials: 'include',
        });

        if (response.ok) {
            const blob = await response.blob();
            return URL.createObjectURL(blob);
        }else{
            throw new Error('Ошибка загрузки изображения', "error");
        }
    } catch (error) {
        console.error(error);
    }
}

function redirectToLogin() {
    window.location.href = '/signin';
}

function redirectToAccount(){
    window.location.href = "/account";
}

function redirectToChatWithUser(username){
    window.location.href = `/chats?receiverUsername=${username}`;
}

function redirectToProfile(username){
    window.location.href = `/profile/${username}`;
}

//Получение информации (username, name, surname...) об авторизованном пользователе
async function getUserInfo(){
    try {
        const response = await fetch('/api/account', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401) {
            redirectToLogin();
        }

        if (!response.ok) {
            throw new Error('Ошибка при загрузке данных');
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error(error.message);
    }
}

//Получение username авторизованного пользователя
async function getUsername(){
    try{
        const response = await fetch('/api/account/username', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if(response.status == 401){
            redirectToLogin();
        }
        if(!response.ok){
            throw new Error('Ошибка получения имени пользователя!');
        }

        let data = await response.json();
        return data.username;
    }catch(error){
        console.log(error);
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
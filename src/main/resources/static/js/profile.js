const username = window.location.pathname.split('/profile/')[1];

//Обработчик загрузки страницы
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch(`/api/profile/${username}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401) {
            redirectToLogin();
        }else if(response.redirected){
            redirectToAccount();
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
    }
});

async function isContact(username){
    try{
        const response = await fetch(`/api/contacts/check?contact=${username}`, {
            method: "GET",
            credentials: "include",
            headers : {
                "Accept" : "application/json"
            }
        });

        if(response.status == 401){
            redirectToLogin();
        }
        if(!response.ok){
            throw new Error('Ошибка получения данных');
        }

        const data = await response.text();
        return data == "true";
    }catch (error) {
       console.error('Ошибка:', error.message);
    }
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

    const photo = document.getElementById('avatar-preview');
    if(photo)
        photo.src = `/images/users/${data.photoPath || 'no_img.jpg'}`;
}

//Обработчик нажатия на кнопку перехода к чату
document.getElementById("goToChatBtn").addEventListener("click", (e) => {
    redirectToChatWithUser(username);
});

//Обработчик добавления пользователя в контакты
document.getElementById("addToContactBtn").addEventListener("click", async (e) => {
    try{
        const response = await fetch("/api/contacts", {
            method: 'POST',
            credentials: "include",
            headers: {
                "Accept" : "application/json",
                "Content-Type" : "application/json"
            },
            body: JSON.stringify({
                  'contact' : username
            })
        });

        if(response.status == 401){
            redirectToLogin();
        }

        if(!response.ok){
            throw new Error("Ошибка при добавлении пользователя в контакты");
        }

        document.getElementById('addToContactBtn').classList.add('hidden');
        document.getElementById('removeFromContactsBtn').classList.remove('hidden');
    }catch(error){
        console.log(error);
    }
});

//Обработчик удаления пользователя из контактов
document.getElementById("removeFromContactsBtn").addEventListener("click", async (e) => {
    try{
        const response = await fetch("/api/contacts", {
            method: 'DELETE',
            credentials: "include",
            headers: {
                "Accept" : "application/json",
                "Content-Type" : "application/json"
            },
            body: JSON.stringify({
                  'contact' : username
            })
        });

        if(response.status == 401){
            redirectToLogin();
        }

        if(!response.ok){
            throw new Error("Ошибка при удалении пользователя из контактов");
        }

        document.getElementById('addToContactBtn').classList.remove('hidden');
        document.getElementById('removeFromContactsBtn').classList.add('hidden');
    }catch(error){
        console.log(error);
    }
});


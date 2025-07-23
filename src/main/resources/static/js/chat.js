let currentUser; //username авторизованного пользователя
let selectedUser = null; //username собеседника

const url = "http://localhost:8080";
const chatContainer = document.getElementById('chat-list');
const chatMessagesContainer = document.getElementById('chat-messages');
const sendMessageBtn = document.getElementById('message-btn');
const messageInput = document.getElementById('message-input');
const messageMediaInput = document.getElementById('message-media');
const mediaFilenameBlock = document.getElementById('media-filename');
const cancelUploadingIcon = document.getElementById('cancel-uploading-image');

let lastMessageDate;

let stompClient;
let socket;
function activateWebSocket(){
    socket = new SockJS(url + '/chat');

    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
    });

    stompClient.onConnect = (frame) => {
        console.log("connected: " + frame);

        stompClient.subscribe('/topic/messages/' + currentUser, function(response) {
            //Логика обработки нового сообщения
            var data = JSON.parse(response.body);
            if(selectedUser == data.senderUsername){
                addMessageToChat(data, lastMessageDate);
                readAllMessages(data.senderUsername);
            }else{
                addUnreadMessage(data);
            }
        });
    };

    stompClient.activate();
}

window.addEventListener('beforeunload', () => {
    if (stompClient && stompClient.connected) {
        stompClient.deactivate();
    }
});

document.addEventListener('DOMContentLoaded', async () => {
    activateWebSocket();

    currentUser = await getUsername();

    //Подгружаем все чаты
    await loadChats(currentUser);

    const urlParams = new URLSearchParams(window.location.search);
    const receiverUsername = urlParams.get('receiverUsername');
    if(receiverUsername && receiverUsername != currentUser){
        await openRequestedChat(receiverUsername);
    }
});

async function openRequestedChat(receiverUsername){
    const chatElement = document.querySelector(`[data-username="${receiverUsername}"]`);
    if(chatElement){
        chatElement.click();
    }else{
        const chatPartnerInfo = await getChatPartnerInfo(receiverUsername);
        chatPartnerInfo.unreadMessageCount = (await getUnreadMessageCount(receiverUsername)).count;
        appendChat(chatPartnerInfo);

        document.querySelector(`[data-username="${receiverUsername}"]`).click();
    }
}

async function readAllMessages(senderUsername){
    const response = await fetch(`/api/chat/history/mark/read?username=${senderUsername}`, {
        method : "PATCH",
        credentials: "include",
    });

    if(response.status == 401){
        redirectToLogin();
    }
}

sendMessageBtn.addEventListener("click", async (e) => {
    //Если получатель не определен - выходим из функции
    if(!selectedUser) return;

    const text = messageInput.value;
    const files = messageMediaInput.files;
    //Если и текстовое поле не заполнено, и изображение не загружено - выходим из функции
    if(!text && !files[0]) return;

    //Загружаем изображение на сервер и получаем путь к нему
    let pathToImage;
    if(files[0]){
        pathToImage = await uploadFile(files[0]);
    }

    //Отправляем сообщение по вебсокету
    const wsMessage = {
        text: text,
        pathToImage : pathToImage
    };
    sendMessage(wsMessage);

    //Очищаем поля для ввода
    messageInput.value = '';
    cancelUploadingIcon.click();

    //Выводим сообщение в чат
    message = {
        text: text,
        createdAt: new Date(),
        senderUsername: currentUser,
        receiverUsername: selectedUser,
        pathToImage : pathToImage,
    };
    addMessageToChat(message, lastMessageDate);
});

async function sendMessage(wsMessage){
    stompClient.publish({
      destination: '/app/chat/' + selectedUser,
      body: JSON.stringify(wsMessage),
      credentials: 'include'
    });
}

async function uploadFile(file){
    let formData = new FormData();
    formData.append('image', file);

    try{
        const response = await fetch('/api/images/messages', {
            method: 'POST',
            credentials: 'include',
            body: formData
        });

        if(response.status == 401){
            redirectToLogin();
        }
        if(!response.ok){
            throw new Error(response);
        }

        return await response.text();
    }catch(error){
        console.log(error);
        return null;
    }
}

async function loadChats(username){
    const response = await fetch('/api/chat/list', {
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
        return false;
    }

    const data = await response.json();
    chatContainer.innerHTML = '';
    await appendChats(data.partners);
    return true;
}

async function appendChats(data){
    for(let chat of data){
        let username = chat.username;
        let photoPath = chat.photoPath;

        let unreadMessageCount = (await getUnreadMessageCount(username)).count;

        let chatInfo = {
            username : username,
            photoPath : photoPath,
            unreadMessageCount: unreadMessageCount
        };
        appendChat(chatInfo);

    }
}

function appendChat(chatInfo){
    chatContainer.innerHTML += `
        <div class="chat-item" data-username = ${chatInfo.username}>
            <div class="chat-avatar">
               <img src="/images/users/${chatInfo.photoPath || 'no_img.jpg'}" alt="Аватар">
           </div>
            <div class="chat-info">
               <div class="chat-name">${chatInfo.username}</div>
            </div>
            <div class="unread-messages-counter hidden" data-value=0>0</div>
        </div>`;

    let chatItem = chatContainer.querySelector(`.chat-item[data-username='${chatInfo.username}']`);
    updateMsgCounterByNewValue(chatItem, chatInfo.unreadMessageCount);
    return chatItem;
}

async function getUnreadMessageCount(username){
    try{
        const response = await fetch(`/api/chat/history/unread/count?username=${username}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                "Accept" : "application/json"
            }
        });
        if(response.status == 401){
            redirectToLogin();
        }

        if(!response.ok){
            throw new Error(response);
        }

        return await response.json();
    }catch(error){
        console.log(error);
    }
}

async function addUnreadMessage(data){
    //Получаем username отправителя
    const senderUsername = data.senderUsername;

    //Находим в блоке с чатами нужный чат
    let chatItem = chatContainer.querySelector(`[data-username=${senderUsername}]`);

    if(!chatItem){
        //Если чат не найден - добавляем его
        let chatInfo = await getChatPartnerInfo(senderUsername);
        chatInfo.unreadMessageCount = (await getUnreadMessageCount(senderUsername)).count;
        chatItem = appendChat(chatInfo);
    }else{
        //Иначе увеличиваем счётчик непрочитанных сообщений
        incrementMsgCounter(chatItem);
    }
}

async function getChatPartnerInfo(username){
    try{
        let response = await fetch(`/api/chat/user?username=${username}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                "Accept" : "application/json"
            }
        });

        if(response.status == 401){
            redirectToLogin();
        }
        if(!response.ok){
            throw new Error(response);
        }

        return await response.json();
    }catch(error){
        console.log(error);
    }
}

function incrementMsgCounter(chatItem){
    let val = +chatItem.querySelector('.unread-messages-counter').dataset.value;
    updateMsgCounterByNewValue(chatItem, val + 1);
}

function updateMsgCounterByNewValue(chatItem, newValue){
    if(newValue == 0) return;

    let unreadMessageCounter = chatItem.querySelector('.unread-messages-counter');
    unreadMessageCounter.classList.remove('hidden');
    if(newValue > 99){
        unreadMessageCounter.innerHTML = '+99';
    }else{
        unreadMessageCounter.innerHTML = newValue;
    }
    unreadMessageCounter.dataset.value = newValue;
}

//Обработчик отправки сообщения при нажатии на Enter
document.addEventListener('keyup', function(event) {
  if (event.code == 'NumpadEnter') {
    sendMessageBtn.click();
  }
});

//Обработчик открытия чата
chatContainer.addEventListener('click', async (event) => {
    const clickedItem = event.target.closest('.chat-item');

    if (clickedItem) {
        //Удаляем фокус с предыдущего чата
        if(selectedUser){
            document.querySelector(`[data-username="${selectedUser}"]`).classList.remove("active");
        }

        //Устанавливаем фокус на текущий чат
        clickedItem.classList.add("active");
        selectedUser = clickedItem.dataset.username;

        //Для маленьких экранов скрываем левый блок с чатами
        if (window.innerWidth <= 768) {
            document.getElementById('chat-sidebar').style.display = 'none';
            document.getElementById('chat-main').style.display = "flex";
            document.getElementById('close-chat').classList.remove('hidden');
        }

        //Выводим новые данные
        resetUnreadMessagesCounter(clickedItem);
        appendChatHeader(selectedUser);

        lastMessageDate = null;
        chatMessagesContainer.innerHTML = "";
        loadChatHistory(selectedUser);
    }
});

function resetUnreadMessagesCounter(chat){
    let unreadMessageCounter = chat.querySelector('.unread-messages-counter');
    unreadMessageCounter.dataset.value = 0;
    unreadMessageCounter.classList.add('hidden');
}

function appendChatHeader(name){
    const currentChatUser = document.getElementById('current-chat-user');
    currentChatUser.innerHTML = `
        <div class="chat-user-info">
            <div class="chat-user-name">${name}</div>
        </div>`;
    currentChatUser.dataset.username = name;
}

async function loadChatHistory(selectedUser){
    const historyResponse = await fetch(`/api/chat/history?username=${selectedUser}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(historyResponse.status == 401){
        redirectToLogin();
    }

    if(historyResponse.ok){
        addMessages(await historyResponse.json());
    }
}

function addMessages(data){
    for(let i of data.history){
        addMessageToChat(i, lastMessageDate);
        lastMessageDate = new Date(i.createdAt);
    }
}

function addMessageToChat(message, prevDate){
    //Текст сообщения
    let text = message.text;

    //Дата отправки сообщения
    let date = new Date(message.createdAt);
    let createdAt = ('0' + date.getHours()).slice(-2) + ":" + ('0' + date.getMinutes()).slice(-2);

    //Username отправителя и получателя
    let senderUsername = message.senderUsername;
    let receiverUsername = message.receiverUsername;

    //Путь к изображению (если есть)
    let pathToImage = message.pathToImage != null ? `src=/images/messages/${message.pathToImage}` : "";

    //Если сообщение состоит только из 1 изображения - формируем название доп. класса
    let messageContainsOnlyOmg = (!text ? " message-only-img" : "");

    //Если предыдущего сообщения не было или оно отличается датой от текущего - выводим разделитель
    if(!prevDate || prevDate.setHours(0, 0, 0, 0) != date.setHours(0, 0, 0, 0)){
        chatMessagesContainer.innerHTML += `
            <div class="message-delimiter">${formatDateDifference(date)}</div>
        `;
    }

    //Добавляем блок сообщение в зависимости от его типа ("отправленное" или "полученное")
    if(senderUsername == selectedUser){
        chatMessagesContainer.innerHTML += `
            <div class="message received">
                <div class="message-content${messageContainsOnlyOmg}">
                    <img ${pathToImage} class="message-img-received">
                    <span>${text}</span>
                </div>
                <div class="message-time">${createdAt}</div>
            </div>`;
    }else{
        chatMessagesContainer.innerHTML += `
            <div class="message sent">
                <div class="message-content${messageContainsOnlyOmg}">
                    <img ${pathToImage} class="message-img-sent">
                    <span>${text}</span>
                </div>
                <div class="message-time">${createdAt}</div>
            </div>`;
    }

    //Устанавливаем скролл на позицию самого нового сообщения
    chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
}

function formatDateDifference(d1) {
    const months = [
        'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
        'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
    ];

    const currentDate = new Date();

    const day1 = d1.getDate();
    const month1 = months[d1.getMonth()];
    const year1 = d1.getFullYear();

    const diffTime = Math.abs(currentDate - d1);
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    //Если сообщение отправлено сегодня или вчера - возвращаем спец. значения
    if(diffDays === 0){
        return "Сегодня"
    }
    if (diffDays === 1) {
        return "Вчера";
    }

    //Если сообщение было отправлено не в этом году, дополнительно записываем год
    if (year1 !== currentDate.getFullYear()) {
        return `${day1} ${month1} ${year1}`;
    }

    //Стандартный формат "день - месяц"
    return `${day1} ${month1}`;
}

//Обработчик нажатия на поле с именем пользователя в "шапке" чата
document.getElementById('current-chat-user').addEventListener("click", (e) => {
    let item = e.target.closest('.current-chat-user');
    if(item.dataset.username)
        redirectToProfile(item.dataset.username);
})

//Обработчик кнопки "Закрыть чат" (маленькие экраны)
document.getElementById('close-chat').addEventListener('click', (e) => {
    document.getElementById('current-chat-user').dataset.username = null;

    if (window.innerWidth <= 768) {
        document.getElementById('chat-sidebar').style.display = 'flex';
        document.getElementById('chat-main').style.display = "none";
        document.getElementById('close-chat').classList.add('hidden');

        if(selectedUser){
            document.querySelector(`[data-username="${selectedUser}"]`).classList.remove('active');
        }
        selectedUser = null;
    }
});

//Обработчик нажатия на кнопку "Удалить чат"
document.getElementById('remove-chat').addEventListener('click', async (e) => {
    if(!selectedUser){
        return;
    }

    const removeResponse = await fetch(`/api/chat?second=${selectedUser}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(removeResponse.status == 401){
        redirectToLogin();
    }

    //Если чат успешно удален
    if(removeResponse.ok){
        //Удаляем все сообщения с экрана
        chatMessagesContainer.innerHTML = "";

        //Удаляем информацию о текущем пользователе в "шапке" чата
        let currentChatUser = document.getElementById('current-chat-user');
        currentChatUser.innerHTML = "";
        currentChatUser.dataset.username = null;

        //Удаляем блок с собеседником из сайдбара
        document.querySelector(`[data-username="${selectedUser}"]`).remove();

        //Сбрасываем выбор текущего собеседника
        selectedUser = null;
    }

    //Закрываем текущий чат
    document.getElementById('close-chat').click();
});

//Обработчик нажатия на иконку добавления изображения к сообщению
document.getElementById('attach-btn').querySelector(".fa-paperclip").addEventListener('click', function(e) {
    e.stopPropagation();

    //Кнопка кликабельна только при открытом чате с кем-либо
    if(selectedUser)
        messageMediaInput.click();
});

//Обработчик для изменения выбора загружаемого файла
messageMediaInput.addEventListener('change', function(e) {
    //Очищаем поле с названием текущего загружаемого файла
    mediaFilenameBlock.innerHTML = "";

    //Скрываем элементы
    mediaFilenameBlock.classList.add('hidden');
    cancelUploadingIcon.classList.add('hidden');

    //Если выбрано новое изображение, устанавливаем новые данные
    if (this.files && this.files[0]) {
        //Выводим элементы
        mediaFilenameBlock.classList.remove('hidden');
        cancelUploadingIcon.classList.remove('hidden');

        //Устанавливаем название файла
        mediaFilenameBlock.innerHTML =
            this.files[0].name.substring(0, 10) + (this.files[0].name.length > 10 ? "..." : "");

    }
});

//Обработчик отмены загрузки (отправки) изображения
cancelUploadingIcon.addEventListener('click', function(e) {
    //Удаляем файл из поля для загрузки
    messageMediaInput.value = '';

    //Очищаем поле с названием файла
    mediaFilenameBlock.innerHTML = "";

    //Скрываем элементы загрузки
    mediaFilenameBlock.classList.add('hidden');
    cancelUploadingIcon.classList.add('hidden');
});



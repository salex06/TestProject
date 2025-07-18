let selectedUser = null;
const chatMessagesContainer = document.getElementById('chat-messages');
const sendMessageBtn = document.getElementById('message-btn');
const messageInput = document.getElementById('message-input');
const messageMediaInput = document.getElementById('message-media');
const cancelUploadingBtn = document.getElementById('cancel-uploading-image');
const chatContainer = document.getElementById('chat-list');

let stompClient;
let socket;
let currentUser;
document.addEventListener('DOMContentLoaded', async () => {
    const url = "http://localhost:8080";
    socket = new SockJS(url + '/chat');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
    });

    currentUser = await getUsername();
    stompClient.activate();
    stompClient.onConnect = (frame) => {
        console.log("connected: " + frame);

        stompClient.subscribe('/topic/messages/' + currentUser, function(response) {
            //Логика обработки нового сообщения
            var data = JSON.parse(response.body);
            let createdAt = new Date(data.createdAt);
            data.createdAt = createdAt.getHours() + ":" + createdAt.getMinutes();
            if(selectedUser == data.senderUsername){
                addMessageToChat(data);
                readAllMessages(data.senderUsername);
            }else{
                addUnreadMessage(data);
            }
        });
    };
    sendMessageBtn.addEventListener("click", sendMessage);

    window.addEventListener('beforeunload', () => {
        if (stompClient && stompClient.connected) {
            stompClient.deactivate();
        }
    });

    //Подгружаем все чаты
    await loadChats(currentUser);

    const urlParams = new URLSearchParams(window.location.search);
    const receiverUsername = urlParams.get('receiverUsername');
    if(receiverUsername && receiverUsername != currentUser){
        const chatElement = document.querySelector(`[data-username="${receiverUsername}"]`);
        if(chatElement){
            chatElement.click();
        }else{
            let photoResp = await fetch(`/api/users/photo?username=${receiverUsername}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    "Accept" : "application/json"
                }
            });
            let photoPath;
            if(photoResp.ok){
                photoPath = await photoResp.text();
            }
            let unreadMessageCount = (await getUnreadMessageCount(receiverUsername)).count;
            chatInfo = {
                username : receiverUsername,
                photoPath : photoPath,
                unreadMessageCount : unreadMessageCount
            };
            appendChat(chatInfo);
            document.querySelector(`[data-username="${receiverUsername}"]`).click();
        }
    }
});

async function readAllMessages(senderUsername){
    const response = await fetch(`/api/chat/history/mark/read?username=${senderUsername}`, {
        method : "PATCH",
        credentials: "include",
    });

    if(response.status == 401){
        redirectToLogin();
    }
}

async function sendMessage(e){
    if(!selectedUser) return;

    const text = messageInput.value;
    const files = messageMediaInput.files;
    if(!text && !files[0])
        return;

    let pathToImage;
    if(files[0]){
        pathToImage = await uploadFile(files[0]);
    }

    const wsMessage = {
        text: text,
        pathToImage : pathToImage
    };
    stompClient.publish({
      destination: '/app/chat/' + selectedUser,
      body: JSON.stringify(wsMessage),
      credentials: 'include'
    });

    messageInput.value = '';
    cancelUploadingBtn.click();

    date = new Date();
    message = {
        text: text,
        createdAt: date.getHours() + ":" + date.getMinutes(),
        senderUsername: currentUser,
        receiverUsername: selectedUser,
        pathToImage : pathToImage
    };

    addMessageToChat(message);
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
        return;
    }

    const data = await response.json();
    chatContainer.innerHTML = '';
    for(let chat of data.partners){
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

    let chatItem =  chatContainer.querySelector(`.chat-item[data-username='${chatInfo.username}']`);
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

        const data = await response.json();
        console.log(data.count + " " + username);
        return data;
    }catch(error){
        console.log(error);
    }
}

function addMessageToChat(message){
    let text = message.text;
    let createdAt = message.createdAt;
    let senderUsername = message.senderUsername;
    let receiverUsername = message.receiverUsername;
    let pathToImage = message.pathToImage != null ? `src=/images/messages/${message.pathToImage}` : "";
    if(senderUsername == selectedUser){
        chatMessagesContainer.innerHTML += `
            <div class="message received">
                <div class="message-content">
                    <img ${pathToImage} class="message-img">
                    <span>${text}</span>
                </div>

                <div class="message-time">${createdAt}</div>
            </div>`;
    }else{
        chatMessagesContainer.innerHTML += `
            <div class="message sent">
                <div class="message-content">
                    <img ${pathToImage} class="message-img">
                    <span>${text}</span>
                </div>
                <div class="message-time">${createdAt}</div>
            </div>`;
    }
    chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
}

async function addUnreadMessage(data){
    const senderUsername = data.senderUsername;
    let chatItem = chatContainer.querySelector(`[data-username=${senderUsername}]`);
    if(!chatItem){
        let chatInfo = await getChatPartnerInfo(senderUsername);
        chatInfo.unreadMessageCount = (await getUnreadMessageCount(senderUsername)).count;
        chatItem = appendChat(chatInfo);
    }else{
        updateMsgCounter(chatItem);
    }
}

function updateMsgCounter(chatItem){
    let unreadMessageCounter = chatItem.querySelector('.unread-messages-counter');
    unreadMessageCounter.classList.remove('hidden');
    let val = +unreadMessageCounter.dataset.value;
    val += 1;
    if(val > 99){
        unreadMessageCounter.innerHTML = '+99';
    }else{
        unreadMessageCounter.innerHTML = val;
    }
    unreadMessageCounter.dataset.value = val;
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

document.addEventListener('keyup', function(event) {
  if (event.code == 'NumpadEnter') {
    sendMessage(event);
  }
});

document.getElementById('chat-list').addEventListener('click', async (event) => {
    const clickedItem = event.target.closest('.chat-item');

    if (clickedItem) {
        if(selectedUser){
            document.querySelector(`[data-username="${selectedUser}"]`).classList.remove("active");
        }
        clickedItem.className = "chat-item active";
        const name = clickedItem.dataset.username;
        selectedUser = name;

        let unreadMessageCounter = clickedItem.querySelector('.unread-messages-counter');
        unreadMessageCounter.dataset.value = 0;
        unreadMessageCounter.classList.add('hidden');

        const historyResponse = await fetch(`/api/chat/history?username=${name}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if(historyResponse.status == 401){
            redirectToLogin();
        }

        const currentChatUser = document.getElementById('current-chat-user');
        currentChatUser.innerHTML = `
            <div class="chat-user-info">
                <div class="chat-user-name">${name}</div>
            </div>`;
        currentChatUser.dataset.username = name;

        document.getElementById('remove-chat').classList.remove('hidden');

        chatMessagesContainer.innerHTML = "";
        if (window.innerWidth <= 768) {
            document.getElementById('chat-sidebar').style.display = 'none';
            document.getElementById('chat-main').style.display = "flex";
            document.getElementById('close-chat').classList.remove('hidden');
        }

        if(historyResponse.ok){
            let data = await historyResponse.json();
            for(let i of data.history){
                let date = new Date(i.createdAt);
                i.createdAt =  date.getHours() + ":" + date.getMinutes();
                addMessageToChat(i);
            }
        }
    }
});

document.getElementById('current-chat-user').addEventListener("click", (e) => {
    let item = e.target.closest('.current-chat-user');
    if(item.dataset.username)
        redirectToProfile(item.dataset.username);
})

document.getElementById('close-chat').addEventListener('click', (e) => {
    document.getElementById('current-chat-user').dataset.username = null;
    if (window.innerWidth <= 768) {
        document.getElementById('chat-sidebar').style.display = 'flex';
        document.getElementById('chat-main').style.display = "none";
        document.getElementById('close-chat').classList.add('hidden');

        if(selectedUser){
            document.querySelector(`[data-username="${selectedUser}"]`).className = "chat-item";
        }
        selectedUser = null;
    }
});

document.getElementById('remove-chat').addEventListener('click', async (e) => {
    const removeResponse = await fetch(`/api/chat?second=${selectedUser}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    });

    if(removeResponse.ok){
        document.getElementById('chat-messages').innerHTML = "";
        document.getElementById('current-chat-user').innerHTML = "";
        document.querySelector(`[data-username="${selectedUser}"]`).remove();
        selectedUser = null;
    }

    document.getElementById('close-chat').click();
});

document.getElementById('attach-btn').querySelector(".fa-paperclip").addEventListener('click', function(e) {
    e.stopPropagation();
    if(selectedUser)
        document.getElementById('message-media').click();
});

document.getElementById('message-media').addEventListener('change', function(e) {
    let mediaFilenameBlock = document.getElementById('media-filename');
    let cancelUploadingIcon = document.getElementById('cancel-uploading-image');
    mediaFilenameBlock.innerHTML = "";
    mediaFilenameBlock.classList.add('hidden');
    cancelUploadingIcon.classList.add('hidden');
    if (this.files && this.files[0]) {
        mediaFilenameBlock.classList.remove('hidden');
        cancelUploadingIcon.classList.remove('hidden');
        mediaFilenameBlock.innerHTML =
            this.files[0].name.substring(0, 10) + (this.files[0].name.length > 10 ? "..." : "");

    }
});

document.getElementById('cancel-uploading-image').addEventListener('click', function(e) {
    document.getElementById('message-media').value = '';
    let mediaFilenameBlock = document.getElementById('media-filename');
    let cancelUploadingIcon = document.getElementById('cancel-uploading-image');
    mediaFilenameBlock.innerHTML = "";
    mediaFilenameBlock.classList.add('hidden');
    cancelUploadingIcon.classList.add('hidden');
});
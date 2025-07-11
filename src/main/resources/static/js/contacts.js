const contactsContainer = document.getElementById('contacts-list');
document.addEventListener('DOMContentLoaded', async () => {
    const response = await fetch('/api/contacts', {
        method : 'GET',
        credentials: 'include'
    });

    if(response.status == 401){
        redirectToLogin();
    }

    if(response.ok){
        const data = await response.json();
        const contacts = data.contacts;
        for(let i of contacts){
            appendContact(i);
        }
    }
});

function redirectToLogin(){
    window.location.href = '/signin';
}

function appendContact(contact){
    contactsContainer.innerHTML += `
        <div class="contact-card">
            <div class="contact-info" data-username=${contact.username}>
                <div class="contact-avatar">
                    <img src="/images/${contact.photoPath || 'no_img.jpg'}" alt="Аватар" class="contact-img">
                </div>
                <div class="contact-details">
                    <span class="contact-username">${contact.username}</span>
                    <span class="contact-name">${contact.name} ${contact.surname}</span>
                </div>
            </div>
            <div class="contact-actions">
                <button class="btn btn-chat" data-username=${contact.username}>
                    <i class="fas fa-comment-dots"></i>
                </button>
                <button class="btn btn-delete" data-username=${contact.username}>
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        </div>
    `;
}

contactsContainer.addEventListener('click', (event) => {
    const isClickToProfile = event.target.closest('.contact-info');

    if (isClickToProfile) {
        const name = isClickToProfile.dataset.username;
        window.location.href = `/profile/${name}`;
    }

    const isClickToChat = event.target.closest('.btn-chat');
    if (isClickToChat) {
        const name = isClickToChat.dataset.username;
        window.location.href = `/chats?receiverUsername=${name}`;
    }

    const isClickToRemoveContact = event.target.closest('.btn-delete');
    if(isClickToRemoveContact) {
        const name = isClickToRemoveContact.dataset.username;
        if(removeContact(name)){
            isClickToRemoveContact.parentElement.parentElement.remove();
        }
    }
});

async function removeContact(username){
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
        return false;
    }

    return true;
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
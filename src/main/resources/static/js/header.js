document.addEventListener('DOMContentLoaded', loadInfoFromServer);

document.addEventListener("avatarWasChanged", function(event) {
    loadInfoFromServer();
});

document.getElementById('header-avatar').addEventListener("click", (e) => {
    window.location.href = "/account";
})

document.getElementById('header-username').addEventListener("click", (e) => {
    window.location.href = "/account";
})

async function loadInfoFromServer() {
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

//Поиск пользователей
let currentPage = 0;
const pageSize = 5;
let isLoading = false;
const searchInput = document.getElementById('global-search');
const dropdownList = document.getElementById('search-results');

dropdownList.addEventListener('click', (event) => {
    const clickedItem = event.target.closest('.search-result-item');

    if (clickedItem) {
        const name = clickedItem.dataset.username; // Получаем data-name
        window.location.href = `/profile/${name}`;
    }
});

let debounceTimer;
searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    dropdownList.style.display = "none";
    debounceTimer = setTimeout(() => {
        if (searchInput.value.length > 2) {
            currentPage = 0;
            dropdownList.innerHTML = '';
            dropdownList.addEventListener('scroll', handleScroll);
            fetchSuggestions(searchInput.value);
        } else {
            dropdownList.innerHTML = '';
        }
    }, 300);
});

async function fetchSuggestions(query) {
    if (isLoading) return; // Не вызывать новый запрос, пока идёт текущий
    isLoading = true;

    try {
        const response = await fetch(`/api/users/search?query=${encodeURIComponent(query)}&page=${currentPage}&size=${pageSize}`);
        if(response.status == '401'){
            redirectToLogin();
        }
        const data = await response.json();

        if(data.content.length == 0 && currentPage == 0){
            dropdownList.innerHTML = `
               <div class="search-result-item">
                   <span>Ничего не найдено</span>
               </div>`;
            dropdownList.style.display = "block";
            return;
        }

        // Добавляем новые элементы в список
        data.content.forEach(user => {
            dropdownList.innerHTML +=
                `<div class="search-result-item" data-username="${user.username}">
                    <img src="/images/${user.photoPath || 'no_img.jpg'}" alt="${user.username}">
                    <span class="search-result-item-username">${user.username} </span>
                    <span class="search-result-item-otherInfo">${user.name} ${user.surname}</span>
                </div>`;
        });

        currentPage++;

        if (currentPage >= data.totalPages) {
            dropdownList.removeEventListener('scroll', handleScroll);
        }

        dropdownList.style.display = "block";
    } catch (error) {
        console.error("Ошибка загрузки:", error);
    } finally {
        isLoading = false;
    }
}

function isScrollNearBottom() {
    const { scrollTop, clientHeight, scrollHeight } = dropdownList;
    return scrollTop + clientHeight >= scrollHeight - 20;
}

function handleScroll() {
    if (isScrollNearBottom()) {
        fetchSuggestions(searchInput.value);
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
document.getElementById('headerLogoutBtn').addEventListener('click', quit);

function redirectToLogin() {
    window.location.href = '/signin';
}
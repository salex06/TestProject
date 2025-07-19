//Обработчик загрузки страницы
document.addEventListener('DOMContentLoaded', async (e) => {
    updateHeaderInfo();
});

//Обработчик события обновления фото пользователя
document.addEventListener("avatarWasChanged", async (e) => {
    updateHeaderInfo();
});

async function updateHeaderInfo(){
    const data = await getUserInfo();

    const headerUsername = document.getElementById('header-username');
    if (headerUsername) {
        headerUsername.textContent = `${data.username}`;
    }

    const headerAvatar = document.getElementById('header-avatar');
    if(headerAvatar){
        headerAvatar.src = `/images/users/${data.photoPath || 'no_img.jpg'}`;
    }
}

//Обработчик нажатия на информационное поле header - переход в аккаунт
document.querySelector('.header-right').addEventListener("click", (e) => {
    redirectToAccount();
})

//Обработчик нажатия на кнопку выхода из аккаунта
document.getElementById('headerLogoutBtn').addEventListener('click', quit);

const dropdownList = document.getElementById('search-results');
//Обработчик нажатия на запись с найденным пользователем
dropdownList.addEventListener('click', (event) => {
    const clickedItem = event.target.closest('.search-result-item');

    if (clickedItem) {
        const name = clickedItem.dataset.username;
        redirectToProfile(name);
    }
});

const searchInput = document.getElementById('global-search');
let debounceTimer;
//Обработчик ввода данных в строку поиска пользователей
searchInput.addEventListener('input', () => {
    dropdownList.style.display = "none";

    clearTimeout(debounceTimer);
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

const pageSize = 5;
let currentPage = 0;
let isLoading = false;
async function fetchSuggestions(query) {
    if (isLoading) return; // Не вызывать новый запрос, пока идёт текущий
    isLoading = true;

    try {
        const response = await fetch(
            `/api/users/search?query=${encodeURIComponent(query)}&page=${currentPage}&size=${pageSize}`
        );

        if(response.status == 401){
            redirectToLogin();
        }
        if(!response.ok){
            throw new Error("Ошибка поиска");
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
                    <img src="/images/users/${user.photoPath || 'no_img.jpg'}" alt="${user.username}">
                    <span class="search-result-item-username">${user.username} </span>
                    <span class="search-result-item-otherInfo">${user.name} ${user.surname}</span>
                </div>`;
        });

        //Увеличиваем номер страницы
        currentPage++;

        //Если страница последняя - останавливаем механизм пагинации
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


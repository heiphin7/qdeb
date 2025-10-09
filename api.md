QDEB API Документация

Версия: 1.0
База URL: `http://localhost:5234`
Аутентификация: `Bearer <JWT_TOKEN>` в заголовке `Authorization` (кроме публичных/открытых маршрутов)

## Содержание
- Аутентификация
  - POST `/api/auth/signup` — регистрация
  - POST `/api/auth/signin` — авторизация (получение JWT)
- Тестовые/примерные эндпоинты
  - GET `/api/test/public` — публичный
  - GET `/api/test/user` — доступ для роли `ROLE_USERS`
  - GET `/api/test/admin` — доступ для роли `ROLE_ADMIN`
  - GET `/api/test/profile` — профиль текущего пользователя
- Файлы и изображения
  - GET `/api/files/profile-picture/{fileName}` — получение фото профиля
  - GET `/api/files/{fileName}` — получение любого файла из uploads

---

## Аутентификация

### POST /api/auth/signup
Регистрация нового пользователя с возможностью загрузки фото профиля. По умолчанию присваивается роль `ROLE_USERS`.

**Интеграция с Tabbycat:** При успешной регистрации пользователь автоматически создается в системе Tabbycat с теми же данными.

Заголовки:
- `Content-Type: multipart/form-data`

Параметры:
- `register` — JSON строка с данными регистрации
- `profilePicture` — файл изображения (опционально)

JSON для параметра `register`:
```json
{
  "username": "string (3..50)",
  "email": "string (email)",
  "password": "string (>=6)",
  "fullName": "string (max 100)",
  "phone": "string (max 20, optional)",
  "description": "string (max 500, optional)"
}
```

Ответы:
- 200 OK — успешная регистрация
```json
"Пользователь успешно зарегистрирован!"
```
- 400 Bad Request — имя пользователя уже занято
```json
"Ошибка: Имя пользователя уже используется!"
```
- 400 Bad Request — email уже используется
```json
"Ошибка: Email уже используется!"
```
- 400 Bad Request — неподдерживаемый тип файла
```json
"Поддерживаются только изображения (JPG, PNG, GIF, WebP)"
```

Замечания:
- Поля `username`, `email`, `password`, `fullName` обязательны.
- Поля `phone` и `description` опциональны.
- Поле `profilePicture` опционально, поддерживаются форматы: JPG, PNG, GIF, WebP (макс. 10MB).
- Валидация осуществляется на стороне сервера.

---

### POST /api/auth/signin
Авторизация и получение JWT токена.

Заголовки:
- `Content-Type: application/json`

Тело запроса:
```json
{
  "username": "string",
  "password": "string"
}
```

Ответы:
- 200 OK — успешная аутентификация, возвращает JWT и базовую информацию о пользователе:
```json
{
  "token": "string",
  "type": "Bearer",
  "username": "string",
  "email": "string"
}
```
- 401 Unauthorized — ошибка аутентификации (неверные логин/пароль)

Примечания:
- Полученный `token` необходимо передавать в заголовке `Authorization: Bearer <token>` для доступа к защищенным эндпоинтам.
- Срок жизни токена — 24 часа.

---

## Тестовые/примерные эндпоинты

### GET /api/test/public
Публичный эндпоинт, доступен без авторизации.

Ответы:
- 200 OK
```json
"Это публичный endpoint, доступен всем."
```

---

### GET /api/test/user
Доступен только аутентифицированным пользователям с ролью `ROLE_USERS`.

Заголовки:
- `Authorization: Bearer <JWT_TOKEN>`

Ответы:
- 200 OK
```json
"Привет, <username>! Это защищенный endpoint для пользователей."
```
- 401 Unauthorized — отсутствует/невалидный токен
- 403 Forbidden — недостаточно прав (нет роли `ROLE_USERS`)

---

### GET /api/test/admin
Доступен только аутентифицированным пользователям с ролью `ROLE_ADMIN`.

Заголовки:
- `Authorization: Bearer <JWT_TOKEN>`

Ответы:
- 200 OK
```json
"Привет, <username>! Это защищенный endpoint для администраторов."
```
- 401 Unauthorized — отсутствует/невалидный токен
- 403 Forbidden — недостаточно прав (нет роли `ROLE_ADMIN`)

---

### GET /api/test/profile
Возвращает профиль текущего аутентифицированного пользователя.

Заголовки:
- `Authorization: Bearer <JWT_TOKEN>`

Ответы:
- 200 OK
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "password": "<скрыт на клиенте>",
  "roles": [
    {
      "id": 1,
      "name": "ROLE_USERS"
    }
  ]
}
```
- 401 Unauthorized — отсутствует/невалидный токен

Примечания:
- Возвращается полная JPA-сущность пользователя, включая роли. Пароль захеширован на сервере; не используйте его значение на клиенте.

---

## Файлы и изображения

### GET /api/files/profile-picture/{fileName}
Получение изображения профиля по имени файла.

Параметры URL:
- `fileName` — имя файла изображения

Ответы:
- 200 OK — возвращает изображение
- 404 Not Found — файл не найден

Примечания:
- Этот endpoint публичный, не требует аутентификации
- Поддерживаемые форматы: JPG, PNG, GIF, WebP

---

### GET /api/files/{fileName}
Получение любого файла из папки uploads по имени файла.

Параметры URL:
- `fileName` — имя файла (с расширением)

Ответы:
- 200 OK — возвращает файл с правильным Content-Type
- 404 Not Found — файл не найден
- 400 Bad Request — некорректный путь к файлу

Поддерживаемые типы файлов:
- **Изображения:** JPG, JPEG, PNG, GIF, WebP
- **Документы:** PDF, TXT, JSON, XML
- **Архивы:** ZIP
- **Видео:** MP4
- **Аудио:** MP3
- **Остальные:** application/octet-stream

Примеры использования:
```
GET /api/files/document.pdf
GET /api/files/image.png
GET /api/files/data.json
GET /api/files/archive.zip
```

Примечания:
- Этот endpoint публичный, не требует аутентификации
- Content-Type определяется автоматически по расширению файла
- Файлы отображаются в браузере (inline), а не скачиваются

---

## Интеграция с Tabbycat

Приложение интегрировано с системой Tabbycat для управления турнирами. При регистрации пользователя:

1. **Создается пользователь в локальной базе данных**
2. **Автоматически создается профиль в Tabbycat** с параметрами:
   - `username` = `username` (имя пользователя из регистрации)
   - `email` = email пользователя
   - `password` = пароль пользователя
   - `is_superuser` = `false`
   - `is_staff` = `false`
   - `is_active` = `true`

### Конфигурация Tabbycat

```properties
# Tabbycat API Configuration
tabbycat.api.url=http://localhost:8000/api/v1
tabbycat.api.key=tabbycat.api-key
```

**Примечание:** Если создание пользователя в Tabbycat не удается, регистрация в основной системе все равно завершается успешно. Ошибки интеграции логируются, но не прерывают процесс.

---

## 4. Команды

### 4.1 Создать команду
**POST** `/api/teams`

**Описание:** Создать новую команду (создатель становится лидером)

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Тело запроса:**
```json
{
  "name": "Название команды"
}
```

**Ответ:**
```json
{
  "id": 1,
  "name": "Название команды",
  "leader": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "phone": "+1234567890",
    "description": "Описание пользователя",
    "profilePicture": "uploads/profile.jpg",
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "joinCode": "ABC12345",
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00",
  "memberCount": 1,
  "isFull": false
}
```

### 4.2 Вступить в команду
**POST** `/api/teams/join`

**Описание:** Вступить в команду по коду приглашения

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Тело запроса:**
```json
{
  "joinCode": "ABC12345"
}
```

**Ответ:** Аналогично ответу создания команды

### 4.3 Покинуть команду
**POST** `/api/teams/leave`

**Описание:** Покинуть текущую команду

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Ответ:**
```json
{
  "id": 1,
  "name": "Название команды",
  "leader": {
    "id": 2,
    "username": "newleader",
    "email": "leader@example.com",
    "fullName": "New Leader",
    "phone": "+1234567891",
    "description": "Новый лидер",
    "profilePicture": null,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "joinCode": "ABC12345",
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00",
  "memberCount": 1,
  "isFull": false
}
```

**Примечание:** Если команда остается пустой после выхода лидера, команда удаляется и возвращается сообщение: "Команда удалена (не осталось участников)"

### 4.4 Получить мою команду
**GET** `/api/teams/my`

**Описание:** Получить информацию о команде текущего пользователя

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Ответ:** Аналогично ответу создания команды

---

## Поведение безопасности
- Открытые маршруты: `/api/auth/**`, `/api/test/public`, `/api/files/**`
- Защищенные маршруты: `/api/teams/**`, `/api/test/**` (кроме public)
- Все прочие маршруты требуют аутентификации и действительного JWT.
- Авторизация по ролям осуществляется через аннотации `@PreAuthorize` на контроллерах.

## Коды ошибок и общие ответы
- 400 — ошибка валидации/некорректный запрос
- 401 — неавторизовано (нет/невалидный токен)
- 403 — запрещено (недостаточно прав)
- 404 — ресурс не найден
- 500 — внутренняя ошибка сервера

## Примеры использования

Регистрация:
```http
POST /api/auth/signup HTTP/1.1
Host: localhost:5234
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="register"
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Иван Иванов",
  "phone": "+7-999-123-45-67",
  "description": "Разработчик с опытом работы в веб-технологиях"
}
--boundary
Content-Disposition: form-data; name="profilePicture"; filename="profile.jpg"
Content-Type: image/jpeg

[binary data]
--boundary--
```

Логин и использование токена:
```http
POST /api/auth/signin HTTP/1.1
Host: localhost:5234
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

```http
GET /api/test/user HTTP/1.1
Host: localhost:5234
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Получение фото профиля:
```http
GET /api/files/profile-picture/uuid-filename.jpg HTTP/1.1
Host: localhost:5234
```


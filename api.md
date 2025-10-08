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

---

## Аутентификация

### POST /api/auth/signup
Регистрация нового пользователя с возможностью загрузки фото профиля. По умолчанию присваивается роль `ROLE_USERS`.

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

## Поведение безопасности
- Открытые маршруты: `/api/auth/**`, `/api/test/public`, `/api/files/profile-picture/**`
- Защищенные маршруты: `/api/test/**` (кроме public)
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


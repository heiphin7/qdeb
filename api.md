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

## 4. Профиль пользователя

### 4.1 Получить мой профиль
**GET** `/api/profile`

**Описание:** Получить профиль текущего пользователя с информацией о команде

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Ответ:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "fullName": "Test User",
  "phone": "+1234567890",
  "description": "Описание пользователя",
  "profilePicture": "uploads/profile.jpg",
  "createdAt": "2024-01-01T12:00:00",
  "updatedAt": "2024-01-01T12:00:00",
  "team": {
    "id": 1,
    "name": "Название команды",
    "joinCode": "ABC12345",
    "role": "LEADER",
    "memberCount": 2,
    "isFull": true,
    "joinedAt": "2024-01-01T12:00:00"
  }
}
```

**Примечание:** Если пользователь не состоит в команде, поле `team` будет `null`.

### 4.2 Получить профиль по username
**GET** `/api/profile/{username}`

**Описание:** Получить профиль пользователя по его username (публичный endpoint)

**Заголовки:** Не требуются

**Параметры URL:**
- `username` — имя пользователя

**Ответ:** Аналогично ответу получения моего профиля

---

## 5. Команды

### 5.1 Создать команду
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

### 5.2 Вступить в команду
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

### 5.3 Покинуть команду
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

### 5.4 Получить мою команду
**GET** `/api/teams/my`

**Описание:** Получить информацию о команде текущего пользователя

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Ответ:** Аналогично ответу создания команды

### 5.5 Получить заявки команды
**GET** `/api/teams/{teamId}/applications`

**Описание:** Получить все заявки, поданные командой на различные турниры с возможностью фильтрации по статусу

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Параметры URL:**
- `teamId` — ID команды

**Параметры запроса:**
- `status` — статус заявки для фильтрации (опционально)
  - `PENDING` — ожидающие рассмотрения
  - `APPROVED` — принятые
  - `REJECTED` — отклоненные

**Ответы:**
- 200 OK — список заявок команды
```json
[
  {
    "id": 1,
    "tournamentId": 1,
    "tournamentName": "test1",
    "tournamentSlug": "test1",
    "team": {
      "id": 3,
      "name": "Dream Team",
      "joinCode": "ABC12345",
      "leader": {
        "id": 1,
        "username": "alice",
        "email": "alice@example.com",
        "fullName": "Alice Johnson",
        "phone": "+1234567890",
        "description": "Team captain",
        "profilePicture": "uuid-filename.jpg",
        "createdAt": "2025-01-10T12:00:00"
      },
      "member": {
        "id": 2,
        "username": "bob",
        "email": "bob@example.com",
        "fullName": "Bob Smith",
        "phone": "+1234567891",
        "description": "Team member",
        "profilePicture": null,
        "createdAt": "2025-01-10T12:00:00"
      },
      "memberCount": 2,
      "isFull": true,
      "createdAt": "2025-01-10T12:00:00"
    },
    "submittedBy": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Johnson",
      "phone": "+1234567890",
      "description": "Team captain",
      "profilePicture": "uuid-filename.jpg",
      "createdAt": "2025-01-10T12:00:00"
    },
    "status": "PENDING",
    "fields": [
      {
        "id": 1,
        "name": "Full Name",
        "value": "Alice Johnson"
      },
      {
        "id": 2,
        "name": "Work or College",
        "value": "Stanford University"
      }
    ],
    "createdAt": "2025-01-10T15:00:00",
    "updatedAt": "2025-01-10T15:00:00"
  }
]
```

- 403 Forbidden — пользователь не является участником команды
```json
"У вас нет доступа к заявкам этой команды"
```

- 404 Not Found — команда не найдена
```json
"Команда не найдена"
```

- 400 Bad Request — некорректный статус
```json
"Некорректный статус заявки. Доступные статусы: PENDING, APPROVED, REJECTED"
```

**Примеры использования:**

Получить все заявки команды:
```http
GET /api/teams/1/applications HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только ожидающие заявки команды:
```http
GET /api/teams/1/applications?status=PENDING HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только принятые заявки команды:
```http
GET /api/teams/1/applications?status=APPROVED HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только отклоненные заявки команды:
```http
GET /api/teams/1/applications?status=REJECTED HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

**Параметры ответа:**
- `id` — ID заявки
- `tournamentId` — ID турнира
- `tournamentName` — название турнира
- `tournamentSlug` — slug турнира
- `team` — информация о команде
  - `id` — ID команды
  - `name` — название команды
  - `joinCode` — код приглашения
  - `leader` — информация о капитане
  - `member` — информация об участнике (может быть null)
  - `memberCount` — количество участников
  - `isFull` — заполнена ли команда
  - `createdAt` — дата создания команды
- `submittedBy` — информация о подавшем заявку
- `status` — статус заявки (PENDING, APPROVED, REJECTED)
- `fields` — поля заявки
  - `id` — ID поля
  - `name` — название поля
  - `value` — значение поля
- `createdAt` — дата подачи заявки
- `updatedAt` — дата последнего обновления

**Валидации:**
1. **Участник команды** — пользователь должен быть участником указанной команды
2. **Существование команды** — команда должна существовать в системе

**Примечания:**
- Возвращает массив всех заявок команды на различные турниры
- Каждая заявка включает полную информацию о турнире и команде
- Если заявок нет, возвращается пустой массив `[]`
- Подробное логирование всех операций
- Доступ только для участников команды

---

## 7. Управление заявками (Администратор)

### 7.1 Принять заявку
**POST** `/api/applications/{applicationId}/accept`

**Описание:** Принять заявку на участие в турнире (только для администраторов)

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Параметры URL:**
- `applicationId` — ID заявки

**Ответы:**
- 200 OK — заявка успешно принята
```json
{
  "id": 1,
  "tournamentId": 1,
  "tournamentName": "test1",
  "tournamentSlug": "test1",
  "team": {
    "id": 3,
    "name": "Dream Team",
    "joinCode": "ABC12345",
    "leader": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Johnson",
      "phone": "+1234567890",
      "description": "Team captain",
      "profilePicture": "uuid-filename.jpg",
      "createdAt": "2025-01-10T12:00:00"
    },
    "member": {
      "id": 2,
      "username": "bob",
      "email": "bob@example.com",
      "fullName": "Bob Smith",
      "phone": "+1234567891",
      "description": "Team member",
      "profilePicture": null,
      "createdAt": "2025-01-10T12:00:00"
    },
    "memberCount": 2,
    "isFull": true,
    "createdAt": "2025-01-10T12:00:00"
  },
  "submittedBy": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "fullName": "Alice Johnson",
    "phone": "+1234567890",
    "description": "Team captain",
    "profilePicture": "uuid-filename.jpg",
    "createdAt": "2025-01-10T12:00:00"
  },
  "status": "APPROVED",
  "fields": [
    {
      "id": 1,
      "name": "Full Name",
      "value": "Alice Johnson"
    },
    {
      "id": 2,
      "name": "Work or College",
      "value": "Stanford University"
    }
  ],
  "createdAt": "2025-01-10T15:00:00",
  "updatedAt": "2025-01-10T16:00:00"
}
```

- 400 Bad Request — заявка не в статусе PENDING
```json
"Можно принимать только заявки в статусе PENDING. Текущий статус: APPROVED"
```

- 403 Forbidden — недостаточно прав (нет роли ROLE_ADMIN)
```json
"Access Denied"
```

- 404 Not Found — заявка не найдена
```json
"Заявка не найдена"
```

### 7.2 Отклонить заявку
**POST** `/api/applications/{applicationId}/reject`

**Описание:** Отклонить заявку на участие в турнире (только для администраторов)

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Параметры URL:**
- `applicationId` — ID заявки

**Ответы:**
- 200 OK — заявка успешно отклонена
```json
{
  "id": 1,
  "tournamentId": 1,
  "tournamentName": "test1",
  "tournamentSlug": "test1",
  "team": {
    "id": 3,
    "name": "Dream Team",
    "joinCode": "ABC12345",
    "leader": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Johnson",
      "phone": "+1234567890",
      "description": "Team captain",
      "profilePicture": "uuid-filename.jpg",
      "createdAt": "2025-01-10T12:00:00"
    },
    "member": {
      "id": 2,
      "username": "bob",
      "email": "bob@example.com",
      "fullName": "Bob Smith",
      "phone": "+1234567891",
      "description": "Team member",
      "profilePicture": null,
      "createdAt": "2025-01-10T12:00:00"
    },
    "memberCount": 2,
    "isFull": true,
    "createdAt": "2025-01-10T12:00:00"
  },
  "submittedBy": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "fullName": "Alice Johnson",
    "phone": "+1234567890",
    "description": "Team captain",
    "profilePicture": "uuid-filename.jpg",
    "createdAt": "2025-01-10T12:00:00"
  },
  "status": "REJECTED",
  "fields": [
    {
      "id": 1,
      "name": "Full Name",
      "value": "Alice Johnson"
    },
    {
      "id": 2,
      "name": "Work or College",
      "value": "Stanford University"
    }
  ],
  "createdAt": "2025-01-10T15:00:00",
  "updatedAt": "2025-01-10T16:00:00"
}
```

- 400 Bad Request — заявка не в статусе PENDING
```json
"Можно отклонять только заявки в статусе PENDING. Текущий статус: APPROVED"
```

- 403 Forbidden — недостаточно прав (нет роли ROLE_ADMIN)
```json
"Access Denied"
```

- 404 Not Found — заявка не найдена
```json
"Заявка не найдена"
```

**Валидации:**
1. **Роль администратора** — только пользователи с ролью `ROLE_ADMIN` могут принимать/отклонять заявки
2. **Статус заявки** — можно изменять статус только у заявок в статусе `PENDING`
3. **Существование заявки** — заявка должна существовать в системе

**Примечания:**
- Доступ только для администраторов (роль `ROLE_ADMIN`)
- Можно изменять статус только у заявок в статусе `PENDING`
- После принятия/отклонения статус заявки изменяется на `APPROVED`/`REJECTED`
- Поле `updatedAt` автоматически обновляется при изменении статуса
- **Интеграция с Tabbycat:** При принятии заявки автоматически создается команда в системе Tabbycat
- Подробное логирование всех операций с указанием администратора
- Транзакционность — операции выполняются атомарно
- Ошибки интеграции с Tabbycat не прерывают процесс принятия заявки

**Примеры использования:**

Принять заявку:
```http
POST /api/applications/1/accept HTTP/1.1
Host: localhost:5234
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

Отклонить заявку:
```http
POST /api/applications/1/reject HTTP/1.1
Host: localhost:5234
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

---

## 8. Интеграция с Tabbycat

### 8.1 Создание команды в Tabbycat

При принятии заявки администратором (`POST /api/applications/{applicationId}/accept`) автоматически выполняется интеграция с системой Tabbycat:

**Процесс интеграции:**
1. Заявка принимается в локальной базе данных
2. Статус заявки изменяется на `APPROVED`
3. Автоматически создается команда в Tabbycat через API

**API запрос к Tabbycat:**
```
POST http://localhost:8000/api/v1/tournaments/{tournament_slug}/teams
```

**Заголовки:**
- `Content-Type: application/json`
- `Authorization: Token {tabbycat.api.key}`

**Тело запроса:**
```json
{
  "institution": null,
  "break_categories": null,
  "institution_conflicts": null,
  "venue_constraints": null,
  "answers": null,
  "reference": "Название команды",
  "short_reference": "Название команды",
  "code_name": "Название команды",
  "use_institution_prefix": false,
  "seed": null,
  "emoji": null,
  "speakers": [
    {
      "name": "Имя участника",
      "categories": [],
      "barcode": null,
      "answers": [],
      "last_name": "Фамилия участника",
      "email": "email@example.com",
      "phone": "+1234567890",
      "anonymous": true,
      "code_name": "username",
      "url_key": null,
      "gender": "O",
      "pronoun": null
    }
  ]
}
```

**Маппинг данных:**

**Основные поля команды:**
- `reference` = название команды из заявки
- `short_reference` = название команды из заявки
- `code_name` = название команды из заявки
- `use_institution_prefix` = `false`
- `seed` = `null`
- `emoji` = `null`

**Поля спикеров (из данных команды):**
- `name` = второе слово из `fullName` пользователя
- `last_name` = первое слово из `fullName` пользователя
- `email` = email пользователя
- `phone` = телефон пользователя
- `code_name` = username пользователя
- `gender` = `"O"` (Other)
- `pronoun` = `null`
- `anonymous` = `true`
- `categories` = `[]`
- `answers` = `[]`
- `barcode` = `null`
- `url_key` = `null`

**Обработка ошибок:**
- Ошибки интеграции с Tabbycat логируются, но не прерывают процесс принятия заявки
- Заявка остается принятой в локальной базе данных даже при сбое интеграции
- Подробное логирование всех операций интеграции

**Конфигурация:**
```properties
# Tabbycat API Configuration
tabbycat.api.url=http://localhost:8000/api/v1
tabbycat.api.key=db0388ad46a02252055ecfc5e1fc8caf17e17a84
```

**Пример логов:**
```
INFO  - Создание команды в Tabbycat для принятой заявки 1
INFO  - Отправка запроса в Tabbycat: http://localhost:8000/api/v1/tournaments/test3/teams
INFO  - Данные команды: reference=Devs, speakers=2
INFO  - Создан спикер: name=User1, lastName=User1, email=user1@gmail.com, username=user1
INFO  - Создан спикер: name=User2, lastName=User2, email=user2@gmail.com, username=user2
INFO  - Команда успешно создана в Tabbycat для заявки 1
```

---

## Поведение безопасности
- Открытые маршруты: `/api/auth/**`, `/api/test/public`, `/api/files/**`
- Защищенные маршруты: `/api/profile/**`, `/api/teams/**`, `/api/test/**` (кроме public)
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

---

## 6. Турниры

### 6.1 Создать турнир
**POST** `/api/tournaments`

**Описание:** Создать новый турнир с полной интеграцией в Tabbycat

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: multipart/form-data`

**Параметры:**
- `tournament` — JSON строка с данными турнира
- `tournamentPicture` — файл изображения турнира (опционально)

JSON для параметра `tournament`:
```json
{
  "name": "test1",
  "slug": "test1",
  "organizerName": "someOrganizer",
  "organizerContact": "some@gmail.com",
  "description": "best tournament in the world",
  "date": "2025-12-31",
  "active": true,
  "fee": 500,
  "level": "NATIONAL",
  "format": "online",
  "seq": 1,
  "registraionFields": [
    {
      "name": "Full Name",
      "type": "DESCRIPTION",
      "required": true
    },
    {
      "name": "Work or College",
      "type": "DESCRIPTION",
      "required": true
    }
  ]
}
```

**Параметры:**
- `name` — название турнира
- `slug` — уникальный идентификатор турнира (URL-friendly)
- `organizerName` — имя организатора
- `organizerContact` — контактная информация организатора
- `description` — описание турнира
- `date` — дата проведения турнира (YYYY-MM-DD)
- `active` — активен ли турнир
- `fee` — стоимость участия
- `level` — уровень турнира (NATIONAL, REGIONAL, INTERNATIONAL, LOCAL)
- `format` — формат проведения (online, offline)
- `seq` — порядковый номер
- `registraionFields` — массив полей регистрации
  - `name` — название поля
  - `type` — тип поля (DESCRIPTION, TEXT)
  - `required` — обязательное ли поле

**Ответ:**
```json
{
  "id": 1,
  "name": "test1",
  "slug": "test1",
  "organizerName": "someOrganizer",
  "organizerContact": "some@gmail.com",
  "description": "best tournament in the world",
  "date": "2025-12-31",
  "active": true,
  "fee": 500,
  "level": "NATIONAL",
  "format": "online",
  "seq": 1,
  "tournamentPicture": "uuid-filename.jpg",
  "imageURL": "/api/files/uuid-filename.jpg",
  "createdAt": "2025-01-10T12:00:00",
  "updatedAt": "2025-01-10T12:00:00",
  "registrationFields": [
    {
      "id": 1,
      "name": "Full Name",
      "type": "DESCRIPTION",
      "required": true
    }
  ],
  "rounds": []
}
```

**Примечания:**
- При создании турнира автоматически создается соответствующий турнир в системе Tabbycat
- Раунды на этом шаге НЕ создаются и не передаются в Tabbycat
- Поле `tournamentPicture` опционально, поддерживаются форматы: JPG, PNG, GIF, WebP (макс. 10MB)
- Поле `imageURL` содержит полный URL для доступа к изображению турнира
- Если турнир не имеет изображения, поле `imageURL` будет `null`
- Все операции логируются для отслеживания процесса создания

**Пример использования:**
```http
POST /api/tournaments HTTP/1.1
Host: localhost:4232
Authorization: Bearer <JWT_TOKEN>
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="tournament"
Content-Type: application/json

{
  "name": "test1",
  "slug": "test1",
  "organizerName": "someOrganizer",
  "organizerContact": "some@gmail.com",
  "description": "best tournament in the world",
  "date": "2025-12-31",
  "active": true,
  "fee": 500,
  "level": "NATIONAL",
  "format": "online",
  "seq": 1,
  "registraionFields": [
    {
      "name": "Full Name",
      "type": "DESCRIPTION",
      "required": true
    }
  ]
}
--boundary
Content-Disposition: form-data; name="tournamentPicture"; filename="tournament.jpg"
Content-Type: image/jpeg

[binary data]
--boundary--
```

**Логирование:**
Система ведет подробное логирование всех операций:
- Получение запроса на создание турнира
- Сохранение данных в локальную базу данных
- Отправка запросов в Tabbycat API
- Результаты создания турнира и раундов
- Ошибки и исключения

### 6.2 Получить все турниры
**GET** `/api/tournaments`

**Описание:** Получить список всех турниров

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "test1",
    "slug": "test1",
    "organizerName": "someOrganizer",
    "organizerContact": "some@gmail.com",
    "description": "best tournament in the world",
    "date": "2025-12-31",
    "active": true,
    "fee": 500,
    "level": "NATIONAL",
    "format": "online",
    "seq": 1,
    "tournamentPicture": "uuid-filename.jpg",
    "imageURL": "/api/files/uuid-filename.jpg",
    "createdAt": "2025-01-10T12:00:00",
    "updatedAt": "2025-01-10T12:00:00",
    "registrationFields": [
      {
        "id": 1,
        "name": "Full Name",
        "type": "DESCRIPTION",
        "required": true
      }
    ],
    "rounds": [
      {
        "id": 1,
        "name": "Раунд 1",
        "abbreviation": "R1",
        "seq": 1,
        "stage": "P",
        "drawType": "R",
        "drawStatus": "N",
        "breakCategory": null,
        "startsAt": "2025-12-31T10:00:00",
        "completed": false,
        "feedbackWeight": 0.1,
        "silent": false,
        "motionsReleased": false,
        "weight": 1
      }
    ]
  }
]
```

**Примечания:**
- Возвращает массив всех турниров в системе
- Каждый турнир включает полную информацию о полях регистрации и раундах
- Поле `imageURL` содержит полный URL для доступа к изображению турнира
- Если турнир не имеет изображения, поле `imageURL` будет `null`
- Если турниров нет, возвращается пустой массив `[]`

### 6.3 Подать заявку на турнир
**POST** `/api/tournaments/{tournamentId}/apply`

**Описание:** Подать заявку от команды на участие в турнире

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Параметры URL:**
- `tournamentId` — ID турнира

**Тело запроса:**
```json
{
  "teamId": 3,
  "fields": [
    {
      "name": "Full Name",
      "value": "Alice Johnson"
    },
    {
      "name": "Work or College",
      "value": "Stanford University"
    }
  ]
}
```

**Параметры:**
- `teamId` — ID команды, подающей заявку
- `fields` — массив полей регистрации
  - `name` — название поля (должно соответствовать RegistrationField турнира)
  - `value` — значение поля

**Ответы:**
- 201 Created — заявка успешно подана
```json
{
  "message": "Application submitted successfully",
  "applicationId": 12
}
```

- 400 Bad Request — турнир не активен
```json
"Tournament is not active"
```

- 400 Bad Request — отсутствует обязательное поле
```json
"Missing required field: Full Name"
```

- 400 Bad Request — команда невалидна
```json
"Команда должна иметь ровно 2 участника"
```

- 400 Bad Request — команда уже подавала заявку
```json
"Команда уже подавала заявку на этот турнир"
```

- 403 Forbidden — пользователь не является участником команды
```json
"Пользователь не является участником команды"
```

- 403 Forbidden — пользователь не является капитаном
```json
"Только капитан команды может подавать заявки"
```

- 404 Not Found — турнир не найден
```json
"Турнир не найден"
```

- 404 Not Found — команда не найдена
```json
"Команда не найдена"
```

**Валидации:**
1. **Активность турнира** — турнир должен быть активен (`isActive = true`)
2. **Участник команды** — пользователь должен быть участником указанной команды
3. **Капитан команды** — только капитан может подавать заявки
4. **Размер команды** — команда должна иметь ровно 2 участника
5. **Обязательные поля** — все поля с `required = true` должны быть заполнены
6. **Уникальность заявки** — команда может подать только одну заявку на турнир

**Примечания:**
- Заявка создается со статусом `PENDING`
- Все поля регистрации сохраняются для истории
- Подробное логирование всех операций
- Транзакционность — либо все сохраняется, либо ничего

### 6.4 Получить заявки на турнир
**GET** `/api/tournaments/{tournamentSlug}/applications`

**Описание:** Получить все заявки, поданные на турнир по его slug с возможностью фильтрации по статусу

**Заголовки:**
- `Authorization: Bearer <JWT_TOKEN>`

**Параметры URL:**
- `tournamentSlug` — slug турнира

**Параметры запроса:**
- `status` — статус заявки для фильтрации (опционально)
  - `PENDING` — ожидающие рассмотрения
  - `APPROVED` — принятые
  - `REJECTED` — отклоненные

**Ответы:**
- 200 OK — список заявок
```json
[
  {
    "id": 1,
    "tournamentId": 1,
    "tournamentName": "test1",
    "tournamentSlug": "test1",
    "team": {
      "id": 3,
      "name": "Dream Team",
      "joinCode": "ABC12345",
      "leader": {
        "id": 1,
        "username": "alice",
        "email": "alice@example.com",
        "fullName": "Alice Johnson",
        "phone": "+1234567890",
        "description": "Team captain",
        "profilePicture": "uuid-filename.jpg",
        "imageURL": "/api/files/uuid-filename.jpg",
        "createdAt": "2025-01-10T12:00:00"
      },
      "member": {
        "id": 2,
        "username": "bob",
        "email": "bob@example.com",
        "fullName": "Bob Smith",
        "phone": "+1234567891",
        "description": "Team member",
        "profilePicture": null,
        "imageURL": null,
        "createdAt": "2025-01-10T12:00:00"
      },
      "memberCount": 2,
      "isFull": true,
      "createdAt": "2025-01-10T12:00:00"
    },
    "submittedBy": {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Johnson",
      "phone": "+1234567890",
      "description": "Team captain",
      "profilePicture": "uuid-filename.jpg",
      "imageURL": "/api/files/uuid-filename.jpg",
      "createdAt": "2025-01-10T12:00:00"
    },
    "status": "PENDING",
    "fields": [
      {
        "id": 1,
        "name": "Full Name",
        "value": "Alice Johnson"
      },
      {
        "id": 2,
        "name": "Work or College",
        "value": "Stanford University"
      }
    ],
    "createdAt": "2025-01-10T15:00:00",
    "updatedAt": "2025-01-10T15:00:00"
  }
]
```

- 404 Not Found — турнир не найден
```json
"Турнир не найден"
```

- 400 Bad Request — некорректный статус
```json
"Некорректный статус заявки. Доступные статусы: PENDING, APPROVED, REJECTED"
```

**Примеры использования:**

Получить все заявки на турнир:
```http
GET /api/tournaments/test1/applications HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только ожидающие заявки:
```http
GET /api/tournaments/test1/applications?status=PENDING HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только принятые заявки:
```http
GET /api/tournaments/test1/applications?status=APPROVED HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

Получить только отклоненные заявки:
```http
GET /api/tournaments/test1/applications?status=REJECTED HTTP/1.1
Host: localhost:5234
Authorization: Bearer <JWT_TOKEN>
```

**Параметры ответа:**
- `id` — ID заявки
- `tournamentId` — ID турнира
- `tournamentName` — название турнира
- `tournamentSlug` — slug турнира
- `team` — информация о команде
  - `id` — ID команды
  - `name` — название команды
  - `joinCode` — код приглашения
  - `leader` — информация о капитане
  - `member` — информация об участнике (может быть null)
  - `memberCount` — количество участников
  - `isFull` — заполнена ли команда
  - `createdAt` — дата создания команды
- `submittedBy` — информация о подавшем заявку
- `status` — статус заявки (PENDING, APPROVED, REJECTED)
- `fields` — поля заявки
  - `id` — ID поля
  - `name` — название поля
  - `value` — значение поля
- `createdAt` — дата подачи заявки
- `updatedAt` — дата последнего обновления

**Примечания:**
- Возвращает массив всех заявок на турнир
- Каждая заявка включает полную информацию о команде и участниках
- Поле `imageURL` автоматически формируется для профильных фотографий
- Если заявок нет, возвращается пустой массив `[]`
- Подробное логирование всех операций

---

Вот общий json который мы принимаем, вот тут пожалуйста обрати внимание на типа registraion fields, типа видишь что у нас есть вот динамическое добавление полей, получается вот нужно тебе как то тоже сохранять чтобы вот в будушем когда у нас будем делать чтобы это учитывалось, чисто так чтобы ты в конекст себе записал:
{
    "name": "test1",
    "slug": "test1",
    "organizerName": "someOrganizer",
    "organizerContance": "some@gmail.com",
    "description": "best tournament in the world",
    "date": "2025-12-31",
    "active": true,
    "fee": 500,
    "level": "NATIONAL", // Здесь получается типа national, regional и так далее, добавь enum
    "format": "online",
    "seq": 1,
    "registraionFields": [
        {
            "name": "Full Name",
            "type": "DESCRIPTION",
            "required": true
        },
        {
            "name": "Work or College",
            "type": "DESCRIPTION", # тут типа description or text
            "required": true
        }
    ],
    "rounds": [ // раундов может быть несколько
      {
        "break_category": "" // string or null <uri>

        "starts_at": "" // string or null <date-time>

        seq": "integer (Порядковый номер)" // Число, определяющее номер раунда, должно рассчитывать последовательно от 1 для первого раунда

        "completed": "" // Верно есть раунд завершен, что обычно означает, что все результаты были введены и подтверждены

        "name": "" // string (Название) <= 40 characters например, "Раунд 1"

        "abbreviation": "" // string (Аббревиатура) <= 10 charactersнапример, "R1"

        "stage": "" // string (Стадия)
        Enum: "P" "E"
        Отборочные=раунды до брейка, брейковые=раунды плей/офф

        P - Отборочные
        E - Брейковый


        "draw_type": "" // string (Тип сетки)
        Enum: "R" "M" "D" "P" "E" "S"
        Какой метод составления сетки использовать

        R - Случайный
        M - Ручной
        D - Раунд-Робин
        P - Сочетание по силе команд
        E - Брейковый
        S - Seeded

        "draw_status	": "" // string (Статус сетки)
        Enum: "N" "D" "C" "R"
        Статус сетки на этот раунд

        N - Нет
        D - Черновик
        C - Подтвержденные
        R - Выпущено

        "feedback_weight": "" // number <double> (Вес обратной связи)
        Насколько рейтинг судьи зависит от обратной связи и исходного рейтинга. При значении "0" рейтинг судьи на 100% соответствует исходному рейтингу, при значении "1" он на 100% соответствует среднему значению обратной связи.

        "silent": "" // boolean: Если раунд отмечен как закрытый, вся информация о нем (например, его результаты) не будет показана публично.

        "motions_released": "" // boolean (Темы опубликованы)
        Будет ли появляться темы на сайте публично, если включить эту функци

        "weight": "" // integer
        A factor for the points received in the round. For example, if 2, all points are doubled.
      }
    ]
}

Вот видишь какую огромную информацию мы получаем, окей будет POST endpoint где мы получим все это и далее нужно:

сохранить в НАШУ базу данных и также сделать вот запросы на создание именно внутри tabbycat, тоесть под еще одним апишкой, вот данные для этих типа берешь вот от основного запроса:

POST http://localhost:8000/api/v1/tournaments

body:

{
  "name": "string",
  "short_name": "string",
  "seq": -2147483648,
  "slug": "^-$",
  "active": true
}

Далее вот типа сколько раундов добавлено, столько вот нужно сделать запросов, то есть если у нас там 3 раунда было указано при создании основного запроса то тогда столько вот запросов нужно будет:

POST http://localhost:8000/api/v1/tournaments/{tournament_slug}/rounds

(Здесь сделай пожалуйста motion пустым всегда)

{
"break_category": "http://example.com",
"motions": [],
"starts_at": "2019-08-24T14:15:22Z",
"seq": 2147483647,
"completed": true,
"name": "string",
"abbreviation": "string",
"stage": "P",
"draw_type": "R",
"draw_status": "N",
"feedback_weight": 0.1,
"silent": true,
"motions_released": true,
"weight": -2147483648
}

также пожалуйста сделай логгирование в файле api.md, просто добавь в конец, оттуда ничего не урезай и также сделай тщательное логгирование пожалуйста
# QDEB - Система аутентификации с JWT

Это Spring Boot приложение с системой регистрации и авторизации, использующее JWT токены и роли пользователей.

## Технологии

- Spring Boot 3.5.6
- Spring Security
- JWT (JSON Web Tokens)
- PostgreSQL
- JPA/Hibernate
- Lombok

## Настройка базы данных

Убедитесь, что PostgreSQL запущен и создана база данных `qdeb`:

```sql
CREATE DATABASE qdeb;
```

Параметры подключения:
- Host: localhost
- Port: 5432
- Database: qdeb
- Username: postgres
- Password: admin

## Запуск приложения

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: http://localhost:8080

## API Endpoints

### Аутентификация

#### Регистрация
```
POST /api/auth/signup
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
}
```

#### Авторизация
```
POST /api/auth/signin
Content-Type: application/json

{
    "username": "testuser",
    "password": "password123"
}
```

Ответ:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "testuser",
    "email": "test@example.com"
}
```

### Тестовые endpoints

#### Публичный endpoint
```
GET /api/test/public
```

#### Защищенный endpoint для пользователей
```
GET /api/test/user
Authorization: Bearer <JWT_TOKEN>
```

#### Защищенный endpoint для администраторов
```
GET /api/test/admin
Authorization: Bearer <JWT_TOKEN>
```

#### Профиль пользователя
```
GET /api/test/profile
Authorization: Bearer <JWT_TOKEN>
```

#### Публичный профиль пользователя по username
```
GET /api/profile/{username}
```

## Роли

- `ROLE_USERS` - Базовая роль для всех зарегистрированных пользователей
- `ROLE_ADMIN` - Роль администратора

При регистрации пользователь автоматически получает роль `ROLE_USERS`.

## Структура проекта

```
src/main/java/com/qdeb/
├── config/           # Конфигурации Spring
├── controller/       # REST контроллеры
├── dto/             # Data Transfer Objects
├── entity/          # JPA сущности
├── repository/      # JPA репозитории
├── security/        # JWT фильтры
├── service/         # Бизнес логика
└── util/            # Утилиты (JWT)
```

## Использование JWT токенов

После успешной авторизации вы получите JWT токен. Используйте его в заголовке `Authorization` для доступа к защищенным endpoints:

```
Authorization: Bearer <JWT_TOKEN>
```

Токен действителен 24 часа (86400000 миллисекунд).

# JWT Авторизация: Сервер и Клиент

Это руководство описывает реализацию JWT аутентификации и авторизации для сервера на базе **Spring Boot** и клиента на **JavaScript**.

## Сервер (Spring Boot)

### 1. Аутентификация
- Пользователь отправляет логин и пароль.
- Сервер проверяет учетные данные.
- Генерируются два токена:
    - **Access токен** — для авторизации запросов.
    - **Refresh токен** — для обновления access токена.
- **Refresh токен** сохраняется в базе данных.

### 2. Авторизация
- Каждый запрос проверяется на наличие **access токена**.
- Проверяются права доступа (роли).
- Если **access токен** истек, используется **refresh токен** для его обновления.
- Если истек и **refresh токен**, требуется повторный вход в систему.

## Клиент (JavaScript)

### 1. Хранение токенов
- **Access токен** используется для авторизации запросов.
- **Refresh токен** используется для обновления **access токена**.
- Токены хранятся в **localStorage** для доступа между сессиями.

### 2. Обработка запросов
- **Автоматическое добавление токенов** к запросам.
- **Обновление токенов** при необходимости с использованием **refresh токена**.
- Обработка ошибок авторизации (например, при истечении токенов).
- **Перенаправление на страницу входа** при необходимости (например, если токены истекли).

## Запуск клиента

### 1. Установка http-server
```bash
npm install -g http-server
```
### 2. Запуск сервера
```bash
cd путь/к/клиентскому/приложению
http-server -p 3000
```

### 2. Доступ к приложению
Откройте в браузере:
http://localhost:3000

Сервер должен быть запущен на порту 8080.

## Основные функции
- Вход/выход из системы.
- Разные роли пользователей (например, ADMIN, USER).
- Защищенные маршруты, доступные только авторизованным пользователям.
- Автоматическое обновление токенов при их истечении.
- Возможность отзыва токенов администратором.
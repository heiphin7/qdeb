#!/bin/bash

echo "Запуск тестов аутентификации..."

echo "Выполняем тесты аутентификации..."
./mvnw test -Dtest=AuthenticationUnitTest

echo "Выполняем тесты валидации..."
./mvnw test -Dtest=AuthenticationValidationTest

echo "Выполняем тесты JWT токенов..."
./mvnw test -Dtest=JwtTokenTest

echo "Все тесты завершены!"

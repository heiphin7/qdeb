# Используем официальный образ OpenJDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и Maven wrapper
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Делаем mvnw исполняемым
RUN chmod +x ./mvnw

# Загружаем зависимости Maven
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package

# Создаем директории для загрузок
RUN mkdir -p /app/uploads

# Открываем порт
EXPOSE 4232

# Запускаем приложение
CMD ["java", "-jar", "target/qdeb-0.0.1-SNAPSHOT.jar"]

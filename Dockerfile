# Используем многоэтапную сборку для оптимизации размера образа
FROM eclipse-temurin:21-jdk-alpine AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Даем права на выполнение mvnw
RUN chmod +x mvnw

# Загружаем зависимости (это кешируется, если pom.xml не изменился)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Финальный этап - runtime образ
FROM eclipse-temurin:21-jre-alpine

# Создаем пользователя для безопасности
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл из builder этапа
COPY --from=builder /app/target/LandOfChokolate-*.jar app.jar

# Меняем владельца файла
RUN chown spring:spring app.jar

# Переключаемся на пользователя spring
USER spring:spring

# Открываем порт (Render автоматически настроит PORT)
EXPOSE 10000

# ИСПРАВЛЕНО: Убираем конфликтующие JVM параметры
# Render использует переменную JAVA_OPTS из Environment Variables
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


## Используем многоэтапную сборку для оптимизации размера образа
#FROM eclipse-temurin:21-jdk-alpine AS builder
#
## Устанавливаем рабочую директорию
#WORKDIR /app
#
## Копируем файлы Maven
#COPY pom.xml .
#COPY .mvn .mvn
#COPY mvnw .
#
## Даем права на выполнение mvnw
#RUN chmod +x mvnw
#
## Загружаем зависимости (это кешируется, если pom.xml не изменился)
#RUN ./mvnw dependency:go-offline -B
#
## Копируем исходный код
#COPY src ./src
#
## Собираем приложение
#RUN ./mvnw clean package -DskipTests
#
## Финальный этап - runtime образ
#FROM eclipse-temurin:21-jre-alpine
#
## Создаем пользователя для безопасности
#RUN addgroup -g 1001 -S spring && \
#    adduser -u 1001 -S spring -G spring
#
## Устанавливаем рабочую директорию
#WORKDIR /app
#
## Копируем JAR файл из builder этапа
#COPY --from=builder /app/target/LandOfChokolate-*.jar app.jar
#
## Меняем владельца файла
#RUN chown spring:spring app.jar
#
## Переключаемся на пользователя spring
#USER spring:spring
#
## Открываем порт (Render автоматически настроит PORT)
#EXPOSE 10000
#
## Настройки JVM для контейнера
#ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
#
## Запускаем приложение
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
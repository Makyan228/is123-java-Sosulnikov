# sklad-system

Курсовой проект по дисциплине, представляющий собой веб-приложение для работы склада.

## Описание проекта

`sklad-system` — это информационная система склада, разработанная на языке Java.  
Проект создаётся в формате веб-приложения и предназначен для автоматизации основных складских операций.

На текущем этапе реализована базовая структура Spring Boot проекта, стартовая веб-страница и тестовый вывод списка товаров.

## Цель проекта

Разработать веб-сайт складской системы с возможностью дальнейшего расширения функционала:
- учёт товаров;
- хранение информации о количестве товаров;
- просмотр списка товаров;
- добавление, редактирование и удаление записей;
- подключение базы данных;
- реализация пользовательского интерфейса для работы со складом.

## Используемые технологии

- Java 21
- Spring Boot
- Maven
- Thymeleaf
- HTML
- CSS
- Git / GitHub

## Структура проекта

```text
├── Create-bd.sql
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── skladsystem
│   │   │           ├── config
│   │   │           │   ├── AuthInterceptor.java
│   │   │           │   └── WebConfig.java
│   │   │           ├── controller
│   │   │           │   ├── DashboardController.java
│   │   │           │   ├── GlobalModelAdvice.java
│   │   │           │   ├── InventoryController.java
│   │   │           │   ├── LoginController.java
│   │   │           │   ├── ProductController.java
│   │   │           │   ├── ReceiptController.java
│   │   │           │   └── ShipmentController.java
│   │   │           ├── model
│   │   │           │   ├── AppUser.java
│   │   │           │   ├── Counterparty.java
│   │   │           │   ├── MeasureUnit.java
│   │   │           │   ├── ProductCategory.java
│   │   │           │   ├── Product.java
│   │   │           │   ├── StockDocumentItem.java
│   │   │           │   ├── StockDocument.java
│   │   │           │   ├── StorageLocation.java
│   │   │           │   └── Warehouse.java
│   │   │           ├── repository
│   │   │           │   ├── AppUserRepository.java
│   │   │           │   ├── MeasureUnitRepository.java
│   │   │           │   ├── ProductCategoryRepository.java
│   │   │           │   ├── ProductRepository.java
│   │   │           │   ├── StockDocumentItemRepository.java
│   │   │           │   ├── StockDocumentRepository.java
│   │   │           │   ├── StorageLocationRepository.java
│   │   │           │   └── WarehouseRepository.java
│   │   │           ├── service
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── CurrentUserService.java
│   │   │           │   └── ProductService.java
│   │   │           └── SkladSystemApplication.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── static
│   │       │   └── css
│   │       │       └── style.css
│   │       └── templates
│   │           ├── fragments
│   │           │   ├── header.html
│   │           │   └── sidebar.html
│   │           ├── index.html
│   │           ├── inventory.html
│   │           ├── login.html
│   │           ├── product-form.html
│   │           ├── products.html
│   │           ├── receipt-form.html
│   │           ├── receipt-items.html
│   │           ├── receipts.html
│   │           ├── shipment-form.html
│   │           ├── shipment-items.html
│   │           └── shipments.html

## Как запустить проект
Требования

Для запуска проекта должны быть установлены:

Java 21
Maven или Maven Wrapper
СУБД Firebird
настроенная база данных sklad-system
1. Клонирование репозитория
git clone https://github.com/Makyan228/is123-java-Sosulnikov.git
cd is123-java-Sosulnikov
2. Настройка базы данных

Перед запуском необходимо:

создать базу данных sklad-system
выполнить SQL-скрипт создания таблиц

3. Сборка проекта

Если используется Maven Wrapper:

Linux
./mvnw clean package -DskipTests

Если используется обычный Maven:

mvn clean package -DskipTests

4. Запуск проекта
Linux
./mvnw spring-boot:run

или через Maven:

mvn spring-boot:run
5. Открытие приложения

После запуска приложение будет доступно в браузере по адресу:

http://localhost:8080
6. Вход в систему

Для входа в приложение необходимо выбрать пользователя и ввести пароль.

В системе предусмотрены роли:

администратор
оператор поступлений
оператор отгрузок
грузчик

Каждая роль имеет свой уровень доступа к разделам системы.

7. Проверка сборки на GitHub

В репозитории настроен GitHub Actions.
Он автоматически проверяет, что проект успешно собирается после отправки изменений в репозиторий.
